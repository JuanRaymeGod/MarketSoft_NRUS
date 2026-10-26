package marketsoft.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FechaUtilTest {

    @Test
    void utcALima_restaCincoHoras() {
        LocalDateTime lima = FechaUtil.utcALima("2026-09-02 15:00:00");
        assertEquals(LocalDateTime.of(2026, 9, 2, 10, 0, 0), lima);
    }

    @Test
    void utcALima_aceptaElSeparadorT() {
        assertEquals(LocalDateTime.of(2026, 9, 2, 10, 0, 0), FechaUtil.utcALima("2026-09-02T15:00:00"));
    }

    @Test
    void utcALima_nuloOEnBlanco_devuelveNull() {
        assertNull(FechaUtil.utcALima(null));
        assertNull(FechaUtil.utcALima("   "));
    }

    @Test
    void formatearLima_usaElFormatoDiaMesAnioHoraMinuto() {
        assertEquals("02/09/2026 10:00", FechaUtil.formatearLima("2026-09-02 15:00:00"));
    }

    @Test
    void formatearLima_sinFecha_devuelveTextoVacio() {
        assertEquals("", FechaUtil.formatearLima((String) null));
    }
}
