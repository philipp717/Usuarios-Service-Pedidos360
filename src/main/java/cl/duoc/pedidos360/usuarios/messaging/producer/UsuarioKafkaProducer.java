package cl.duoc.pedidos360.usuarios.messaging.producer;

import cl.duoc.pedidos360.usuarios.messaging.dto.UsuarioRegistradoEvent;
import cl.duoc.pedidos360.usuarios.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Productor Kafka del evento de usuario registrado. Un fallo de Kafka no afecta
 * el registro del usuario ni el flujo de RabbitMQ.
 */
@Component
public class UsuarioKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public UsuarioKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${pedidos360.kafka.topics.usuarios-registrados}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publicarUsuarioRegistrado(Usuario usuario) {
        UsuarioRegistradoEvent evento = new UsuarioRegistradoEvent(
                UUID.randomUUID(),
                usuario.getId(),
                usuario.getEntraObjectId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getActivo(),
                Instant.now());

        try {
            kafkaTemplate.send(topic, String.valueOf(evento.usuarioId()), evento)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("event=usuario_registrado_kafka_error usuarioId={} topic={} error={}",
                                    evento.usuarioId(), topic, error.getMessage());
                        } else {
                            log.info("event=usuario_registrado_kafka_publicado usuarioId={} topic={} particion={} offset={}",
                                    evento.usuarioId(), topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("event=usuario_registrado_kafka_error usuarioId={} topic={} error={}",
                    evento.usuarioId(), topic, e.getMessage());
        }
    }
}
