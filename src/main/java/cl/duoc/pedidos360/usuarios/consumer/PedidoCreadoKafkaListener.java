package cl.duoc.pedidos360.usuarios.consumer;

import cl.duoc.pedidos360.usuarios.messaging.dto.PedidoCreadoEvent;
import cl.duoc.pedidos360.usuarios.service.PedidoEventoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener Kafka del tópico de pedidos creados. Solo valida y delega al servicio de dominio.
 * Las excepciones se relanzan para que el DefaultErrorHandler aplique reintentos y DLT.
 */
@Component
public class PedidoCreadoKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoKafkaListener.class);

    private final PedidoEventoService pedidoEventoService;

    public PedidoCreadoKafkaListener(PedidoEventoService pedidoEventoService) {
        this.pedidoEventoService = pedidoEventoService;
    }

    @KafkaListener(
            topics = "${pedidos360.kafka.topics.pedidos-creados}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void recibir(
            @Payload(required = false) PedidoCreadoEvent evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            validar(evento);
            pedidoEventoService.registrarActividadDePedido(evento);
            log.info("event=pedido_creado_kafka_recibido eventoId={} pedidoId={} topic={} particion={} offset={}",
                    evento.eventoId(), evento.pedidoId(), topic, partition, offset);
        } catch (Exception error) {
            log.error("event=error_pedido_creado_kafka topic={} particion={} offset={} pedidoId={} error={}",
                    topic, partition, offset,
                    evento != null ? evento.pedidoId() : null,
                    error.getMessage(), error);
            throw error;
        }
    }

    private void validar(PedidoCreadoEvent evento) {
        if (evento == null
                || evento.eventoId() == null
                || evento.pedidoId() == null
                || evento.cliente() == null
                || evento.cliente().isBlank()) {
            throw new IllegalArgumentException("Evento PedidoCreadoEvent nulo, vacío o inválido");
        }
    }
}
