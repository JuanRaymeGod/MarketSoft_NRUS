package marketsoft.view;

import com.toedter.calendar.JDateChooser;
import marketsoft.controller.VentaController;
import marketsoft.model.Venta;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Historial legible con filtros (cliente/DNI, método, fecha específica y rango de horas),
 * colores por método, totalizador, y el ID oculto de la vista (sigue existiendo en el modelo).
 */
public class HistorialVentasPanel extends JPanel {

    private final VentaController ventaController = new VentaController();
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
    private final boolean puedeEliminar;

    private DefaultTableModel modeloVentas;
    private JTable tablaVentas;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtFiltro, txtHoraDesde, txtHoraHasta;
    private JComboBox<String> cbMetodo;
    private JDateChooser dateFiltro;
    private JLabel lblTotalizador;

    private List<Venta> todasLasVentas = new ArrayList<>();

    public HistorialVentasPanel(boolean puedeEliminar) {
        this.puedeEliminar = puedeEliminar;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        cargarVentas();
    }

    private void initComponents() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros"));

        txtFiltro = new JTextField(18);
        txtFiltro.putClientProperty("JTextField.placeholderText", "🔍 Cliente o DNI…");
        txtFiltro.setToolTipText("Busca solo por nombre de cliente o DNI (no por ID)");
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtFiltro);

        cbMetodo = new JComboBox<>(new String[]{"Todos", "Efectivo", "Yape", "Plin"});
        cbMetodo.addActionListener(e -> aplicarFiltro());
        filtros.add(new JLabel("Método:"));
        filtros.add(cbMetodo);

        dateFiltro = new JDateChooser();
        dateFiltro.setDateFormatString("yyyy-MM-dd");
        dateFiltro.setPreferredSize(new Dimension(120, dateFiltro.getPreferredSize().height));
        dateFiltro.setToolTipText("Filtra ventas de un día específico");
        dateFiltro.getDateEditor().addPropertyChangeListener(e -> { if ("date".equals(e.getPropertyName())) aplicarFiltro(); });
        filtros.add(new JLabel("Fecha:"));
        filtros.add(dateFiltro);
        JButton btnQuitarFecha = new JButton("✕");
        btnQuitarFecha.setToolTipText("Quitar filtro de fecha");
        btnQuitarFecha.addActionListener(e -> { dateFiltro.setDate(null); aplicarFiltro(); });
        filtros.add(btnQuitarFecha);

        txtHoraDesde = new JTextField(5);
        txtHoraDesde.putClientProperty("JTextField.placeholderText", "08:00");
        txtHoraDesde.setToolTipText("Hora desde (formato HH:mm, 24h)");
        txtHoraHasta = new JTextField(5);
        txtHoraHasta.putClientProperty("JTextField.placeholderText", "20:00");
        txtHoraHasta.setToolTipText("Hora hasta (formato HH:mm, 24h)");
        javax.swing.event.DocumentListener dlHora = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        };
        txtHoraDesde.getDocument().addDocumentListener(dlHora);
        txtHoraHasta.getDocument().addDocumentListener(dlHora);
        filtros.add(new JLabel("Hora desde:"));
        filtros.add(txtHoraDesde);
        filtros.add(new JLabel("hasta:"));
        filtros.add(txtHoraHasta);

        JButton btnLimpiarFiltros = new JButton("Limpiar filtros");
        btnLimpiarFiltros.addActionListener(e -> {
            txtFiltro.setText(""); cbMetodo.setSelectedIndex(0);
            dateFiltro.setDate(null); txtHoraDesde.setText(""); txtHoraHasta.setText("");
            aplicarFiltro();
        });
        filtros.add(btnLimpiarFiltros);

        add(filtros, BorderLayout.NORTH);

        modeloVentas = new DefaultTableModel(
                new String[]{"ID", "Fecha", "Cliente", "DNI", "Método", "Monto Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };
        tablaVentas = new JTable(modeloVentas);
        UiHelper.estiloTabla(tablaVentas);
        sorter = new TableRowSorter<>(modeloVentas);
        tablaVentas.setRowSorter(sorter);
        tablaVentas.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    String m = v == null ? "" : v.toString();
                    setHorizontalAlignment(CENTER); setFont(getFont().deriveFont(Font.BOLD));
                    if (m.equals("Yape")) { comp.setBackground(new Color(0xE8EAF6)); comp.setForeground(new Color(0x4527A0)); }
                    else if (m.equals("Plin")) { comp.setBackground(new Color(0xE0F2F1)); comp.setForeground(new Color(0x00695C)); }
                    else { comp.setBackground(new Color(0xFFF3E0)); comp.setForeground(new Color(0xE65100)); }
                }
                return comp;
            }
        });
        // Oculta el ID solo de la vista; sigue en el modelo (lo usan cargarSeleccionEnFormulario/eliminar).
        tablaVentas.removeColumn(tablaVentas.getColumnModel().getColumn(0));
        add(new JScrollPane(tablaVentas), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        lblTotalizador = new JLabel("—");
        lblTotalizador.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sur.add(lblTotalizador, BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnActualizar = new JButton("↻ Actualizar");
        btnActualizar.addActionListener(e -> cargarVentas());
        botones.add(btnActualizar);
        if (puedeEliminar) {
            JButton btnEliminar = new JButton("🗑 Eliminar seleccionada");
            btnEliminar.setToolTipText("Solo Administrador. Devuelve stock y descuenta del total.");
            btnEliminar.addActionListener(e -> eliminarVentaSeleccionada());
            botones.add(btnEliminar);
        } else {
            JLabel aviso = new JLabel("Solo lectura (rol Cajero)");
            aviso.setForeground(Color.GRAY);
            botones.add(aviso);
        }
        sur.add(botones, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);
    }

    public void recargar() { cargarVentas(); }

    private void cargarVentas() {
        todasLasVentas = ventaController.listarVentas();
        aplicarFiltro();
    }

    // Filtra en memoria sobre la lista completa de ventas (no con RowFilter de texto por columna),
    // porque la fecha y el rango de horas necesitan comparar LocalDate/LocalTime reales, no texto.
    private void aplicarFiltro() {
        String texto = txtFiltro.getText().trim().toLowerCase();
        String metodo = (String) cbMetodo.getSelectedItem();
        LocalDate fechaFiltro = dateFiltro.getDate() != null
                ? dateFiltro.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        LocalTime horaDesde = parseHora(txtHoraDesde.getText(), LocalTime.MIN);
        LocalTime horaHasta = parseHora(txtHoraHasta.getText(), LocalTime.MAX);

        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : todasLasVentas) {
            if (!texto.isEmpty()) {
                String nombre = v.getNombreCliente() != null ? v.getNombreCliente().toLowerCase() : "";
                String dni = v.getDniCliente() != null ? v.getDniCliente().toLowerCase() : "";
                if (!nombre.contains(texto) && !dni.contains(texto)) continue;
            }
            if (!"Todos".equals(metodo) && !metodo.equals(v.getMetodoPago())) continue;
            if (fechaFiltro != null) {
                if (v.getFechaHora() == null || !v.getFechaHora().toLocalDate().equals(fechaFiltro)) continue;
            }
            if (v.getFechaHora() != null) {
                LocalTime hora = v.getFechaHora().toLocalTime();
                if (hora.isBefore(horaDesde) || hora.isAfter(horaHasta)) continue;
            }
            filtradas.add(v);
        }
        renderTabla(filtradas);
    }

    private LocalTime parseHora(String texto, LocalTime porDefecto) {
        texto = texto.trim();
        if (texto.isEmpty()) return porDefecto;
        try {
            return LocalTime.parse(texto, formatoHora);
        } catch (Exception e) {
            return porDefecto; // mientras el usuario sigue escribiendo, no bloquea con error
        }
    }

    private void renderTabla(List<Venta> lista) {
        modeloVentas.setRowCount(0);
        double total = 0;
        for (Venta v : lista) {
            modeloVentas.addRow(new Object[]{
                    v.getIdVenta(),
                    v.getFechaHora() != null ? v.getFechaHora().format(formatoFecha) : "",
                    v.getNombreCliente() != null ? v.getNombreCliente() : "—",
                    v.getDniCliente() != null ? v.getDniCliente() : "—",
                    v.getMetodoPago(),
                    String.format("S/. %.2f", v.getTotal())
            });
            total += v.getTotal();
        }
        if (todasLasVentas.isEmpty()) {
            lblTotalizador.setText("Sin ventas registradas. Ve a ⚡ Venta Express para la primera venta.");
        } else if (lista.isEmpty()) {
            lblTotalizador.setText("Sin resultados para estos filtros.");
        } else {
            lblTotalizador.setText(String.format("Mostrando %d venta(s) · Total: S/. %.2f", lista.size(), total));
        }
    }

    private void eliminarVentaSeleccionada() {
        int vista = tablaVentas.getSelectedRow();
        if (vista == -1) { JOptionPane.showMessageDialog(this, "Selecciona una venta.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        int idVenta = (int) modeloVentas.getValueAt(tablaVentas.convertRowIndexToModel(vista), 0);
        int c = JOptionPane.showConfirmDialog(this,
                "¿Eliminar venta #" + idVenta + "? Se devolverá el stock y se descontará del total/NRUS.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            if (ventaController.eliminarVenta(idVenta)) {
                JOptionPane.showMessageDialog(this, "✓ Venta eliminada y stock restaurado.");
                cargarVentas();
            } else JOptionPane.showMessageDialog(this, "No se pudo eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}