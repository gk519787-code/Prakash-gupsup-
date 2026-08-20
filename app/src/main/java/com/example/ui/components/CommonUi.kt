package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

fun getPaletteColor(index: Int): Color {
    return MediaColorPalette[index % MediaColorPalette.size]
}

fun parseColorHex(hex: String, defaultColor: Color = GupSupBlue): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun GupSupAvatar(
    name: String,
    colorIndex: Int,
    size: Dp = 44.dp,
    hasActiveStory: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val baseModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = false, radius = size / 2),
            onClick = onClick
        )
    } else Modifier

    Box(
        contentAlignment = Alignment.Center,
        modifier = baseModifier
            .size(if (hasActiveStory) size + 8.dp else size)
            .then(
                if (hasActiveStory) {
                    Modifier
                        .border(1.5.dp, GupSupRed, CircleShape)
                        .padding(3.dp)
                } else Modifier
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(getPaletteColor(colorIndex))
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = GupSupText,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}

@Composable
fun FrameGridOverlay(
    modifier: Modifier = Modifier,
    lineColor: Color = GupSupText.copy(alpha = 0.12f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Draw grid lines
        drawLine(lineColor, Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth = 1f)
        drawLine(lineColor, Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), strokeWidth = 1f)
        drawLine(lineColor, Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
        drawLine(lineColor, Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
    }
}

@Composable
fun GupSupIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = GupSupText,
    badgeCount: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .size(44.dp)
            .border(1.dp, GupSupLine, CircleShape)
            .background(GupSupSurface, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(16.dp)
                        .background(GupSupRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OverlineTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = GupSupBlue
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}
