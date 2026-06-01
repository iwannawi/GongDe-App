/**
 * 设置页面
 *
 * 提供应用各项配置的界面，包括：
 * - 触觉反馈开关
 * - 机械轴声音选择（青/红/茶轴）
 * - 主题切换（4 种预设）
 * - 冥想模式入口
 * - ASMR 模式入口
 */
package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.CardBgColorColor
import com.gongde.app.ui.theme.CardBorderColor
import com.gongde.app.ui.theme.GoldColorColor
import com.gongde.app.ui.theme.MutedGrayColor
import com.gongde.app.ui.theme.ThemePresets

// 颜色从 ui.theme.Color 统一导入

/**
 * 设置页面主入口
 *
 * @param hapticEnabled 当前触觉反馈状态
 * @param switchType 当前轴体类型
 * @param themeId 当前主题 ID
 * @param onSettingsChange 设置变更回调 (key, value)
 * @param onOpenMeditation 点击冥想模式的回调
 * @param onOpenAsmr 点击 ASMR 模式的回调
 */
@Composable
fun SettingsScreen(
    hapticEnabled: Boolean,
    switchType: String,
    themeId: String,
    onSettingsChange: (String, Any) -> Unit = { _, _ -> },
    onOpenMeditation: () -> Unit = {},
    onOpenAsmr: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 页面标题
        Text("设置", color = GoldColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        // ===== 触觉反馈 =====
        SectionTitle("触觉反馈")
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("按键震动", color = Color.White, fontSize = 14.sp)
                // 开关按钮
                Box(
                    modifier = Modifier
                        .width(48.dp).height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (hapticEnabled) GoldColor else Color(0xFF333333))
                        .clickable {
                            onSettingsChange("haptic", !hapticEnabled)
                        },
                    contentAlignment = if (hapticEnabled) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        Modifier.padding(3.dp).size(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== 机械轴声音 =====
        SectionTitle("按键音效")
        SettingsCard {
            Column {
                SwitchOption("青轴 · 清脆", "blue", switchType) {
                    onSettingsChange("switch", it)
                }
                Spacer(Modifier.height(8.dp))
                SwitchOption("红轴 · 柔和", "red", switchType) {
                    onSettingsChange("switch", it)
                }
                Spacer(Modifier.height(8.dp))
                SwitchOption("茶轴 · 适中", "brown", switchType) {
                    onSettingsChange("switch", it)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== 主题切换 =====
        SectionTitle("主题")
        SettingsCard {
            Column {
                ThemePresets.allThemeIds.forEachIndexed { index, id ->
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    ThemeOption(
                        name = ThemePresets.getDisplayName(id),
                        gradient = ThemePresets.getGradient(id),
                        selected = themeId == id,
                        onClick = { onSettingsChange("theme", id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== 特殊模式 =====
        SectionTitle("特殊模式")
        SettingsCard {
            Column {
                ModeEntry("🧘 冥想模式", "静心计数，每3秒自动+1") { onOpenMeditation() }
                Spacer(Modifier.height(8.dp))
                ModeEntry("🎧 ASMR 模式", "沉浸式音效，双手拇指操作") { onOpenAsmr() }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

/** 区域标题 */
@Composable
private fun SectionTitle(text: String) {
    Text(text, color = MutedGrayColor, fontSize = 12.sp, letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 8.dp))
}

/** 设置卡片容器 */
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
            .background(CardBgColor)
            .padding(16.dp),
        content = content
    )
}

/** 声音选项（单选） */
@Composable
private fun SwitchOption(
    label: String, value: String, selected: String, onSelect: (String) -> Unit
) {
    val isSelected = value == selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0x15FFD54F) else Color.Transparent)
            .clickable { onSelect(value) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 单选圆点
        Box(
            Modifier.size(16.dp).clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, if (isSelected) GoldColor else MutedGrayColor, RoundedCornerShape(8.dp))
        ) {
            if (isSelected) {
                Box(Modifier.padding(3.dp).fillMaxSize()
                    .clip(RoundedCornerShape(5.dp)).background(GoldColor))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}

/** 主题选项 */
@Composable
private fun ThemeOption(
    name: String, gradient: List<Color>, selected: Boolean, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (selected) Modifier.border(1.5.dp, GoldColor, RoundedCornerShape(10.dp))
                else Modifier.border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 颜色预览条
        Box(
            Modifier.size(32.dp, 16.dp).clip(RoundedCornerShape(4.dp))
                .background(gradient[1])
        )
        Spacer(Modifier.width(12.dp))
        Text(name, color = Color.White, fontSize = 14.sp)
        if (selected) {
            Spacer(Modifier.weight(1f))
            Text("当前", color = GoldColor, fontSize = 12.sp)
        }
    }
}

/** 模式入口 */
@Composable
private fun ModeEntry(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = MutedGrayColor, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Text("›", color = MutedGrayColor, fontSize = 20.sp)
    }
}
