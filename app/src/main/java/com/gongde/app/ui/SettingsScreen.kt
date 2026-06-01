/**
 * 设置页面
 *
 * 使用 Material3 Switch / RadioButton 组件，支持无障碍。
 */
package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.CardBgColor
import com.gongde.app.ui.theme.CardBorderColor
import com.gongde.app.ui.theme.GoldColor
import com.gongde.app.ui.theme.GongDeThemeExt
import com.gongde.app.ui.theme.MutedGrayColor
import com.gongde.app.ui.theme.KeycapRed
import com.gongde.app.ui.theme.KeycapLightRed
import com.gongde.app.ui.theme.ThemePresets

@Composable
fun SettingsScreen(
    hapticEnabled: Boolean,
    switchType: String,
    themeId: String,
    onSettingsChange: (String, Any) -> Unit = { _, _ -> },
    onOpenMeditation: () -> Unit = {},
    onOpenAsmr: () -> Unit = {}
) {
    val accent = GongDeThemeExt.colors.accent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
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
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { onSettingsChange("haptic", it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GoldColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF333333),
                        uncheckedBorderColor = Color(0xFF555555)
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== 机械轴声音 =====
        SectionTitle("按键音效")
        SettingsCard {
            Column {
                SwitchOption("青轴 · 清脆", "blue", switchType, accent) {
                    onSettingsChange("switch", it)
                }
                Spacer(Modifier.height(8.dp))
                SwitchOption("红轴 · 柔和", "red", switchType, accent) {
                    onSettingsChange("switch", it)
                }
                Spacer(Modifier.height(8.dp))
                SwitchOption("茶轴 · 适中", "brown", switchType, accent) {
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
                        accentColor = ThemePresets.getAccent(id),
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

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = MutedGrayColor, fontSize = 12.sp, letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 8.dp))
}

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

/** 声音选项（Material3 RadioButton） */
@Composable
private fun SwitchOption(
    label: String, value: String, selected: String, accent: Color,
    onSelect: (String) -> Unit
) {
    val isSelected = value == selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) accent.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onSelect(value) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(
                selectedColor = GoldColor,
                unselectedColor = MutedGrayColor
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}

/** 主题选项（含背景渐变 + 强调色预览） */
@Composable
private fun ThemeOption(
    name: String, gradient: List<Color>, accentColor: Color,
    selected: Boolean, onClick: () -> Unit
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
        // 背景色预览
        Box(
            Modifier.size(28.dp, 16.dp).clip(RoundedCornerShape(4.dp))
                .background(gradient[1])
        )
        Spacer(Modifier.width(4.dp))
        // 强调色预览
        Box(
            Modifier.size(28.dp, 16.dp).clip(RoundedCornerShape(4.dp))
                .background(accentColor)
        )
        Spacer(Modifier.width(12.dp))
        Text(name, color = Color.White, fontSize = 14.sp)
        if (selected) {
            Spacer(Modifier.weight(1f))
            Text("当前", color = GoldColor, fontSize = 12.sp)
        }
    }
}

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
