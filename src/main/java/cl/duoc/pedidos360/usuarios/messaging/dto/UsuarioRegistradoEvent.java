package cl.duoc.pedidos360.usuarios.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record UsuarioRegistradoEvent(
        UUID eventoId,
        Long usuarioId,
        String entraObjectId,
        String nombre,
        String email,
        Boolean activo,
        Instant fechaRegistro) {
}
