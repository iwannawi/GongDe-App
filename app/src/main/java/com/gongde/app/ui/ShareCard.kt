package com.gongde.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.core.content.FileProvider
import com.gongde.app.ui.theme.GongDeThemeExt
import java.io.FileOutputStream
import kotlin.random.Random

// 随机背景渐变色组合（清新愉悦色调）
private val BG_PALETTES = listOf(
    listOf(Color(0xFF81C784), Color(0xFF66BB6A)),  // 清新绿
    listOf(Color(0xFF4FC3F7), Color(0xFF29B6F6)),  // 天空蓝
    listOf(Color(0xFFFFB74D), Color(0xFFFFA726)),  // 暖阳橙
    listOf(Color(0xFFAED581), Color(0xFF8BC34A)),  // 嫩芽绿
    listOf(Color(0xFF4DD0E1), Color(0xFF26C6DA)),  // 清水蓝
    listOf(Color(0xFFFFD54F), Color(0xFFFFC107)),  // 阳光黄
)

@Composable
fun ShareCardView(
    todayCount: Int,
    modifier: Modifier = Modifier,
    refreshKey: Int = 0
) {
    val funQuote = remember(todayCount, refreshKey) { getRandomFunQuote() }
    val colors = GongDeThemeExt.colors
    val accent = colors.accent
    // 随机选择一个背景色板
    val palette = remember(todayCount, refreshKey) { BG_PALETTES[Random.nextInt(BG_PALETTES.size)] }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        // 装饰性柔和光斑 + 更多活泼小圆
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // 大柔光圆
            drawCircle(Color.White.copy(alpha = 0.08f), w * 0.5f, Offset(w * 0.2f, h * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.06f), w * 0.4f, Offset(w * 0.8f, h * 0.7f))
            drawCircle(Color.White.copy(alpha = 0.1f), w * 0.25f, Offset(w * 0.6f, h * 0.15f))
            // 额外装饰柔光圆
            drawCircle(Color.White.copy(alpha = 0.05f), w * 0.35f, Offset(w * 0.1f, h * 0.8f))
            drawCircle(Color.White.copy(alpha = 0.07f), w * 0.3f, Offset(w * 0.9f, h * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.04f), w * 0.45f, Offset(w * 0.5f, h * 0.9f))
            // 小亮点
            drawCircle(Color.White.copy(alpha = 0.3f), 3f, Offset(w * 0.15f, h * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.25f), 2f, Offset(w * 0.85f, h * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.2f), 2.5f, Offset(w * 0.3f, h * 0.8f))
            drawCircle(Color.White.copy(alpha = 0.3f), 2f, Offset(w * 0.7f, h * 0.85f))
            // 额外活泼小亮点
            drawCircle(Color.White.copy(alpha = 0.35f), 2f, Offset(w * 0.45f, h * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.2f), 1.5f, Offset(w * 0.05f, h * 0.5f))
            drawCircle(Color.White.copy(alpha = 0.25f), 2f, Offset(w * 0.95f, h * 0.55f))
            drawCircle(Color.White.copy(alpha = 0.15f), 1.5f, Offset(w * 0.6f, h * 0.65f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = funQuote,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.3f), offset = Offset(2f, 2f), blurRadius = 8f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "今日功德 +$todayCount",
                color = Color.White,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.2f), offset = Offset(1f, 1f), blurRadius = 4f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "解压键盘 · 功德+1",
                color = Color.White,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun ShareButton(
    todayCount: Int,
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    val colors = GongDeThemeExt.colors

    // 共享内容状态：页面预览和弹窗使用同一份数据
    var currentQuote by remember { mutableStateOf(getRandomFunQuote()) }
    var currentPalette by remember { mutableStateOf(BG_PALETTES[Random.nextInt(BG_PALETTES.size)]) }

    // 刷新内容（弹窗关闭后调用）
    val refreshContent: () -> Unit = {
        currentQuote = getRandomFunQuote()
        currentPalette = BG_PALETTES[Random.nextInt(BG_PALETTES.size)]
    }

    if (showPreview) {
        SharePreviewDialogWithContent(
            todayCount = todayCount,
            quote = currentQuote,
            palette = currentPalette,
            onDismiss = {
                showPreview = false
                refreshContent()
            },
            onShare = {
                showPreview = false
                refreshContent()
                val accentArgb = colors.accent.toArgb()
                shareMeritCard(context, todayCount, accentArgb, currentQuote, currentPalette)
            }
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 页面预览卡片（与弹窗内容一致）
        ShareCardViewWithContent(
            todayCount = todayCount,
            quote = currentQuote,
            palette = currentPalette
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 分享按钮
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, colors.accent, RoundedCornerShape(10.dp))
                .background(Color.Transparent)
                .clickable { showPreview = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "秀一下今日功德",
                    color = colors.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ShareCardViewWithContent(
    todayCount: Int,
    quote: String,
    palette: List<Color>,
    modifier: Modifier = Modifier
) {
    val colors = GongDeThemeExt.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width; val h = size.height
            drawCircle(Color.White.copy(alpha = 0.08f), w * 0.5f, Offset(w * 0.2f, h * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.06f), w * 0.4f, Offset(w * 0.8f, h * 0.7f))
            drawCircle(Color.White.copy(alpha = 0.1f), w * 0.25f, Offset(w * 0.6f, h * 0.15f))
            drawCircle(Color.White.copy(alpha = 0.05f), w * 0.35f, Offset(w * 0.1f, h * 0.8f))
            drawCircle(Color.White.copy(alpha = 0.07f), w * 0.3f, Offset(w * 0.9f, h * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.04f), w * 0.45f, Offset(w * 0.5f, h * 0.9f))
            drawCircle(Color.White.copy(alpha = 0.3f), 3f, Offset(w * 0.15f, h * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.25f), 2f, Offset(w * 0.85f, h * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.2f), 2.5f, Offset(w * 0.3f, h * 0.8f))
            drawCircle(Color.White.copy(alpha = 0.3f), 2f, Offset(w * 0.7f, h * 0.85f))
            drawCircle(Color.White.copy(alpha = 0.35f), 2f, Offset(w * 0.45f, h * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.2f), 1.5f, Offset(w * 0.05f, h * 0.5f))
            drawCircle(Color.White.copy(alpha = 0.25f), 2f, Offset(w * 0.95f, h * 0.55f))
            drawCircle(Color.White.copy(alpha = 0.15f), 1.5f, Offset(w * 0.6f, h * 0.65f))
        }

        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = quote,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.3f), offset = Offset(2f, 2f), blurRadius = 8f)
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "今日功德 +$todayCount",
                color = Color.White,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.2f), offset = Offset(1f, 1f), blurRadius = 4f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "解压键盘 · 功德+1", color = Color.White, fontSize = 15.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun SharePreviewDialogWithContent(
    todayCount: Int,
    quote: String,
    palette: List<Color>,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val colors = GongDeThemeExt.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.dialogBg,
        title = { Text("分享预览", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = { ShareCardViewWithContent(todayCount = todayCount, quote = quote, palette = palette) },
        confirmButton = { TextButton(onClick = onShare) { Text("分享", color = colors.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = colors.textSecondary, fontSize = 15.sp) } }
    )
}

private fun shareMeritCard(
    context: Context,
    todayCount: Int,
    accentArgb: Int,
    quote: String,
    palette: List<Color>
) {
    try {
        val bitmap = renderShareBitmap(context, todayCount, accentArgb, quote, palette)
        try {
            val shareDir = java.io.File(context.cacheDir, "share").also { it.mkdirs() }
            val file = java.io.File(shareDir, "share_merit.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "秀一下今日功德"))
        } finally {
            bitmap.recycle()
        }
    } catch (e: Exception) {
        Log.e("ShareCard", "Failed to share merit card", e)
    }
}

private fun renderShareBitmap(
    context: Context,
    todayCount: Int,
    accentArgb: Int,
    quote: String,
    palette: List<Color>
): Bitmap {
    val density = context.resources.displayMetrics.density
    val w = (300 * density).toInt()
    val h = (260 * density).toInt()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val tf = Typeface.DEFAULT

    val topColor = palette[0].toArgb()
    val bottomColor = palette[1].toArgb()
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply {
        shader = android.graphics.LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), topColor, bottomColor, android.graphics.Shader.TileMode.CLAMP)
    })

    // 柔光装饰
    val glowPaint = Paint().apply { color = android.graphics.Color.argb(20, 255, 255, 255); isAntiAlias = true }
    canvas.drawCircle(w * 0.2f, h * 0.3f, w * 0.5f, glowPaint)
    val glowPaint2 = Paint().apply { color = android.graphics.Color.argb(15, 255, 255, 255); isAntiAlias = true }
    canvas.drawCircle(w * 0.8f, h * 0.7f, w * 0.4f, glowPaint2)

    val whiteArgb = Color.White.toArgb()

    // 语录（大字、居中）
    canvas.drawText(quote, w / 2f, h / 2f - 20 * density, Paint().apply {
        color = whiteArgb; textSize = 28 * density; isFakeBoldText = true; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = tf; setShadowLayer(8f * density, 2f * density, 2f * density, android.graphics.Color.argb(76, 0, 0, 0))
    })

    // 功德数字（小字）
    canvas.drawText("今日功德 +$todayCount", w / 2f, h / 2f + 25 * density, Paint().apply {
        color = whiteArgb; textSize = 18 * density; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = tf; setShadowLayer(4f * density, 1f * density, 1f * density, android.graphics.Color.argb(50, 0, 0, 0))
    })

    // 水印
    canvas.drawText("解压键盘 · 功德+1", w / 2f, h - 20 * density, Paint().apply {
        color = android.graphics.Color.argb(150, 255, 255, 255); textSize = 15 * density; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = tf
    })

    return bitmap
}
