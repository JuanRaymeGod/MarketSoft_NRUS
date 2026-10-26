package marketsoft.view;

import marketsoft.controller.ProductoController;
import marketsoft.controller.VentaController;
import marketsoft.model.DetalleVenta;
import marketsoft.model.Producto;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * OPCIONES 5-8 + 12-13:
 * 5) Layout 3 zonas con JSplitPane, 6) Total gigante + vuelto en vivo,
 * 7) Toggle pago + montos rapidos, 8) Carrito profesional con spinner y atajos.
 */
public class VentaExpressPanel extends JPanel {

    private final ProductoController productoController = new ProductoController();
    private final VentaController ventaController = new VentaController();

    private DefaultTableModel modeloCatalogo, modeloCarrito;
    private JTable tablaCatalogo, tablaCarrito;
    private TableRowSorter<DefaultTableModel> sorterCat;
    private final List<DetalleVenta> carrito = new ArrayList<>();

    private JTextField txtBuscar, txtMontoRecibido, txtDniCliente, txtNombreCliente;
    private JSpinner spnCantidad;
    private JLabel lblTotal, lblVuelto, lblEstadoVuelto;
    private JToggleButton tbEfectivo, tbYape, tbPlin;

    public VentaExpressPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, construirPanelCatalogo(), construirPanelDerecho());
        split.setResizeWeight(0.45);
        split.setDividerLocation(430);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);
        cargarCatalogo();
        configurarAtajos();
        txtBuscar.requestFocusInWindow();
    }

    // ---------- 5) ZONA 1: CATALOGO ----------
    private JPanel construirPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("① Catálogo (Enter = buscar · Doble-clic = agregar)"));

        JPanel busqueda = new JPanel(new BorderLayout(6, 0));
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "🔍 Nombre o código de barras… (solo esos 2)");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarCatalogo(); }
            public void removeUpdate(DocumentEvent e) { filtrarCatalogo(); }
            public void changedUpdate(DocumentEvent e) { filtrarCatalogo(); }
        });
        txtBuscar.addActionListener(e -> { if (tablaCatalogo.getRowCount() > 0) { tablaCatalogo.setRowSelectionInterval(0, 0); agregarSeleccionado(); } });
        busqueda.add(txtBuscar, BorderLayout.CENTER);
        panel.add(busqueda, BorderLayout.NORTH);

        modeloCatalogo = new DefaultTableModel(new String[]{"ID", "Producto", "Código", "Precio", "Stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 4) return Integer.class;
                if (c == 3) return Double.class;
                return String.class;
            }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
        UiHelper.estiloTabla(tablaCatalogo);
        sorterCat = new TableRowSorter<>(modeloCatalogo);
        tablaCatalogo.setRowSorter(sorterCat);
        tablaCatalogo.getColumnModel().getColumn(3).setCellRenderer(UiHelper.rendererMoneda());
        tablaCatalogo.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s && v instanceof Number n) {
                    int stock = n.intValue();
                    if (stock <= 0) { comp.setBackground(UiHelper.FONDO_ROJO_SUAVE); comp.setForeground(UiHelper.ROJO); }
                    else if (stock <= 5) { comp.setBackground(UiHelper.FONDO_AMARILLO_SUAVE); comp.setForeground(UiHelper.NARANJA); }
                    else { comp.setBackground(Color.WHITE); comp.setForeground(Color.BLACK); }
                    setHorizontalAlignment(CENTER); setFont(getFont().deriveFont(Font.BOLD));
                }
                return comp;
            }
        });
        tablaCatalogo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) agregarSeleccionado(); }
        });
        tablaCatalogo.removeColumn(tablaCatalogo.getColumnModel().getColumn(0)); // oculta ID
        panel.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(6, 0));
        abajo.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        JPanel cantPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        cantPanel.add(new JLabel("Cant.:"));
        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        ((JSpinner.DefaultEditor) spnCantidad.getEditor()).getTextField().setColumns(4);
        cantPanel.add(spnCantidad);
        abajo.add(cantPanel, BorderLayout.WEST);
        JButton btnAgregar = new JButton("＋ Agregar (Enter)");
        btnAgregar.setBackground(new Color(0x1565C0)); btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAgregar.addActionListener(e -> agregarSeleccionado());
        abajo.add(btnAgregar, BorderLayout.CENTER);
        panel.add(abajo, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPanelDerecho() {
        JPanel der = new JPanel(new BorderLayout(8, 8));
        der.add(construirPanelCarrito(), BorderLayout.CENTER);
        der.add(construirPanelCobro(), BorderLayout.SOUTH);
        return der;
    }

    // ---------- 8) ZONA 2: CARRITO ----------
    private JPanel construirPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("② Carrito (Supr = quitar · F2 = cobrar)"));

        modeloCarrito = new DefaultTableModel(new String[]{"Producto", "P. Unit", "Cant.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        UiHelper.estiloTabla(tablaCarrito);
        tablaCarrito.getColumnModel().getColumn(1).setCellRenderer(UiHelper.rendererMoneda());
        tablaCarrito.getColumnModel().getColumn(3).setCellRenderer(UiHelper.rendererMoneda());
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton btnMas = new JButton("+");
        btnMas.setToolTipText("Aumentar cantidad (+1, respeta stock)");
        btnMas.addActionListener(e -> cambiarCantidad(1));
        JButton btnMenos = new JButton("−");
        btnMenos.setToolTipText("Disminuir cantidad (−1)");
        btnMenos.addActionListener(e -> cambiarCantidad(-1));
        JButton btnQuitar = new JButton("Quitar");
        btnQuitar.setToolTipText("Quitar línea seleccionada (Supr)");
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        JButton btnVaciar = new JButton("Vaciar");
        btnVaciar.setToolTipText("Vacía todo el carrito (pide confirmación)");
        btnVaciar.addActionListener(e -> vaciarCarrito());
        acciones.add(new JLabel("Editar:"));
        acciones.add(btnMenos); acciones.add(btnMas); acciones.add(btnQuitar); acciones.add(btnVaciar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- 6+7) ZONA 3: COBRO ----------
    private JPanel construirPanelCobro() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("③ Cobro"));

        // Total gigante
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(new Color(0xE3F2FD));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JLabel lblTit = new JLabel("TOTAL A PAGAR");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal = new JLabel("S/. 0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTotal.setForeground(new Color(0x0D47A1));
        totalPanel.add(lblTit, BorderLayout.WEST);
        totalPanel.add(lblTotal, BorderLayout.CENTER);
        panel.add(totalPanel, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4); g.fill = GridBagConstraints.HORIZONTAL;

        // Metodo pago con toggles (1 clic)
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        centro.add(new JLabel("Método de pago (1 clic):"), g);
        g.gridy = 1;
        JPanel pagoPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        ButtonGroup bg = new ButtonGroup();
        tbEfectivo = new JToggleButton("💵 Efectivo", true);
        tbYape = new JToggleButton("📱 Yape");
        tbPlin = new JToggleButton("📲 Plin");
        for (JToggleButton b : new JToggleButton[]{tbEfectivo, tbYape, tbPlin}) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            bg.add(b); pagoPanel.add(b);
            b.addActionListener(e -> alCambiarMetodoPago());
        }
        centro.add(pagoPanel, g);

        // Montos rapidos
        g.gridy = 2;
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chips.add(new JLabel("Rápido:"));
        for (String s : new String[]{"Exacto", "10", "20", "50", "100"}) {
            JButton b = new JButton(s);
            b.setMargin(new Insets(2, 8, 2, 8));
            b.addActionListener(e -> aplicarMontoRapido(s));
            chips.add(b);
        }
        centro.add(chips, g);

        g.gridwidth = 1;
        g.gridy = 3; g.gridx = 0; centro.add(new JLabel("Monto recibido S/:"), g);
        g.gridx = 1;
        txtMontoRecibido = new JTextField();
        txtMontoRecibido.putClientProperty("JTextField.placeholderText", "Ej. 50.00");
        UiHelper.soloDecimales(txtMontoRecibido);
        txtMontoRecibido.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcularVuelto(); }
            public void removeUpdate(DocumentEvent e) { recalcularVuelto(); }
            public void changedUpdate(DocumentEvent e) { recalcularVuelto(); }
        });
        centro.add(txtMontoRecibido, g);

        g.gridx = 0; g.gridy = 4; centro.add(new JLabel("Vuelto:"), g);
        g.gridx = 1;
        JPanel vueltoPanel = new JPanel(new BorderLayout());
        lblVuelto = new JLabel("S/. 0.00");
        lblVuelto.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblEstadoVuelto = new JLabel("—");
        lblEstadoVuelto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        vueltoPanel.add(lblVuelto, BorderLayout.CENTER);
        vueltoPanel.add(lblEstadoVuelto, BorderLayout.EAST);
        centro.add(vueltoPanel, g);

        g.gridx = 0; g.gridy = 5; centro.add(new JLabel("DNI (8 díg., opc.):"), g);
        g.gridx = 1;
        txtDniCliente = new JTextField();
        UiHelper.soloDni(txtDniCliente);
        txtDniCliente.putClientProperty("JTextField.placeholderText", "Opcional · 8 dígitos");
        centro.add(txtDniCliente, g);

        g.gridx = 0; g.gridy = 6; centro.add(new JLabel("Cliente (opc.):"), g);
        g.gridx = 1;
        txtNombreCliente = new JTextField();
        txtNombreCliente.putClientProperty("JTextField.placeholderText", "Opcional · Ej. Juan Pérez");
        centro.add(txtNombreCliente, g);

        panel.add(centro, BorderLayout.CENTER);

        JButton btnProcesar = new JButton("✔ PROCESAR Y CERRAR VENTA  (F2)");
        btnProcesar.setBackground(new Color(0x2E7D32)); btnProcesar.setForeground(Color.WHITE);
        btnProcesar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnProcesar.addActionListener(e -> procesarVenta());
        panel.add(btnProcesar, BorderLayout.SOUTH);
        return panel;
    }

    private void configurarAtajos() {
        InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke("F2"), "cobrar");
        am.put("cobrar", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { procesarVenta(); } });
        im.put(KeyStroke.getKeyStroke("DELETE"), "quitar");
        am.put("quitar", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { quitarDelCarrito(); } });
    }

    // ---------- Logica ----------
    private void filtrarCatalogo() {
        String t = txtBuscar.getText().trim();
        if (t.isEmpty()) { sorterCat.setRowFilter(null); return; }
        String q = "(?i)" + java.util.regex.Pattern.quote(t);
        sorterCat.setRowFilter(RowFilter.orFilter(java.util.List.of(
                RowFilter.regexFilter(q, 1), RowFilter.regexFilter(q, 2))));
    }

    private void cargarCatalogo() {
        String f = txtBuscar != null ? txtBuscar.getText().trim() : "";
        modeloCatalogo.setRowCount(0);
        for (Producto p : productoController.listarProductos())
            modeloCatalogo.addRow(new Object[]{p.getIdProducto(), p.getNombre(),
                    p.getCodigoBarras() != null ? p.getCodigoBarras() : "—",
                    p.getPrecioVentaUnitario(), p.getStockUnidades()});
        if (!f.isBlank()) filtrarCatalogo();
    }

    /** Punto 2 admin: refresca catalogo al volver a la pestana (nuevo producto / nuevo stock). */
    public void recargar() {
        // Revalida carrito contra stock actual (si otro usuario vendio, evita vender de mas)
        for (int i = carrito.size() - 1; i >= 0; i--) {
            var d = carrito.get(i);
            Producto p = productoController.obtenerPorId(d.getIdProducto());
            if (p == null || p.getStockUnidades() <= 0) carrito.remove(i);
            else if (d.getCantidad() > p.getStockUnidades()) d.setCantidad(p.getStockUnidades());
        }
        cargarCatalogo();
        refrescarCarrito();
    }

    private void agregarSeleccionado() {
        int vista = tablaCatalogo.getSelectedRow();
        if (vista == -1) { JOptionPane.showMessageDialog(this, "Selecciona un producto (o escribe y pulsa Enter).", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        int id = (int) modeloCatalogo.getValueAt(tablaCatalogo.convertRowIndexToModel(vista), 0);
        agregarAlCarrito(id, (int) spnCantidad.getValue());
        txtBuscar.requestFocusInWindow();
    }

    private void agregarAlCarrito(int idProducto, int cantidad) {
        Producto p = productoController.obtenerPorId(idProducto);
        if (p == null) return;
        if (cantidad <= 0) { JOptionPane.showMessageDialog(this, "Cantidad mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        int ya = carrito.stream().filter(d -> d.getIdProducto() == idProducto).mapToInt(DetalleVenta::getCantidad).sum();
        if (ya + cantidad > p.getStockUnidades()) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuficiente de \"" + p.getNombre() + "\".\nDisponible: " + p.getStockUnidades() + " · En carrito: " + ya,
                    "Sin stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DetalleVenta ex = carrito.stream().filter(d -> d.getIdProducto() == idProducto).findFirst().orElse(null);
        if (ex != null) ex.setCantidad(ex.getCantidad() + cantidad);
        else carrito.add(new DetalleVenta(p.getIdProducto(), p.getNombre(), cantidad,
                p.getPrecioVentaUnitario(), p.getPrecioCompraUnitario())); // costo congelado al momento de la venta
        refrescarCarrito();
    }

    private void cambiarCantidad(int delta) {
        int fila = tablaCarrito.getSelectedRow();
        if (fila == -1) { JOptionPane.showMessageDialog(this, "Selecciona una línea del carrito.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        DetalleVenta d = carrito.get(fila);
        int nuevo = d.getCantidad() + delta;
        if (nuevo <= 0) { carrito.remove(fila); refrescarCarrito(); return; }
        Producto p = productoController.obtenerPorId(d.getIdProducto());
        int otros = carrito.stream().filter(x -> x.getIdProducto() == d.getIdProducto()).mapToInt(DetalleVenta::getCantidad).sum() - d.getCantidad();
        if (p != null && otros + nuevo > p.getStockUnidades()) {
            JOptionPane.showMessageDialog(this, "No hay más stock de \"" + d.getNombreProducto() + "\" (" + p.getStockUnidades() + ").", "Sin stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        d.setCantidad(nuevo);
        refrescarCarrito();
        tablaCarrito.setRowSelectionInterval(fila, fila);
    }

    private void quitarDelCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila == -1) { JOptionPane.showMessageDialog(this, "Selecciona un producto del carrito.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        carrito.remove(fila);
        refrescarCarrito();
    }

    private void vaciarCarrito() {
        if (carrito.isEmpty()) return;
        int r = JOptionPane.showConfirmDialog(this, "¿Vaciar todo el carrito (" + carrito.size() + " líneas)?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) { carrito.clear(); refrescarCarrito(); }
    }

    private String metodoPago() { return tbYape.isSelected() ? "Yape" : tbPlin.isSelected() ? "Plin" : "Efectivo"; }

    private void alCambiarMetodoPago() {
        boolean digital = !tbEfectivo.isSelected();
        if (digital) { // Yape/Plin: monto exacto, sin vuelto
            txtMontoRecibido.setText(String.format("%.2f", totalCarrito()));
            txtMontoRecibido.setEnabled(false);
        } else txtMontoRecibido.setEnabled(true);
        recalcularVuelto();
    }

    private double totalCarrito() { return carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum(); }

    private void aplicarMontoRapido(String s) {
        double total = totalCarrito();
        if (s.equals("Exacto")) txtMontoRecibido.setText(String.format("%.2f", total));
        else txtMontoRecibido.setText(s + ".00");
        if (!tbEfectivo.isSelected()) { tbEfectivo.setSelected(true); txtMontoRecibido.setEnabled(true); }
        recalcularVuelto();
    }

    private void refrescarCarrito() {
        modeloCarrito.setRowCount(0);
        double total = 0;
        for (DetalleVenta d : carrito) {
            modeloCarrito.addRow(new Object[]{d.getNombreProducto(), d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal()});
            total += d.getSubtotal();
        }
        lblTotal.setText(String.format("S/. %.2f", total));
        if (!tbEfectivo.isSelected()) txtMontoRecibido.setText(String.format("%.2f", total));
        recalcularVuelto();
    }

    private void recalcularVuelto() {
        double total = totalCarrito();
        double recibido = UiHelper.parseDouble(txtMontoRecibido.getText(), Double.NaN);
        if (Double.isNaN(recibido) || txtMontoRecibido.getText().trim().isEmpty()) {
            lblVuelto.setText("S/. 0.00"); lblVuelto.setForeground(Color.GRAY);
            lblEstadoVuelto.setText(carrito.isEmpty() ? "—" : "Ingresa monto…"); lblEstadoVuelto.setForeground(Color.GRAY);
            return;
        }
        double vuelto = recibido - total;
        lblVuelto.setText(String.format("S/. %.2f", vuelto));
        if (vuelto < 0) {
            lblVuelto.setForeground(UiHelper.ROJO);
            lblEstadoVuelto.setText(String.format("Faltan S/. %.2f", -vuelto));
            lblEstadoVuelto.setForeground(UiHelper.ROJO);
        } else {
            lblVuelto.setForeground(UiHelper.VERDE);
            lblEstadoVuelto.setText(vuelto == 0 ? "✓ Monto exacto" : "✓ Dar vuelto");
            lblEstadoVuelto.setForeground(UiHelper.VERDE);
        }
    }

    private void procesarVenta() {
        if (carrito.isEmpty()) { JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String dni = txtDniCliente.getText().trim();
        if (!UiHelper.dniValido(dni)) { JOptionPane.showMessageDialog(this, "DNI inválido: debe tener 8 dígitos (o déjalo vacío).", "Validación", JOptionPane.WARNING_MESSAGE); txtDniCliente.requestFocus(); return; }
        double total = totalCarrito();
        double recibido = UiHelper.parseDouble(txtMontoRecibido.getText(), Double.NaN);
        if (Double.isNaN(recibido)) { JOptionPane.showMessageDialog(this, "Ingresa un monto recibido válido.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        if (recibido < total) { JOptionPane.showMessageDialog(this, String.format("Monto insuficiente.\nTotal: S/. %.2f · Recibido: S/. %.2f", total, recibido), "Error", JOptionPane.ERROR_MESSAGE); return; }
        try {
            boolean ok = ventaController.procesarVenta(carrito, recibido, metodoPago(), dni, txtNombreCliente.getText().trim());
            if (ok) {
                double vuelto = recibido - total;
                JOptionPane.showMessageDialog(this,
                        String.format("✓ Venta registrada.\nTotal: S/. %.2f · Vuelto: S/. %.2f · %s", total, vuelto, metodoPago()));
                carrito.clear(); refrescarCarrito();
                txtMontoRecibido.setText(""); txtDniCliente.setText(""); txtNombreCliente.setText("");
                tbEfectivo.setSelected(true); txtMontoRecibido.setEnabled(true);
                spnCantidad.setValue(1);
                cargarCatalogo(); filtrarCatalogo();
                txtBuscar.setText(""); txtBuscar.requestFocusInWindow();
            } else JOptionPane.showMessageDialog(this, "El monto recibido es menor al total.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar: " + ex.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}