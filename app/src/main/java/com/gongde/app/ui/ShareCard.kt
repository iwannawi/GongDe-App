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
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.content.FileProvider
import com.gongde.app.R
import com.gongde.app.ui.theme.GongDeThemeExt
import java.io.FileOutputStream
import kotlin.random.Random

// 随机背景渐变色组合（明快、欢快色调）
private val BG_PALETTES = listOf(
    listOf(Color(0xFF667eea), Color(0xFF764ba2)),  // 紫蓝梦幻
    listOf(Color(0xFFf093fb), Color(0xFFf5576c)),  // 粉红浪漫
    listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),  // 蓝青清爽
    listOf(Color(0xFF43e97b), Color(0xFF38f9d7)),  // 绿翠生机
    listOf(Color(0xFFfa709a), Color(0xFFfee140)),  // 粉黄活泼
    listOf(Color(0xFFa18cd1), Color(0xFFfbc2eb)),  // 淡紫温柔
    listOf(Color(0xFFfccb90), Color(0xFFd57eeb)),  // 橙紫夕阳
    listOf(Color(0xFFe0c3fc), Color(0xFF8ec5fc)),  // 紫蓝天空
)

@Composable
fun ShareCardView(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val funQuote = remember(totalCount) { getRandomFunQuote() }
    val colors = GongDeThemeExt.colors
    val accent = colors.accent
    // 随机选择一个背景色板
    val palette = remember(totalCount) { BG_PALETTES[Random.nextInt(BG_PALETTES.size)] }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        // 装饰性柔和光斑
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // 大柔光圆
            drawCircle(Color.White.copy(alpha = 0.08f), w * 0.5f, Offset(w * 0.2f, h * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.06f), w * 0.4f, Offset(w * 0.8f, h * 0.7f))
            drawCircle(Color.White.copy(alpha = 0.1f), w * 0.25f, Offset(w * 0.6f, h * 0.15f))
            // 小亮点
            drawCircle(Color.White.copy(alpha = 0.3f), 3f, Offset(w * 0.15f, h * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.25f), 2f, Offset(w * 0.85f, h * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.2f), 2.5f, Offset(w * 0.3f, h * 0.8f))
            drawCircle(Color.White.copy(alpha = 0.3f), 2f, Offset(w * 0.7f, h * 0.85f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.keycap_off),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp, 56.dp).graphicsLayer { alpha = 0.9f }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "今日功德 +$totalCount",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.3f), offset = Offset(2f, 2f), blurRadius = 8f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = funQuote,
                color = Color.White,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
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
    totalCount: Int,
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    val colors = GongDeThemeExt.colors

    if (showPreview) {
        SharePreviewDialog(
            totalCount = totalCount,
            onDismiss = { showPreview = false },
            onShare = {
                showPreview = false
                val accentArgb = colors.accent.toArgb()
                shareMeritCard(context, totalCount, accentArgb)
            }
        )
    }

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
                Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(18.dp)
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

@Composable
private fun SharePreviewDialog(
    totalCount: Int,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val colors = GongDeThemeExt.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.dialogBg,
        title = {
            Text("分享预览", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            ShareCardView(totalCount = totalCount)
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Text("分享", color = colors.accent, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.textSecondary, fontSize = 15.sp)
            }
        }
    )
}

private fun shareMeritCard(
    context: Context,
    totalCount: Int,
    accentArgb: Int
) {
    try {
        val bitmap = renderShareBitmap(context, totalCount, accentArgb)
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
    totalCount: Int,
    accentArgb: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val w = (300 * density).toInt()
    val h = (260 * density).toInt()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val tf = Typeface.DEFAULT

    // 随机渐变背景
    val palette = BG_PALETTES[Random.nextInt(BG_PALETTES.size)]
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

    // 功德数字
    val whiteArgb = Color.White.toArgb()
    canvas.drawText("今日功德 +$totalCount", w / 2f, h / 2f, Paint().apply {
        color = whiteArgb; textSize = 32 * density; isFakeBoldText = true; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = tf; setShadowLayer(8f * density, 2f * density, 2f * density, android.graphics.Color.argb(76, 0, 0, 0))
    })

    // 语录
    canvas.drawText(getRandomFunQuote(), w / 2f, h / 2f + 40 * density, Paint().apply {
        color = android.graphics.Color.argb(230, 255, 255, 255); textSize = 15 * density; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = tf; setShadowLayer(4f * density, 1f * density, 1f * density, android.graphics.Color.argb(50, 0, 0, 0))
    })

    // 水印
    canvas.drawText("解压键盘 · 功德+1", w / 2f, h - 20 * density, Paint().apply {
        color = android.graphics.Color.argb(150, 255, 255, 255); textSize = 15 * density; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = tf
    })

    return bitmap
}
