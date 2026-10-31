package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.UserPreferencesRepository
import com.example.model.CountdownEvent
import com.example.widget.WidgetBitmapRenderer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CountdownUnitTest {

    @Test
    fun testWidgetBitmapRendererGeneratesValidBitmap() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val event = CountdownEvent(
            id = 1L,
            title = "AVENGERS: DOOMSDAY",
            targetDateTimeMillis = System.currentTimeMillis() + 86400000L * 47,
            accentColorHex = "#FF2E2E",
            fontStyle = "dot_matrix"
        )

        val bitmap = WidgetBitmapRenderer.renderWidgetBitmap(
            context = context,
            event = event,
            widthPx = 600,
            heightPx = 360
        )

        assertNotNull(bitmap)
        assertTrue(bitmap.width >= 360)
        assertTrue(bitmap.height >= 240)
    }

    @Test
    fun testTallVerticalWidgetRendererGeneratesValidBitmap() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val event = CountdownEvent(
            id = 2L,
            title = "AVENGERS: DOOMSDAY",
            targetDateTimeMillis = System.currentTimeMillis() + 86400000L * 47,
            accentColorHex = "#FF2E2E",
            fontStyle = "modern_bold",
            notes = "Final Incursion Protocol"
        )

        val tallBitmap = WidgetBitmapRenderer.renderWidgetBitmap(
            context = context,
            event = event,
            widthPx = 720,
            heightPx = 1100
        )

        assertNotNull(tallBitmap)
        assertTrue(tallBitmap.width >= 360)
        assertTrue(tallBitmap.height >= 500)
    }

    @Test
    fun testUserPreferencesCoinsAndUnlocks() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesRepository(context)

        // Starting coins should be at least 500
        assertTrue(prefs.coins.value >= 500)

        // Default unlocked items
        assertTrue(prefs.isItemUnlocked("doomsday_red"))
        assertTrue(prefs.isItemUnlocked("bg_doomsday_portal"))

        // Add coins
        val before = prefs.coins.value
        prefs.addCoins(100)
        assertEquals(before + 100, prefs.coins.value)

        // Spend coins
        val spent = prefs.spendCoins(50)
        assertTrue(spent)
        assertEquals(before + 50, prefs.coins.value)

        // Unlock item
        prefs.unlockItem("cyber_neon")
        assertTrue(prefs.isItemUnlocked("cyber_neon"))
    }
}
