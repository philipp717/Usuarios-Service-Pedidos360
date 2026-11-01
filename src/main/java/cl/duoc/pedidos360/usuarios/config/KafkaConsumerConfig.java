package cl.duoc.pedidos360.usuarios.config;

import cl.duoc.pedidos360.usuarios.messaging.dto.PedidoCreadoEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del cliente Kafka (consumidor): deserialización JSON segura, reintentos con
 * backoff exponencial y envío a un tópico DLT cuando se agotan los reintentos.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${pedidos360.kafka.consumer.concurrency:3}")
    private int concurrency;

    @Value("${pedidos360.kafka.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${pedidos360.kafka.retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${pedidos360.kafka.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${pedidos360.kafka.retry.max-interval:5000}")
    private long maxInterval;

    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Un payload corrupto no detiene el consumidor: se delega al error handler
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PedidoCreadoEvent.class.getName());
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "cl.duoc.pedidos360.*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Tras agotar reintentos, el mensaje va a <topico>.DLT (cualquier partición)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxAttempts);
        backOff.setInitialInterval(initialInterval);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxInterval);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // Payload inválido: reintentar no lo arregla, va directo a la DLT
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        handler.setRetryListeners((record, ex, attempt) -> log.warn(
                "event=kafka_reintento topic={} particion={} offset={} intento={} error={}",
                record.topic(), record.partition(), record.offset(), attempt, ex.getMessage()));
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> kafkaConsumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
