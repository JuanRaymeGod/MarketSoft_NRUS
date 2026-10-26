package marketsoft.controller;

import marketsoft.config.ConexionSQLite;
import marketsoft.util.CifradorUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RespaldoController {

    // Exporta una copia consistente (VACUUM INTO) y luego la cifra hacia 'destino'.
    public void exportar(File destino, char[] contrasena) throws Exception {
        File temporalPlano = File.createTempFile("marketsoft_export_", ".db");
        temporalPlano.deleteOnExit();
        try {
            if (temporalPlano.exists()) {
                temporalPlano.delete(); // VACUUM INTO exige que el archivo destino NO exista
            }
            try (Statement st = ConexionSQLite.getConexion().createStatement()) {
                st.execute("VACUUM INTO '" + temporalPlano.getAbsolutePath().replace("'", "''") + "'");
            }
            CifradorUtil.cifrarArchivo(temporalPlano, destino, contrasena);
        } finally {
            temporalPlano.delete();
        }
    }

    // Descifra, valida y reemplaza la base de datos actual por la del archivo 'origen'.
    public void importar(File origen, char[] contrasena) throws Exception {
        File temporalPlano = File.createTempFile("marketsoft_import_", ".db");
        temporalPlano.deleteOnExit();
        try {
            CifradorUtil.descifrarArchivo(origen, temporalPlano, contrasena);
            validarBaseDeDatosCompatible(temporalPlano);

            String rutaActual = ConexionSQLite.getRutaArchivoDb();
            File actual = new File(rutaActual);
            File respaldoPrevio = new File(rutaActual + ".bak");

            ConexionSQLite.cerrarConexion(); // libera el archivo antes de reemplazarlo

            if (actual.exists()) {
                Files.copy(actual.toPath(), respaldoPrevio.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.copy(temporalPlano.toPath(), actual.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            temporalPlano.delete();
        }
    }

    // Verifica que el archivo descifrado sea realmente una base de MarketSoft (no cualquier .db).
    private void validarBaseDeDatosCompatible(File archivo) throws Exception {
        String url = "jdbc:sqlite:" + archivo.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name IN "
                     + "('productos','ventas','detalle_ventas','usuarios','categorias','mermas')")) {
            int encontradas = 0;
            while (rs.next()) {
                encontradas++;
            }
            if (encontradas < 6) {
                throw new IOException("El archivo no contiene una base de datos compatible con MarketSoft.");
            }
        } catch (SQLException e) {
            throw new IOException("El archivo no es una base de datos SQLite válida.", e);
        }
    }
}
