/**
 * 功德分享卡片组件
 *
 * 生成可截图分享的功德展示卡片，包含：
 * - 深色渐变背景与装饰性圆圈图案
 * - 键帽小图标
 * - "功德 +{count}" 金色大字
 * - 随机禅语
 * - App 品牌水印
 * - 系统分享 Intent（截图 + 分享）
 */

package com.gongde.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.gongde.app.R
import com.gongde.app.ui.theme.GoldColor
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

// ==================== 颜色常量 ====================

private val DeepPurple = Color(0xFF1A0033)
private val MediumPurple = Color(0xFF2D1055)
private val MutedGray = Color(0x55B0BEC5)
private val CircleBorder = Color(0x22FFFFFF)

// ==================== 禅语库 ====================

private val ZEN_QUOTES = listOf(
    "一念清净，功德自来",
    "心若菩提，步步生莲",
    "万般带不走，唯有业随身",
    "静坐常思己过，闲谈莫论人非",
    "一切有为法，如梦幻泡影",
    "菩提本无树，明镜亦非台",
    "色即是空，空即是色",
    "心无挂碍，无有恐怖",
    "应无所住而生其心",
    "知足常乐，能忍自安",
    "善恶到头终有报，人间正道是沧桑",
    "种如是因，收如是果",
    "放下屠刀，立地成佛",
    "苦海无边，回头是岸",
    "一花一世界，一叶一菩提",
)

/**
 * 获取一条随机禅语
 */
fun getRandomZenQuote(): String = ZEN_QUOTES[Random.nextInt(ZEN_QUOTES.size)]

/**
 * 分享功德卡片视图
 *
 * @param totalCount 累计功德总数
 * @param modifier 外部修饰符
 */
@Composable
fun ShareCardView(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val zenQuote = remember(totalCount) { getRandomZenQuote() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(DeepPurple, MediumPurple)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // 装饰性背景圆圈
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(CircleBorder, size.width * 0.45f, Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
            drawCircle(CircleBorder, size.width * 0.32f, Offset(cx, cy - size.height * 0.05f), style = Stroke(1.dp.toPx()))
            drawCircle(CircleBorder, size.width * 0.18f, Offset(cx, cy - size.height * 0.02f), style = Stroke(0.8.dp.toPx()))
            drawCircle(CircleBorder, size.width * 0.12f, Offset(size.width * 0.12f, size.height * 0.2f), style = Stroke(0.8.dp.toPx()))
            drawCircle(CircleBorder, size.width * 0.1f, Offset(size.width * 0.88f, size.height * 0.75f), style = Stroke(0.8.dp.toPx()))
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

            // 随机禅语
            Text(
                text = zenQuote,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App 品牌水印
            Text(
                text = "解压键盘 · 功德计数",
                color = MutedGray,
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
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, GoldColor, RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .clickable {
                shareMeritCard(context, totalCount)
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
                text = "分享功德",
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
private fun shareMeritCard(context: Context, totalCount: Int) {
    try {
        val bitmap = renderShareBitmap(context, totalCount)
        val file = File(context.cacheDir, "share_merit.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享功德"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 将功德卡片渲染为 Bitmap（300x200dp → 对应像素）
 */
private fun renderShareBitmap(context: Context, totalCount: Int): Bitmap {
    val density = context.resources.displayMetrics.density
    val widthPx = (300 * density).toInt()
    val heightPx = (200 * density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    // 绘制渐变背景
    val bgPaint = android.graphics.Paint().apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, heightPx.toFloat(),
            0xFF1A0033.toInt(), 0xFF2D1055.toInt(),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), bgPaint)

    // 绘制功德数字
    val textPaint = android.graphics.Paint().apply {
        color = 0xFFFFD54F.toInt()
        textSize = 36 * density
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText("功德 +$totalCount", widthPx / 2f, heightPx / 2f, textPaint)

    // 绘制禅语
    val quotePaint = android.graphics.Paint().apply {
        color = 0xB3FFFFFF.toInt()
        textSize = 12 * density
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText(getRandomZenQuote(), widthPx / 2f, heightPx / 2f + 40 * density, quotePaint)

    // 绘制水印
    val watermarkPaint = android.graphics.Paint().apply {
        color = 0x55B0BEC5
        textSize = 9 * density
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText("解压键盘 · 功德计数", widthPx / 2f, heightPx - 20 * density, watermarkPaint)

    return bitmap
}
