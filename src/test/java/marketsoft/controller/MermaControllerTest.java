package marketsoft.controller;

import marketsoft.fakes.FakeMermaDAO;
import marketsoft.model.Merma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MermaControllerTest {

    private FakeMermaDAO dao;
    private MermaController ctrl;

    @BeforeEach
    void preparar() {
        dao = new FakeMermaDAO();
        ctrl = new MermaController(dao);
    }

    @Test
    void cantidadCero_noRegistraNiLlamaAlDAO() {
        assertFalse(ctrl.registrarMerma(1, 0, "Vencimiento"));
        assertEquals(0, dao.llamadasRegistrar);
    }

    @Test
    void cantidadNegativa_noRegistra() {
        assertFalse(ctrl.registrarMerma(1, -3, "Rotura"));
        assertEquals(0, dao.llamadasRegistrar);
    }

    @Test
    void cantidadValida_delegaAlDAOConLosMismosDatos() {
        assertTrue(ctrl.registrarMerma(7, 3, "Vencimiento"));
        assertEquals(1, dao.llamadasRegistrar);
        assertEquals(7, dao.ultimoIdProducto);
        assertEquals(3, dao.ultimaCantidad);
        assertEquals("Vencimiento", dao.ultimoMotivo);
    }

    @Test
    void listarPorRango_pasaLasFechasAlDAO() {
        ctrl.listarPorRango("2026-08-01", "2026-08-31");
        assertEquals("2026-08-01", dao.ultimoDesde);
        assertEquals("2026-08-31", dao.ultimoHasta);
    }

    @Test
    void totalPerdida_sumaLaPerdidaDeCadaMerma() {
        Merma a = new Merma(); a.setPerdidaTotal(7.60);
        Merma b = new Merma(); b.setPerdidaTotal(2.40);
        assertEquals(10.00, ctrl.totalPerdida(List.of(a, b)), 0.001);
    }

    @Test
    void totalPerdida_listaVacia_esCero() {
        assertEquals(0.0, ctrl.totalPerdida(List.of()), 0.001);
    }
}
