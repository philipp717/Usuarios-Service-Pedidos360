package cl.duoc.pedidos360.usuarios.repository;

import cl.duoc.pedidos360.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
