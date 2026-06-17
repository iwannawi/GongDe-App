package com.gongde.app.ui

import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
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
    onPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pressAnim = remember { Animatable(0f) }
    val view = LocalView.current

    val imageOff = ImageBitmap.imageResource(R.drawable.keycap_off)
    val imageMid = ImageBitmap.imageResource(R.drawable.keycap_mid)
    val imageOn = ImageBitmap.imageResource(R.drawable.keycap_on)

    Box(
        modifier = modifier
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

        // ── 点击区域（缩小到键帽中心）──
        Box(
            modifier = Modifier
                .fillMaxSize(fraction = 0.65f)
                .pointerInput(hapticEnabled, switchType, asmrMode, soundEngine, hapticEngine) {
                    detectTapGestures {
                        try {
                            if (asmrMode) soundEngine?.playAsmrClick()
                            else soundEngine?.playClick(switchType)
                            if (hapticEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                hapticEngine?.tick()
                            }
                        } catch (e: Exception) {
                            Log.e("MechanicalButton", "Audio/haptic failed", e)
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
