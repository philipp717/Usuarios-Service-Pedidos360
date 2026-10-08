package cl.duoc.pedidos360.usuarios.consumer;

import cl.duoc.pedidos360.usuarios.messaging.dto.PedidoCreadoEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PedidoCreadoListener {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoCreadoListener.class);

    @RabbitListener(
            queues = "${pedidos360.rabbitmq.usuarios.queue}"
    )
    public void recibir(
            PedidoCreadoEvent evento,
            Message message,
            Channel channel) throws IOException {

        long deliveryTag =
                message.getMessageProperties().getDeliveryTag();

        try {
            validarEvento(evento);

            // Simulamos una notificación relacionada con el pedido.
            log.info(
                    "event=notificacion_usuario_simulada pedidoId={} cliente={} producto={} cantidad={}",
                    evento.pedidoId(),
                    evento.cliente(),
                    evento.producto(),
                    evento.cantidad()
            );

            channel.basicAck(deliveryTag, false);

            log.info(
                    "event=pedido_recibido_usuarios eventoId={} pedidoId={} ack=true",
                    evento.eventoId(),
                    evento.pedidoId()
            );

        } catch (Exception error) {

            log.error(
                    "event=error_pedido_usuarios pedidoId={} error={}",
                    evento != null ? evento.pedidoId() : null,
                    error.getMessage(),
                    error
            );

            // Rechazar sin reencolar: RabbitMQ lo envía a la DLQ.
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void validarEvento(PedidoCreadoEvent evento) {

        if (evento == null
                || evento.eventoId() == null
                || evento.pedidoId() == null
                || evento.cliente() == null
                || evento.cliente().isBlank()) {

            throw new IllegalArgumentException(
                    "Evento PedidoCreadoEvent inválido"
            );
        }
    }
}
