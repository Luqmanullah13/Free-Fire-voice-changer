package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianDeep
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class ParticleNode(
  val xNorm: Float,
  val yNorm: Float,
  val vx: Float,
  val vy: Float,
  val radius: Float,
  val phase: Float,
)

@Composable
fun CrimsonConstellationBackground(modifier: Modifier = Modifier) {
  val nodes = remember {
    val rng = Random(42)
    List(30) {
      ParticleNode(
        xNorm = rng.nextFloat(),
        yNorm = rng.nextFloat(),
        vx = (rng.nextFloat() - 0.5f) * 0.14f,
        vy = (rng.nextFloat() - 0.5f) * 0.14f,
        radius = 3.2f + rng.nextFloat() * 4.5f,
        phase = rng.nextFloat() * 6.28f,
      )
    }
  }

  val transition = rememberInfiniteTransition(label = "constellation")
  val tick by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = 6.283185f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 12000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "particle_tick",
    )

  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(Color(0xFF14070B), ObsidianDeep, Color(0xFF12080E))
        )
    )

    val w = size.width
    val h = size.height
    val maxDist = minOf(w, h) * 0.28f

    val positions =
      nodes.map { node ->
        val px = ((node.xNorm + sin((tick + node.phase).toDouble()).toFloat() * 0.08f) * w)
        val py = ((node.yNorm + sin((tick * 0.8f + node.phase * 1.3f).toDouble()).toFloat() * 0.08f) * h)
        Offset(px.coerceIn(0f, w), py.coerceIn(0f, h))
      }

    for (i in positions.indices) {
      for (j in i + 1 until positions.size) {
        val dist = hypot(positions[i].x - positions[j].x, positions[i].y - positions[j].y)
        if (dist < maxDist) {
          val alpha = (1f - dist / maxDist) * 0.55f
          drawLine(
            color = CrimsonPrimary.copy(alpha = alpha),
            start = positions[i],
            end = positions[j],
            strokeWidth = 1.6f,
          )
        }
      }
    }

    positions.forEachIndexed { idx, pos ->
      val r = nodes[idx].radius
      drawCircle(
        color = CrimsonBright.copy(alpha = 0.25f),
        radius = r * 2.2f,
        center = pos,
      )
      drawCircle(color = CrimsonBright, radius = r, center = pos)
    }
  }
}

@Composable
fun LiveAudioWaveform(
  bars: List<Float>,
  isActive: Boolean,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().height(42.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val count = bars.size.coerceAtLeast(1)
      val spacing = 4.dp.toPx()
      val totalSpacing = spacing * (count - 1)
      val barWidth = ((size.width - totalSpacing) / count).coerceAtLeast(3f)

      bars.forEachIndexed { idx, value ->
        val heightFrac = if (isActive) value.coerceIn(0.10f, 1f) else 0.10f
        val barHeight = size.height * heightFrac
        val left = idx * (barWidth + spacing)
        val top = (size.height - barHeight) / 2f
        val color =
          if (!isActive) Color(0xFF343C52)
          else if (idx % 3 == 0) CyberCyan
          else accentColor

        drawRoundRect(
          color = color,
          topLeft = Offset(left, top),
          size = Size(barWidth, barHeight),
          cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
        )
      }
    }
  }
}
