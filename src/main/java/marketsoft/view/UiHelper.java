package marketsoft.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;
import java.awt.*;

/** Utilidades visuales compartidas (opciones 12-13). */
public final class UiHelper {
    private UiHelper() {}

    public static final Color VERDE = new Color(0x1B7F3B);
    public static final Color NARANJA = new Color(0xB26A00);
    public static final Color ROJO = new Color(0xC62828);
    public static final Color FONDO_ROJO_SUAVE = new Color(0xFDECEA);
    public static final Color FONDO_AMARILLO_SUAVE = new Color(0xFFF8E1);
    public static final Color FONDO_VERDE_SUAVE = new Color(0xE8F5E9);

    public static String moneda(double v) { return String.format("S/. %.2f", v); }

    /** Filtro que solo permite decimales con 2 decimales max. */
    public static void soloDecimales(JTextField txt) {
        ((AbstractDocument) txt.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
                replace(fb, off, 0, s, a);
            }
            @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, off) + (s == null ? "" : s) + actual.substring(off + len);
                if (nuevo.isEmpty() || nuevo.matches("\\d{0,9}(\\.\\d{0,2})?")) super.replace(fb, off, len, s, a);
                else Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    public static void soloEnteros(JTextField txt) {
        ((AbstractDocument) txt.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
                replace(fb, off, 0, s, a);
            }
            @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
                String actual;
                try { actual = fb.getDocument().getText(0, fb.getDocument().getLength()); }
                catch (Exception e) { return; }
                String nuevo = actual.substring(0, off) + (s == null ? "" : s) + actual.substring(off + len);
                if (nuevo.isEmpty() || nuevo.matches("\\d{0,9}")) super.replace(fb, off, len, s, a);
                else Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    /** DNI peruano: max 8 digitos. */
    public static void soloDni(JTextField txt) {
        ((AbstractDocument) txt.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, off) + (s == null ? "" : s) + actual.substring(off + len);
                if (nuevo.isEmpty() || nuevo.matches("\\d{0,8}")) super.replace(fb, off, len, s, a);
                else Toolkit.getDefaultToolkit().beep();
            }
            @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
                replace(fb, off, 0, s, a);
            }
        });
    }

    public static boolean dniValido(String dni) {
        if (dni == null || dni.isBlank()) return true; // opcional
        return dni.trim().matches("\\d{8}");
    }

    public static double parseDouble(String txt, double defecto) {
        try {
            String t = txt == null ? "" : txt.trim();
            if (t.isEmpty()) return defecto;
            return Double.parseDouble(t);
        } catch (NumberFormatException e) { return defecto; }
    }

    public static int parseInt(String txt, int defecto) {
        try {
            String t = txt == null ? "" : txt.trim();
            if (t.isEmpty()) return defecto;
            return Integer.parseInt(t);
        } catch (NumberFormatException e) { return defecto; }
    }

    /** Renderer generico que pinta fila completa segun color de fondo dado por funcion. */
    public static DefaultTableCellRenderer rendererMoneda() {
        return new DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                if (v instanceof Number n) setText(String.format("%.2f", n.doubleValue()));
                else super.setValue(v);
                setHorizontalAlignment(RIGHT);
            }
        };
    }

    public static void estiloTabla(JTable t) {
        t.setRowHeight(24);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        t.setAutoCreateRowSorter(true);
        t.setFillsViewportHeight(true);
    }

    public static JPanel tarjeta(String titulo) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xD0D0D0), 1, true), titulo),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        p.setBackground(Color.WHITE);
        return p;
    }
}
