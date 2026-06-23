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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt
import com.gongde.app.ui.theme.ThemePresets
import com.gongde.app.viewmodel.SettingsAction

@Composable
fun SettingsScreen(
    hapticEnabled: Boolean,
    switchType: String,
    themeId: String,
    onSettingsAction: (SettingsAction) -> Unit = {},
    onReset: () -> Unit = {}
) {
    val accent = GongDeThemeExt.colors.accent
    val colors = GongDeThemeExt.colors
    val context = LocalContext.current
    val hapticPreview = remember { HapticEngine(context) }
    var showResetDialog by remember { mutableStateOf(false) }
    DisposableEffect(hapticPreview) {
        onDispose { hapticPreview.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 32.dp)
    ) {
        Text("设置", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        // ===== 触觉反馈 =====
        SectionTitle("触觉反馈")
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("按键震动", color = colors.textPrimary, fontSize = 15.sp)
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { enabled ->
                        onSettingsAction(SettingsAction.SetHaptic(enabled))
                        if (enabled) hapticPreview.tick()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.accent,
                        uncheckedThumbColor = colors.textPrimary,
                        uncheckedTrackColor = colors.barTrack,
                        uncheckedBorderColor = colors.unselected
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
                    onSettingsAction(SettingsAction.SetSwitchType(it))
                }
                Spacer(Modifier.height(8.dp))
                SwitchOption("红轴 · 柔和", "red", switchType, accent) {
                    onSettingsAction(SettingsAction.SetSwitchType(it))
                }
                Spacer(Modifier.height(8.dp))
                SwitchOption("茶轴 · 适中", "brown", switchType, accent) {
                    onSettingsAction(SettingsAction.SetSwitchType(it))
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
                        onClick = { onSettingsAction(SettingsAction.SetTheme(id)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("数据管理")
        OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
        ) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("清零功德计数")
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("隐私")
        Text(
            "用于统计匿名的启动、按键里程碑、模式进入和分享事件；不主动上传功德明细、分享内容、姓名或联系方式。",
            color = colors.textMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = colors.dialogBg,
            title = { Text("确认清零", color = colors.textPrimary) },
            text = { Text("累计功德和今日功德将归零，成就和历史记录会保留。", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { showResetDialog = false; onReset() }) {
                    Text("清零", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("取消", color = colors.textSecondary) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = GongDeThemeExt.colors.textMuted, fontSize = 15.sp, letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GongDeThemeExt.colors.cardBorder, RoundedCornerShape(8.dp))
            .background(GongDeThemeExt.colors.cardBg)
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
    val colors = GongDeThemeExt.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accent.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onSelect(value) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accent,
                unselectedColor = GongDeThemeExt.colors.mutedGray
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = colors.textPrimary, fontSize = 15.sp)
    }
}

/** 主题选项（含背景渐变 + 强调色预览） */
@Composable
private fun ThemeOption(
    name: String, gradient: List<Color>, accentColor: Color,
    selected: Boolean, onClick: () -> Unit
) {
    val colors = GongDeThemeExt.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (selected) Modifier.border(1.5.dp, colors.accent, RoundedCornerShape(8.dp))
                else Modifier.border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
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
        Text(name, color = colors.textPrimary, fontSize = 15.sp)
        if (selected) {
            Spacer(Modifier.weight(1f))
            Text("当前", color = colors.accent, fontSize = 15.sp)
        }
    }
}
