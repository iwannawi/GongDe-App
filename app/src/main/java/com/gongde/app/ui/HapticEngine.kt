package com.gongde.app.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 触觉反馈引擎
 *
 * 为机械键盘按键提供短促的触觉震动反馈。
 * 支持 API 26+ (VibrationEffect) 以及更低版本的降级方案。
 */
class HapticEngine(context: Context) {

    /** 系统振动器服务 */
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // API 31+：通过 VibratorManager 获取
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * 短促点击触觉反馈（20ms，振幅 128）
     *
     * 适用于按键触发、段落感模拟等场景。
     * 仅在设备支持振动器时执行。
     */
    fun tick() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+：使用 VibrationEffect 精确控制振幅
            v.vibrate(VibrationEffect.createOneShot(20, 128))
        } else {
            // API 25 及以下：降级方案，无法指定振幅
            @Suppress("DEPRECATION")
            v.vibrate(20)
        }
    }

    /**
     * 释放振动器资源
     *
     * 当前 Vibrator 由系统管理，无需显式释放，
     * 此方法预留用于未来可能的资源清理扩展。
     */
    fun release() {
        // Vibrator 由系统服务持有，无需额外释放
        // 预留接口以便后续扩展
    }
}
