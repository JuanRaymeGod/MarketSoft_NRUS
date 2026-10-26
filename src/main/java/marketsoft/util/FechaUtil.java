package marketsoft.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * SQLite CURRENT_TIMESTAMP guarda UTC. Peru (America/Lima) es UTC-5 fijo (sin horario de verano).
 * Todas las lecturas convierten UTC -> Lima; los filtros SQL usan '-5 hours' para coincidir.
 */
public final class FechaUtil {
    private FechaUtil() {}

    public static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter SQLITE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQLITE_T = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Convierte "YYYY-MM-DD HH:MM:SS" (UTC) a hora Lima. Si ya viene con T, tambien lo acepta. */
    public static LocalDateTime utcALima(String sqliteFechaHora) {
        if (sqliteFechaHora == null || sqliteFechaHora.isBlank()) return null;
        String s = sqliteFechaHora.trim().replace("T", " ");
        if (s.length() > 19) s = s.substring(0, 19);
        LocalDateTime utc = LocalDateTime.parse(s, SQLITE);
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(LIMA).toLocalDateTime();
    }

    public static String formatearLima(String sqliteFechaHora) {
        LocalDateTime lima = utcALima(sqliteFechaHora);
        return lima != null ? lima.format(DIA_HORA) : "";
    }

    public static String formatearLima(LocalDateTime lima) {
        return lima != null ? lima.format(DIA_HORA) : "";
    }
}
