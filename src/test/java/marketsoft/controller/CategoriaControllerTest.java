package marketsoft.controller;

import marketsoft.fakes.FakeCategoriaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoriaControllerTest {

    private FakeCategoriaDAO dao;
    private CategoriaController ctrl;

    @BeforeEach
    void preparar() {
        dao = new FakeCategoriaDAO();
        ctrl = new CategoriaController(dao);
    }

    @Test
    void nombreEnBlanco_noGuarda() {
        assertFalse(ctrl.guardarCategoria("   "));
        assertFalse(ctrl.guardarCategoria(null));
        assertEquals(0, dao.llamadasGuardar);
    }

    @Test
    void nombreConEspacios_seGuardaRecortado() {
        assertTrue(ctrl.guardarCategoria("  Panadería  "));
        assertEquals("Panadería", dao.ultimoNombre);
    }

    @Test
    void eliminar_delegaElIdAlDAO() {
        ctrl.eliminarCategoria(4);
        assertEquals(4, dao.ultimoIdEliminado);
    }
}
