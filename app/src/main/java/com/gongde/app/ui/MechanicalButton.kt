package com.gongde.app.ui

import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gongde.app.R
import kotlinx.coroutines.launch

/**
 * 机械键盘按钮组件
 *
 * 使用三帧动画模拟按键效果：
 * - keycap_off：未按下态（永久显示，作为底座基准）
 * - keycap_mid：中间态（暖光效果，叠加在底座上）
 * - keycap_on：按下态（键帽前倾，叠加在底座上）
 *
 * 关键设计：keycap_off 始终作为底层显示，确保轴体和底座
 * 在整个动画过程中保持稳定不偏移。中间态和按下态通过
 * alpha 通道交叉淡入淡出，只改变键帽部分的视觉效果。
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
    val scale = remember { Animatable(1f) }
    val view = LocalView.current

    val painterOff = painterResource(R.drawable.keycap_off)
    val painterMid = painterResource(R.drawable.keycap_mid)
    val painterOn = painterResource(R.drawable.keycap_on)

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        contentAlignment = Alignment.Center
    ) {
        // ── 底座层（永久显示，不参与动画）──
        // keycap_off 作为基准，确保轴体和底座始终稳定
        Image(
            painter = painterOff,
            contentDescription = "机械键盘按键，点击获得功德",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── 中间态叠加层（暖光效果）──
        // pressAnim 0→0.5 时淡入，0.5→1 时淡出
        Image(
            painter = painterMid,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val t = pressAnim.value
                    alpha = if (t < 0.5f) t * 2f else (1f - t) * 2f
                }
        )

        // ── 按下态叠加层（键帽前倾）──
        // pressAnim 0→1 时淡入
        Image(
            painter = painterOn,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = pressAnim.value }
        )

        // ── 点击区域（缩小到键帽中心）──
        Box(
            modifier = Modifier
                .fillMaxSize(fraction = 0.65f)
                .pointerInput(Unit) {
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
                        scope.launch {
                            scale.animateTo(1.04f, tween(80, easing = FastOutSlowInEasing))
                            scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
                        }
                    }
                }
        )
    }
}
