package marketsoft.controller;

import marketsoft.fakes.FakeProductoDAO;
import marketsoft.model.Producto;
import marketsoft.model.ResultadoEliminacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductoControllerTest {

    private FakeProductoDAO dao;
    private ProductoController ctrl;

    @BeforeEach
    void preparar() {
        dao = new FakeProductoDAO();
        ctrl = new ProductoController(dao);
    }

    private Producto productoEnCaja(int id) {
        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre("Galletas Soda 6Pk");
        p.setVieneEnCaja(true);
        p.setUnidadesPorCaja(24);
        p.setPrecioCompraCaja(18.00);
        return p;
    }

    // ---------- guardarProducto ----------
    @Test
    void nombreEnBlanco_noGuardaNiActualiza() {
        Producto p = new Producto();
        p.setNombre("  ");
        assertFalse(ctrl.guardarProducto(p));
        assertTrue(dao.guardados.isEmpty());
        assertTrue(dao.actualizados.isEmpty());
    }

    @Test
    void productoNuevo_idCero_usaGuardar() {
        assertTrue(ctrl.guardarProducto(productoEnCaja(0)));
        assertEquals(1, dao.guardados.size());
        assertTrue(dao.actualizados.isEmpty());
    }

    @Test
    void productoExistente_conId_usaActualizar() {
        assertTrue(ctrl.guardarProducto(productoEnCaja(15)));
        assertEquals(1, dao.actualizados.size());
        assertTrue(dao.guardados.isEmpty());
    }

    @Test
    void productoEnCaja_calculaElPrecioUnitarioDeCompra() {
        Producto p = productoEnCaja(0);              // caja de 24 a S/ 18.00
        ctrl.guardarProducto(p);
        assertEquals(0.75, p.getPrecioCompraUnitario(), 0.0001);
    }

    @Test
    void productoSueltoNoRecalculaElPrecio() {
        Producto p = new Producto();
        p.setNombre("Chicle");
        p.setVieneEnCaja(false);
        p.setPrecioCompraUnitario(0.30);
        ctrl.guardarProducto(p);
        assertEquals(0.30, p.getPrecioCompraUnitario(), 0.0001);
    }

    // ---------- stock ----------
    @Test
    void ingresoDeStock_conCantidadInvalida_noLlamaAlDAO() {
        assertFalse(ctrl.registrarIngresoStock(1, 0));
        assertFalse(ctrl.registrarIngresoStock(1, -5));
        assertEquals(0, dao.llamadasAumentar);
    }

    @Test
    void ingresoDeStock_valido_delegaAlDAO() {
        assertTrue(ctrl.registrarIngresoStock(9, 24));
        assertEquals(9, dao.ultimoIdStock);
        assertEquals(24, dao.ultimaCantidadStock);
    }

    @Test
    void retiroDeStock_conCantidadInvalida_noLlamaAlDAO() {
        assertFalse(ctrl.registrarRetiroStock(1, 0));
        assertEquals(0, dao.llamadasDisminuir);
    }

    // ---------- actualizarPrecios ----------
    @Test
    void actualizarPrecios_conValoresNegativos_devuelveFalse() {
        assertFalse(ctrl.actualizarPrecios(1, -1, 2));
        assertFalse(ctrl.actualizarPrecios(1, 1, -2));
        assertTrue(dao.actualizados.isEmpty());
    }

    @Test
    void actualizarPrecios_productoInexistente_devuelveFalse() {
        dao.productoPorId = null;
        assertFalse(ctrl.actualizarPrecios(99, 5, 8));
    }

    @Test
    void actualizarPrecios_productoEnCaja_elCostoEsDeLaCaja() {
        Producto p = productoEnCaja(3);
        dao.productoPorId = p;
        assertTrue(ctrl.actualizarPrecios(3, 24.00, 1.50));   // caja de 24 ahora cuesta 24
        assertEquals(24.00, p.getPrecioCompraCaja(), 0.0001);
        assertEquals(1.00, p.getPrecioCompraUnitario(), 0.0001);
        assertEquals(1.50, p.getPrecioVentaUnitario(), 0.0001);
    }

    @Test
    void actualizarPrecios_productoSuelto_elCostoEsPorUnidad() {
        Producto p = new Producto();
        p.setIdProducto(4);
        p.setVieneEnCaja(false);
        dao.productoPorId = p;
        assertTrue(ctrl.actualizarPrecios(4, 2.50, 4.00));
        assertEquals(2.50, p.getPrecioCompraUnitario(), 0.0001);
    }

    // ---------- alertas y eliminación ----------
    @Test
    void alertasPorVencer_usaUnaVentanaDeCincoDias() {
        ctrl.alertasPorVencer();
        assertEquals(5, dao.diasLimiteRecibido);
    }

    @Test
    void eliminarProducto_devuelveElResultadoDelDAO() {
        dao.resultadoEliminar = ResultadoEliminacion.OCULTADO;
        assertEquals(ResultadoEliminacion.OCULTADO, ctrl.eliminarProducto(1));
    }
}
