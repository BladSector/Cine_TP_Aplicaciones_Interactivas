package service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AdminService {
    private final String usuarioConfigurado;
    private final String contraseniaConfigurada;
    private final SesionService sesionService;

    public AdminService(@Value("${app.admin.username}") String usuarioConfigurado,
                        @Value("${app.admin.password}") String contraseniaConfigurada,
                        SesionService sesionService) {
        this.usuarioConfigurado = usuarioConfigurado;
        this.contraseniaConfigurada = contraseniaConfigurada;
        this.sesionService = sesionService;
    }

    public void iniciarSesion(String usuario, String contrasenia, HttpSession sesion) {
        if (!credencialesValidas(usuario, contrasenia)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Las credenciales del administrador no son correctas.");
        }
        sesionService.iniciarAdministrador(sesion);
    }

    public boolean credencialesValidas(String usuario, String contrasenia) {
        return coincide(usuario, usuarioConfigurado) && coincide(contrasenia, contraseniaConfigurada);
    }

    private boolean coincide(String valor, String esperado) {
        byte[] recibido = (valor == null ? "" : valor).getBytes(StandardCharsets.UTF_8);
        byte[] configurado = esperado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(recibido, configurado);
    }
}
