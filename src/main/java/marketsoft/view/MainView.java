package marketsoft.view;
import marketsoft.model.Usuario;
import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    private final Usuario usuarioActivo;
    private JTabbedPane tabs;
    private DashboardPanel dashboardPanel;
    private HistorialVentasPanel historialPanel;
    private VentaExpressPanel ventaPanel;
    private InventarioPanel inventarioPanel;
    private ReportesPanel reportesPanel;

    public MainView(Usuario usuarioActivo) {
        this.usuarioActivo = usuarioActivo;
        setTitle("MarketSoft NRUS - Bodega · " + usuarioActivo.getUsuario() + " (" + usuarioActivo.getRol() + ")");
        setSize(1180, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel lblTitulo = new JLabel("MarketSoft NRUS · Nuevo RUS Cat.1 (Tope S/. 8,000/mes)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(new Color(0x1565C0));
        JLabel lblCajero = new JLabel("Usuario: " + usuarioActivo.getUsuario() + "  (" + usuarioActivo.getRol() + ")");
        lblCajero.setHorizontalAlignment(SwingConstants.RIGHT);
        barra.add(lblTitulo, BorderLayout.WEST);
        barra.add(lblCajero, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        boolean esAdministrador = usuarioActivo.getRol().equalsIgnoreCase("Administrador");

        dashboardPanel = new DashboardPanel();
        ventaPanel = new VentaExpressPanel();
        historialPanel = new HistorialVentasPanel(esAdministrador);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("⚡ Venta Express", ventaPanel);
        tabs.setToolTipTextAt(0, "Punto de venta rápido (F2 = cobrar). Se actualiza solo al entrar.");
        tabs.addTab("📊 Dashboard", dashboardPanel);
        tabs.setToolTipTextAt(1, "Ventas del día, tope NRUS y alertas");
        tabs.addTab("🧾 Historial", historialPanel);
        tabs.setToolTipTextAt(2, "Ventas registradas y anulaciones (admin)");

        if (esAdministrador) {
            inventarioPanel = new InventarioPanel();
            reportesPanel = new ReportesPanel();
            tabs.addTab("📦 Inventario", inventarioPanel);
            tabs.addTab("📈 Reportes", reportesPanel);
        }
        tabs.setMnemonicAt(0, 'V');
        tabs.setMnemonicAt(1, 'D');
        tabs.setMnemonicAt(2, 'H');

        // Punto 2 admin: todo se refresca al cambiar de pestana (nuevo producto/stock visible al instante)
        tabs.addChangeListener(e -> {
            Component sel = tabs.getSelectedComponent();
            if (sel == dashboardPanel) dashboardPanel.recargar();
            else if (sel == historialPanel) historialPanel.recargar();
            else if (sel == ventaPanel) ventaPanel.recargar();
            else if (sel == inventarioPanel && inventarioPanel != null) inventarioPanel.recargar();
        });

        add(tabs, BorderLayout.CENTER);
    }
}
