package marketsoft.view;

import marketsoft.controller.MermaController;
import marketsoft.controller.ReporteController;
import marketsoft.model.FilaReporte;
import marketsoft.model.FilaVenta;
import marketsoft.model.Merma;
import marketsoft.util.FechaUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Punto 4 admin + OPCION 14: pestanas Ganancias y Perdidas por merma (valorizada a costo). */
public class ReportesPanel extends JPanel {

    private final ReporteController reporteController = new ReporteController();
    private final MermaController mermaController = new MermaController();
    private JTextField txtDesde, txtHasta;
    private DefaultTableModel modeloReporte, modeloMermas;
    private JTable tabla;
    private JLabel lblVenta, lblCosto, lblGanancia, lblNVentas, lblPerdida, lblMermasN;
    private JTabbedPane tabs;

    public ReportesPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Rango (AAAA-MM-DD) — aplica a ambas pestañas"));
        txtDesde = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
        txtHasta = new JTextField(LocalDate.now().toString(), 10);
        txtDesde.putClientProperty("JTextField.placeholderText", "2026-09-01");
        txtHasta.putClientProperty("JTextField.placeholderText", "2026-09-28");
        JButton btnGenerar = new JButton("📊 Generar");
        btnGenerar.setBackground(new Color(0x1565C0)); btnGenerar.setForeground(Color.WHITE);
        btnGenerar.addActionListener(e -> generarReporte());
        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(txtDesde);
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(txtHasta);
        panelFiltros.add(btnGenerar);

        JButton btnExportarCSV = new JButton("📥 Exportar CSV (Excel)");
        btnExportarCSV.setToolTipText("Exporta ventas del rango (una fila por venta)");
        btnExportarCSV.addActionListener(e -> exportarCSV());
        panelFiltros.add(btnExportarCSV);

        JButton btnExportarMermas = new JButton("📥 Exportar mermas CSV");
        btnExportarMermas.setToolTipText("Exporta pérdidas por merma del rango");
        btnExportarMermas.addActionListener(e -> exportarMermasCSV());
        panelFiltros.add(btnExportarMermas);
        add(panelFiltros, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.addTab("💰 Ganancias por producto", panelGanancias());
        tabs.addTab("🗑 Pérdidas por merma", panelMermas());
        add(tabs, BorderLayout.CENTER);

        JPanel resumen = new JPanel(new GridLayout(1, 5, 8, 0));
        lblVenta = resumenLabel(); lblCosto = resumenLabel(); lblGanancia = resumenLabel();
        lblNVentas = resumenLabel(); lblPerdida = resumenLabel();
        lblMermasN = lblNVentas;
        resumen.add(resumenCard("Venta total", lblVenta));
        resumen.add(resumenCard("Costo total", lblCosto));
        resumen.add(resumenCard("Ganancia neta", lblGanancia));
        resumen.add(resumenCard("Pérdida merma", lblPerdida));
        lblPerdida.setForeground(UiHelper.ROJO);
        resumen.add(resumenCard("N° ventas", lblNVentas));
        resumen.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        add(resumen, BorderLayout.SOUTH);

        generarReporte();
    }

    private JPanel panelGanancias() {
        JPanel p = new JPanel(new BorderLayout());
        modeloReporte = new DefaultTableModel(
                new String[]{"Producto", "Cantidad", "Costo Total", "Venta Total", "Ganancia Neta"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloReporte);
        UiHelper.estiloTabla(tabla);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    try {
                        double g = Double.parseDouble(v.toString());
                        comp.setBackground(g < 0 ? UiHelper.FONDO_ROJO_SUAVE : Color.WHITE);
                        comp.setForeground(g < 0 ? UiHelper.ROJO : UiHelper.VERDE);
                        setFont(getFont().deriveFont(Font.BOLD)); setHorizontalAlignment(RIGHT);
                    } catch (Exception ignored) {}
                }
                return comp;
            }
        });
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    private JTable tablaMermas;
    private JPanel panelMermas() {
        JPanel p = new JPanel(new BorderLayout());
        modeloMermas = new DefaultTableModel(new String[]{"Fecha", "Producto", "Cant.(unid.)", "Costo Unit.", "Pérdida S/.", "Motivo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMermas = new JTable(modeloMermas);
        UiHelper.estiloTabla(tablaMermas);
        tablaMermas.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) { comp.setForeground(UiHelper.ROJO); setFont(getFont().deriveFont(Font.BOLD)); setHorizontalAlignment(RIGHT); }
                return comp;
            }
        });
        p.add(new JScrollPane(tablaMermas), BorderLayout.CENTER);
        JLabel hint = new JLabel("ℹ Se genera al usar '− Retirar (merma)' en Inventario. Valorizada a costo unitario de compra.");
        hint.setForeground(Color.GRAY);
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    private JLabel resumenLabel() {
        JLabel l = new JLabel("S/. 0.00", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        return l;
    }
    private JPanel resumenCard(String t, JLabel v) {
        JPanel c = UiHelper.tarjeta(t);
        if (t.contains("Ganancia")) v.setForeground(UiHelper.VERDE);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    private boolean fechasValidas(String desde, String hasta) {
        try {
            LocalDate d1 = LocalDate.parse(desde), d2 = LocalDate.parse(hasta);
            if (d1.isAfter(d2)) { JOptionPane.showMessageDialog(this, "'Desde' no puede ser posterior a 'Hasta'.", "Validación", JOptionPane.WARNING_MESSAGE); return false; }
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fechas inválidas. Usa formato AAAA-MM-DD (Ej. 2026-09-01).", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private void generarReporte() {
        String desde = txtDesde.getText().trim(), hasta = txtHasta.getText().trim();
        if (!fechasValidas(desde, hasta)) return;
        // Ganancias
        modeloReporte.setRowCount(0);
        List<FilaReporte> filas = reporteController.obtenerRentabilidad(desde, hasta);
        double vT = 0, cT = 0, gT = 0;
        for (FilaReporte f : filas) {
            modeloReporte.addRow(new Object[]{f.producto, f.cantidad,
                    String.format("%.2f", f.costoTotal), String.format("%.2f", f.ventaTotal), String.format("%.2f", f.gananciaNeta)});
            vT += f.ventaTotal; cT += f.costoTotal; gT += f.gananciaNeta;
        }
        lblVenta.setText(String.format("S/. %.2f", vT));
        lblCosto.setText(String.format("S/. %.2f", cT));
        lblGanancia.setText(String.format("S/. %.2f", gT));
        lblGanancia.setForeground(gT < 0 ? UiHelper.ROJO : UiHelper.VERDE);
        List<FilaVenta> ventas = reporteController.obtenerVentasConCosto(desde, hasta);
        lblNVentas.setText(ventas.isEmpty() ? "0" : ventas.size() + " (" + UiHelper.moneda(vT / ventas.size()) + " c/u)");
        // Mermas (fecha UTC -> Lima)
        modeloMermas.setRowCount(0);
        List<Merma> mermas = mermaController.listarPorRango(desde, hasta);
        for (Merma m : mermas) {
            String fecha = FechaUtil.formatearLima(m.getFecha());
            modeloMermas.addRow(new Object[]{fecha, m.getNombreProducto(), m.getCantidad(),
                    String.format("%.2f", m.getCostoUnitario()), String.format("%.2f", m.getPerdidaTotal()), m.getMotivo()});
        }
        double perdida = mermaController.totalPerdida(mermas);
        lblPerdida.setText(String.format("S/. %.2f (%d)", perdida, mermas.size()));
        tabs.setTitleAt(0, "💰 Ganancias (" + filas.size() + ")");
        tabs.setTitleAt(1, "🗑 Pérdidas por merma (" + mermas.size() + ")");
    }

    private void exportarCSV() {
        String desde = txtDesde.getText().trim(), hasta = txtHasta.getText().trim();
        if (!fechasValidas(desde, hasta)) return;
        List<FilaVenta> ventas = reporteController.obtenerVentasConCosto(desde, hasta);
        if (ventas.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay ventas en ese rango.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("historial_ventas_" + desde + "_a_" + hasta + ".csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = selector.getSelectedFile();
        Locale localeExcel = new Locale("es", "PE");
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Fecha;Cliente;DNI;Metodo de Pago;Venta Total;Costo Total;Ganancia Neta\n");
            for (FilaVenta f : ventas) {
                String fecha = FechaUtil.formatearLima(f.fecha); // UTC -> Lima (antes salia +5h)
                w.write(String.format(localeExcel, "%s;%s;%s;%s;%.2f;%.2f;%.2f%n",
                        fecha, f.cliente != null ? f.cliente : "", f.dni != null ? f.dni : "",
                        f.metodoPago, f.ventaTotal, f.costoTotal, f.gananciaNeta));
            }
            JOptionPane.showMessageDialog(this, "✓ Exportado:\n" + archivo.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarMermasCSV() {
        String desde = txtDesde.getText().trim(), hasta = txtHasta.getText().trim();
        if (!fechasValidas(desde, hasta)) return;
        List<Merma> mermas = mermaController.listarPorRango(desde, hasta);
        if (mermas.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay mermas en ese rango.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("mermas_" + desde + "_a_" + hasta + ".csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(selector.getSelectedFile()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Fecha;Producto;Cantidad;Costo Unitario;Perdida Total;Motivo\n");
            Locale es = new Locale("es", "PE");
            for (Merma m : mermas)
                w.write(String.format(es, "%s;%s;%d;%.2f;%.2f;%s%n",
                        FechaUtil.formatearLima(m.getFecha()), m.getNombreProducto(), m.getCantidad(),
                        m.getCostoUnitario(), m.getPerdidaTotal(), m.getMotivo() != null ? m.getMotivo() : ""));
            JOptionPane.showMessageDialog(this, "✓ Mermas exportadas:\n" + selector.getSelectedFile().getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
