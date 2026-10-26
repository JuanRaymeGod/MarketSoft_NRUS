package marketsoft.dao;

import marketsoft.model.Usuario;

/** Contrato de acceso a datos (el controller depende de esta interfaz, no de la clase concreta). */
public interface IUsuarioDAO {

    Usuario autenticar(String usuario, String contrasena);
}
