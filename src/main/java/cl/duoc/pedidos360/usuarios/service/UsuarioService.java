package cl.duoc.pedidos360.usuarios.service;

import cl.duoc.pedidos360.usuarios.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    private final Map<Long, Usuario> usuarios = new LinkedHashMap<>();
    private long siguienteId = 1L;

    public UsuarioService() {
        agregarUsuarioInicial(
                "11111111-1111-1111-1111-111111111111",
                "Laura Méndez",
                "laura.mendez@example.com",
                "+56 9 1111 1111",
                "Av. Providencia 100",
                true);
        agregarUsuarioInicial(
                "22222222-2222-2222-2222-222222222222",
                "Diego Rojas",
                "diego.rojas@example.com",
                "+56 9 2222 2222",
                "Av. Irarrázaval 200",
                true);
        agregarUsuarioInicial(
                "33333333-3333-3333-3333-333333333333",
                "Sofía Castro",
                "sofia.castro@example.com",
                "+56 9 3333 3333",
                "Av. Las Condes 300",
                false);
    }

    public synchronized List<Usuario> listarTodos() {
        return List.copyOf(usuarios.values());
    }

    public synchronized Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public synchronized Optional<Usuario> buscarPorEntraObjectId(String entraObjectId) {
        if (entraObjectId == null) {
            return Optional.empty();
        }

        return usuarios.values().stream()
                .filter(usuario -> entraObjectId.equals(usuario.getEntraObjectId()))
                .findFirst();
    }

    public synchronized Usuario crear(Usuario usuario) {
        usuario.setId(siguienteId++);
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    public synchronized Optional<Usuario> actualizar(Long id, Usuario usuario) {
        if (!usuarios.containsKey(id)) {
            return Optional.empty();
        }

        usuario.setId(id);
        usuarios.put(id, usuario);
        return Optional.of(usuario);
    }

    public synchronized boolean eliminar(Long id) {
        return usuarios.remove(id) != null;
    }

    private void agregarUsuarioInicial(
            String entraObjectId,
            String nombre,
            String email,
            String telefono,
            String direccion,
            Boolean activo) {
        Usuario usuario = new Usuario(
                siguienteId++, entraObjectId, nombre, email, telefono, direccion, activo);
        usuarios.put(usuario.getId(), usuario);
    }
}
