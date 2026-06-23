package com.gongde.app.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.gongde.app.R
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * 机械键盘按钮组件
 *
 * 使用三帧动画模拟按键效果：
 * - 底座/轴体：永远只绘制 keycap_off 的底部区域，坐标保持不变
 * - 键帽：只在上部键帽区域切换 off/mid/on 三态
 *
 * 这样点击时只有键帽发生视觉位移，轴体和底座不会被按下态图片带着移动。
 */
@Composable
fun MechanicalButton(
    modifier: Modifier = Modifier,
    soundEngine: SoundEngine? = null,
    hapticEngine: HapticEngine? = null,
    hapticEnabled: Boolean = true,
    switchType: SwitchType = SwitchType.BLUE,
    asmrMode: Boolean = false,
    onKeycapOriginChanged: (Offset) -> Unit = {},
    onPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pressAnim = remember { Animatable(0f) }

    val imageOff = ImageBitmap.imageResource(R.drawable.keycap_off)
    val imageMid = ImageBitmap.imageResource(R.drawable.keycap_mid)
    val imageOn = ImageBitmap.imageResource(R.drawable.keycap_on)

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val localOrigin = keycapFloatingTextOrigin(
                    destinationSize = Size(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    ),
                    imageSize = IntSize(imageOff.width, imageOff.height)
                )
                onKeycapOriginChanged(coordinates.positionInRoot() + localOrigin)
            }
            .semantics { contentDescription = "机械键盘按键，点击获得功德" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val t = pressAnim.value.coerceIn(0f, 1f)
            val capBottom = size.height * KeycapClipBottomFraction
            val baseTop = size.height * StableBaseTopFraction

            // 底座层只取未按下图的底部，避免 mid/on 图里的底座参与动画。
            clipRect(top = baseTop) {
                drawCroppedKeycapImage(imageOff)
            }

            // 键帽层只取上部区域，三态交叉淡入淡出。
            clipRect(bottom = capBottom) {
                drawCroppedKeycapImage(imageOff, alpha = 1f - t)
                val midAlpha = if (t < 0.5f) t * 2f else (1f - t) * 2f
                drawCroppedKeycapImage(imageMid, alpha = midAlpha.coerceIn(0f, 1f))
                drawCroppedKeycapImage(imageOn, alpha = t)
            }
        }

        // 手势铺满画布，但只有键帽上平面的四边形区域响应点击。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hapticEnabled, switchType, asmrMode, soundEngine, hapticEngine) {
                    detectTapGestures { position ->
                        if (!isInsideKeycapTopPlane(position, size, IntSize(imageOff.width, imageOff.height))) {
                            return@detectTapGestures
                        }
                        try {
                            if (asmrMode) soundEngine?.playAsmrClick()
                            else soundEngine?.playClick(switchType)
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
                            try { onPressed() } catch (e: Exception) {
                                Log.e("MechanicalButton", "onPressed failed", e)
                            }
                            pressAnim.animateTo(1f, tween(100, easing = FastOutSlowInEasing))
                            pressAnim.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                        }
                    }
                }
        )
    }
}

private const val StableBaseTopFraction = 0.59f
private const val KeycapClipBottomFraction = 0.63f
private const val FloatingTextGapFraction = 0.035f

// 顶面轮廓基于 keycap 图片原始坐标归一化，映射时与 Canvas 的 centerCrop 保持一致。
private val KeycapTopPlane = listOf(
    Offset(0.372f, 0.116f),
    Offset(0.704f, 0.139f),
    Offset(0.651f, 0.419f),
    Offset(0.291f, 0.392f)
)

internal fun isInsideKeycapTopPlane(
    position: Offset,
    destinationSize: IntSize,
    imageSize: IntSize
): Boolean {
    if (destinationSize.width <= 0 || destinationSize.height <= 0) return false
    val polygon = KeycapTopPlane.map {
        mapImagePointToDestination(
            it,
            Size(destinationSize.width.toFloat(), destinationSize.height.toFloat()),
            imageSize
        )
    }

    var sign = 0
    polygon.indices.forEach { index ->
        val start = polygon[index]
        val end = polygon[(index + 1) % polygon.size]
        val cross = (end.x - start.x) * (position.y - start.y) -
            (end.y - start.y) * (position.x - start.x)
        if (cross != 0f) {
            val currentSign = if (cross > 0f) 1 else -1
            if (sign != 0 && sign != currentSign) return false
            sign = currentSign
        }
    }
    return true
}

private fun keycapFloatingTextOrigin(destinationSize: Size, imageSize: IntSize): Offset {
    val topCenter = Offset(
        x = (KeycapTopPlane[0].x + KeycapTopPlane[1].x) / 2f,
        y = (KeycapTopPlane[0].y + KeycapTopPlane[1].y) / 2f - FloatingTextGapFraction
    )
    return mapImagePointToDestination(topCenter, destinationSize, imageSize)
}

private fun mapImagePointToDestination(
    normalizedPoint: Offset,
    destinationSize: Size,
    imageSize: IntSize
): Offset {
    val scale = max(destinationSize.width / imageSize.width, destinationSize.height / imageSize.height)
    val imageLeft = (destinationSize.width - imageSize.width * scale) / 2f
    val imageTop = (destinationSize.height - imageSize.height * scale) / 2f
    return Offset(
        x = imageLeft + normalizedPoint.x * imageSize.width * scale,
        y = imageTop + normalizedPoint.y * imageSize.height * scale
    )
}

private fun DrawScope.drawCroppedKeycapImage(
    image: ImageBitmap,
    alpha: Float = 1f
) {
    val destinationWidth = size.width
    val destinationHeight = size.height
    if (destinationWidth <= 0f || destinationHeight <= 0f) return

    val scale = max(destinationWidth / image.width, destinationHeight / image.height)
    val sourceWidth = (destinationWidth / scale).roundToInt().coerceIn(1, image.width)
    val sourceHeight = (destinationHeight / scale).roundToInt().coerceIn(1, image.height)
    val sourceLeft = ((image.width - sourceWidth) / 2f).roundToInt().coerceAtLeast(0)
    val sourceTop = ((image.height - sourceHeight) / 2f).roundToInt().coerceAtLeast(0)

    drawImage(
        image = image,
        srcOffset = IntOffset(sourceLeft, sourceTop),
        srcSize = IntSize(sourceWidth, sourceHeight),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(destinationWidth.roundToInt(), destinationHeight.roundToInt()),
        alpha = alpha
    )
}
