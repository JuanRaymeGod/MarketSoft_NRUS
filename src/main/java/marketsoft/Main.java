package marketsoft;
import com.formdev.flatlaf.FlatLightLaf;
import marketsoft.view.LoginView;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        // Pulido FlatLaf moderno (opcion 12): fuentes, focos, bordes redondeados
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.rowHeight", 26);
        UIManager.put("TabbedPane.tabHeight", 32);
        Font base = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", base);
        javax.swing.SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}
