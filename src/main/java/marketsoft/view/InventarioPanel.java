package marketsoft.view;

import com.toedter.calendar.JDateChooser;
import marketsoft.controller.CategoriaController;
import marketsoft.model.ResultadoEliminacion;
import marketsoft.controller.MermaController;
import marketsoft.controller.ProductoController;
import marketsoft.model.Categoria;
import marketsoft.model.Producto;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * OPCIONES 1-4 + 12-13:
 * 1) Formulario por secciones, 2) Modo Unidad/Caja con calculo en vivo,
 * 3) Validacion guiada + JDateChooser, 4) Tabla enriquecida con semaforo.
 */
public class InventarioPanel extends JPanel {

    private final ProductoController productoController = new ProductoController();
    private final CategoriaController categoriaController = new CategoriaController();
    private final MermaController mermaController = new MermaController();

    // Seccion 1
    private JTextField txtNombre, txtCodigoBarras, txtBuscar;
    private JComboBox<Categoria> cbCategoria;
    // Seccion 2 (modo dual)
    private JRadioButton rbUnidad, rbCaja;
    private JTextField txtPrecioCompraUnidad, txtUnidadesPorCaja, txtPrecioCompraCaja, txtPrecioVenta;
    private JLabel lblCostoCalc, lblMargen;
    // Seccion 3
    private JTextField txtStockActual, txtStockMinimo;
    private JLabel lblStockTitulo, lblEquivStock;
    private JDateChooser dateVencimiento;
    private JCheckBox chkSinVencimiento;

    private DefaultTableModel modeloProductos;
    private JTable tablaProductos;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnIngresarStock, btnRetirarStock, btnActualizarPrecio, btnRetirarProducto;
    private int idProductoSeleccionado = 0;

    public InventarioPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(construirFormulario(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        cargarCategorias();
        cargarProductos();
        actualizarModoCompra();
    }

    // ---------- 1) FORMULARIO POR SECCIONES ----------
    private JPanel construirFormulario() {
        JPanel contenedor = new JPanel(new BorderLayout(5, 8));
        contenedor.setBorder(BorderFactory.createTitledBorder("Registro / Edición de Producto"));

        JPanel secciones = new JPanel(new GridLayout(1, 3, 10, 0));
        secciones.add(seccionDatosBasicos());
        secciones.add(seccionCostos());
        secciones.add(seccionStock());
        contenedor.add(secciones, BorderLayout.CENTER);
        contenedor.add(panelBotones(), BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel seccionDatosBasicos() {
        JPanel p = UiHelper.tarjeta("1 · Datos básicos");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.fill = GridBagConstraints.HORIZONTAL; g.gridx = 0; g.weightx = 1;

        g.gridy = 0; p.add(new JLabel("Nombre *:"), g);
        g.gridy = 1; txtNombre = new JTextField(16);
        txtNombre.putClientProperty("JTextField.placeholderText", "Ej. Leche Gloria 400g");
        txtNombre.setToolTipText("Nombre visible en catálogo y ticket");
        p.add(txtNombre, g);

        g.gridy = 2; p.add(new JLabel("Categoría:"), g);
        g.gridy = 3;
        JPanel catPanel = new JPanel(new BorderLayout(4, 0));
        cbCategoria = new JComboBox<>();
        JButton btnNuevaCat = new JButton("+");
        btnNuevaCat.setToolTipText("Crear categoría rápida (Ej. Limpieza, Mascotas)");
        btnNuevaCat.addActionListener(e -> crearCategoriaRapida());
        JButton btnGestionar = new JButton("⚙");
        btnGestionar.setToolTipText("Gestionar categorías: agregar / eliminar (12 base de bodega)");
        btnGestionar.addActionListener(e -> gestionarCategorias());
        JPanel btns = new JPanel(new GridLayout(1, 2, 2, 0));
        btns.add(btnNuevaCat); btns.add(btnGestionar);
        catPanel.add(cbCategoria, BorderLayout.CENTER);
        catPanel.add(btns, BorderLayout.EAST);
        p.add(catPanel, g);

        g.gridy = 4; p.add(new JLabel("Código de barras:"), g);
        g.gridy = 5; txtCodigoBarras = new JTextField(16);
        txtCodigoBarras.putClientProperty("JTextField.placeholderText", "Escanea o escribe (opcional)");
        p.add(txtCodigoBarras, g);
        return p;
    }

    private JPanel seccionCostos() {
        JPanel p = UiHelper.tarjeta("2 · Costos y precios (Unidad vs Caja)");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4); g.fill = GridBagConstraints.HORIZONTAL; g.gridx = 0; g.weightx = 1;

        // 2) Modo dual
        ButtonGroup grupo = new ButtonGroup();
        rbUnidad = new JRadioButton("Compra por UNIDAD", true);
        rbCaja = new JRadioButton("Compra por CAJA / PAQUETE");
        rbUnidad.setToolTipText("Ej. compras suelta al proveedor");
        rbCaja.setToolTipText("Ej. caja de 12 unidades a S/. 84");
        grupo.add(rbUnidad); grupo.add(rbCaja);
        rbUnidad.addActionListener(e -> actualizarModoCompra());
        rbCaja.addActionListener(e -> actualizarModoCompra());
        g.gridy = 0; p.add(rbUnidad, g);
        g.gridy = 1; p.add(rbCaja, g);

        g.gridy = 2; p.add(new JLabel("Costo compra x UNIDAD S/:"), g);
        g.gridy = 3; txtPrecioCompraUnidad = new JTextField("0.00");
        UiHelper.soloDecimales(txtPrecioCompraUnidad);
        p.add(txtPrecioCompraUnidad, g);

        g.gridy = 4; p.add(new JLabel("Unidades x caja + Precio caja S/:"), g);
        g.gridy = 5;
        JPanel cajaFila = new JPanel(new GridLayout(1, 2, 6, 0));
        txtUnidadesPorCaja = new JTextField();
        txtUnidadesPorCaja.putClientProperty("JTextField.placeholderText", "Ej. 12");
        UiHelper.soloEnteros(txtUnidadesPorCaja);
        txtPrecioCompraCaja = new JTextField();
        txtPrecioCompraCaja.putClientProperty("JTextField.placeholderText", "Ej. 84.00");
        UiHelper.soloDecimales(txtPrecioCompraCaja);
        cajaFila.add(txtUnidadesPorCaja); cajaFila.add(txtPrecioCompraCaja);
        p.add(cajaFila, g);

        g.gridy = 6; p.add(new JLabel("Precio VENTA x unidad S/ *:"), g);
        g.gridy = 7; txtPrecioVenta = new JTextField();
        txtPrecioVenta.putClientProperty("JTextField.placeholderText", "Ej. 8.50");
        UiHelper.soloDecimales(txtPrecioVenta);
        p.add(txtPrecioVenta, g);

        g.gridy = 8;
        lblCostoCalc = new JLabel("Costo unit.: S/. 0.00");
        lblCostoCalc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCostoCalc.setForeground(new Color(0x1565C0));
        p.add(lblCostoCalc, g);
        g.gridy = 9;
        lblMargen = new JLabel("Margen: —");
        lblMargen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(lblMargen, g);

        DocumentListener calc = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcular(); actualizarEquivStock(); }
            public void removeUpdate(DocumentEvent e) { recalcular(); actualizarEquivStock(); }
            public void changedUpdate(DocumentEvent e) { recalcular(); actualizarEquivStock(); }
        };
        txtPrecioCompraUnidad.getDocument().addDocumentListener(calc);
        txtUnidadesPorCaja.getDocument().addDocumentListener(calc);
        txtPrecioCompraCaja.getDocument().addDocumentListener(calc);
        txtPrecioVenta.getDocument().addDocumentListener(calc);
        return p;
    }

    private JPanel seccionStock() {
        JPanel p = UiHelper.tarjeta("3 · Stock y vencimiento");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.fill = GridBagConstraints.HORIZONTAL; g.gridx = 0; g.weightx = 1;

        g.gridy = 0; lblStockTitulo = new JLabel("Stock inicial (unidades):"); p.add(lblStockTitulo, g);
        g.gridy = 1; txtStockActual = new JTextField("0");
        UiHelper.soloEnteros(txtStockActual);
        txtStockActual.setToolTipText("Si es POR CAJA: escribe N° DE CAJAS, no unidades. Al editar, usa +Ingresar / -Retirar.");
        txtStockActual.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarEquivStock(); }
            public void removeUpdate(DocumentEvent e) { actualizarEquivStock(); }
            public void changedUpdate(DocumentEvent e) { actualizarEquivStock(); }
        });
        p.add(txtStockActual, g);

        g.gridy = 2;
        lblEquivStock = new JLabel(" ");
        lblEquivStock.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblEquivStock.setForeground(new Color(0x1565C0));
        p.add(lblEquivStock, g);

        g.gridy = 3; p.add(new JLabel("Stock mínimo (alerta, en unidades):"), g);
        g.gridy = 4; txtStockMinimo = new JTextField("5");
        UiHelper.soloEnteros(txtStockMinimo);
        p.add(txtStockMinimo, g);

        g.gridy = 5;
        chkSinVencimiento = new JCheckBox("Sin vencimiento (abarrotes secos)", true);
        chkSinVencimiento.addActionListener(e -> dateVencimiento.setEnabled(!chkSinVencimiento.isSelected()));
        p.add(chkSinVencimiento, g);

        g.gridy = 6; p.add(new JLabel("Fecha vencimiento:"), g);
        g.gridy = 7;
        dateVencimiento = new JDateChooser();
        dateVencimiento.setDateFormatString("yyyy-MM-dd");
        dateVencimiento.setEnabled(false);
        dateVencimiento.setToolTipText("Usa el calendario, evita errores de formato");
        p.add(dateVencimiento, g);

        g.gridy = 8;
        JLabel hint = new JLabel("<html><i>Tip: si es perecible, desmarca 'Sin vencimiento'.</i></html>");
        hint.setForeground(Color.GRAY);
        p.add(hint, g);
        return p;
    }

    private JPanel panelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnGuardar = new JButton("💾 Guardar Producto");
        btnGuardar.setBackground(new Color(0x1565C0)); btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setToolTipText("Valida y guarda (Enter en Nombre también guarda)");
        btnGuardar.addActionListener(e -> guardarProducto());
        panelBotones.add(btnGuardar);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        panelBotones.add(btnLimpiar);

        btnIngresarStock = new JButton("+ Ingresar Stock");
        btnIngresarStock.setEnabled(false);
        btnIngresarStock.setToolTipText("Suma unidades al stock actual (reposición)");
        btnIngresarStock.addActionListener(e -> ingresarStock());
        panelBotones.add(btnIngresarStock);

        btnRetirarStock = new JButton("- Retirar Stock");
        btnRetirarStock.setEnabled(false);
        btnRetirarStock.setToolTipText("Resta por merma / ajuste (no deja negativo)");
        btnRetirarStock.addActionListener(e -> retirarStock());
        panelBotones.add(btnRetirarStock);

        btnActualizarPrecio = new JButton("💲 Actualizar Precio");
        btnActualizarPrecio.setEnabled(false);
        btnActualizarPrecio.setToolTipText("Cambia precio de compra y/o venta, muestra el margen antes/después");
        btnActualizarPrecio.addActionListener(e -> actualizarPrecio());
        panelBotones.add(btnActualizarPrecio);

        btnRetirarProducto = new JButton("🗑 Retirar Producto");
        btnRetirarProducto.setEnabled(false);
        btnRetirarProducto.setForeground(UiHelper.ROJO);
        btnRetirarProducto.setToolTipText("Elimina el producto (si tiene historial, se oculta en vez de borrarse)");
        btnRetirarProducto.addActionListener(e -> retirarProducto());
        panelBotones.add(btnRetirarProducto);
        return panelBotones;
    }

    // ---------- 2) LOGICA MODO CAJA/UNIDAD ----------
    private void actualizarModoCompra() {
        boolean esCaja = rbCaja.isSelected();
        txtUnidadesPorCaja.setEnabled(esCaja);
        txtPrecioCompraCaja.setEnabled(esCaja);
        txtPrecioCompraUnidad.setEnabled(!esCaja);
        if (!esCaja) { txtUnidadesPorCaja.setText(""); txtPrecioCompraCaja.setText(""); }
        if (lblStockTitulo != null)
            lblStockTitulo.setText(esCaja ? "Stock inicial (N° DE CAJAS — no unidades):" : "Stock inicial (unidades):");
        if (btnIngresarStock != null)
            btnIngresarStock.setText(esCaja ? "+ Ingresar Stock (por cajas)" : "+ Ingresar Stock");
        if (btnRetirarStock != null)
            btnRetirarStock.setText(esCaja ? "− Retirar (merma, por cajas/unid.)" : "- Retirar Stock (merma)");
        recalcular();
        actualizarEquivStock();
    }

    /** Muestra equivalencia cajas->unidades para que el usuario no multiplique manual. */
    private void actualizarEquivStock() {
        if (lblEquivStock == null || txtStockActual == null) return;
        if (rbCaja != null && rbCaja.isSelected()) {
            int cajas = UiHelper.parseInt(txtStockActual.getText(), 0);
            int und = UiHelper.parseInt(txtUnidadesPorCaja.getText(), 0);
            if (cajas > 0 && und > 0) lblEquivStock.setText("= " + cajas + " cajas × " + und + " = " + (cajas * und) + " unidades");
            else if (und > 0) lblEquivStock.setText("Escribe N° de cajas. Ej. 5 cajas × " + und + " = " + (5 * und) + " unid.");
            else lblEquivStock.setText("Completa 'Unidades x caja' para ver la equivalencia.");
        } else lblEquivStock.setText(" ");
    }

    private double costoUnitarioActual() {
        if (rbCaja.isSelected()) {
            int und = UiHelper.parseInt(txtUnidadesPorCaja.getText(), 0);
            double caja = UiHelper.parseDouble(txtPrecioCompraCaja.getText(), 0);
            if (und > 0) return caja / und;
            return 0;
        }
        return UiHelper.parseDouble(txtPrecioCompraUnidad.getText(), 0);
    }

    private void recalcular() {
        double costo = costoUnitarioActual();
        double venta = UiHelper.parseDouble(txtPrecioVenta.getText(), 0);
        lblCostoCalc.setText(String.format("Costo unit.: S/. %.2f", costo));
        if (costo > 0 && venta > 0) {
            double margen = (venta - costo) / costo * 100;
            lblMargen.setText(String.format("Margen: %.1f%% %s", margen, margen < 0 ? "⚠ PÉRDIDA" : margen < 10 ? "⚠ bajo" : "✓"));
            lblMargen.setForeground(margen < 0 ? UiHelper.ROJO : margen < 10 ? UiHelper.NARANJA : UiHelper.VERDE);
        } else {
            lblMargen.setText("Margen: —");
            lblMargen.setForeground(Color.GRAY);
        }
    }

    // ---------- 4) TABLA ENRIQUECIDA ----------
    private JPanel construirTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Catálogo de Productos (doble-clic para editar)"));

        JPanel panelBusqueda = new JPanel(new BorderLayout(6, 0));
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "🔍 Buscar por NOMBRE o CÓDIGO DE BARRAS… (no filtra por stock)");
        txtBuscar.setToolTipText("Filtra solo por Nombre y Código de barras. El Código es el de la columna 'Código' (ej. 7751234).");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        JLabel lblAyuda = new JLabel("Código = código de barras · Stock NO filtra");
        lblAyuda.setForeground(Color.GRAY);
        panelBusqueda.add(lblAyuda, BorderLayout.EAST);
        panel.add(panelBusqueda, BorderLayout.NORTH);

        modeloProductos = new DefaultTableModel(
                new String[]{"ID", "Producto", "Código", "Categoría", "Stock", "P. Venta", "Costo Unit.", "Margen %", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 4) return Integer.class;
                if (c == 5 || c == 6 || c == 7) return Double.class;
                return String.class;
            }
        };
        tablaProductos = new JTable(modeloProductos);
        UiHelper.estiloTabla(tablaProductos);
        sorter = new TableRowSorter<>(modeloProductos);
        tablaProductos.setRowSorter(sorter);
        tablaProductos.getColumnModel().getColumn(5).setCellRenderer(UiHelper.rendererMoneda());
        tablaProductos.getColumnModel().getColumn(6).setCellRenderer(UiHelper.rendererMoneda());
        // Semaforo por Estado (col 8)
        tablaProductos.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String s = v == null ? "" : v.toString();
                    if (s.contains("CRÍTICO")) { c.setBackground(UiHelper.FONDO_ROJO_SUAVE); c.setForeground(UiHelper.ROJO); }
                    else if (s.contains("Bajo") || s.contains("vencer")) { c.setBackground(UiHelper.FONDO_AMARILLO_SUAVE); c.setForeground(UiHelper.NARANJA); }
                    else { c.setBackground(Color.WHITE); c.setForeground(UiHelper.VERDE); }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });
        tablaProductos.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) cargarSeleccionEnFormulario(); });
        tablaProductos.removeColumn(tablaProductos.getColumnModel().getColumn(0)); // oculta ID (sigue en el modelo)
        panel.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        JLabel lblPie = new JLabel("ℹ Rojo = stock crítico · Amarillo = bajo o por vencer (≤5 días) · Clic en cabecera para ordenar");
        lblPie.setForeground(Color.GRAY);
        panel.add(lblPie, BorderLayout.SOUTH);
        return panel;
    }

    private void filtrar() {
        String t = txtBuscar.getText().trim();
        if (t.isEmpty()) { sorter.setRowFilter(null); return; }
        // Solo Nombre (col 1) y Codigo (col 2). NUNCA stock, para evitar confusion.
        String q = "(?i)" + java.util.regex.Pattern.quote(t);
        sorter.setRowFilter(RowFilter.orFilter(java.util.List.of(
                RowFilter.regexFilter(q, 1), RowFilter.regexFilter(q, 2))));
    }

    private String estadoDe(Producto p) {
        boolean bajo = p.getStockUnidades() <= p.getStockMinimo();
        boolean porVencer = false;
        if (p.getFechaVencimiento() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), p.getFechaVencimiento());
            porVencer = dias <= 5;
        }
        if (bajo && porVencer) return "🔴 CRÍTICO";
        if (bajo) return "🟡 Stock Bajo";
        if (porVencer) return "🟡 Por vencer";
        return "🟢 OK";
    }

    private void cargarCategorias() {
        Object sel = cbCategoria != null ? cbCategoria.getSelectedItem() : null;
        int selId = (sel instanceof Categoria) ? ((Categoria) sel).getIdCategoria() : -1;
        cbCategoria.removeAllItems();
        for (Categoria c : categoriaController.listarCategorias()) cbCategoria.addItem(c);
        if (selId != -1) for (int i = 0; i < cbCategoria.getItemCount(); i++)
            if (cbCategoria.getItemAt(i).getIdCategoria() == selId) { cbCategoria.setSelectedIndex(i); break; }
    }

    /** Sincroniza combo + tabla (usado al volver a la pestana: punto 2 del admin). */
    public void recargar() { cargarCategorias(); cargarProductos(); }

    private void crearCategoriaRapida() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva categoría (Ej. Limpieza, Mascotas):", "Nueva categoría", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;
        if (categoriaController.guardarCategoria(nombre.trim())) {
            cargarCategorias();
            for (int i = 0; i < cbCategoria.getItemCount(); i++)
                if (cbCategoria.getItemAt(i).getNombre().equalsIgnoreCase(nombre.trim())) cbCategoria.setSelectedIndex(i);
        } else JOptionPane.showMessageDialog(this, "No se pudo crear (¿ya existe?).", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void gestionarCategorias() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gestionar categorías", true);
        dlg.setSize(380, 380); dlg.setLocationRelativeTo(this);
        DefaultListModel<Categoria> model = new DefaultListModel<>();
        for (Categoria c : categoriaController.listarCategorias()) model.addElement(c);
        JList<Categoria> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTextField txtNueva = new JTextField(15);
        txtNueva.putClientProperty("JTextField.placeholderText", "Nueva categoría…");
        JButton btnAdd = new JButton("Agregar");
        btnAdd.addActionListener(e -> {
            String n = txtNueva.getText().trim();
            if (n.isBlank()) return;
            if (categoriaController.guardarCategoria(n)) {
                model.clear();
                for (Categoria c : categoriaController.listarCategorias()) model.addElement(c);
                txtNueva.setText("");
                cargarCategorias();
            } else JOptionPane.showMessageDialog(dlg, "Ya existe esa categoría.", "Aviso", JOptionPane.WARNING_MESSAGE);
        });
        JButton btnDel = new JButton("Eliminar seleccionada");
        btnDel.addActionListener(e -> {
            Categoria c = list.getSelectedValue();
            if (c == null) return;
            int r = JOptionPane.showConfirmDialog(dlg, "¿Eliminar '" + c.getNombre() + "'?\nNo se puede si tiene productos.", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            if (categoriaController.eliminarCategoria(c.getIdCategoria())) {
                model.removeElement(c); cargarCategorias();
            } else JOptionPane.showMessageDialog(dlg, "No se pudo eliminar (tiene productos asociados).", "Error", JOptionPane.ERROR_MESSAGE);
        });
        JPanel sur = new JPanel(new BorderLayout(5, 5));
        sur.add(txtNueva, BorderLayout.CENTER); sur.add(btnAdd, BorderLayout.EAST); sur.add(btnDel, BorderLayout.SOUTH);
        dlg.add(new JScrollPane(list), BorderLayout.CENTER);
        dlg.add(sur, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void cargarProductos() {
        String filtro = txtBuscar != null ? txtBuscar.getText().trim() : "";
        modeloProductos.setRowCount(0);
        List<Producto> productos = productoController.listarProductos();
        for (Producto p : productos) {
            double margen = p.getPrecioCompraUnitario() > 0
                    ? (p.getPrecioVentaUnitario() - p.getPrecioCompraUnitario()) / p.getPrecioCompraUnitario() * 100 : 0;
            modeloProductos.addRow(new Object[]{p.getIdProducto(), p.getNombre(),
                    p.getCodigoBarras() != null ? p.getCodigoBarras() : "—",
                    p.getNombreCategoria(), p.getStockUnidades(), p.getPrecioVentaUnitario(),
                    p.getPrecioCompraUnitario(), margen, estadoDe(p)});
        }
        if (filtro != null && !filtro.isBlank()) filtrar();
    }

    private void cargarSeleccionEnFormulario() {
        int vista = tablaProductos.getSelectedRow();
        if (vista == -1) return;
        int id = (int) modeloProductos.getValueAt(tablaProductos.convertRowIndexToModel(vista), 0);
        Producto p = productoController.obtenerPorId(id);
        if (p == null) return;

        idProductoSeleccionado = p.getIdProducto();
        txtNombre.setText(p.getNombre());
        txtCodigoBarras.setText(p.getCodigoBarras());
        txtPrecioVenta.setText(String.valueOf(p.getPrecioVentaUnitario()));
        if (p.isVieneEnCaja()) { rbCaja.setSelected(true); }
        else { rbUnidad.setSelected(true); }
        txtPrecioCompraUnidad.setText(String.valueOf(p.getPrecioCompraUnitario()));
        txtUnidadesPorCaja.setText(p.isVieneEnCaja() ? String.valueOf(p.getUnidadesPorCaja()) : "");
        txtPrecioCompraCaja.setText(p.isVieneEnCaja() ? String.valueOf(p.getPrecioCompraCaja()) : "");
        txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        if (p.getFechaVencimiento() != null) {
            chkSinVencimiento.setSelected(false);
            dateVencimiento.setEnabled(true);
            dateVencimiento.setDate(Date.from(p.getFechaVencimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            chkSinVencimiento.setSelected(true);
            dateVencimiento.setEnabled(false);
            dateVencimiento.setDate(null);
        }
        txtStockActual.setEditable(false);
        btnIngresarStock.setEnabled(true);
        btnRetirarStock.setEnabled(true);
        btnActualizarPrecio.setEnabled(true);
        btnRetirarProducto.setEnabled(true);
        // Campo visible: si es por caja muestra CAJAS (punto 3), si no unidades
        if (p.isVieneEnCaja() && p.getUnidadesPorCaja() > 0)
            txtStockActual.setText(String.valueOf(p.getStockUnidades() / p.getUnidadesPorCaja()));
        else
            txtStockActual.setText(String.valueOf(p.getStockUnidades()));
        actualizarModoCompra();
        // Repone equivalencia real tras actualizarModoCompra (que la recalcula con el campo)
        if (p.isVieneEnCaja() && p.getUnidadesPorCaja() > 0) {
            int cajas = p.getStockUnidades() / p.getUnidadesPorCaja();
            lblEquivStock.setText("Actual: " + p.getStockUnidades() + " unidades (" + cajas + " cajas × " + p.getUnidadesPorCaja() + ")");
        }

        for (int i = 0; i < cbCategoria.getItemCount(); i++)
            if (cbCategoria.getItemAt(i).getIdCategoria() == p.getIdCategoria()) { cbCategoria.setSelectedIndex(i); break; }
    }

    private void guardarProducto() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isBlank()) { JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE); txtNombre.requestFocus(); return; }
        double venta = UiHelper.parseDouble(txtPrecioVenta.getText(), -1);
        if (venta < 0) { JOptionPane.showMessageDialog(this, "Precio de venta inválido.", "Validación", JOptionPane.WARNING_MESSAGE); return; }
        boolean esCaja = rbCaja.isSelected();
        int und = UiHelper.parseInt(txtUnidadesPorCaja.getText(), esCaja ? -1 : 1);
        double precioCaja = UiHelper.parseDouble(txtPrecioCompraCaja.getText(), 0);
        double costoUnd = UiHelper.parseDouble(txtPrecioCompraUnidad.getText(), 0);
        if (esCaja && und <= 0) { JOptionPane.showMessageDialog(this, "Unidades x caja debe ser mayor a 0.", "Validación", JOptionPane.WARNING_MESSAGE); return; }
        if (esCaja && precioCaja <= 0) {
            int r = JOptionPane.showConfirmDialog(this, "Precio de caja en 0. ¿Guardar igual?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }
        if (venta < costoUnitarioActual() && costoUnitarioActual() > 0) {
            int r = JOptionPane.showConfirmDialog(this,
                    String.format("El precio de venta (S/. %.2f) es MENOR al costo (S/. %.2f). ¿Guardar igual?", venta, costoUnitarioActual()),
                    "Margen negativo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        try {
            Producto p = new Producto();
            p.setIdProducto(idProductoSeleccionado);
            p.setNombre(nombre);
            String cb = txtCodigoBarras.getText().trim();
            p.setCodigoBarras(cb.isBlank() ? null : cb);
            Categoria cat = (Categoria) cbCategoria.getSelectedItem();
            if (cat != null) p.setIdCategoria(cat.getIdCategoria());
            p.setPrecioVentaUnitario(venta);
            p.setVieneEnCaja(esCaja);
            p.setUnidadesPorCaja(esCaja ? und : 1);
            p.setPrecioCompraCaja(esCaja ? precioCaja : 0);
            if (!esCaja) p.setPrecioCompraUnitario(costoUnd);
            p.setStockMinimo(txtStockMinimo.getText().isBlank() ? 5 : UiHelper.parseInt(txtStockMinimo.getText(), 5));
            if (chkSinVencimiento.isSelected() || dateVencimiento.getDate() == null) p.setFechaVencimiento(null);
            else p.setFechaVencimiento(dateVencimiento.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            if (idProductoSeleccionado == 0) {
                // Punto 3 admin: si es por CAJA el campo son CAJAS -> convertir a unidades (sin multiplicar manual)
                int capturado = UiHelper.parseInt(txtStockActual.getText(), 0);
                if (esCaja) p.setStockUnidades(Math.max(0, capturado * und));
                else p.setStockUnidades(Math.max(0, capturado));
            }
            else
                p.setStockUnidades(productoController.obtenerPorId(idProductoSeleccionado).getStockUnidades());

            if (productoController.guardarProducto(p)) {
                String extra = (idProductoSeleccionado == 0 && esCaja)
                        ? "\nStock: " + txtStockActual.getText() + " cajas × " + und + " = " + p.getStockUnidades() + " unidades."
                        : "";
                JOptionPane.showMessageDialog(this, "✓ Producto guardado.\n" + lblCostoCalc.getText() + " · " + lblMargen.getText() + extra);
                limpiarFormulario(); cargarProductos();
            } else JOptionPane.showMessageDialog(this, "No se pudo guardar. Revisa código de barras duplicado.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ingresarStock() {
        if (idProductoSeleccionado == 0) return;
        Producto prod = productoController.obtenerPorId(idProductoSeleccionado);
        if (prod == null) return;
        int cantidadUnidades;
        // Punto 3: si viene en caja, pide CAJAS y convierte solo (no multiplica el usuario)
        if (prod.isVieneEnCaja() && prod.getUnidadesPorCaja() > 0) {
            JSpinner spnCajas = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
            JLabel lblInfo = new JLabel("Cada caja = " + prod.getUnidadesPorCaja() + " unidades.");
            JPanel pnl = new JPanel(new GridLayout(3, 1, 4, 4));
            pnl.add(new JLabel("¿Cuántas CAJAS ingresan de \"" + prod.getNombre() + "\"?"));
            pnl.add(spnCajas); pnl.add(lblInfo);
            int r = JOptionPane.showConfirmDialog(this, pnl, "Ingreso por CAJAS", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            cantidadUnidades = ((int) spnCajas.getValue()) * prod.getUnidadesPorCaja();
        } else {
            String input = JOptionPane.showInputDialog(this, "¿Cuántas UNIDADES ingresan?", "Ingreso de Stock", JOptionPane.QUESTION_MESSAGE);
            if (input == null || input.isBlank()) return;
            try { cantidadUnidades = Integer.parseInt(input.trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Ingresa un número válido.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        }
        if (productoController.registrarIngresoStock(idProductoSeleccionado, cantidadUnidades)) {
            JOptionPane.showMessageDialog(this, "✓ Stock actualizado (+" + cantidadUnidades + " unidades).");
            cargarProductos(); recargarSeleccion();
        } else JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void retirarStock() {
        if (idProductoSeleccionado == 0) return;
        Producto prod = productoController.obtenerPorId(idProductoSeleccionado);
        if (prod == null) return;
        boolean esCaja = prod.isVieneEnCaja() && prod.getUnidadesPorCaja() > 0;
        // Dialogo: cantidad (cajas o unidades) + motivo de merma (punto 4)
        JComboBox<String> cbModo = new JComboBox<>(esCaja ? new String[]{"Cajas", "Unidades"} : new String[]{"Unidades"});
        JSpinner spnCant = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        JComboBox<String> cbMotivo = new JComboBox<>(new String[]{"Vencimiento", "Rotura / daño", "Robo / pérdida", "Donación", "Ajuste de inventario", "Otro"});
        JPanel pnl = new JPanel(new GridLayout(0, 2, 6, 6));
        pnl.add(new JLabel("Medida:")); pnl.add(cbModo);
        pnl.add(new JLabel("Cantidad:")); pnl.add(spnCant);
        pnl.add(new JLabel("Motivo (merma):")); pnl.add(cbMotivo);
        if (esCaja) pnl.add(new JLabel("1 caja = " + prod.getUnidadesPorCaja() + " unid."));
        int r = JOptionPane.showConfirmDialog(this, pnl, "Retiro / MERMA de \"" + prod.getNombre() + "\"", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        int cant = (int) spnCant.getValue();
        int cantidadUnidades = ("Cajas".equals(cbModo.getSelectedItem())) ? cant * prod.getUnidadesPorCaja() : cant;
        String motivo = (String) cbMotivo.getSelectedItem();
        if (mermaController.registrarMerma(idProductoSeleccionado, cantidadUnidades, motivo)) {
            JOptionPane.showMessageDialog(this, "✓ Merma registrada: -" + cantidadUnidades + " unidades (" + motivo + ").\nVer Reportes → Pérdidas por merma.");
            cargarProductos(); recargarSeleccion();
        } else JOptionPane.showMessageDialog(this, "Cantidad inválida o mayor al stock disponible (" + prod.getStockUnidades() + " unid.).", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void actualizarPrecio() {
        if (idProductoSeleccionado == 0) return;
        Producto prod = productoController.obtenerPorId(idProductoSeleccionado);
        if (prod == null) return;

        boolean esCaja = prod.isVieneEnCaja() && prod.getUnidadesPorCaja() > 0;
        double costoActualCapturado = esCaja ? prod.getPrecioCompraCaja() : prod.getPrecioCompraUnitario();
        double costoActualUnitario = prod.getPrecioCompraUnitario();
        double ventaActual = prod.getPrecioVentaUnitario();
        double margenActual = costoActualUnitario > 0 ? (ventaActual - costoActualUnitario) / costoActualUnitario * 100 : 0;

        JTextField txtNuevoCosto = new JTextField(String.format("%.2f", costoActualCapturado));
        UiHelper.soloDecimales(txtNuevoCosto);
        JTextField txtNuevaVenta = new JTextField(String.format("%.2f", ventaActual));
        UiHelper.soloDecimales(txtNuevaVenta);

        JLabel lblMargenActual = new JLabel(String.format("Margen ACTUAL: %.1f%%", margenActual));
        JLabel lblMargenNuevo = new JLabel("Margen NUEVO: ---");
        lblMargenNuevo.setFont(lblMargenNuevo.getFont().deriveFont(Font.BOLD));

        Runnable recalc = () -> {
            double nuevoCosto = UiHelper.parseDouble(txtNuevoCosto.getText(), 0);
            double nuevoCostoUnitario = esCaja && prod.getUnidadesPorCaja() > 0
                    ? nuevoCosto / prod.getUnidadesPorCaja() : nuevoCosto;
            double nuevaVenta = UiHelper.parseDouble(txtNuevaVenta.getText(), 0);
            if (nuevoCostoUnitario > 0 && nuevaVenta > 0) {
                double margenNuevo = (nuevaVenta - nuevoCostoUnitario) / nuevoCostoUnitario * 100;
                lblMargenNuevo.setText(String.format("Margen NUEVO: %.1f%% %s", margenNuevo,
                        margenNuevo < 0 ? "⚠ PÉRDIDA" : margenNuevo < 10 ? "⚠ bajo" : "✓"));
                lblMargenNuevo.setForeground(margenNuevo < 0 ? UiHelper.ROJO : margenNuevo < 10 ? UiHelper.NARANJA : UiHelper.VERDE);
            } else {
                lblMargenNuevo.setText("Margen NUEVO: ---");
                lblMargenNuevo.setForeground(Color.GRAY);
            }
        };
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalc.run(); }
            public void removeUpdate(DocumentEvent e) { recalc.run(); }
            public void changedUpdate(DocumentEvent e) { recalc.run(); }
        };
        txtNuevoCosto.getDocument().addDocumentListener(dl);
        txtNuevaVenta.getDocument().addDocumentListener(dl);
        recalc.run();

        JPanel pnl = new JPanel(new GridLayout(0, 1, 4, 4));
        pnl.add(new JLabel("Producto: " + prod.getNombre() + (esCaja ? " (modo: CAJA)" : " (modo: UNIDAD)")));
        pnl.add(new JLabel(esCaja ? "Nuevo costo de compra x CAJA S/:" : "Nuevo costo de compra x UNIDAD S/:"));
        pnl.add(txtNuevoCosto);
        pnl.add(new JLabel("Nuevo precio de VENTA x unidad S/:"));
        pnl.add(txtNuevaVenta);
        pnl.add(lblMargenActual);
        pnl.add(lblMargenNuevo);

        int r = JOptionPane.showConfirmDialog(this, pnl, "Actualizar Precio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        double nuevoCosto = UiHelper.parseDouble(txtNuevoCosto.getText(), -1);
        double nuevaVenta = UiHelper.parseDouble(txtNuevaVenta.getText(), -1);
        if (nuevoCosto < 0 || nuevaVenta < 0) {
            JOptionPane.showMessageDialog(this, "Valores inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (productoController.actualizarPrecios(idProductoSeleccionado, nuevoCosto, nuevaVenta)) {
            JOptionPane.showMessageDialog(this, "✓ Precios actualizados.\n" + lblMargenNuevo.getText());
            cargarProductos();
            recargarSeleccion();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void retirarProducto() {
        if (idProductoSeleccionado == 0) return;
        Producto prod = productoController.obtenerPorId(idProductoSeleccionado);
        if (prod == null) return;

        Object[] opciones = {"Sí", "No"};
        int r = JOptionPane.showOptionDialog(this,
                "¿Estás seguro de eliminar este producto?\n\"" + prod.getNombre() + "\"",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                null, opciones, opciones[1]);
        if (r != 0) return; // 0 = "Sí"

        ResultadoEliminacion resultado = productoController.eliminarProducto(idProductoSeleccionado);
        switch (resultado) {
            case ELIMINADO -> {
                JOptionPane.showMessageDialog(this, "✓ Producto eliminado permanentemente del catálogo.");
                limpiarFormulario();
                cargarProductos();
            }
            case OCULTADO -> {
                JOptionPane.showMessageDialog(this,
                        "Este producto ya tiene ventas o mermas registradas, así que no se puede borrar sin " +
                        "perder ese historial.\nSe ocultó del catálogo en su lugar: ya no aparecerá en Inventario ni en Venta Express.");
                limpiarFormulario();
                cargarProductos();
            }
            case ERROR -> JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recargarSeleccion() {
        // Refresca campo stock tras ingreso/retiro respetando vista cajas/unidades
        Producto p = productoController.obtenerPorId(idProductoSeleccionado);
        if (p == null) return;
        if (p.isVieneEnCaja() && p.getUnidadesPorCaja() > 0) {
            txtStockActual.setText(String.valueOf(p.getStockUnidades() / p.getUnidadesPorCaja()));
            lblEquivStock.setText("Actual: " + p.getStockUnidades() + " unidades");
        } else txtStockActual.setText(String.valueOf(p.getStockUnidades()));
    }

    private void limpiarFormulario() {
        idProductoSeleccionado = 0;
        txtNombre.setText(""); txtCodigoBarras.setText(""); txtPrecioVenta.setText("");
        rbUnidad.setSelected(true);
        txtPrecioCompraUnidad.setText("0.00"); txtUnidadesPorCaja.setText(""); txtPrecioCompraCaja.setText("");
        txtStockMinimo.setText("5"); txtStockActual.setText("0"); txtStockActual.setEditable(true);
        chkSinVencimiento.setSelected(true); dateVencimiento.setDate(null); dateVencimiento.setEnabled(false);
        btnIngresarStock.setEnabled(false); btnRetirarStock.setEnabled(false);
        btnActualizarPrecio.setEnabled(false); btnRetirarProducto.setEnabled(false);
        tablaProductos.clearSelection(); actualizarModoCompra();
    }
}
