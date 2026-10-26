package marketsoft.controller;

import marketsoft.fakes.FakeUsuarioDAO;
import marketsoft.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private FakeUsuarioDAO dao;
    private LoginController ctrl;

    @BeforeEach
    void preparar() {
        dao = new FakeUsuarioDAO();
        ctrl = new LoginController(dao);   // inyección: sin base de datos
    }

    @Test
    void credencialesValidas_devuelveElUsuario() {
        dao.respuesta = new Usuario(1, "admin", "Administrador");
        Usuario u = ctrl.iniciarSesion("admin", "1234");
        assertNotNull(u);
        assertEquals("Administrador", u.getRol());
        assertEquals("admin", dao.ultimoUsuario);
    }

    @Test
    void credencialesIncorrectas_devuelveNull() {
        dao.respuesta = null;
        assertNull(ctrl.iniciarSesion("admin", "mala"));
        assertEquals(1, dao.llamadas);
    }

    @Test
    void camposEnBlanco_noConsultaLaBaseDeDatos() {
        assertNull(ctrl.iniciarSesion("   ", ""));
        assertNull(ctrl.iniciarSesion("admin", "  "));
        assertEquals(0, dao.llamadas);
    }

    @Test
    void camposNulos_noConsultaLaBaseDeDatos() {
        assertNull(ctrl.iniciarSesion(null, null));
        assertEquals(0, dao.llamadas);
    }
}
