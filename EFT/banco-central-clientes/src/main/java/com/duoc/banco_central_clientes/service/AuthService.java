package com.duoc.banco_central_clientes.service;

import com.duoc.banco_central_clientes.entity.Usuario;
import com.duoc.banco_central_clientes.repository.UsuarioRepository;
import com.duoc.banco_central_clientes.security.JwtService;
import com.duoc.eventos.AlertaSeguridadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String TOPIC_ALERTAS = "alertas-seguridad";
    private static final int MAX_INTENTOS_FALLIDOS = 3;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Intentos fallidos consecutivos por usuario. Vive en memoria: se pierde al reiniciar el servicio.
    private final Map<String, Integer> intentosFallidos = new ConcurrentHashMap<>();

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String login(String username, String password, String canal) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        if (usuario == null || !passwordEncoder.matches(password, usuario.getPasswordHash())) {
            registrarIntentoFallido(username, canal);
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        intentosFallidos.remove(username);
        return jwtService.generarToken(usuario.getUsername(), usuario.getRol().name(), usuario.getCuentaIdLegacy());
    }

    private void registrarIntentoFallido(String username, String canal) {
        int intentos = intentosFallidos.merge(username, 1, Integer::sum);
        log.warn("Login fallido para el usuario '{}' (intento {} de {})", username, intentos, MAX_INTENTOS_FALLIDOS);

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            AlertaSeguridadEvent alerta = new AlertaSeguridadEvent(
                    username,
                    canal,
                    intentos,
                    "Intentos de login fallidos consecutivos",
                    LocalDateTime.now()
            );
            kafkaTemplate.send(TOPIC_ALERTAS, username, alerta);
            log.warn("Publicado AlertaSeguridadEvent: usuario={}, intentos={}, canal={}", username, intentos, canal);
            intentosFallidos.remove(username);
        }
    }
}