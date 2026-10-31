package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.StoreCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("countdown_user_prefs", Context.MODE_PRIVATE)

    private val _coins = MutableStateFlow(getSavedCoins())
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _unlockedItems = MutableStateFlow(getSavedUnlockedItems())
    val unlockedItems: StateFlow<Set<String>> = _unlockedItems.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK_THEME, true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme(): Boolean {
        val next = !_isDarkTheme.value
        prefs.edit().putBoolean(KEY_DARK_THEME, next).apply()
        _isDarkTheme.value = next
        return next
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
    }

    private fun getSavedCoins(): Int {
        if (!prefs.contains(KEY_COINS)) {
            // First launch grant 500 coins
            prefs.edit().putInt(KEY_COINS, 500).apply()
            return 500
        }
        return prefs.getInt(KEY_COINS, 500)
    }

    private fun getSavedUnlockedItems(): Set<String> {
        val defaultUnlocked = StoreCatalog.items.filter { it.isDefaultUnlocked }.map { it.id }.toSet()
        val saved = prefs.getStringSet(KEY_UNLOCKED_ITEMS, null) ?: emptySet()
        return defaultUnlocked + saved
    }

    fun addCoins(amount: Int) {
        val current = _coins.value
        val updated = current + amount
        prefs.edit().putInt(KEY_COINS, updated).apply()
        _coins.value = updated
    }

    fun spendCoins(amount: Int): Boolean {
        val current = _coins.value
        if (current >= amount) {
            val updated = current - amount
            prefs.edit().putInt(KEY_COINS, updated).apply()
            _coins.value = updated
            return true
        }
        return false
    }

    fun unlockItem(itemId: String): Boolean {
        val currentSet = _unlockedItems.value.toMutableSet()
        currentSet.add(itemId)
        prefs.edit().putStringSet(KEY_UNLOCKED_ITEMS, currentSet).apply()
        _unlockedItems.value = currentSet
        return true
    }

    fun isItemUnlocked(itemId: String): Boolean {
        return _unlockedItems.value.contains(itemId)
    }

    fun canClaimDailyBonus(): Boolean {
        val lastClaim = prefs.getLong(KEY_LAST_CLAIM, 0L)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        return (now - lastClaim) >= oneDayMillis
    }

    fun claimDailyBonus(): Int {
        val bonus = 100
        addCoins(bonus)
        prefs.edit().putLong(KEY_LAST_CLAIM, System.currentTimeMillis()).apply()
        return bonus
    }

    companion object {
        private const val KEY_COINS = "user_coins"
        private const val KEY_UNLOCKED_ITEMS = "unlocked_store_items"
        private const val KEY_LAST_CLAIM = "last_daily_bonus_claim"
        private const val KEY_DARK_THEME = "is_dark_theme_enabled"
    }
}
