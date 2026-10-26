package marketsoft.dao;

import marketsoft.config.ConexionSQLite;
import marketsoft.model.Merma;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Mermas: perdidas por vencimiento/rotura/robo. Registra merma + descuenta stock en una transaccion. */
public class MermaDAO implements IMermaDAO {

    /** Registra merma en UNIDADES y descuenta stock atomicamente. Devuelve false si stock insuficiente. */
    @Override
    public boolean registrarMerma(int idProducto, int cantidadUnidades, String motivo) {
        if (cantidadUnidades <= 0) return false;
        Connection conn = ConexionSQLite.getConexion();
        try {
            conn.setAutoCommit(false);
            int stock;
            try (PreparedStatement ps = conn.prepareStatement("SELECT stock_unidades FROM productos WHERE id_producto = ?")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return false; }
                    stock = rs.getInt(1);
                }
            }
            if (stock < cantidadUnidades) { conn.rollback(); return false; }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO mermas (id_producto, cantidad, motivo) VALUES (?,?,?)")) {
                ps.setInt(1, idProducto);
                ps.setInt(2, cantidadUnidades);
                ps.setString(3, motivo == null || motivo.isBlank() ? "Ajuste" : motivo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE productos SET stock_unidades = stock_unidades - ? WHERE id_producto = ?")) {
                ps.setInt(1, cantidadUnidades);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    @Override
    public List<Merma> listarPorRango(String desde, String hasta) {
        List<Merma> lista = new ArrayList<>();
        String sql = "SELECT m.id_merma, m.id_producto, p.nombre, m.cantidad, m.motivo, m.fecha, "
                + "COALESCE(p.precio_compra_unitario,0) AS costo "
                + "FROM mermas m JOIN productos p ON p.id_producto = m.id_producto "
                + "WHERE date(m.fecha,'-5 hours') BETWEEN ? AND ? ORDER BY m.fecha DESC";
        try (PreparedStatement ps = ConexionSQLite.getConexion().prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Merma m = new Merma();
                    m.setIdMerma(rs.getInt("id_merma"));
                    m.setIdProducto(rs.getInt("id_producto"));
                    m.setNombreProducto(rs.getString("nombre"));
                    m.setCantidad(rs.getInt("cantidad"));
                    m.setMotivo(rs.getString("motivo"));
                    m.setFecha(rs.getString("fecha"));
                    m.setCostoUnitario(rs.getDouble("costo"));
                    m.setPerdidaTotal(m.getCantidad() * m.getCostoUnitario());
                    lista.add(m);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}
