package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt

private val LcdBg = Color(0x881A1A2E)
private val LcdBorder = Color(0x664FC3F7)
private val LcdGlow = Color(0xFF4FC3F7)
private val LcdText = Color(0xFF00FFCC)

@Composable
fun MeritCounter(
    totalCount: Int,
    todayCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = GongDeThemeExt.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(
                Brush.verticalGradient(listOf(LcdBg, Color(0x8812121F))),
                RoundedCornerShape(14.dp)
            )
    ) {
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
                    .background(LcdGlow.copy(alpha = 0.2f))
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
                        .background(LcdGlow.copy(alpha = 0.2f))
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
                    .background(LcdGlow.copy(alpha = 0.15f))
            )

            // 底部提示
            Text(
                text = "▸ 点击键盘 功德+1",
                color = LcdGlow.copy(alpha = 0.5f),
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
            color = LcdGlow.copy(alpha = 0.6f),
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
