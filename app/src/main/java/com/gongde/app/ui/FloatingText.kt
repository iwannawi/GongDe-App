/**
 * 浮动文字动画组件
 *
 * 当用户点击功德按钮时，在屏幕上显示"功德+1"的文字，
 * 文字从按钮位置向上飘浮并逐渐淡出，模拟游戏中的得分反馈效果。
 * 支持多个文字同时显示，互不干扰。
 */

package com.gongde.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 浮动文字数据项
 *
 * 每个浮动文字实例的动画状态和属性。
 *
 * @param id 唯一标识符（基于纳秒时间戳），用于 Compose 的 key 稳定性
 * @param y 垂直位置动画器（从 0 向上移动到负值）
 * @param alpha 透明度动画器（从完全不透明到完全透明）
 * @param xDrift 水平偏移量（随机值，让每个文字略有左右漂移，避免重叠）
 * @param scale 缩放动画器（从小到大，产生弹出效果）
 */
private data class FloatingTextItem(
    val id: Long,
    val y: Animatable<Float, *> = Animatable(0f),
    val alpha: Animatable<Float, *> = Animatable(1f),
    val xDrift: Float = Random.nextFloat() * 60f - 30f,  // -30dp ~ +30dp 随机偏移
    val scale: Animatable<Float, *> = Animatable(0.6f)
)

/**
 * 浮动文字容器
 *
 * 管理所有浮动文字的生命周期：创建、动画、移除。
 * 每次 triggerCount 变化时创建一个新的浮动文字项。
 *
 * @param triggerCount 触发计数器，每次增加1都会生成一个新浮动文字
 * @param text 浮动显示的文字内容，默认"功德+1"
 * @param modifier 外部修饰符
 * @param distancePx 浮动距离（像素），文字向上飘浮的总距离
 * @param durationMs 动画持续时间（毫秒），文字从出现到消失的总时长
 */
@Composable
fun FloatingTextContainer(
    triggerCount: Int,
    text: String = "功德+1",
    modifier: Modifier = Modifier,
    distancePx: Float = 700f,
    durationMs: Int = 2000
) {
    // 浮动文字列表，支持动态添加和移除
    val items = remember { mutableStateListOf<FloatingTextItem>() }

    // 当触发计数变化时，创建一个新的浮动文字项（上限 15 个，避免快速点击时过多协程）
    LaunchedEffect(triggerCount) {
        if (triggerCount > 0) {
            if (items.size >= 15) items.removeAt(0)
            val item = FloatingTextItem(id = System.nanoTime())
            items.add(item)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 遍历渲染所有浮动文字
        items.forEach { item ->
            // 使用 item.id 作为 key，确保每个文字的动画状态独立
            key(item.id) {
                LaunchedEffect(item.id) {
                    // 并行启动三个独立动画，互不阻塞

                    // 动画1：缩放弹出（0.6 → 1.0），200ms，先快后慢
                    launch {
                        item.scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    // 动画2：向上飘浮（0 → -distancePx），全程线性匀速
                    launch {
                        item.y.animateTo(
                            targetValue = -distancePx,
                            animationSpec = tween(
                                durationMillis = durationMs,
                                easing = LinearEasing
                            )
                        )
                    }

                    // 动画3：淡出（延迟10%后开始），从不透明到完全透明
                    launch {
                        delay((durationMs * 0.1).toLong())  // 先显示一小段时间再开始淡出
                        item.alpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = (durationMs * 0.9).toInt(),  // 剩余90%时间淡出
                                easing = LinearEasing
                            )
                        )
                    }

                    // 等待整个动画周期结束后，从列表中移除该文字项
                    delay(durationMs.toLong())
                    items.remove(item)
                }

                // 渲染文字，应用所有动画值
                Text(
                    text = text,
                    modifier = Modifier
                        .align(Alignment.TopCenter)  // 水平居中对齐
                        .offset {
                            IntOffset(
                                x = item.xDrift.dp.roundToPx(),   // 水平随机偏移
                                y = 500.dp.roundToPx() + item.y.value.dp.roundToPx()  // 从键盘位置向上飘
                            )
                        }
                        .alpha(item.alpha.value),  // 应用淡出透明度
                    style = TextStyle(
                        color = Color(0xFF6B7B8D),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.9f),
                            offset = Offset(0f, 0f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}
