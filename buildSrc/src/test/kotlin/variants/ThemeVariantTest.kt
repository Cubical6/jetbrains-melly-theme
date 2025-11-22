package variants

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ThemeVariantTest {

    @Test
    fun `standard variant has no suffix`() {
        assertEquals("", ThemeVariant.Standard.suffix)
        assertEquals("", ThemeVariant.Standard.displayName)
    }

    @Test
    fun `rounded variant has suffix`() {
        assertEquals(" Rounded", ThemeVariant.Rounded.suffix)
        assertEquals("Rounded", ThemeVariant.Rounded.displayName)
    }

    @Test
    fun `standard variant has all arcs set to 0`() {
        val arcs = ThemeVariant.Standard.arcValues
        assertEquals(0, arcs.component)
        assertEquals(0, arcs.button)
        assertEquals(0, arcs.tabbedPane)
        assertEquals(0, arcs.progressBar)
    }

    @Test
    fun `rounded variant has positive arc values`() {
        val arcs = ThemeVariant.Rounded.arcValues
        assertTrue(arcs.component > 0)
        assertTrue(arcs.button > 0)
        assertTrue(arcs.popup > 0)
    }

    @Test
    fun `all returns both variants`() {
        val variants = ThemeVariant.all()
        assertEquals(2, variants.size)
        assertTrue(variants.contains(ThemeVariant.Standard))
        assertTrue(variants.contains(ThemeVariant.Rounded))
    }

    @Test
    fun `toPlaceholders creates correct map`() {
        val placeholders = ThemeVariant.Rounded.arcValues.toPlaceholders()

        assertEquals("8", placeholders["\$arc_component$"])
        assertEquals("6", placeholders["\$arc_button$"])
        assertEquals("12", placeholders["\$arc_popup$"])
        assertEquals(10, placeholders.size) // 10 arc properties
    }
}
