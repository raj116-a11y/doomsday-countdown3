package com.example.model

enum class StoreCategoryType {
    WIDGET_THEME,
    BACKGROUND,
    LAYOUT_STYLE
}

data class StoreItem(
    val id: String,
    val title: String,
    val description: String,
    val type: StoreCategoryType,
    val coinCost: Int,
    val isDefaultUnlocked: Boolean = false,
    val previewDrawableName: String = "",
    val accentColorHex: String = "#FF3333",
    val secondaryColorHex: String = "#00FFCC",
    val badgeText: String = ""
)

object StoreCatalog {
    val items = listOf(
        // Themes
        StoreItem(
            id = "doomsday_red",
            title = "Avengers Doomsday",
            description = "Menacing red & amber dot-matrix display with dark tactical border.",
            type = StoreCategoryType.WIDGET_THEME,
            coinCost = 0,
            isDefaultUnlocked = true,
            previewDrawableName = "bg_doomsday_portal",
            accentColorHex = "#FF2E2E",
            secondaryColorHex = "#FF8C00",
            badgeText = "FEATURED"
        ),
        StoreItem(
            id = "matrix_green",
            title = "Quantum Matrix",
            description = "Luminous emerald green glow with digital terminal typography.",
            type = StoreCategoryType.WIDGET_THEME,
            coinCost = 150,
            isDefaultUnlocked = true,
            previewDrawableName = "bg_doomsday_portal",
            accentColorHex = "#00FF66",
            secondaryColorHex = "#008833",
            badgeText = "POPULAR"
        ),
        StoreItem(
            id = "cyber_neon",
            title = "Cyberpunk 2099",
            description = "Electrifying violet, hot pink, and cyan holographic aesthetics.",
            type = StoreCategoryType.WIDGET_THEME,
            coinCost = 250,
            isDefaultUnlocked = false,
            previewDrawableName = "bg_cyber_neon",
            accentColorHex = "#00F0FF",
            secondaryColorHex = "#FF007F",
            badgeText = "PREMIUM"
        ),
        StoreItem(
            id = "midnight_oled",
            title = "Midnight OLED",
            description = "Ultra sleek pure pitch black with titanium white numbers and subtle glow.",
            type = StoreCategoryType.WIDGET_THEME,
            coinCost = 100,
            isDefaultUnlocked = true,
            previewDrawableName = "",
            accentColorHex = "#FFFFFF",
            secondaryColorHex = "#888888",
            badgeText = "BATTERY SAVER"
        ),
        StoreItem(
            id = "solar_flare",
            title = "Solar Flare",
            description = "Warm energetic sunset orange and golden yellow gradient glow.",
            type = StoreCategoryType.WIDGET_THEME,
            coinCost = 200,
            isDefaultUnlocked = false,
            previewDrawableName = "",
            accentColorHex = "#FF9900",
            secondaryColorHex = "#FF3300",
            badgeText = "EXCLUSIVE"
        ),
        StoreItem(
            id = "holographic_titan",
            title = "Holographic Titan",
            description = "Multi-spectrum prismatic cyber glass with animated shift effects.",
            type = StoreCategoryType.WIDGET_THEME,
            coinCost = 350,
            isDefaultUnlocked = false,
            previewDrawableName = "bg_cyber_neon",
            accentColorHex = "#7B2CBF",
            secondaryColorHex = "#00F5D4",
            badgeText = "PRO"
        ),

        // Backgrounds
        StoreItem(
            id = "bg_doomsday_portal",
            title = "Doomsday Portal 4K",
            description = "Swirling dimensional rift with ancient mechanical arches.",
            type = StoreCategoryType.BACKGROUND,
            coinCost = 0,
            isDefaultUnlocked = true,
            previewDrawableName = "bg_doomsday_portal",
            accentColorHex = "#FF3333",
            badgeText = "DEFAULT"
        ),
        StoreItem(
            id = "bg_cyber_neon",
            title = "Neo-Kyoto Skyline",
            description = "Towering cyberpunk megastructure with neon dragon holograms.",
            type = StoreCategoryType.BACKGROUND,
            coinCost = 200,
            isDefaultUnlocked = false,
            previewDrawableName = "bg_cyber_neon",
            accentColorHex = "#9D4EDD",
            badgeText = "NEW"
        ),
        StoreItem(
            id = "bg_cosmic_void",
            title = "Deep Cosmic Void",
            description = "Infinite starlight and glowing stellar nebula clouds.",
            type = StoreCategoryType.BACKGROUND,
            coinCost = 150,
            isDefaultUnlocked = false,
            previewDrawableName = "",
            accentColorHex = "#4361EE",
            badgeText = "STAFF PICK"
        ),
        StoreItem(
            id = "bg_crimson_nebula",
            title = "Blood Moon Protocol",
            description = "Dramatic lunar eclipse with deep crimson dust clouds.",
            type = StoreCategoryType.BACKGROUND,
            coinCost = 250,
            isDefaultUnlocked = false,
            previewDrawableName = "",
            accentColorHex = "#D90429",
            badgeText = "LEGENDARY"
        ),

        // Layout Styles
        StoreItem(
            id = "layout_dot_split",
            title = "Split Dot-Matrix",
            description = "Screenshot-inspired layout: Massive dot-matrix days on left, digital time on right.",
            type = StoreCategoryType.LAYOUT_STYLE,
            coinCost = 0,
            isDefaultUnlocked = true,
            accentColorHex = "#FF3333",
            badgeText = "SCREENSHOT MATCH"
        ),
        StoreItem(
            id = "layout_ring_gauge",
            title = "Circular Tech Gauge",
            description = "Tactical ring meter showing percentage of target time elapsed with digital center.",
            type = StoreCategoryType.LAYOUT_STYLE,
            coinCost = 200,
            isDefaultUnlocked = false,
            accentColorHex = "#00FFCC",
            badgeText = "POPULAR"
        ),
        StoreItem(
            id = "layout_giant_hero",
            title = "Giant Countdown Hero",
            description = "Full-bleed massive day counter designed for prominent 4x2 and 4x4 home screens.",
            type = StoreCategoryType.LAYOUT_STYLE,
            coinCost = 150,
            isDefaultUnlocked = true,
            accentColorHex = "#FF9900",
            badgeText = "CLEAN"
        ),
        StoreItem(
            id = "layout_minimal_grid",
            title = "Minimalist Grid",
            description = "Clean segmented boxes for Days, Hours, Minutes, and Seconds with sharp contrast.",
            type = StoreCategoryType.LAYOUT_STYLE,
            coinCost = 180,
            isDefaultUnlocked = false,
            accentColorHex = "#FFFFFF",
            badgeText = "MINIMAL"
        )
    )
}
