package com.gongde.app.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import com.gongde.app.R
import kotlin.math.min
import kotlinx.coroutines.launch

/**
 * Mechanical key visual extracted directly from the generated concept mockup.
 */
@Composable
fun MechanicalButton(
    modifier: Modifier = Modifier,
    soundEngine: SoundEngine? = null,
    hapticEngine: HapticEngine? = null,
    hapticEnabled: Boolean = true,
    switchType: SwitchType = SwitchType.BLUE,
    onKeycapOriginChanged: (Offset) -> Unit = {},
    onPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pressAnim = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val localOrigin = keycapFloatingTextOrigin(
                    Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                )
                onKeycapOriginChanged(coordinates.positionInRoot() + localOrigin)
            }
            .semantics { contentDescription = "机械键帽，点击上表面释放压力" },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.concept_key_transparent),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val t = pressAnim.value.coerceIn(0f, 1f)
                    scaleX = 1f - t * 0.012f
                    scaleY = 1f - t * 0.012f
                },
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hapticEnabled, switchType, soundEngine, hapticEngine) {
                    detectTapGestures { position ->
                        if (!isInsideKeycapTopPlane(position, size, IntSize.Zero)) {
                            return@detectTapGestures
                        }
                        try {
                            soundEngine?.playClick(switchType)
                        } catch (e: Exception) {
                            Log.e("MechanicalButton", "Audio playback failed", e)
                        }
                        if (hapticEnabled) {
                            try {
                                hapticEngine?.tick()
                            } catch (e: Exception) {
                                Log.e("MechanicalButton", "Haptic feedback failed", e)
                            }
                        }
                        scope.launch {
                            try {
                                onPressed()
                            } catch (e: Exception) {
                                Log.e("MechanicalButton", "onPressed failed", e)
                            }
                            pressAnim.animateTo(1f, tween(82, easing = FastOutSlowInEasing))
                            pressAnim.animateTo(0f, tween(190, easing = FastOutSlowInEasing))
                        }
                    }
                }
        )
    }
}

private val KeycapTopPlane = listOf(
    Offset(0.26f, 0.25f),
    Offset(0.56f, 0.11f),
    Offset(0.79f, 0.31f),
    Offset(0.48f, 0.51f)
)

private const val FloatingTextGapFraction = 0.06f

internal fun isInsideKeycapTopPlane(
    position: Offset,
    destinationSize: IntSize,
    @Suppress("UNUSED_PARAMETER") imageSize: IntSize
): Boolean {
    if (destinationSize.width <= 0 || destinationSize.height <= 0) return false
    val polygon = KeycapTopPlane.map {
        Offset(it.x * destinationSize.width, it.y * destinationSize.height)
    }
    return isPointInsideConvexPolygon(position, polygon)
}

private fun keycapFloatingTextOrigin(destinationSize: Size): Offset {
    val centerX = (KeycapTopPlane[0].x + KeycapTopPlane[1].x + KeycapTopPlane[2].x + KeycapTopPlane[3].x) / 4f
    val topY = min(KeycapTopPlane[0].y, KeycapTopPlane[1].y) - FloatingTextGapFraction
    return Offset(centerX * destinationSize.width, topY * destinationSize.height)
}

private fun isPointInsideConvexPolygon(point: Offset, polygon: List<Offset>): Boolean {
    var sign = 0
    polygon.indices.forEach { index ->
        val start = polygon[index]
        val end = polygon[(index + 1) % polygon.size]
        val cross = (end.x - start.x) * (point.y - start.y) -
            (end.y - start.y) * (point.x - start.x)
        if (cross != 0f) {
            val currentSign = if (cross > 0f) 1 else -1
            if (sign != 0 && sign != currentSign) return false
            sign = currentSign
        }
    }
    return true
}
