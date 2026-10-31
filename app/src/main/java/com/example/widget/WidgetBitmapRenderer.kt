package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.R
import com.example.model.CountdownEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object WidgetBitmapRenderer {

    fun renderWidgetBitmap(
        context: Context,
        event: CountdownEvent,
        widthPx: Int = 720,
        heightPx: Int = 420
    ): Bitmap {
        val w = max(300, widthPx)
        val h = max(200, heightPx)

        val isTallVertical = h > (w * 1.05f)

        return if (isTallVertical) {
            renderTallVerticalWidget(context, event, w, h)
        } else {
            renderHorizontalWidget(context, event, w, h)
        }
    }

    /**
     * Full-screen tall vertical widget occupying the full home screen (4x4, 4x5, 4x6).
     */
    private fun renderTallVerticalWidget(
        context: Context,
        event: CountdownEvent,
        w: Int,
        h: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Draw Background
        drawBackground(context, event, canvas, w, h)

        // 2. Deep Dark OLED Vignette Scrim
        val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    Color.argb(180, 0, 0, 0),
                    Color.argb(220, 0, 0, 2),
                    Color.argb(250, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrimPaint)

        val accentColor = try {
            Color.parseColor(event.accentColorHex)
        } catch (e: Exception) {
            Color.parseColor("#FF2E2E")
        }

        // 3. Ambient Top Glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.35f,
                Color.argb(60, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.35f, glowPaint)

        // Time calculations
        val now = System.currentTimeMillis()
        val diffMillis = (event.targetDateTimeMillis - now).coerceAtLeast(0)
        val totalSeconds = diffMillis / 1000L
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val padX = w * 0.08f
        val topY = h * 0.07f

        // 4. Header: Tactical Event Title & Status Badge
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(padX, topY, padX + (w * 0.36f), topY + (h * 0.035f))
        canvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)

        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = h * 0.018f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.2f
        }
        canvas.drawText("● COUNTDOWN LIVE", padX + 16f, topY + (h * 0.024f), badgeTextPaint)

        // Event Main Title (Solid, high-impact)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (h * 0.045f).coerceAtMost(w * 0.08f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val displayTitle = if (event.title.length > 20) event.title.take(18) + ".." else event.title
        canvas.drawText(displayTitle.uppercase(), padX, topY + (h * 0.075f), titlePaint)

        // 5. Massive Hero Days Section
        val daysCenterY = h * 0.28f
        val daysStr = String.format("%03d", days.coerceIn(0, 999))

        if (event.fontStyle == "dot_matrix") {
            drawDotMatrixDays(canvas, daysStr, padX, daysCenterY, h * 0.015f)
        } else {
            val heroDaysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = h * 0.14f
                typeface = getFontTypeface(event.fontStyle)
                letterSpacing = 0.04f
            }
            canvas.drawText(daysStr, padX, daysCenterY, heroDaysPaint)
        }

        val daysSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = h * 0.024f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.25f
        }
        canvas.drawText("DAYS REMAINING", padX, daysCenterY + (h * 0.045f), daysSubPaint)

        // 6. Time Breakdown Cards (HOURS, MINUTES, SECONDS in vertical glass panels)
        val boxWidth = (w - (padX * 2) - 32f) / 3f
        val boxHeight = h * 0.12f
        val boxY = daysCenterY + (h * 0.08f)

        drawTimeCapsule(canvas, padX, boxY, boxWidth, boxHeight, String.format("%02d", hours), "HOURS", accentColor)
        drawTimeCapsule(canvas, padX + boxWidth + 16f, boxY, boxWidth, boxHeight, String.format("%02d", minutes), "MINUTES", accentColor)
        drawTimeCapsule(canvas, padX + (boxWidth * 2) + 32f, boxY, boxWidth, boxHeight, String.format("%02d", seconds), "SECONDS", accentColor)

        // 7. Target Date Card
        val dateCardY = boxY + boxHeight + (h * 0.035f)
        val dateCardHeight = h * 0.09f
        val cardRect = RectF(padX, dateCardY, w - padX, dateCardY + dateCardHeight)

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 15, 15, 22)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 18f, 18f, cardBgPaint)

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(cardRect, 18f, 18f, cardBorderPaint)

        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val dateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = h * 0.022f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(dateFormat.format(Date(event.targetDateTimeMillis)), padX + 24f, dateCardY + (dateCardHeight * 0.44f), dateTextPaint)

        val timeSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 200, 200, 215)
            textSize = h * 0.018f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        canvas.drawText("Target: " + timeFormat.format(Date(event.targetDateTimeMillis)), padX + 24f, dateCardY + (dateCardHeight * 0.78f), timeSubPaint)

        // 8. Bottom Tactical Progress Bar
        val progressY = dateCardY + dateCardHeight + (h * 0.035f)
        val progressHeight = 8f
        val progressTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(padX, progressY, w - padX, progressY + progressHeight), 4f, 4f, progressTrackPaint)

        val fillWidth = ((w - (padX * 2)) * 0.65f) // dynamic or milestone pace
        val progressFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(padX, progressY, padX + fillWidth, progressY + progressHeight), 4f, 4f, progressFillPaint)

        if (event.notes.isNotEmpty()) {
            val notesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(160, 220, 220, 230)
                textSize = h * 0.019f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            }
            val shortNote = if (event.notes.length > 40) event.notes.take(38) + ".." else event.notes
            canvas.drawText("\"$shortNote\"", padX, progressY + (h * 0.045f), notesPaint)
        }

        return createRoundedBitmap(output, cornerRadius = w * 0.08f)
    }

    /**
     * Standard horizontal widget (2x1, 3x2, 4x2).
     */
    private fun renderHorizontalWidget(
        context: Context,
        event: CountdownEvent,
        w: Int,
        h: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Draw Background
        drawBackground(context, event, canvas, w, h)

        // 2. Pure Dark Scrim
        val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    Color.argb(170, 0, 0, 0),
                    Color.argb(200, 2, 2, 5),
                    Color.argb(245, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrimPaint)

        val accentColor = try {
            Color.parseColor(event.accentColorHex)
        } catch (e: Exception) {
            Color.parseColor("#FF2E2E")
        }

        // Time calculations
        val now = System.currentTimeMillis()
        val diffMillis = (event.targetDateTimeMillis - now).coerceAtLeast(0)
        val totalSeconds = diffMillis / 1000L
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val paddingLeft = w * 0.08f
        val paddingTop = h * 0.18f

        // 3. Header: Event Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = h * 0.09f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.2f
        }
        val displayTitle = if (event.title.length > 24) event.title.take(22) + ".." else event.title
        canvas.drawText(displayTitle.uppercase(), paddingLeft, paddingTop, titlePaint)

        // 4. Days and Time Numbers
        val daysStr = String.format("%03d", days.coerceIn(0, 999))
        val timeStr = String.format("%d:%02d:%02d", hours, minutes, seconds)
        val centerY = h * 0.60f

        if (event.fontStyle == "dot_matrix") {
            val dotRadius = h * 0.016f
            val dotSpacing = dotRadius * 2.5f
            var currentX = paddingLeft

            for (char in daysStr) {
                val digit = char - '0'
                if (digit in 0..9) {
                    val pattern = DIGIT_DOT_PATTERNS[digit]
                    drawDotMatrixGlyph(canvas, pattern, currentX, centerY - (7 * dotSpacing / 2), dotRadius, dotSpacing)
                    currentX += (5 * dotSpacing) + (dotSpacing * 1.5f)
                }
            }

            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = h * 0.22f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.05f
            }
            canvas.drawText(timeStr, currentX + (w * 0.04f), centerY + (h * 0.07f), timePaint)
        } else {
            // Modern Sleek Solid Font (Default)
            val fontTypeface = getFontTypeface(event.fontStyle)

            val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = h * 0.36f
                typeface = fontTypeface
                letterSpacing = 0.03f
            }
            canvas.drawText(daysStr, paddingLeft, centerY + (h * 0.08f), daysPaint)
            val daysWidth = daysPaint.measureText(daysStr)

            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E6E6E6")
                textSize = h * 0.22f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.05f
            }
            canvas.drawText(timeStr, paddingLeft + daysWidth + (w * 0.05f), centerY + (h * 0.05f), timePaint)
        }

        // 5. "DAYS" Label below
        val daysLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B0B0C0")
            textSize = h * 0.075f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.3f
        }
        canvas.drawText("DAYS", paddingLeft, h * 0.88f, daysLabelPaint)

        return createRoundedBitmap(output, cornerRadius = w * 0.07f)
    }

    private fun drawBackground(context: Context, event: CountdownEvent, canvas: Canvas, w: Int, h: Int) {
        var bgBitmap: Bitmap? = null
        if (event.backgroundType == "custom_photo" && event.backgroundResourceOrUri.isNotEmpty()) {
            val file = File(event.backgroundResourceOrUri)
            if (file.exists()) {
                bgBitmap = BitmapFactory.decodeFile(file.absolutePath)
            }
        }

        if (bgBitmap == null) {
            val resId = when (event.backgroundResourceOrUri) {
                "bg_cyber_neon" -> R.drawable.bg_cyber_neon
                "bg_doomsday_portal" -> R.drawable.bg_doomsday_portal
                else -> R.drawable.bg_doomsday_portal
            }
            try {
                bgBitmap = BitmapFactory.decodeResource(context.resources, resId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (bgBitmap != null) {
            val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
            val dstRect = Rect(0, 0, w, h)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
        } else {
            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, h.toFloat(),
                    Color.parseColor("#0C0C12"),
                    Color.parseColor("#000000"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), gradientPaint)
        }
    }

    private fun drawTimeCapsule(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        value: String,
        label: String,
        accent: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)

        // Glass background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 15, 15, 22)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)

        // Glass border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(35, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, 14f, 14f, borderPaint)

        // Value text (solid bold)
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = h * 0.44f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(value, x + (w / 2f), y + (h * 0.52f), valPaint)

        // Label text
        val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 200, 200, 215)
            textSize = h * 0.22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText(label, x + (w / 2f), y + (h * 0.85f), lblPaint)
    }

    private fun getFontTypeface(fontStyle: String): Typeface {
        return when (fontStyle) {
            "cinematic" -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            "digital_led" -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            "scifi_mono" -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            "dot_matrix" -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            else -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) // modern_bold default
        }
    }

    private fun drawDotMatrixDays(canvas: Canvas, daysStr: String, startX: Float, centerY: Float, radius: Float) {
        val spacing = radius * 2.5f
        var currentX = startX
        for (char in daysStr) {
            val digit = char - '0'
            if (digit in 0..9) {
                val pattern = DIGIT_DOT_PATTERNS[digit]
                drawDotMatrixGlyph(canvas, pattern, currentX, centerY - (7 * spacing / 2), radius, spacing)
                currentX += (5 * spacing) + (spacing * 1.5f)
            }
        }
    }

    private fun drawDotMatrixGlyph(canvas: Canvas, pattern: BooleanArray, startX: Float, startY: Float, radius: Float, spacing: Float) {
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        var idx = 0
        for (row in 0 until 7) {
            for (col in 0 until 5) {
                if (pattern[idx]) {
                    val cx = startX + col * spacing
                    val cy = startY + row * spacing
                    canvas.drawCircle(cx, cy, radius, dotPaint)
                }
                idx++
            }
        }
    }

    private fun createRoundedBitmap(src: Bitmap, cornerRadius: Float): Bitmap {
        val rounded = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rounded)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rectF = RectF(0f, 0f, src.width.toFloat(), src.height.toFloat())

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return rounded
    }

    private val DIGIT_DOT_PATTERNS = arrayOf(
        booleanArrayOf(false, true, true, true, false, true, false, false, false, true, true, false, false, true, true, true, false, true, false, true, true, true, false, false, true, true, false, false, false, true, false, true, true, true, false),
        booleanArrayOf(false, false, true, false, false, false, true, true, false, false, false, false, true, false, false, false, false, true, false, false, false, false, true, false, false, false, false, true, false, false, false, true, true, true, false),
        booleanArrayOf(false, true, true, true, false, true, false, false, false, true, false, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, true, true, true, true),
        booleanArrayOf(false, true, true, true, false, true, false, false, false, true, false, false, false, false, true, false, false, true, true, false, false, false, false, false, true, true, false, false, false, true, false, true, true, true, false),
        booleanArrayOf(false, false, false, true, false, false, false, true, true, false, false, true, false, true, false, true, false, false, true, false, true, true, true, true, true, false, false, false, true, false, false, false, false, true, false),
        booleanArrayOf(true, true, true, true, true, true, false, false, false, false, true, true, true, true, false, false, false, false, false, true, false, false, false, false, true, true, false, false, false, true, false, true, true, true, false),
        booleanArrayOf(false, true, true, true, false, true, false, false, false, false, true, true, true, true, false, true, false, false, false, true, true, false, false, false, true, true, false, false, false, true, false, true, true, true, false),
        booleanArrayOf(true, true, true, true, true, false, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, false, true, false, false, false, false, true, false, false, false),
        booleanArrayOf(false, true, true, true, false, true, false, false, false, true, true, false, false, false, true, false, true, true, true, false, true, false, false, false, true, true, false, false, false, true, false, true, true, true, false),
        booleanArrayOf(false, true, true, true, false, true, false, false, false, true, true, false, false, false, true, false, true, true, true, true, false, false, false, false, true, true, false, false, false, true, false, true, true, true, false)
    )
}
