package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Modern floating mesh particles with a subtle gradient pulse background animation.
 * Recreates high-end developer website hero backgrounds with lightweight 60fps Canvas math.
 */
private data class MeshParticleNode(
    val baseNormalizedX: Float,
    val baseNormalizedY: Float,
    val orbitRadius: Float,
    val speedMultiplier: Float,
    val phaseOffset: Float,
    val radiusDp: Float
)

@Composable
fun ModernFloatingMeshBackground(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.background

    // Deterministic particle positions for beautiful spatial distribution
    val particles = remember {
        listOf(
            MeshParticleNode(0.08f, 0.12f, 0.045f, 0.8f, 0.2f, 3.5f),
            MeshParticleNode(0.25f, 0.08f, 0.035f, 1.2f, 1.4f, 2.8f),
            MeshParticleNode(0.42f, 0.18f, 0.050f, 0.9f, 2.7f, 3.2f),
            MeshParticleNode(0.68f, 0.10f, 0.040f, 1.1f, 0.8f, 3.0f),
            MeshParticleNode(0.88f, 0.15f, 0.042f, 0.7f, 3.5f, 2.5f),

            MeshParticleNode(0.15f, 0.32f, 0.048f, 1.0f, 4.1f, 3.0f),
            MeshParticleNode(0.35f, 0.38f, 0.038f, 0.85f, 1.9f, 4.0f),
            MeshParticleNode(0.55f, 0.28f, 0.052f, 1.15f, 5.2f, 2.6f),
            MeshParticleNode(0.82f, 0.36f, 0.044f, 0.95f, 0.5f, 3.4f),

            MeshParticleNode(0.06f, 0.58f, 0.036f, 1.3f, 2.1f, 2.5f),
            MeshParticleNode(0.28f, 0.52f, 0.046f, 0.75f, 3.8f, 3.6f),
            MeshParticleNode(0.48f, 0.62f, 0.042f, 1.05f, 1.1f, 3.0f),
            MeshParticleNode(0.72f, 0.54f, 0.050f, 0.9f, 4.7f, 3.8f),
            MeshParticleNode(0.92f, 0.64f, 0.039f, 1.25f, 2.9f, 2.8f),

            MeshParticleNode(0.18f, 0.78f, 0.044f, 0.8f, 0.4f, 3.2f),
            MeshParticleNode(0.38f, 0.84f, 0.052f, 1.1f, 3.3f, 2.7f),
            MeshParticleNode(0.62f, 0.76f, 0.041f, 0.95f, 5.0f, 3.5f),
            MeshParticleNode(0.85f, 0.88f, 0.048f, 1.2f, 1.7f, 3.0f),

            MeshParticleNode(0.10f, 0.94f, 0.035f, 1.0f, 2.5f, 2.5f),
            MeshParticleNode(0.50f, 0.95f, 0.045f, 0.85f, 4.2f, 3.4f)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mesh_anim")
    val timeProgression by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_prog"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Subtle radial gradient glow pulse in the background
        val pulseCenter1 = Offset(width * 0.22f, height * 0.18f)
        val pulseCenter2 = Offset(width * 0.78f, height * 0.72f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = pulseGlow * 0.45f),
                    primaryColor.copy(alpha = pulseGlow * 0.15f),
                    Color.Transparent
                ),
                center = pulseCenter1,
                radius = width * 0.65f
            ),
            radius = width * 0.65f,
            center = pulseCenter1
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = pulseGlow * 0.35f),
                    tertiaryColor.copy(alpha = pulseGlow * 0.10f),
                    Color.Transparent
                ),
                center = pulseCenter2,
                radius = width * 0.55f
            ),
            radius = width * 0.55f,
            center = pulseCenter2
        )

        // 2. Compute live orbital positions for all nodes
        val livePositions = particles.map { p ->
            val angle = (timeProgression * p.speedMultiplier) + p.phaseOffset
            val x = (p.baseNormalizedX + cos(angle) * p.orbitRadius) * width
            val y = (p.baseNormalizedY + sin(angle) * p.orbitRadius) * height
            Offset(x, y)
        }

        // 3. Draw connecting bezier filaments between nodes within connection distance
        val maxDist = width * 0.28f
        val maxDistSq = maxDist * maxDist

        for (i in livePositions.indices) {
            val p1 = livePositions[i]
            for (j in i + 1 until livePositions.size) {
                val p2 = livePositions[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distSq = dx * dx + dy * dy

                if (distSq < maxDistSq) {
                    val dist = sqrt(distSq)
                    val proximity = 1f - (dist / maxDist)
                    val lineAlpha = (proximity * proximity * 0.22f).coerceIn(0.01f, 0.26f)

                    drawLine(
                        color = primaryColor.copy(alpha = lineAlpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.2f * proximity + 0.4f
                    )
                }
            }
        }

        // 4. Draw glowing nodes
        for (i in livePositions.indices) {
            val pos = livePositions[i]
            val node = particles[i]
            val nodeColor = if (i % 2 == 0) primaryColor else secondaryColor

            // Ambient node halo
            drawCircle(
                color = nodeColor.copy(alpha = 0.25f),
                radius = node.radiusDp * 2.8f,
                center = pos
            )
            // Core node
            drawCircle(
                color = nodeColor.copy(alpha = 0.85f),
                radius = node.radiusDp,
                center = pos
            )
        }
    }
}
