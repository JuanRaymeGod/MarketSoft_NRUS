package marketsoft.controller;

import marketsoft.fakes.FakeVentaDAO;
import marketsoft.model.DetalleVenta;
import marketsoft.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class VentaControllerTest {

    private FakeVentaDAO dao;
    private VentaController ctrl;

    @BeforeEach
    void preparar() {
        dao = new FakeVentaDAO();
        ctrl = new VentaController(dao);
    }

    /** Carrito de ejemplo: 2 x Arroz (4.20) + 1 x Leche (3.80) = S/ 12.20 */
    private List<DetalleVenta> carritoEjemplo() {
        List<DetalleVenta> c = new ArrayList<>();
        c.add(new DetalleVenta(1, "Arroz Costeño 1kg", 2, 4.20, 3.00));
        c.add(new DetalleVenta(2, "Leche Gloria 400g", 1, 3.80, 3.00));
        return c;
    }

    // ---------- cálculo ----------
    @Test
    void calcularVuelto_esMontoRecibidoMenosTotal() {
        assertEquals(7.80, ctrl.calcularVuelto(12.20, 20.00), 0.001);
    }

    // ---------- procesarVenta: camino feliz ----------
    @Test
    void ventaCorrecta_seRegistraConTotalYVueltoCalculados() throws Exception {
        assertTrue(ctrl.procesarVenta(carritoEjemplo(), 20.00, "Efectivo", null, null));
        assertEquals(1, dao.registradas.size());
        Venta v = dao.registradas.get(0);
        assertEquals(12.20, v.getTotal(), 0.001);
        assertEquals(7.80, v.getVuelto(), 0.001);
        assertEquals("Efectivo", v.getMetodoPago());
        assertEquals(2, v.getDetalles().size());
    }

    @Test
    void ventaAnonima_dniYNombreEnBlancoQuedanNulos() throws Exception {
        ctrl.procesarVenta(carritoEjemplo(), 20.00, "Yape", "   ", "");
        Venta v = dao.registradas.get(0);
        assertNull(v.getDniCliente());
        assertNull(v.getNombreCliente());
    }

    @Test
    void dniYNombre_seGuardanSinEspaciosSobrantes() throws Exception {
        ctrl.procesarVenta(carritoEjemplo(), 20.00, "Plin", " 45678912 ", "  Juan Pérez ");
        Venta v = dao.registradas.get(0);
        assertEquals("45678912", v.getDniCliente());
        assertEquals("Juan Pérez", v.getNombreCliente());
    }

    // ---------- procesarVenta: reglas de rechazo ----------
    @Test
    void montoInsuficiente_noRegistraLaVenta() throws Exception {
        assertFalse(ctrl.procesarVenta(carritoEjemplo(), 10.00, "Efectivo", null, null));
        assertTrue(dao.registradas.isEmpty());
    }

    @Test
    void carritoVacio_noRegistraLaVenta() throws Exception {
        assertFalse(ctrl.procesarVenta(new ArrayList<>(), 10.00, "Efectivo", null, null));
        assertTrue(dao.registradas.isEmpty());
    }

    @Test
    void carritoNulo_noRegistraLaVenta() throws Exception {
        assertFalse(ctrl.procesarVenta(null, 10.00, "Efectivo", null, null));
        assertTrue(dao.registradas.isEmpty());
    }

    @Test
    void dniInvalido_noRegistraLaVenta() throws Exception {
        assertFalse(ctrl.procesarVenta(carritoEjemplo(), 20.00, "Efectivo", "123", null));
        assertFalse(ctrl.procesarVenta(carritoEjemplo(), 20.00, "Efectivo", "1234567A", null));
        assertTrue(dao.registradas.isEmpty());
    }

    // ---------- monitor NRUS y arqueo ----------
    @Test
    void porcentajeLimiteNRUS_calculaVentasDelMesSobreElLimite() {
        dao.totalMes = 4640.00;
        assertEquals(58.0, ctrl.porcentajeLimiteNRUS(8000.0), 0.001);
    }

    @Test
    void porcentajeLimiteNRUS_conLimiteCeroOrNegativo_devuelveCero() {
        dao.totalMes = 4640.00;
        assertEquals(0.0, ctrl.porcentajeLimiteNRUS(0), 0.001);
        assertEquals(0.0, ctrl.porcentajeLimiteNRUS(-5), 0.001);
    }

    @Test
    void arqueoPorMetodoDePago_consultaElMetodoCorrecto() {
        ctrl.totalEfectivoDia();
        assertEquals("Efectivo", dao.ultimoMetodoConsultado);
        ctrl.totalYapeDia();
        assertEquals("Yape", dao.ultimoMetodoConsultado);
        ctrl.totalPlinDia();
        assertEquals("Plin", dao.ultimoMetodoConsultado);
    }
}
