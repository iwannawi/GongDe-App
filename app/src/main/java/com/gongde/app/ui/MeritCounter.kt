/**
 * 功德计数面板组件
 *
 * 底部展示功德计数的 UI 面板，包含：
 * - 累计功德和今日功德两列数据展示
 * - 星芒图标（累计功德）和时钟图标（今日功德）
 * - 科技感渐变边框和电路装饰线
 * - 底部提示文案"点一下，功德 +1 ✨"
 */

package com.gongde.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GoldColor
import com.gongde.app.ui.theme.GongDeThemeExt

/**
 * 功德计数主面板
 *
 * 展示累计功德和今日功德的数据卡片，采用双层嵌套 Box 实现：
 * 外层为渐变发光边框，内层为深色半透明卡片。
 *
 * @param totalCount 累计功德总数
 * @param todayCount 今日功德计数
 * @param modifier 外部修饰符
 */
@Composable
fun MeritCounter(
    totalCount: Int,
    todayCount: Int,
    modifier: Modifier = Modifier
) {
    val accent = GongDeThemeExt.colors.accent
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 外层发光边框：使用扫描渐变（蓝金交替）模拟动态光效
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    // 扫描渐变：蓝色和金色交替，形成旋转光效的静态近似
                    Brush.sweepGradient(
                        colors = listOf(
                            accent,   // 浅蓝色
                            GoldColor,   // 金色
                            accent,   // 浅蓝色
                            GoldColor,   // 金色
                            accent    // 浅蓝色
                        )
                    )
                )
                .padding(1.dp)  // 内缩1dp作为边框宽度
        ) {
            // 内层卡片：深色半透明背景，与外层形成边框效果
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(19.dp))  // 比外层小1dp，露出渐变边框
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xDD0D0D24),  // 上方稍亮
                                Color(0xEE0A0A1A)   // 下方更深
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // 顶部电路装饰线：水平渐变线，模拟科技感电路板
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                ) {
                    val w = size.width
                    drawLine(
                        brush = Brush.horizontalGradient(
                            // 两端透明，中间蓝金渐变
                            colors = listOf(
                                Color.Transparent,
                                accent,   // 蓝色
                                GoldColor,   // 金色
                                accent,   // 蓝色
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, 1f),
                        end = Offset(w, 1f),
                        strokeWidth = 1.5f
                    )
                }

                Column {
                    // 顶部标题标签
                    Text(
                        text = "功德计数",
                        color = GoldColor.copy(alpha = 0.53f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 4.sp,              // 加宽字间距，增加精致感
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 12.dp)
                    )

                    // 数据展示行：左列（累计功德）+ 中间分割线 + 右列（今日功德）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左列：累计功德数据
                        MeritColumn(
                            label = "累计功德",
                            count = totalCount,
                            accentColor = GoldColor,       // 金色主题
                            icon = { SparkleIcon(GoldColor) }, // 星芒图标
                            modifier = Modifier.weight(1f)
                        )

                        // 中间垂直分割线：渐变消失效果
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            accent.copy(alpha = 0.33f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // 右列：今日功德数据
                        MeritColumn(
                            label = "今日功德",
                            count = todayCount,
                            accentColor = accent,        // 蓝色主题
                            icon = { ClockIcon(accent) }, // 时钟图标
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 底部提示文案
                    Text(
                        text = "点一下，功德 +1 ✨",
                        color = Color(0x55B0BEC5),  // 半透明灰色
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 单列功德数据展示组件
 *
 * 包含图标、标签文字和计数数字的垂直排列。
 *
 * @param label 标签文字（如"累计功德"、"今日功德"）
 * @param count 当前功德数值，使用千位分隔格式显示
 * @param accentColor 主题色，用于数字显示和图标
 * @param icon 图标 Composable（星芒或时钟）
 * @param modifier 外部修饰符
 */
@Composable
private fun MeritColumn(
    label: String,
    count: Int,
    accentColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()                                      // 顶部图标
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,                           // 标签文字
            fontSize = 11.sp,
            color = Color(0x77B0BEC5),              // 半透明灰色
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%,d", count),     // 带千位分隔符的数字格式
            fontSize = 30.sp,
            color = accentColor,                    // 使用主题色高亮显示
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

// ─── 极简星芒图标（四角） ───

/**
 * 星芒图标
 *
 * 使用 Canvas 绘制四角星芒，用于累计功德列的装饰图标。
 * 象征"闪耀"和能量感。
 *
 * @param color 图标颜色
 */
@Composable
private fun SparkleIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.42f  // 外尖半径
        val ir = size.width * 0.16f // 内凹半径

        val path = Path().apply {
            // 四角星芒：交替内外半径，绘制8个顶点
            val points = 8
            for (i in 0 until points) {
                val angle = (Math.PI * 2 * i / points - Math.PI / 2).toFloat()
                val radius = if (i % 2 == 0) r else ir
                val x = cx + kotlin.math.cos(angle.toDouble()).toFloat() * radius
                val y = cy + kotlin.math.sin(angle.toDouble()).toFloat() * radius
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f))

        // 中心小圆点
        drawCircle(color, 1.5f, Offset(cx, cy))
    }
}

// ─── 极简时钟图标 ───

/**
 * 时钟图标
 *
 * 使用 Canvas 绘制简约时钟，包含圆圈、时针、分针和中心圆点，
 * 用于今日功德列的装饰图标，暗示"今日"的时间概念。
 *
 * @param color 图标颜色
 */
@Composable
private fun ClockIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.40f  // 时钟半径

        // 绘制时钟外圈（描边圆）
        drawCircle(
            color = color,
            radius = r,
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f)
        )
        // 时针：从中心向上
        drawLine(color, Offset(cx, cy), Offset(cx, cy - r * 0.5f), 1.2f)
        // 分针：从中心向右
        drawLine(color, Offset(cx, cy), Offset(cx + r * 0.4f, cy), 1.2f)
        // 中心小圆点
        drawCircle(color, 1.5f, Offset(cx, cy))
    }
}
