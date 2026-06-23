package com.gongde.app.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.gongde.app.ui.theme.GongDeThemeExt
import java.io.File
import java.io.FileOutputStream

private val SharePaperTop = Color(0xFFF9FBFC)
private val SharePaperBottom = Color(0xFFE8EEF2)
private val ShareRed = Color(0xFFD83A31)

@Composable
fun ShareButton(
    todayCount: Int,
    modifier: Modifier = Modifier,
    onShareStarted: () -> Unit = {},
    onShareCompleted: () -> Unit = {},
    context: Context = LocalContext.current
) {
    var showPreview by remember { mutableStateOf(false) }
    var currentQuote by remember { mutableStateOf(getRandomFunQuote()) }

    val refreshContent = { currentQuote = getRandomFunQuote() }
    val openPreview = { showPreview = true }

    if (showPreview) {
        SharePreviewDialogWithContent(
            todayCount = todayCount,
            quote = currentQuote,
            onDismiss = {
                showPreview = false
                refreshContent()
            },
            onShare = {
                onShareStarted()
                if (shareMeritCard(context, todayCount, currentQuote)) {
                    onShareCompleted()
                }
                showPreview = false
                refreshContent()
            }
        )
    }

    ShareSummaryCard(
        todayCount = todayCount,
        quote = currentQuote,
        modifier = modifier,
        onClick = openPreview
    )
}

@Composable
private fun ShareSummaryCard(
    todayCount: Int,
    quote: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = GongDeThemeExt.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ShareCardViewWithContent(
            todayCount = todayCount,
            quote = quote,
            compact = true,
            modifier = Modifier
                .width(88.dp)
                .height(118.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "今日卡片",
                color = colors.textMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "今日功德 +$todayCount",
                color = colors.textPrimary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = quote,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("分享", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ShareCardViewWithContent(
    todayCount: Int,
    quote: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val horizontalPadding = if (compact) 8.dp else 18.dp
    val verticalPadding = if (compact) 8.dp else 18.dp
    val brandSize = if (compact) 8.sp else 12.sp
    val labelSize = if (compact) 8.sp else 13.sp
    val countSize = if (compact) 24.sp else 42.sp
    val quoteSize = if (compact) 8.sp else 16.sp
    val quoteLineHeight = if (compact) 10.sp else 21.sp
    val watermarkSize = if (compact) 7.sp else 12.sp
    val quoteMaxLines = if (compact) 2 else 4

    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(SharePaperTop, SharePaperBottom))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(Color.White.copy(alpha = 0.7f), w * 0.46f, Offset(w * 0.12f, h * 0.12f))
            drawCircle(Color(0x227C8792), w * 0.38f, Offset(w * 0.92f, h * 0.88f))
            drawLine(
                color = Color(0x1F15191D),
                start = Offset(w * 0.2f, h * 0.73f),
                end = Offset(w * 0.8f, h * 0.73f),
                strokeWidth = 1.dp.toPx()
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("解压键盘", color = Color(0xFF5E6670), fontSize = brandSize, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text("今日功德", color = Color(0xFF7C838E), fontSize = labelSize)
            Text(
                text = "+$todayCount",
                color = ShareRed,
                fontSize = countSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(if (compact) 5.dp else 14.dp))
            Text(
                text = quote,
                color = Color(0xFF15191D),
                fontSize = quoteSize,
                lineHeight = quoteLineHeight,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = quoteMaxLines,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Text("功德+1 · 放松一下", color = Color(0xFF7C838E), fontSize = watermarkSize)
        }
    }
}

@Composable
private fun SharePreviewDialogWithContent(
    todayCount: Int,
    quote: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val colors = GongDeThemeExt.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.dialogBg,
        title = { Text("分享预览", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            ShareCardViewWithContent(
                todayCount = todayCount,
                quote = quote,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Text("分享", color = colors.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
    todayCount: Int,
    quote: String
): Boolean {
    try {
        val bitmap = renderShareBitmap(todayCount, quote)
        try {
            val shareDir = File(context.cacheDir, "share").also { it.mkdirs() }
            val file = File(shareDir, "share_merit_${System.currentTimeMillis()}.png")
            shareDir.listFiles()?.filter { it != file }?.forEach { it.delete() }
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newUri(context.contentResolver, "今日功德", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "分享今日功德"))
            return true
        } finally {
            bitmap.recycle()
        }
    } catch (e: Exception) {
        Log.e("ShareCard", "Failed to share merit card", e)
        return false
    }
}

private fun renderShareBitmap(
    todayCount: Int,
    quote: String
): Bitmap {
    val density = 1f
    val w = 1080
    val h = 1440
    val bitmap = createBitmap(w, h)
    val canvas = AndroidCanvas(bitmap)
    val tf = Typeface.DEFAULT

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            SharePaperTop.toArgb(),
            SharePaperBottom.toArgb(),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(118, 255, 255, 255)
        canvas.drawCircle(w * 0.18f, h * 0.14f, w * 0.38f, this)
        color = android.graphics.Color.argb(28, 124, 135, 146)
        canvas.drawCircle(w * 0.92f, h * 0.88f, w * 0.34f, this)
    }

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF5E6670).toArgb()
        textSize = 38f * density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(tf, Typeface.BOLD)
    }
    canvas.drawText("解压键盘", w / 2f, h * 0.14f, titlePaint)

    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF7C838E).toArgb()
        textSize = 44f * density
        textAlign = Paint.Align.CENTER
        typeface = tf
    }
    canvas.drawText("今日功德", w / 2f, h * 0.31f, labelPaint)

    val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ShareRed.toArgb()
        textSize = 138f * density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(tf, Typeface.BOLD)
    }
    canvas.drawText("+$todayCount", w / 2f, h * 0.43f, countPaint)

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(36, 21, 25, 29)
        strokeWidth = 2f * density
    }
    canvas.drawLine(w * 0.23f, h * 0.52f, w * 0.77f, h * 0.52f, linePaint)

    val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF15191D).toArgb()
        textSize = 54f * density
        typeface = Typeface.create(tf, Typeface.BOLD)
    }
    drawCenteredStaticText(
        canvas = canvas,
        text = quote,
        paint = quotePaint,
        centerX = w / 2f,
        centerY = h * 0.65f,
        width = (w - 180 * density).toInt(),
        maxHeight = (260 * density).toInt()
    )

    val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF7C838E).toArgb()
        textSize = 36f * density
        textAlign = Paint.Align.CENTER
        typeface = tf
    }
    canvas.drawText("功德+1 · 放松一下", w / 2f, h - 118f * density, watermarkPaint)

    return bitmap
}

private fun drawCenteredStaticText(
    canvas: AndroidCanvas,
    text: String,
    paint: TextPaint,
    centerX: Float,
    centerY: Float,
    width: Int,
    maxHeight: Int
) {
    var layout: StaticLayout
    do {
        layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .build()
        if (layout.height > maxHeight) paint.textSize *= 0.9f
    } while (layout.height > maxHeight && paint.textSize > 24f)

    canvas.save()
    canvas.translate(centerX - width / 2f, centerY - layout.height / 2f)
    layout.draw(canvas)
    canvas.restore()
}
