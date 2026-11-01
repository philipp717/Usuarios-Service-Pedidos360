package cl.duoc.pedidos360.usuarios.service;

import cl.duoc.pedidos360.usuarios.messaging.producer.UsuarioKafkaProducer;
import cl.duoc.pedidos360.usuarios.model.Usuario;
import cl.duoc.pedidos360.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioKafkaProducer usuarioKafkaProducer;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioKafkaProducer usuarioKafkaProducer) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioKafkaProducer = usuarioKafkaProducer;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEntraObjectId(String entraObjectId) {
        if (entraObjectId == null) {
            return Optional.empty();
        }

        return usuarioRepository.findAll().stream()
                .filter(usuario -> entraObjectId.equals(usuario.getEntraObjectId()))
                .findFirst();
    }

    public Usuario crear(Usuario usuario) {
        usuario.setId(null);
        Usuario usuarioCreado = usuarioRepository.save(usuario);
        usuarioKafkaProducer.publicarUsuarioRegistrado(usuarioCreado);
        return usuarioCreado;
    }

    public Optional<Usuario> actualizar(Long id, Usuario usuario) {
        if (!usuarioRepository.existsById(id)) {
            return Optional.empty();
        }

        usuario.setId(id);
        return Optional.of(usuarioRepository.save(usuario));
    }

    public boolean eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            return false;
        }

        usuarioRepository.deleteById(id);
        return true;
    }
}
