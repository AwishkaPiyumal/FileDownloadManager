package com.piumal.filedownloadmanager.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * Custom Toggle Switch Component
 *
 * A custom designed toggle switch that matches the app's design language.
 * Design specifications:
 * - Track: 56x22dp, rounded corners (20dp radius)
 * - Thumb: 30x30dp circular with shadow
 * - Off state: Gray track (#CCCCCC), thumb at left
 * - On state: Green track, thumb at right
 *
 * @param checked Whether the toggle is in the ON state
 * @param onCheckedChange Callback when the toggle state changes
 * @param modifier Modifier for styling
 * @param enabled Whether the toggle is enabled for interaction
 */
@Composable
fun CustomToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Track dimensions
    val trackWidth = 56.dp
    val trackHeight = 22.dp
    val trackCornerRadius = 20.dp

    // Thumb dimensions
    val thumbSize = 30.dp

    // Animation for thumb position
    // When off: thumb at left (0dp)
    // When on: thumb at right (trackWidth - thumbSize = 26dp)
    val thumbPositionX by animateDpAsState(
        targetValue = if (checked) (trackWidth - thumbSize) else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumbPosition"
    )

    // Animation for track fill (simulating the box-shadow inset effect)
    val trackFillProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "trackFill"
    )

    // Colors
    val trackColorOff = Color(0xFFCCCCCC)
    val trackColorOn = MaterialTheme.colorScheme.primary
    val thumbColor = Color.White
    val disabledAlpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .size(width = trackWidth, height = thumbSize)
            .graphicsLayer { alpha = disabledAlpha }
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track background (gray base)
        Box(
            modifier = Modifier
                .size(width = trackWidth, height = trackHeight)
                .clip(RoundedCornerShape(trackCornerRadius))
                .background(trackColorOff)
                .align(Alignment.Center)
        )

        // Track fill overlay (green, animated width)
        Box(
            modifier = Modifier
                .size(
                    width = trackWidth * trackFillProgress,
                    height = trackHeight
                )
                .clip(RoundedCornerShape(trackCornerRadius))
                .background(trackColorOn)
                .align(Alignment.CenterStart)
        )

        // Thumb (circular with shadow)
        Box(
            modifier = Modifier
                .offset(x = thumbPositionX)
                .size(thumbSize)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomToggleSwitchOffPreview() {
    FileDownloadManagerTheme {
        CustomToggleSwitch(
            checked = false,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomToggleSwitchOnPreview() {
    FileDownloadManagerTheme {
        CustomToggleSwitch(
            checked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomToggleSwitchDisabledPreview() {
    FileDownloadManagerTheme {
        CustomToggleSwitch(
            checked = false,
            onCheckedChange = {},
            enabled = false
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CustomToggleSwitchDarkPreview() {
    FileDownloadManagerTheme {
        CustomToggleSwitch(
            checked = true,
            onCheckedChange = {}
        )
    }
}

