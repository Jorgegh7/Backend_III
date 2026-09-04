package com.duoc.banco_central_xyz.repository;

import com.duoc.banco_central_xyz.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

Optional<Usuario> findByUsername(String username);
}
