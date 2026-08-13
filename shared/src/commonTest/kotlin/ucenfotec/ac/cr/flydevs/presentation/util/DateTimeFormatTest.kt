package ucenfotec.ac.cr.flydevs.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeFormatTest {

    @Test
    fun `convierte a hora de Costa Rica`() {
        // 2026-07-23T18:55:43Z → 12:55 en UTC-6 (dato real del seed de lotes).
        assertEquals("23 jul 2026 · 12:55", formatDateTime(1_784_832_943_482L))
    }

    @Test
    fun `resta el offset cruzando el cambio de dia`() {
        // 2026-01-01T03:00:00Z todavía es 31 de diciembre en Costa Rica.
        assertEquals("31 dic 2025 · 21:00", formatDateTime(1_767_236_400_000L))
    }

    @Test
    fun `maneja el 29 de febrero de un año bisiesto`() {
        // 2024-02-29T12:00:00Z → 06:00 local.
        assertEquals("29 feb 2024 · 06:00", formatDateTime(1_709_208_000_000L))
    }

    @Test
    fun `maneja el ultimo dia del año`() {
        // 2026-12-31T23:30:00Z → 17:30 local del mismo día.
        assertEquals("31 dic 2026 · 17:30", formatDateTime(1_798_759_800_000L))
    }

    @Test
    fun `usa el placeholder cuando no hay fecha`() {
        assertEquals("—", formatDateTime(0L))
        assertEquals("—", formatDateTime(-1L))
        assertEquals("Pendiente", formatDateTime(0L, placeholder = "Pendiente"))
    }

    @Test
    fun `formatDate omite la hora`() {
        assertEquals("23 jul 2026", formatDate(1_784_832_943_482L))
    }
}
