package marketsoft.dao;
import marketsoft.config.ConexionSQLite;
import marketsoft.model.DetalleVenta;
import marketsoft.model.Venta;
import marketsoft.util.FechaUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO implements IVentaDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();

    // Relanza la excepción real de SQLite para que la vista pueda mostrar el motivo exacto del fallo.
    @Override
    public void registrarVenta(Venta venta) throws SQLException {
        Connection conn = ConexionSQLite.getConexion();
        String sqlVenta = "INSERT INTO ventas (total, monto_recibido, vuelto, metodo_pago, dni_cliente, nombre_cliente) "
                + "VALUES (?,?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, costo_unitario_compra, subtotal) VALUES (?,?,?,?,?,?)";

        try {
            conn.setAutoCommit(false); // todo o nada

            int idVentaGenerado;
            try (PreparedStatement ps = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, venta.getTotal());
                ps.setDouble(2, venta.getMontoRecibido());
                ps.setDouble(3, venta.getVuelto());
                ps.setString(4, venta.getMetodoPago());
                ps.setString(5, venta.getDniCliente());
                ps.setString(6, venta.getNombreCliente());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idVentaGenerado = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDetalle)) {
                for (DetalleVenta d : venta.getDetalles()) {
                    ps.setInt(1, idVentaGenerado);
                    ps.setInt(2, d.getIdProducto());
                    ps.setInt(3, d.getCantidad());
                    ps.setDouble(4, d.getPrecioUnitario());
                    ps.setDouble(5, d.getCostoUnitarioCompra());
                    ps.setDouble(6, d.getSubtotal());
                    ps.addBatch();
                    productoDAO.descontarStock(conn, d.getIdProducto(), d.getCantidad());
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Para el panel de Historial de Ventas
    @Override
    public List<Venta> listarVentas() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY fecha_hora DESC";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setIdVenta(rs.getInt("id_venta"));
                String fecha = rs.getString("fecha_hora");
                if (fecha != null) {
                    // SQLite guarda UTC ("YYYY-MM-DD HH:MM:SS"); se convierte a hora Lima (UTC-5)
                    v.setFechaHora(FechaUtil.utcALima(fecha));
                }
                v.setTotal(rs.getDouble("total"));
                v.setMontoRecibido(rs.getDouble("monto_recibido"));
                v.setVuelto(rs.getDouble("vuelto"));
                v.setMetodoPago(rs.getString("metodo_pago"));
                v.setDniCliente(rs.getString("dni_cliente"));
                v.setNombreCliente(rs.getString("nombre_cliente"));
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Trae qué productos y cantidades se vendieron en una venta, para poder devolver el stock
    public List<DetalleVenta> obtenerDetallesPorVenta(int idVenta) {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT dv.id_producto, p.nombre, dv.cantidad, dv.precio_unitario "
                + "FROM detalle_ventas dv JOIN productos p ON dv.id_producto = p.id_producto "
                + "WHERE dv.id_venta = ?";
        try (PreparedStatement ps = ConexionSQLite.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleVenta(rs.getInt("id_producto"), rs.getString("nombre"),
                            rs.getInt("cantidad"), rs.getDouble("precio_unitario"), 0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Elimina una venta: devuelve el stock de cada producto vendido y borra el registro.
    // El DELETE en "ventas" hace cascada sobre "detalle_ventas" porque el schema lo declara
    // ON DELETE CASCADE y PRAGMA foreign_keys = ON está activo desde ConexionSQLite.
    @Override
    public boolean eliminarVenta(int idVenta) throws SQLException {
        Connection conn = ConexionSQLite.getConexion();
        List<DetalleVenta> detalles = obtenerDetallesPorVenta(idVenta);
        try {
            conn.setAutoCommit(false);

            for (DetalleVenta d : detalles) {
                productoDAO.aumentarStock(d.getIdProducto(), d.getCantidad());
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ventas WHERE id_venta = ?")) {
                ps.setInt(1, idVenta);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public double totalVentasDelMes() {
        // Peru UTC-5: se compara el mes en hora Lima, no UTC (evita que ventas de la 00:xx caigan en otro dia/mes)
        String sql = "SELECT COALESCE(SUM(total),0) AS total FROM ventas "
                + "WHERE strftime('%Y-%m', fecha_hora, '-5 hours') = strftime('%Y-%m','now','-5 hours')";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double totalVentasDelDia() {
        String sql = "SELECT COALESCE(SUM(total),0) AS total FROM ventas WHERE date(fecha_hora,'-5 hours') = date('now','-5 hours')";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int contarVentasDelDia() {
        String sql = "SELECT COUNT(*) AS n FROM ventas WHERE date(fecha_hora,'-5 hours') = date('now','-5 hours')";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("n");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public double ticketPromedioDia() {
        String sql = "SELECT COALESCE(AVG(total),0) AS t FROM ventas WHERE date(fecha_hora,'-5 hours') = date('now','-5 hours')";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("t");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    /** Opcion A dashboard: total del dia por metodo (hora Lima). metodo: Efectivo/Yape/Plin. */
    @Override
    public double totalDiaPorMetodo(String metodoPago) {
        String sql = "SELECT COALESCE(SUM(total),0) AS t FROM ventas "
                + "WHERE date(fecha_hora,'-5 hours') = date('now','-5 hours') AND metodo_pago = ?";
        try (PreparedStatement ps = ConexionSQLite.getConexion().prepareStatement(sql)) {
            ps.setString(1, metodoPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("t");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    /** Suma Yape + Plin del dia (para tarjeta Digital). */
    @Override
    public double totalDigitalDia() {
        String sql = "SELECT COALESCE(SUM(total),0) AS t FROM ventas "
                + "WHERE date(fecha_hora,'-5 hours') = date('now','-5 hours') AND metodo_pago IN ('Yape','Plin')";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("t");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    /** Ganancia estimada del dia: suma(subtotal - cantidad*costo_unitario) en hora Lima. */
    @Override
    public double gananciaEstimadaDelDia() {
        String sql = "SELECT COALESCE(SUM(dv.subtotal - dv.cantidad * COALESCE(dv.costo_unitario_compra, p.precio_compra_unitario)),0) AS g "
                + "FROM detalle_ventas dv "
                + "JOIN ventas v ON dv.id_venta = v.id_venta "
                + "JOIN productos p ON dv.id_producto = p.id_producto "
                + "WHERE date(v.fecha_hora,'-5 hours') = date('now','-5 hours')";
        try (Statement st = ConexionSQLite.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("g");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public double totalVentasDelMesValor() { return totalVentasDelMes(); }
}
