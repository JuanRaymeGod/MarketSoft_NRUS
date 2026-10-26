package marketsoft.config;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionSQLite {

    private static final String NOMBRE_CARPETA_APP = "MarketSoftNRUS";
    private static final String NOMBRE_ARCHIVO_DB = "marketsoft.db";
    private static Connection conexion;

    private ConexionSQLite() {
    }

    // Arma la ruta hacia %APPDATA%\MarketSoftNRUS\marketsoft.db (Windows), creando
    // la carpeta si no existe. Así el .db vive en una ubicación con permisos de
    // escritura garantizados, sin importar en qué carpeta se haya instalado la app.
    private static String obtenerRutaBaseDeDatos() {
        String appData = System.getenv("APPDATA");
        String carpetaBase = (appData != null && !appData.isBlank())
                ? appData
                : System.getProperty("user.home"); // respaldo si APPDATA no existe (Linux/Mac)

        File carpetaApp = new File(carpetaBase, NOMBRE_CARPETA_APP);
        if (!carpetaApp.exists()) {
            carpetaApp.mkdirs();
        }
        return new File(carpetaApp, NOMBRE_ARCHIVO_DB).getAbsolutePath();
    }

    // Expone la ruta del archivo .db actual — la usa RespaldoController para exportar/importar.
    public static String getRutaArchivoDb() {
        return obtenerRutaBaseDeDatos();
    }

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                String url = "jdbc:sqlite:" + obtenerRutaBaseDeDatos();
                conexion = DriverManager.getConnection(url);
                conexion.createStatement().execute("PRAGMA foreign_keys = ON;");
                inicializarBaseDeDatos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conexion;
    }

    private static void inicializarBaseDeDatos() {
        try (InputStream is = ConexionSQLite.class.getResourceAsStream("/schema.sql")) {
            if (is == null) {
                System.err.println("No se encontró /schema.sql en el classpath.");
                return;
            }
            String script = new String(is.readAllBytes());
            try (Statement st = conexion.createStatement()) {
                for (String sentencia : script.split(";")) {
                    String limpia = sentencia.trim();
                    if (limpia.isEmpty()) {
                        continue;
                    }
                    try {
                        st.execute(limpia);
                    } catch (SQLException ex) {
                        System.err.println("Aviso al ejecutar sentencia de schema.sql: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            if (rs.next() && rs.getInt(1) == 0) {
                st.execute("INSERT INTO usuarios (usuario, contrasena, rol) VALUES ('admin','admin','Administrador')");
                st.execute("INSERT INTO usuarios (usuario, contrasena, rol) VALUES ('cajero','cajero','Cajero')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categorias")) {
            boolean vacia = rs.next() && rs.getInt(1) == 0;
            if (vacia) {
                for (String c : categoriasBase()) {
                    st.execute("INSERT INTO categorias (nombre) VALUES ('" + c.replace("'", "''") + "')");
                }
            } else {
                for (String c : categoriasBase()) {
                    try {
                        st.execute("INSERT OR IGNORE INTO categorias (nombre) VALUES ('" + c.replace("'", "''") + "')");
                    } catch (SQLException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Categorias tipicas de bodega/minimarket peruano (12 base). El admin puede crear mas con + / Gestionar. */
    private static String[] categoriasBase() {
        return new String[]{"Abarrotes", "Bebidas", "Lácteos", "Snacks",
                "Limpieza del hogar", "Higiene personal", "Galletas y Panadería",
                "Golosinas y Chocolates", "Embutidos y Congelados", "Licores y Cigarrillos",
                "Mascotas", "Librería y Bazar"};
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}