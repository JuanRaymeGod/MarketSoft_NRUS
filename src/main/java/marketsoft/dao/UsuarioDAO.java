package marketsoft.dao;
import marketsoft.config.ConexionSQLite;
import marketsoft.model.Usuario;
import java.sql.*;

public class UsuarioDAO implements IUsuarioDAO {

    // Login: valida usuario+contraseña y devuelve el Usuario o null
    @Override
    public Usuario autenticar(String usuario, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ?";
        try (PreparedStatement ps = ConexionSQLite.getConexion().prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, contrasena); // ideal: guardar hash, no texto plano
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(rs.getInt("id_usuario"), rs.getString("usuario"), rs.getString("rol"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
