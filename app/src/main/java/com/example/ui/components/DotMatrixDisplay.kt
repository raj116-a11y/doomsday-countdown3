package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 5 columns x 7 rows dot matrix representation for 0..9 and colon
private val DOT_MAP = mapOf(
    '0' to booleanArrayOf(
        false, true, true, true, false,
        true, false, false, false, true,
        true, false, false, true, true,
        true, false, true, false, true,
        true, true, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false
    ),
    '1' to booleanArrayOf(
        false, false, true, false, false,
        false, true, true, false, false,
        false, false, true, false, false,
        false, false, true, false, false,
        false, false, true, false, false,
        false, false, true, false, false,
        false, true, true, true, false
    ),
    '2' to booleanArrayOf(
        false, true, true, true, false,
        true, false, false, false, true,
        false, false, false, false, true,
        false, false, false, true, false,
        false, false, true, false, false,
        false, true, false, false, false,
        true, true, true, true, true
    ),
    '3' to booleanArrayOf(
        false, true, true, true, false,
        true, false, false, false, true,
        false, false, false, false, true,
        false, false, true, true, false,
        false, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false
    ),
    '4' to booleanArrayOf(
        false, false, false, true, false,
        false, false, true, true, false,
        false, true, false, true, false,
        true, false, false, true, false,
        true, true, true, true, true,
        false, false, false, true, false,
        false, false, false, true, false
    ),
    '5' to booleanArrayOf(
        true, true, true, true, true,
        true, false, false, false, false,
        true, true, true, true, false,
        false, false, false, false, true,
        false, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false
    ),
    '6' to booleanArrayOf(
        false, true, true, true, false,
        true, false, false, false, false,
        true, true, true, true, false,
        true, false, false, false, true,
        true, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false
    ),
    '7' to booleanArrayOf(
        true, true, true, true, true,
        false, false, false, false, true,
        false, false, false, true, false,
        false, false, true, false, false,
        false, true, false, false, false,
        false, true, false, false, false,
        false, true, false, false, false
    ),
    '8' to booleanArrayOf(
        false, true, true, true, false,
        true, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false,
        true, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false
    ),
    '9' to booleanArrayOf(
        false, true, true, true, false,
        true, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, true,
        false, false, false, false, true,
        true, false, false, false, true,
        false, true, true, true, false
    ),
    ':' to booleanArrayOf(
        false, false, false, false, false,
        false, false, true, false, false,
        false, false, false, false, false,
        false, false, false, false, false,
        false, false, true, false, false,
        false, false, false, false, false,
        false, false, false, false, false
    )
)

@Composable
fun DotMatrixDigit(
    char: Char,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.08f),
    dotSize: Dp = 4.dp,
    dotSpacing: Dp = 2.dp
) {
    val pattern = DOT_MAP[char] ?: BooleanArray(35) { false }
    val width = (dotSize * 5) + (dotSpacing * 4)
    val height = (dotSize * 7) + (dotSpacing * 6)

    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        val r = dotSize.toPx() / 2f
        val space = dotSpacing.toPx()
        val step = dotSize.toPx() + space

        var idx = 0
        for (row in 0 until 7) {
            for (col in 0 until 5) {
                val isActive = pattern[idx]
                val cx = col * step + r
                val cy = row * step + r
                drawCircle(
                    color = if (isActive) activeColor else inactiveColor,
                    radius = r,
                    center = Offset(cx, cy)
                )
                idx++
            }
        }
    }
}

@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.08f),
    dotSize: Dp = 4.dp,
    dotSpacing: Dp = 2.dp,
    charSpacing: Dp = 6.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            DotMatrixDigit(
                char = char,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotSize = dotSize,
                dotSpacing = dotSpacing
            )
            if (index < text.length - 1) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(charSpacing))
            }
        }
    }
}
