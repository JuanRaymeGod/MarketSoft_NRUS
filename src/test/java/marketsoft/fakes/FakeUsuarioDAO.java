package marketsoft.fakes;

import marketsoft.dao.IUsuarioDAO;
import marketsoft.model.Usuario;

/** DAO falso: no usa SQLite; devuelve lo que la prueba configure. */
public class FakeUsuarioDAO implements IUsuarioDAO {

    public Usuario respuesta = null;
    public int llamadas = 0;
    public String ultimoUsuario;
    public String ultimaContrasena;

    @Override
    public Usuario autenticar(String usuario, String contrasena) {
        llamadas++;
        ultimoUsuario = usuario;
        ultimaContrasena = contrasena;
        return respuesta;
    }
}
