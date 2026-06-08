package com.gongde.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt

// LCD 面板配色
private val LcdBg = Color(0xFF0A0E1A)
private val LcdBorder = Color(0xFF1A2A4A)
private val LcdGlow = Color(0xFF00D4FF)
private val LcdGlowDim = Color(0xFF0088AA)
private val LcdText = Color(0xFF00FFCC)
private val LcdTextDim = Color(0xFF006655)
private val LcdScanLine = Color(0xFF00D4FF).copy(alpha = 0.04f)
private val LcdGrid = Color(0xFF0D1525)

@Composable
fun MeritCounter(
    totalCount: Int,
    todayCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = GongDeThemeExt.colors
    val accent = colors.accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xCC0C1020), Color(0xCC0A0E1A), Color(0xCC080C18))),
                RoundedCornerShape(14.dp)
            )
            .border(1.5.dp, LcdBorder, RoundedCornerShape(14.dp))
    ) {
        // 科技感装饰层
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 六角网格背景
            val hexSize = 20f
            val hexPaint = Color(0xFF0A1020)
            var row = 0
            var y = 0f
            while (y < h + hexSize) {
                val xOffset = if (row % 2 == 1) hexSize * 0.866f else 0f
                var x = xOffset
                while (x < w + hexSize) {
                    drawCircle(hexPaint, hexSize * 0.4f, Offset(x, y))
                    x += hexSize * 1.732f
                }
                y += hexSize * 1.5f
                row++
            }

            // 扫描线
            var sy = 0f
            while (sy < h) {
                drawLine(LcdScanLine, Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
                sy += 3.dp.toPx()
            }

            // 顶部发光条
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, LcdGlow.copy(alpha = 0.4f), LcdGlow, LcdGlow.copy(alpha = 0.4f), Color.Transparent)
                ),
                start = Offset(w * 0.1f, 1f),
                end = Offset(w * 0.9f, 1f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // 底部发光条
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, LcdGlowDim.copy(alpha = 0.3f), LcdGlowDim, LcdGlowDim.copy(alpha = 0.3f), Color.Transparent)
                ),
                start = Offset(w * 0.15f, h - 1f),
                end = Offset(w * 0.85f, h - 1f),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )

            // 角落装饰点
            val dotR = 3f
            drawCircle(LcdGlow.copy(alpha = 0.5f), dotR, Offset(16f, 16f))
            drawCircle(LcdGlow.copy(alpha = 0.5f), dotR, Offset(w - 16f, 16f))
            drawCircle(LcdGlowDim.copy(alpha = 0.3f), dotR, Offset(16f, h - 16f))
            drawCircle(LcdGlowDim.copy(alpha = 0.3f), dotR, Offset(w - 16f, h - 16f))
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 标题
            Text(
                text = "功德计数",
                color = LcdGlow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp)
            )

            // 分割线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, LcdGlow.copy(alpha = 0.3f), Color.Transparent)))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 数据展示行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MeritColumn(
                    label = "累计功德",
                    count = totalCount,
                    modifier = Modifier.weight(1f)
                )

                // 垂直分割线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, LcdGlow.copy(alpha = 0.25f), Color.Transparent)))
                )

                MeritColumn(
                    label = "今日功德",
                    count = todayCount,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 分割线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, LcdGlowDim.copy(alpha = 0.2f), Color.Transparent)))
            )

            // 底部提示
            Text(
                text = "▸ 点击键盘 功德+1",
                color = LcdTextDim,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun MeritColumn(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = LcdGlowDim,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = String.format("%,d", count),
            fontSize = 28.sp,
            color = LcdText,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun SparkleIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.42f
        val ir = size.width * 0.16f
        val path = Path().apply {
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
        drawPath(path, color = color, style = Stroke(1.2f))
        drawCircle(color, 1.5f, Offset(cx, cy))
    }
}

@Composable
private fun ClockIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.40f
        drawCircle(color = color, radius = r, style = Stroke(1.2f))
        drawLine(color, Offset(cx, cy), Offset(cx, cy - r * 0.5f), 1.2f)
        drawLine(color, Offset(cx, cy), Offset(cx + r * 0.4f, cy), 1.2f)
        drawCircle(color, 1.5f, Offset(cx, cy))
    }
}
