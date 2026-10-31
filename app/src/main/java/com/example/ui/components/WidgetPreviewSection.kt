package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CountdownEvent
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DoomsdayRed
import com.example.widget.CountdownWidgetProvider
import com.example.widget.WidgetBitmapRenderer

enum class WidgetPreviewSize(val label: String, val aspectRatio: Float) {
    COMPACT("2 x 1", 2.2f),
    MEDIUM("3 x 2", 1.6f),
    WIDE("4 x 2", 2.0f),
    FULL_TALL("4 x 5 Full Tall", 0.65f)
}

@Composable
fun WidgetPreviewSection(
    event: CountdownEvent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSize by remember { mutableStateOf(WidgetPreviewSize.FULL_TALL) }
    var previewBitmap by remember(event, selectedSize) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    LaunchedEffect(event, selectedSize) {
        val width = when (selectedSize) {
            WidgetPreviewSize.COMPACT -> 600
            WidgetPreviewSize.MEDIUM -> 720
            WidgetPreviewSize.WIDE -> 840
            WidgetPreviewSize.FULL_TALL -> 720
        }
        val height = when (selectedSize) {
            WidgetPreviewSize.COMPACT -> 280
            WidgetPreviewSize.MEDIUM -> 440
            WidgetPreviewSize.WIDE -> 420
            WidgetPreviewSize.FULL_TALL -> 1100
        }
        previewBitmap = WidgetBitmapRenderer.renderWidgetBitmap(
            context = context,
            event = event,
            widthPx = width,
            heightPx = height
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = DoomsdayRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Home Screen Widget Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Size Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Size:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WidgetPreviewSize.entries.forEach { size ->
                    FilterChip(
                        selected = selectedSize == size,
                        onClick = { selectedSize = size },
                        label = { Text(size.label) },
                        leadingIcon = {
                            Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DoomsdayRed.copy(alpha = 0.2f),
                            selectedLabelColor = DoomsdayRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Widget Canvas Preview
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(selectedSize.aspectRatio)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF070709))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Widget Live Preview",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "Rendering widget...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "Add to Home Screen" Pin Action
            Button(
                onClick = {
                    pinWidgetToHomeScreen(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DoomsdayRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_widget_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add This Widget to Home Screen", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun pinWidgetToHomeScreen(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(context, CountdownWidgetProvider::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
            Toast.makeText(context, "Requested widget placement on your home screen!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Long-press your home screen to add Doomsday Countdown widget", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "Long-press your home screen to add Doomsday Countdown widget", Toast.LENGTH_LONG).show()
    }
}
