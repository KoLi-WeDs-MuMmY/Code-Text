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
 * Antigravity floating particles and Google AI Studio mesh gradient background animation.
 * Recreates high-end developer website hero backgrounds with lightweight 60fps Canvas math.
 */
private data class AntigravityParticle(
    val initialX: Float, // Normalized 0..1
    val initialY: Float, // Normalized 0..1
    val speed: Float,    // Vertical upward drift rate
    val waveAmplitude: Float, // Lateral sine wave sway
    val waveFrequency: Float,
    val phase: Float,
    val radius: Float,
    val colorIndex: Int // 0: Emerald, 1: Cyan, 2: Amber
)

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

    // Trichrome palette constants
    val emeraldColor = Color(0xFF10B981)
    val cyanColor = Color(0xFF06B6D4)
    val amberColor = Color(0xFFF59E0B)

    // Antigravity particles rising upwards
    val antigravityParticles = remember {
        listOf(
            AntigravityParticle(0.06f, 0.95f, 0.08f, 0.025f, 2.5f, 0.2f, 2.8f, 0),
            AntigravityParticle(0.18f, 0.85f, 0.06f, 0.030f, 3.0f, 1.1f, 3.5f, 1),
            AntigravityParticle(0.32f, 0.70f, 0.09f, 0.020f, 2.0f, 2.4f, 2.2f, 2),
            AntigravityParticle(0.48f, 0.90f, 0.07f, 0.035f, 3.5f, 0.8f, 3.0f, 0),
            AntigravityParticle(0.64f, 0.60f, 0.08f, 0.022f, 2.2f, 3.1f, 2.6f, 1),
            AntigravityParticle(0.78f, 0.82f, 0.05f, 0.028f, 2.8f, 1.9f, 3.2f, 2),
            AntigravityParticle(0.90f, 0.75f, 0.10f, 0.018f, 1.8f, 4.2f, 2.0f, 0),
            AntigravityParticle(0.12f, 0.40f, 0.07f, 0.032f, 3.2f, 2.8f, 2.4f, 1),
            AntigravityParticle(0.26f, 0.25f, 0.06f, 0.024f, 2.6f, 0.5f, 3.4f, 2),
            AntigravityParticle(0.58f, 0.35f, 0.09f, 0.029f, 3.1f, 3.7f, 2.8f, 0),
            AntigravityParticle(0.72f, 0.15f, 0.05f, 0.021f, 2.1f, 1.4f, 3.0f, 1),
            AntigravityParticle(0.86f, 0.30f, 0.08f, 0.027f, 2.7f, 4.9f, 2.5f, 2),
            AntigravityParticle(0.40f, 0.50f, 0.07f, 0.030f, 3.0f, 1.6f, 3.2f, 1),
            AntigravityParticle(0.95f, 0.92f, 0.06f, 0.025f, 2.4f, 2.1f, 2.8f, 0)
        )
    }

    // Deterministic particle positions for mesh synaptic connections
    val meshNodes = remember {
        listOf(
            MeshParticleNode(0.10f, 0.14f, 0.045f, 0.8f, 0.2f, 3.5f),
            MeshParticleNode(0.28f, 0.09f, 0.035f, 1.2f, 1.4f, 2.8f),
            MeshParticleNode(0.44f, 0.20f, 0.050f, 0.9f, 2.7f, 3.2f),
            MeshParticleNode(0.70f, 0.12f, 0.040f, 1.1f, 0.8f, 3.0f),
            MeshParticleNode(0.90f, 0.18f, 0.042f, 0.7f, 3.5f, 2.5f),

            MeshParticleNode(0.18f, 0.35f, 0.048f, 1.0f, 4.1f, 3.0f),
            MeshParticleNode(0.38f, 0.40f, 0.038f, 0.85f, 1.9f, 4.0f),
            MeshParticleNode(0.58f, 0.30f, 0.052f, 1.15f, 5.2f, 2.6f),
            MeshParticleNode(0.84f, 0.38f, 0.044f, 0.95f, 0.5f, 3.4f),

            MeshParticleNode(0.08f, 0.60f, 0.036f, 1.3f, 2.1f, 2.5f),
            MeshParticleNode(0.30f, 0.54f, 0.046f, 0.75f, 3.8f, 3.6f),
            MeshParticleNode(0.52f, 0.64f, 0.042f, 1.05f, 1.1f, 3.0f),
            MeshParticleNode(0.75f, 0.56f, 0.050f, 0.9f, 4.7f, 3.8f),
            MeshParticleNode(0.93f, 0.66f, 0.039f, 1.25f, 2.9f, 2.8f),

            MeshParticleNode(0.20f, 0.80f, 0.044f, 0.8f, 0.4f, 3.2f),
            MeshParticleNode(0.42f, 0.86f, 0.052f, 1.1f, 3.3f, 2.7f),
            MeshParticleNode(0.66f, 0.78f, 0.041f, 0.95f, 5.0f, 3.5f),
            MeshParticleNode(0.88f, 0.90f, 0.048f, 1.2f, 1.7f, 3.0f)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mesh_anim")

    // Continuous 0..1 progression for upward antigravity flow
    val floatFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "antigravity_flow"
    )

    // Slow orbital rotation
    val orbitalProgression by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_prog"
    )

    // Breathing glow pulse
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Google AI Studio style ambient chromatic mesh gradient blooms
        val bloomCenter1 = Offset(width * 0.20f, height * 0.22f)
        val bloomCenter2 = Offset(width * 0.82f, height * 0.68f)
        val bloomCenter3 = Offset(width * 0.48f, height * 0.88f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cyanColor.copy(alpha = pulseGlow * 0.32f),
                    cyanColor.copy(alpha = pulseGlow * 0.10f),
                    Color.Transparent
                ),
                center = bloomCenter1,
                radius = width * 0.60f
            ),
            radius = width * 0.60f,
            center = bloomCenter1
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    emeraldColor.copy(alpha = pulseGlow * 0.28f),
                    emeraldColor.copy(alpha = pulseGlow * 0.08f),
                    Color.Transparent
                ),
                center = bloomCenter2,
                radius = width * 0.55f
            ),
            radius = width * 0.55f,
            center = bloomCenter2
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    amberColor.copy(alpha = pulseGlow * 0.22f),
                    amberColor.copy(alpha = pulseGlow * 0.05f),
                    Color.Transparent
                ),
                center = bloomCenter3,
                radius = width * 0.50f
            ),
            radius = width * 0.50f,
            center = bloomCenter3
        )

        // 2. Synaptic Mesh Filaments connecting nearby nodes
        val liveNodePositions = meshNodes.map { node ->
            val angle = (orbitalProgression * node.speedMultiplier) + node.phaseOffset
            val x = (node.baseNormalizedX + cos(angle) * node.orbitRadius) * width
            val y = (node.baseNormalizedY + sin(angle) * node.orbitRadius) * height
            Offset(x, y)
        }

        val maxDist = width * 0.26f
        val maxDistSq = maxDist * maxDist

        for (i in liveNodePositions.indices) {
            val p1 = liveNodePositions[i]
            for (j in i + 1 until liveNodePositions.size) {
                val p2 = liveNodePositions[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distSq = dx * dx + dy * dy

                if (distSq < maxDistSq) {
                    val dist = sqrt(distSq)
                    val proximity = 1f - (dist / maxDist)
                    val lineAlpha = (proximity * proximity * 0.20f).coerceIn(0.01f, 0.24f)

                    drawLine(
                        color = cyanColor.copy(alpha = lineAlpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.0f * proximity + 0.5f
                    )
                }
            }
        }

        // 3. Draw glowing mesh nodes
        for (i in liveNodePositions.indices) {
            val pos = liveNodePositions[i]
            val node = meshNodes[i]
            val nodeColor = if (i % 2 == 0) cyanColor else emeraldColor

            drawCircle(
                color = nodeColor.copy(alpha = 0.20f),
                radius = node.radiusDp * 2.5f,
                center = pos
            )
            drawCircle(
                color = nodeColor.copy(alpha = 0.80f),
                radius = node.radiusDp,
                center = pos
            )
        }

        // 4. Antigravity floating stardust particles (floating upward like sparks)
        antigravityParticles.forEach { p ->
            // Compute upward motion with wrapping
            val progress = (floatFlow * p.speed + p.initialY) % 1.0f
            val currentY = (1.0f - progress) * height
            val lateralSway = sin(orbitalProgression * p.waveFrequency + p.phase) * p.waveAmplitude * width
            val currentX = (p.initialX * width + lateralSway).coerceIn(0f, width)

            val pColor = when (p.colorIndex) {
                0 -> emeraldColor
                1 -> cyanColor
                else -> amberColor
            }

            // Alpha fades gently near edges
            val verticalAlphaFactor = (1.0f - (progress - 0.5f).let { it * it * 4f }).coerceIn(0.2f, 1.0f)
            val alpha = (0.55f * verticalAlphaFactor).coerceIn(0.1f, 0.75f)

            // Spark glow
            drawCircle(
                color = pColor.copy(alpha = alpha * 0.35f),
                radius = p.radius * 2.2f,
                center = Offset(currentX, currentY)
            )
            // Spark center
            drawCircle(
                color = pColor.copy(alpha = alpha),
                radius = p.radius,
                center = Offset(currentX, currentY)
            )
        }
    }
}
