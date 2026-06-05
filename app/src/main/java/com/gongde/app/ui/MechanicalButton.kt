/**
 * 机械键盘按键组件
 *
 * 核心交互组件，包含：
 * - 三帧图片切换（未按下 / 按下中 / 按下到底）实现按压动画
 * - 集成 SoundEngine 播放机械轴音效（青/红/茶轴）
 * - 集成 HapticEngine 触觉反馈
 * - 缩放脉冲动画
 *
 * @param soundEngine 声音引擎实例
 * @param hapticEngine 触觉反馈引擎实例
 * @param hapticEnabled 是否启用触觉反馈（受用户设置控制）
 * @param onPressed 点击回调
 */
package com.gongde.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.pointer.pointerInput
import android.view.HapticFeedbackConstants
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.util.Log
import androidx.compose.ui.unit.dp
import com.gongde.app.R
import kotlinx.coroutines.launch

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
    val painterOn  = painterResource(R.drawable.keycap_on)

    // 键帽按钮容器（尺寸由外部 modifier 控制）
    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .pointerInput(Unit) {
                detectTapGestures {
                    try {
                        if (asmrMode) {
                            soundEngine?.playAsmrClick()
                        } else {
                            soundEngine?.playClick(switchType)
                        }
                        // 使用 View 系统震动（更可靠）+ HapticEngine 兜底
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        if (hapticEnabled) hapticEngine?.tick()
                    } catch (e: Exception) {
                        Log.e("MechanicalButton", "Audio/haptic failed", e)
                    }

                    scope.launch {
                        try {
                            onPressed()
                        } catch (e: Exception) {
                            Log.e("MechanicalButton", "onPressed callback failed", e)
                        }
                        pressAnim.animateTo(1f, tween(100, easing = FastOutSlowInEasing))
                        pressAnim.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                    }
                    scope.launch {
                        scale.animateTo(1.04f, tween(80, easing = FastOutSlowInEasing))
                        scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 未按下态（按下时淡出）
        Image(
            painter = painterOff,
            contentDescription = "机械键盘按键，点击获得功德",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (1f - pressAnim.value).coerceIn(0f, 1f) }
        )
        // 按下中间态（过渡时显示）
        Image(
            painter = painterMid,
            contentDescription = "机械键盘按键，点击获得功德",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val t = pressAnim.value
                    alpha = if (t < 0.5f) t * 2f else (1f - t) * 2f
                }
        )
        // 按下到底态（按下时淡入）
        Image(
            painter = painterOn,
            contentDescription = "机械键盘按键，点击获得功德",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = pressAnim.value.coerceIn(0f, 1f) }
        )
    }
}
