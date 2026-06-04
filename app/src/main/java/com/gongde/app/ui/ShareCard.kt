/**
 * 功德分享卡片组件
 *
 * 生成可截图分享的功德展示卡片，包含：
 * - 深色渐变背景与装饰性圆圈图案
 * - 键帽小图标
 * - "功德 +{count}" 金色大字
 * - 随机轻松语录
 * - App 品牌水印
 * - 系统分享 Intent（截图 + 分享）
 */

package com.gongde.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.core.content.FileProvider
import com.gongde.app.R
import com.gongde.app.ui.theme.GoldColor
import com.gongde.app.ui.theme.GongDeThemeExt
import java.io.File
import java.io.FileOutputStream

// ==================== 颜色常量 ====================

private val DeepPurple = Color(0xFF1A0033)
private val MediumPurple = Color(0xFF2D1055)

// ==================== 轻松语录（getRandomFunQuote 定义在 FunQuotes.kt） ====================

/**
 * 分享功德卡片视图
 *
 * @param totalCount 累计功德总数
 * @param cardGradient 卡片背景渐变色列表（默认紫色兜底）
 * @param modifier 外部修饰符
 */
@Composable
fun ShareCardView(
    totalCount: Int,
    cardGradient: List<Color> = listOf(DeepPurple, MediumPurple),
    modifier: Modifier = Modifier
) {
    val funQuote = remember(totalCount) { getRandomFunQuote() }
    val circleBorder = GongDeThemeExt.colors.cardBorder.copy(alpha = 0.11f)
    val mutedGray = GongDeThemeExt.colors.mutedGray

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(cardGradient))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // 装饰性背景圆圈
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(circleBorder, size.width * 0.45f, Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
            drawCircle(circleBorder, size.width * 0.32f, Offset(cx, cy - size.height * 0.05f), style = Stroke(1.dp.toPx()))
            drawCircle(circleBorder, size.width * 0.18f, Offset(cx, cy - size.height * 0.02f), style = Stroke(0.8.dp.toPx()))
            drawCircle(circleBorder, size.width * 0.12f, Offset(size.width * 0.12f, size.height * 0.2f), style = Stroke(0.8.dp.toPx()))
            drawCircle(circleBorder, size.width * 0.1f, Offset(size.width * 0.88f, size.height * 0.75f), style = Stroke(0.8.dp.toPx()))
        }

        // 卡片内容
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 键帽小图标
            Image(
                painter = painterResource(R.drawable.keycap_off),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(48.dp, 56.dp)
                    .graphicsLayer { alpha = 0.9f }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // "功德 +{count}" 大字
            Text(
                text = "功德 +$totalCount",
                color = GoldColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 随机轻松语录
            Text(
                text = funQuote,
                color = GongDeThemeExt.colors.textPrimary.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App 品牌水印
            Text(
                text = "解压键盘 · 功德+1",
                color = mutedGray,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

/**
 * 分享功德按钮
 *
 * 点击后截图 ShareCardView 并通过系统分享 Intent 分享。
 *
 * @param totalCount 功德总数（截图用）
 * @param context Android 上下文
 * @param modifier 外部修饰符
 */
@Composable
fun ShareButton(
    totalCount: Int,
    cardGradient: List<Color> = listOf(DeepPurple, MediumPurple),
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    // 从主题提取 ARGB 色值，供 Bitmap 渲染使用
    val goldArgb = GoldColor.toArgb()
    val quoteArgb = GongDeThemeExt.colors.textPrimary.copy(alpha = 0.7f).toArgb()
    val watermarkArgb = GongDeThemeExt.colors.mutedGray.toArgb()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, GoldColor, RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .clickable {
                shareMeritCard(context, totalCount, cardGradient, goldArgb, quoteArgb, watermarkArgb)
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "📤", fontSize = 14.sp, color = GoldColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "秀一下功德",
                color = GoldColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 生成功德卡片 Bitmap 并通过系统 Intent 分享
 *
 * 流程：渲染 View → 截图 → 保存临时文件 → FileProvider URI → 分享 Intent
 */
private fun shareMeritCard(
    context: Context,
    totalCount: Int,
    cardGradient: List<Color>,
    goldArgb: Int,
    quoteArgb: Int,
    watermarkArgb: Int
) {
    try {
        val bitmap = renderShareBitmap(context, totalCount, cardGradient, goldArgb, quoteArgb, watermarkArgb)
        try {
            val shareDir = java.io.File(context.cacheDir, "share").also { it.mkdirs() }
            val file = java.io.File(shareDir, "share_merit.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "秀一下功德"))
        } finally {
            bitmap.recycle()
        }
    } catch (e: Exception) {
        Log.e("ShareCard", "Failed to share merit card", e)
    }
}

/**
 * 将功德卡片渲染为 Bitmap（300x260dp → 对应像素）
 */
private fun renderShareBitmap(
    context: Context,
    totalCount: Int,
    cardGradient: List<Color>,
    goldArgb: Int,
    quoteArgb: Int,
    watermarkArgb: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val widthPx = (300 * density).toInt()
    val heightPx = (260 * density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val defaultTypeface = Typeface.DEFAULT

    // 绘制渐变背景（使用主题色）
    val topColor = (cardGradient.firstOrNull() ?: DeepPurple).toArgb()
    val bottomColor = (cardGradient.lastOrNull() ?: MediumPurple).toArgb()
    val bgPaint = android.graphics.Paint().apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, heightPx.toFloat(),
            topColor, bottomColor,
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), bgPaint)

    // 绘制功德数字
    val textPaint = android.graphics.Paint().apply {
        color = goldArgb
        textSize = 36 * density
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = defaultTypeface
    }
    canvas.drawText("功德 +$totalCount", widthPx / 2f, heightPx / 2f, textPaint)

    // 绘制轻松语录
    val quotePaint = android.graphics.Paint().apply {
        color = quoteArgb
        textSize = 12 * density
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = defaultTypeface
    }
    canvas.drawText(getRandomFunQuote(), widthPx / 2f, heightPx / 2f + 40 * density, quotePaint)

    // 绘制水印
    val watermarkPaint = android.graphics.Paint().apply {
        color = watermarkArgb
        textSize = 9 * density
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = defaultTypeface
    }
    canvas.drawText("解压键盘 · 功德+1", widthPx / 2f, heightPx - 20 * density, watermarkPaint)

    return bitmap
}
