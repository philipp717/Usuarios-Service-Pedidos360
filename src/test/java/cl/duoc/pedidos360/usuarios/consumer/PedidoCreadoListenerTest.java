package cl.duoc.pedidos360.usuarios.consumer;

import cl.duoc.pedidos360.usuarios.messaging.dto.PedidoCreadoEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PedidoCreadoListenerTest {

    private PedidoCreadoListener listener;
    private Channel channel;
    private Message message;

    @BeforeEach
    void configurar() {
        listener = new PedidoCreadoListener();
        channel = mock(Channel.class);

        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(1L);

        message = new Message(new byte[0], properties);
    }

    @Test
    void pedidoValidoDebeConfirmarseConAck() throws IOException {
        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                UUID.randomUUID(),
                100L,
                "Cliente prueba",
                "Producto RabbitMQ",
                2,
                "NUEVO",
                Instant.now()
        );

        listener.recibir(evento, message, channel);

        verify(channel).basicAck(1L, false);
        verify(channel, never())
                .basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void pedidoConClienteVacioDebeRecibirNack() throws IOException {
        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                UUID.randomUUID(),
                101L,
                "",
                "Producto prueba",
                1,
                "NUEVO",
                Instant.now()
        );

        listener.recibir(evento, message, channel);

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void eventoNuloDebeRecibirNack() throws IOException {
        listener.recibir(null, message, channel);

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}