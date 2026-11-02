package cl.duoc.pedidos360.usuarios.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declaración de los tópicos Kafka del sistema. Spring (KafkaAdmin) los crea al iniciar
 * la aplicación si no existen, con particiones, réplicas, retención y cleanup policy definidos.
 * Los tópicos .DLT reciben los mensajes que agotan los reintentos del consumidor.
 */
@Configuration
public class KafkaTopicsConfig {

    @Value("${pedidos360.kafka.topics.pedidos-creados}")
    private String pedidosCreados;

    @Value("${pedidos360.kafka.topics.usuarios-registrados}")
    private String usuariosRegistrados;

    @Value("${pedidos360.kafka.topic-defaults.partitions:3}")
    private int partitions;

    @Value("${pedidos360.kafka.topic-defaults.replicas:3}")
    private int replicas;

    @Value("${pedidos360.kafka.topic-defaults.min-insync-replicas:2}")
    private String minInsyncReplicas;

    @Value("${pedidos360.kafka.topic-defaults.retention-ms:604800000}")
    private String retentionMs;

    @Value("${pedidos360.kafka.topic-defaults.dlt-retention-ms:2592000000}")
    private String dltRetentionMs;

    @Bean
    public NewTopic pedidosCreadosTopic() {
        return eventTopic(pedidosCreados);
    }

    @Bean
    public NewTopic usuariosRegistradosTopic() {
        return eventTopic(usuariosRegistrados);
    }

    @Bean
    public NewTopic pedidosCreadosDltTopic() {
        return dltTopic(pedidosCreados + ".DLT");
    }

    @Bean
    public NewTopic usuariosRegistradosDltTopic() {
        return dltTopic(usuariosRegistrados + ".DLT");
    }

    private NewTopic eventTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, retentionMs)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas)
                .build();
    }

    private NewTopic dltTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, dltRetentionMs)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas)
                .build();
    }
}
