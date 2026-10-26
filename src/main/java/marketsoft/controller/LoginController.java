package marketsoft.controller;
import marketsoft.dao.UsuarioDAO;
import marketsoft.dao.IUsuarioDAO;
import marketsoft.model.Usuario;

public class LoginController {
    private final IUsuarioDAO usuarioDAO;

    public LoginController() {
        this(new UsuarioDAO());
    }

    public LoginController(IUsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario iniciarSesion(String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isBlank()) {
            return null; // la vista muestra el mensaje de campos vacíos
        }
        return usuarioDAO.autenticar(usuario, contrasena);
    }
}
