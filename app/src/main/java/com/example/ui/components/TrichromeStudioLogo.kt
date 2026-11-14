package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAdaptiveThemeSpec

// Trichrome Color Palette
val TrichromeEmerald = Color(0xFF10B981) // Neon Emerald
val TrichromeCyan = Color(0xFF06B6D4)    // Electric Cyan
val TrichromeAmber = Color(0xFFF59E0B)   // Warm Amber
val TrichromeObsidian = Color(0xFF0F172A)

/**
 * Minimal, aesthetic Trichrome logo featuring interlocking geometric code facets
 * with vibrant Neon Emerald, Electric Cyan, and Warm Amber chromatic accents.
 */
@Composable
fun TrichromeStudioLogo(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 38.dp,
    showGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "trichrome_spin")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(sizeDp * 0.28f))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF18181B),
                        Color(0xFF09090B)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TrichromeCyan.copy(alpha = 0.7f),
                        TrichromeEmerald.copy(alpha = 0.5f),
                        TrichromeAmber.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(sizeDp * 0.28f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp * 0.76f)) {
            val w = size.width
            val h = size.height

            // 1. Warm Amber Facet (Left bracket facet)
            val leftBracket = Path().apply {
                moveTo(w * 0.38f, h * 0.18f)
                lineTo(w * 0.16f, h * 0.50f)
                lineTo(w * 0.38f, h * 0.82f)
            }
            drawPath(
                path = leftBracket,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TrichromeAmber.copy(alpha = pulseAlpha),
                        Color(0xFFD97706)
                    )
                ),
                style = Stroke(
                    width = w * 0.13f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 2. Electric Cyan Facet (Right bracket facet)
            val rightBracket = Path().apply {
                moveTo(w * 0.62f, h * 0.18f)
                lineTo(w * 0.84f, h * 0.50f)
                lineTo(w * 0.62f, h * 0.82f)
            }
            drawPath(
                path = rightBracket,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TrichromeCyan.copy(alpha = pulseAlpha),
                        Color(0xFF0284C7)
                    )
                ),
                style = Stroke(
                    width = w * 0.13f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 3. Neon Emerald Core (Dynamic central slash/spark)
            val centerSlash = Path().apply {
                moveTo(w * 0.60f, h * 0.22f)
                lineTo(w * 0.40f, h * 0.78f)
            }
            drawPath(
                path = centerSlash,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TrichromeEmerald,
                        Color(0xFF34D399)
                    )
                ),
                style = Stroke(
                    width = w * 0.12f,
                    cap = StrokeCap.Round
                )
            )

            // Center spark
            drawCircle(
                color = Color.White.copy(alpha = pulseAlpha * 0.9f),
                radius = w * 0.065f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }
    }
}

/**
 * Top-bar brand banner combining the Trichrome studio mark, single-line title, and adaptive badge.
 */
@Composable
fun TrichromeBrandHeader(
    appName: String,
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = LocalAdaptiveThemeSpec.current.showBrandSubtitle,
    compact: Boolean = LocalAdaptiveThemeSpec.current.useCompactTopActions
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        TrichromeStudioLogo(sizeDp = if (compact) 30.dp else 36.dp)

        Spacer(modifier = Modifier.width(if (compact) 8.dp else 10.dp))

        androidx.compose.foundation.layout.Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                // Trichrome micro badge (hidden on ultra-compact header to keep title crisp)
                if (!compact) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        TrichromeEmerald.copy(alpha = 0.2f),
                                        TrichromeCyan.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .border(
                                0.8.dp,
                                TrichromeCyan.copy(alpha = 0.45f),
                                RoundedCornerShape(5.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "KtVM • M3",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrichromeCyan,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (showSubtitle && !compact) {
                Text(
                    text = "On-Device Kotlin Compiler & Adaptive IDE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
