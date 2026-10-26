package marketsoft.view;
import marketsoft.controller.LoginController;
import marketsoft.model.Usuario;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JComboBox<String> cbRol;
    private JCheckBox chkVer;
    private final LoginController loginController = new LoginController();

    public LoginView() {
        setTitle("MarketSoft NRUS - Iniciar Sesión");
        setSize(420, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("MarketSoft NRUS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(0x1565C0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Bodegas y minimarkets · Nuevo RUS", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy = 1;
        panel.add(lblSub, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        txtUsuario.putClientProperty("JTextField.placeholderText", "Ej. admin / cajero");
        panel.add(txtUsuario, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtContrasena = new JPasswordField(15);
        txtContrasena.putClientProperty("JTextField.placeholderText", "••••••");
        panel.add(txtContrasena, gbc);

        gbc.gridy = 4; gbc.gridx = 1;
        chkVer = new JCheckBox("Mostrar contraseña");
        chkVer.addActionListener(e -> txtContrasena.setEchoChar(chkVer.isSelected() ? (char) 0 : '•'));
        panel.add(chkVer, gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1;
        cbRol = new JComboBox<>(new String[]{"Administrador", "Cajero"});
        panel.add(cbRol, gbc);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnIngresar = new JButton("INICIAR SESIÓN");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setBackground(new Color(0x1565C0));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.addActionListener(e -> intentarLogin());
        panel.add(btnIngresar, gbc);

        add(panel);
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void intentarLogin() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String rolSeleccionado = (String) cbRol.getSelectedItem();

        Usuario u = loginController.iniciarSesion(usuario, contrasena);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!u.getRol().equalsIgnoreCase(rolSeleccionado)) {
            JOptionPane.showMessageDialog(this, "El rol seleccionado no coincide con este usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new MainView(u).setVisible(true);
        dispose();
    }
}
