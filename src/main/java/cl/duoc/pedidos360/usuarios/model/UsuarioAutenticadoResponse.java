package cl.duoc.pedidos360.usuarios.model;

public record UsuarioAutenticadoResponse(
        String objectId,
        String nombre,
        String username,
        String email,
        String tenantId,
        String scope) {
}
