package marketsoft.view;

import marketsoft.config.AppConfig;
import marketsoft.controller.ProductoController;
import marketsoft.controller.VentaController;
import marketsoft.model.Producto;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * OPCION A "Cierre de Caja":
 * Arriba 4 tarjetas: Ventas Hoy | Efectivo en Caja | Digital Yape/Plin | Ganancia Estimada Hoy.
 * Sin Ticket promedio. Inventario solo como badges en alertas. NRUS heroe a ancho completo.
 * Componentes: JPanel cards (UiHelper.tarjeta), JLabel valor grande + sublabel gris,
 * JProgressBar NRUS 0-1000 con semaforo, JTabbedPane alertas con renderers.
 */
public class DashboardPanel extends JPanel {

    private final VentaController ventaController = new VentaController();
    private final ProductoController productoController = new ProductoController();

    private JLabel lblVentas, lblVentasSub;
    private JLabel lblEfectivo, lblEfectivoSub;
    private JLabel lblDigital, lblDigitalSub;
    private JLabel lblGanancia, lblGananciaSub;
    private JLabel lblNrusMonto, lblNrusAlerta;
    private JProgressBar barraLimiteNRUS;
    private DefaultTableModel modeloStock, modeloVence;
    private JTabbedPane tabsAlertas;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(construirKpisCaja(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(barraInferior(), BorderLayout.SOUTH);
        recargar();
    }

    // ---------- Arriba: 4 tarjetas de caja ----------
    private JPanel construirKpisCaja() {
        JPanel p = new JPanel(new GridLayout(1, 4, 10, 0));

        lblVentas = kpiValor();
        lblVentasSub = kpiSub();
        p.add(kpiCard("💰 Ventas de hoy", lblVentas, lblVentasSub,
                "Suma total cobrada hoy (hora Lima). Base del arqueo."));

        lblEfectivo = kpiValor();
        lblEfectivoSub = kpiSub();
        lblEfectivo.setForeground(new Color(0xE65100));
        p.add(kpiCard("💵 Efectivo en caja", lblEfectivo, lblEfectivoSub,
                "Lo que deberías tener en billetes/monedas. Cuéntalo y compara."));

        lblDigital = kpiValor();
        lblDigitalSub = kpiSub();
        lblDigital.setForeground(new Color(0x4527A0));
        p.add(kpiCard("📱 Digital (Yape + Plin)", lblDigital, lblDigitalSub,
                "Lo que ya entró a tus cuentas. No debe estar en el cajón."));

        lblGanancia = kpiValor();
        lblGananciaSub = kpiSub();
        lblGanancia.setForeground(UiHelper.VERDE);
        p.add(kpiCard("📈 Ganancia estimada hoy", lblGanancia, lblGananciaSub,
                "Venta menos costo de compra (usa costo unitario registrado)."));
        return p;
    }

    private JLabel kpiValor() {
        JLabel l = new JLabel("S/. 0.00", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        return l;
    }

    private JLabel kpiSub() {
        JLabel l = new JLabel("—", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.GRAY);
        return l;
    }

    private JPanel kpiCard(String titulo, JLabel valor, JLabel sub, String tip) {
        JPanel c = UiHelper.tarjeta(titulo);
        c.setToolTipText(tip);
        c.add(valor, BorderLayout.CENTER);
        c.add(sub, BorderLayout.SOUTH);
        return c;
    }

    private JPanel construirCentro() {
        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(tarjetaNrus(), BorderLayout.NORTH);
        centro.add(alertasTabs(), BorderLayout.CENTER);
        return centro;
    }

    // ---------- NRUS heroe (se mantiene destacado) ----------
    private JPanel tarjetaNrus() {
        JPanel card = UiHelper.tarjeta("🧮 Límite Tributario NRUS Cat.1 (Tope S/. 8,000 / mes) — valor principal del sistema");
        card.setLayout(new BorderLayout(8, 6));
        JPanel textos = new JPanel(new BorderLayout());
        lblNrusMonto = new JLabel("—", SwingConstants.LEFT);
        lblNrusMonto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNrusAlerta = new JLabel("—");
        lblNrusAlerta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textos.add(lblNrusMonto, BorderLayout.NORTH);
        textos.add(lblNrusAlerta, BorderLayout.CENTER);
        card.add(textos, BorderLayout.NORTH);

        barraLimiteNRUS = new JProgressBar(0, 1000);
        barraLimiteNRUS.setStringPainted(true);
        barraLimiteNRUS.setPreferredSize(new Dimension(100, 26));
        barraLimiteNRUS.setToolTipText("Porcentaje del tope mensual S/. 8,000 consumido (hora Lima). Verde <70% · Naranja 70-90% · Rojo >90%.");
        card.add(barraLimiteNRUS, BorderLayout.CENTER);

        JLabel nota = new JLabel("Si superas S/. 8,000 debes cambiar de categoría/régimen (SUNAT). Se calcula con fecha de Lima.");
        nota.setForeground(Color.GRAY);
        nota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        card.add(nota, BorderLayout.SOUTH);
        return card;
    }

    // ---------- Abajo: alertas legibles con prioridad ----------
    private JComponent alertasTabs() {
        tabsAlertas = new JTabbedPane();
        modeloStock = new DefaultTableModel(new String[]{"Prioridad", "Producto", "Stock", "Mínimo", "Sugerido pedir"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tStock = new JTable(modeloStock);
        UiHelper.estiloTabla(tStock);
        tStock.getColumnModel().getColumn(0).setCellRenderer(celdaPrioridadStock());
        tStock.getColumnModel().getColumn(2).setCellRenderer(celdaStockCritica());
        tabsAlertas.addTab("⚠ Stock bajo", new JScrollPane(tStock));

        modeloVence = new DefaultTableModel(new String[]{"Urgencia", "Producto", "Vence", "Días rest.", "Stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tVence = new JTable(modeloVence);
        UiHelper.estiloTabla(tVence);
        tVence.getColumnModel().getColumn(0).setCellRenderer(celdaPrioridadVence());
        tVence.getColumnModel().getColumn(3).setCellRenderer(celdaDias());
        tabsAlertas.addTab("📅 Por vencer", new JScrollPane(tVence));

        JPanel cont = new JPanel(new BorderLayout());
        cont.setBorder(BorderFactory.createTitledBorder("Alertas operativas — rojo = atender hoy · doble-clic = qué hacer"));
        cont.add(tabsAlertas, BorderLayout.CENTER);

        tStock.addMouseListener(detalleListener(tStock, modeloStock, true));
        tVence.addMouseListener(detalleListener(tVence, modeloVence, false));
        return cont;
    }

    private java.awt.event.MouseAdapter detalleListener(JTable t, DefaultTableModel m, boolean esStock) {
        return new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() != -1) {
                    String prod = String.valueOf(m.getValueAt(t.convertRowIndexToModel(t.getSelectedRow()), 1));
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Producto: " + prod + "\n\nAcción sugerida: ve a 📦 Inventario, busca el producto y " +
                            (esStock ? "repon stock (+Ingresar Stock, por cajas si aplica)." : "revisa fecha/lote y prioriza su venta (oferta 2x1)."),
                            "Detalle de alerta", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
    }

    private DefaultTableCellRenderer celdaPrioridadStock() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    String p = v == null ? "" : v.toString();
                    setHorizontalAlignment(CENTER); setFont(getFont().deriveFont(Font.BOLD));
                    if (p.startsWith("🔴")) { comp.setBackground(UiHelper.FONDO_ROJO_SUAVE); comp.setForeground(UiHelper.ROJO); }
                    else { comp.setBackground(UiHelper.FONDO_AMARILLO_SUAVE); comp.setForeground(UiHelper.NARANJA); }
                }
                return comp;
            }
        };
    }

    private DefaultTableCellRenderer celdaPrioridadVence() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    String p = v == null ? "" : v.toString();
                    setHorizontalAlignment(CENTER); setFont(getFont().deriveFont(Font.BOLD));
                    if (p.startsWith("🔴")) { comp.setBackground(UiHelper.FONDO_ROJO_SUAVE); comp.setForeground(UiHelper.ROJO); }
                    else { comp.setBackground(UiHelper.FONDO_AMARILLO_SUAVE); comp.setForeground(UiHelper.NARANJA); }
                }
                return comp;
            }
        };
    }

    private DefaultTableCellRenderer celdaStockCritica() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    try {
                        int stock = Integer.parseInt(t.getValueAt(r, 2).toString());
                        int min = Integer.parseInt(t.getValueAt(r, 3).toString());
                        if (stock <= 0) { comp.setBackground(UiHelper.FONDO_ROJO_SUAVE); comp.setForeground(UiHelper.ROJO); }
                        else if (stock <= min) { comp.setBackground(UiHelper.FONDO_AMARILLO_SUAVE); comp.setForeground(UiHelper.NARANJA); }
                        else { comp.setBackground(Color.WHITE); comp.setForeground(Color.BLACK); }
                        setFont(getFont().deriveFont(Font.BOLD)); setHorizontalAlignment(CENTER);
                    } catch (Exception ignored) {}
                }
                return comp;
            }
        };
    }

    private DefaultTableCellRenderer celdaDias() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    try {
                        int dias = Integer.parseInt(v.toString());
                        if (dias < 0) { comp.setBackground(UiHelper.FONDO_ROJO_SUAVE); comp.setForeground(UiHelper.ROJO); setText("VENCIDO (" + dias + ")"); }
                        else if (dias <= 2) { comp.setBackground(UiHelper.FONDO_ROJO_SUAVE); comp.setForeground(UiHelper.ROJO); }
                        else if (dias <= 5) { comp.setBackground(UiHelper.FONDO_AMARILLO_SUAVE); comp.setForeground(UiHelper.NARANJA); }
                        else { comp.setBackground(Color.WHITE); comp.setForeground(Color.BLACK); }
                        setFont(getFont().deriveFont(Font.BOLD)); setHorizontalAlignment(CENTER);
                    } catch (Exception ignored) {}
                }
                return comp;
            }
        };
    }

    private JPanel barraInferior() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel hint = new JLabel("ℹ Arqueo: Efectivo + Digital = Ventas. Si no cuadra, revisa 🧾 Historial por método. Se actualiza al entrar.");
        hint.setForeground(Color.GRAY);
        p.add(hint, BorderLayout.WEST);
        JButton btn = new JButton("↻ Actualizar ahora");
        btn.addActionListener(e -> recargar());
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    public void recargar() { cargarDatos(); }

    private void cargarDatos() {
        // --- Fila caja (hora Lima desde SQL) ---
        double hoy = ventaController.totalVentasDelDia();
        int nHoy = ventaController.conteoVentasDelDia();
        double efectivo = ventaController.totalEfectivoDia();
        double digital = ventaController.totalDigitalDia();
        double yape = ventaController.totalYapeDia();
        double plin = ventaController.totalPlinDia();
        double ganancia = ventaController.gananciaEstimadaDia();

        lblVentas.setText(UiHelper.moneda(hoy));
        lblVentasSub.setText(nHoy + (nHoy == 1 ? " venta hoy" : " ventas hoy"));
        lblEfectivo.setText(UiHelper.moneda(efectivo));
        double pctEf = hoy > 0 ? efectivo / hoy * 100 : 0;
        lblEfectivoSub.setText(hoy > 0 ? String.format("%.0f%% de hoy · cuenta billetes", pctEf) : "Sin ventas aún");
        lblDigital.setText(UiHelper.moneda(digital));
        lblDigitalSub.setText(String.format("Yape %s · Plin %s", UiHelper.moneda(yape), UiHelper.moneda(plin)));
        lblGanancia.setText(UiHelper.moneda(ganancia));
        double margen = hoy > 0 ? ganancia / hoy * 100 : 0;
        lblGananciaSub.setText(hoy > 0 ? String.format("Margen %.1f%% del día", margen) : "—");
        lblGanancia.setForeground(ganancia < 0 ? UiHelper.ROJO : UiHelper.VERDE);

        // --- NRUS heroe ---
        double totalMes = ventaController.totalVentasDelMes();
        double limite = AppConfig.LIMITE_NRUS_CATEGORIA1;
        double porc = limite <= 0 ? 0 : totalMes / limite * 100;
        double disponible = limite - totalMes;
        lblNrusMonto.setText(String.format("Consumido: S/. %.2f de S/. %.2f  (%.1f%%)  ·  Disponible: S/. %.2f",
                totalMes, limite, porc, disponible));
        barraLimiteNRUS.setValue((int) Math.min(Math.round(porc * 10), 1000));
        barraLimiteNRUS.setString(String.format("%.1f%% del tope usado", porc));
        if (porc < 70) {
            barraLimiteNRUS.setForeground(UiHelper.VERDE);
            lblNrusAlerta.setText("✓ Nivel seguro. Puedes seguir vendiendo con normalidad.");
            lblNrusAlerta.setForeground(UiHelper.VERDE);
        } else if (porc < 90) {
            barraLimiteNRUS.setForeground(UiHelper.NARANJA);
            lblNrusAlerta.setText("⚠ Más del 70%. Modera promociones y planifica el siguiente mes.");
            lblNrusAlerta.setForeground(UiHelper.NARANJA);
        } else if (porc < 100) {
            barraLimiteNRUS.setForeground(UiHelper.ROJO);
            lblNrusAlerta.setText("🔴 ¡Alerta! Más del 90% del tope. Riesgo de exceder Categoría 1.");
            lblNrusAlerta.setForeground(UiHelper.ROJO);
        } else {
            barraLimiteNRUS.setForeground(UiHelper.ROJO);
            lblNrusAlerta.setText("⛔ TOPE SUPERADO. Excediste S/. 8,000: regulariza tu categoría ante SUNAT.");
            lblNrusAlerta.setForeground(UiHelper.ROJO);
        }

        // --- Alertas con prioridad (agotados y vencidos primero) ---
        var bajo = productoController.alertasStockBajo();
        bajo.sort((a, b) -> Integer.compare(a.getStockUnidades(), b.getStockUnidades()));
        modeloStock.setRowCount(0);
        for (Producto p : bajo) {
            String pri = p.getStockUnidades() <= 0 ? "🔴 HOY" : "🟡 Pronto";
            int sugerido = Math.max(0, p.getStockMinimo() - p.getStockUnidades() + 5);
            modeloStock.addRow(new Object[]{pri, p.getNombre(), p.getStockUnidades(), p.getStockMinimo(), sugerido});
        }
        if (bajo.isEmpty()) modeloStock.addRow(new Object[]{"🟢", "Sin alertas — stock OK", "", "", ""});
        tabsAlertas.setTitleAt(0, "⚠ Stock bajo (" + bajo.size() + ")");

        var vence = productoController.alertasPorVencer();
        vence.sort((a, b) -> {
            long da = a.getFechaVencimiento() != null ? ChronoUnit.DAYS.between(LocalDate.now(), a.getFechaVencimiento()) : 999;
            long db = b.getFechaVencimiento() != null ? ChronoUnit.DAYS.between(LocalDate.now(), b.getFechaVencimiento()) : 999;
            return Long.compare(da, db);
        });
        modeloVence.setRowCount(0);
        for (Producto p : vence) {
            long dias = p.getFechaVencimiento() != null ? ChronoUnit.DAYS.between(LocalDate.now(), p.getFechaVencimiento()) : 999;
            String urg = dias < 0 ? "🔴 Vencido" : dias <= 2 ? "🔴 Hoy" : "🟡 Pronto";
            modeloVence.addRow(new Object[]{urg, p.getNombre(),
                    p.getFechaVencimiento() != null ? p.getFechaVencimiento().toString() : "—", (int) dias, p.getStockUnidades()});
        }
        if (vence.isEmpty()) modeloVence.addRow(new Object[]{"🟢", "Sin vencimientos próximos", "", "", ""});
        tabsAlertas.setTitleAt(1, "📅 Por vencer (" + vence.size() + ")");
    }
}
