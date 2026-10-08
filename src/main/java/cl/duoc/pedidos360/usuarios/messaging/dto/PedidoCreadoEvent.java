package cl.duoc.pedidos360.usuarios.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record PedidoCreadoEvent(
        UUID eventoId,
        Long pedidoId,
        String cliente,
        String producto,
        Integer cantidad,
        String estado,
        Instant fechaCreacion
) {
}