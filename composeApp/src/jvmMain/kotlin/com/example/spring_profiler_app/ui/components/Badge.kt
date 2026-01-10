package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BadgeStyle {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    OUTLINED
}

@Composable
fun Badge(
    text: String,
    style: BadgeStyle = BadgeStyle.PRIMARY,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, borderColor) = when (style) {
        BadgeStyle.PRIMARY -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            null
        )

        BadgeStyle.SECONDARY -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            null
        )

        BadgeStyle.TERTIARY -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            null
        )

        BadgeStyle.OUTLINED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.outlineVariant
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.then(
            if (borderColor != null) {
                Modifier.border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                )
            } else Modifier
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScopeBadge(
    scope: String,
    modifier: Modifier = Modifier
) {
    val style = when (scope.lowercase()) {
        "singleton" -> BadgeStyle.PRIMARY
        "prototype" -> BadgeStyle.TERTIARY
        else -> BadgeStyle.SECONDARY
    }

    Badge(
        text = scope.uppercase(),
        style = style,
        modifier = modifier
    )
}

@Composable
fun UnitBadge(
    unit: String,
    modifier: Modifier = Modifier
) {
    Badge(
        text = unit.lowercase(),
        style = BadgeStyle.OUTLINED,
        modifier = modifier
    )
}

@Composable
fun EndpointContextBadges(
    endpoint: String,
    context: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Badge(text = endpoint, style = BadgeStyle.PRIMARY)
        Badge(text = context, style = BadgeStyle.SECONDARY)
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp
) {
    val statusColor = getStatusColor(status)

    Surface(
        color = statusColor.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

fun getStatusColor(status: String): Color = when (status.uppercase()) {
    "UP" -> Color(0xFF2E7D32)
    "DOWN" -> Color(0xFFD32F2F)
    "OUT_OF_SERVICE" -> Color(0xFFED6C02)
    else -> Color.Gray
}
