package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
