/**
 * 应用排版（Typography）定义
 *
 * 定义功德应用各文本层级的字体样式，
 * 包括标题、正文、标签等，并设置与主题匹配的颜色。
 */

package com.gongde.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

/**
 * 全局排版配置
 *
 * 基于 Material3 Typography 系统定义六个文本层级：
 * - headlineLarge：大标题（28sp，金色，粗体）
 * - headlineMedium：中标题（22sp，白色，粗体）
 * - titleLarge：大标题（20sp，白色，粗体）
 * - bodyLarge：大号正文（16sp，白色，常规）
 * - bodyMedium：正文（14sp，次要灰色，常规）
 * - labelLarge：大号标签（18sp，金色，粗体）
 *
 * 所有样式均使用系统默认字体族（FontFamily.Default）。
 */
val Typography = Typography(
    // 大标题：用于最重要的页面级标题
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = TextGold          // 金色高亮
    ),
    // 中标题：用于二级标题或重要信息
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = TextWhite         // 白色文字
    ),
    // 大标题：用于三级标题
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TextWhite         // 白色文字
    ),
    // 大号正文：用于主要内容文本
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextWhite         // 白色文字
    ),
    // 正文：用于辅助说明或次要内容
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary     // 次要灰色
    ),
    // 大号标签：用于按钮文字或标签
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = TextGold          // 金色高亮
    )
)
