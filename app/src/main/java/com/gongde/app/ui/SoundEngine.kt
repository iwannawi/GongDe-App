package com.gongde.app.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * 机械键盘轴体类型
 */
enum class SwitchType(
    val label: String,
    /** 频率列表 [Hz] */
    val freqs: DoubleArray,
    /** 各频率振幅 */
    val amps: DoubleArray,
    /** 持续时间 ms */
    val durationMs: Int,
    /** 包络指数：(1-env)^exponent */
    val envelopeExp: Int
) {
    /** 青轴 - 清脆段落感 */
    BLUE(
        label = "青轴",
        freqs = doubleArrayOf(4000.0, 8000.0, 1200.0),
        amps = doubleArrayOf(0.6, 0.3, 0.1),
        durationMs = 35,
        envelopeExp = 4
    ),
    /** 红轴 - 线性轻柔 */
    RED(
        label = "红轴",
        freqs = doubleArrayOf(1200.0, 600.0, 2400.0),
        amps = doubleArrayOf(0.5, 0.4, 0.1),
        durationMs = 25,
        envelopeExp = 2
    ),
    /** 茶轴 - 微段落感 */
    BROWN(
        label = "茶轴",
        freqs = doubleArrayOf(2800.0, 5000.0, 1000.0),
        amps = doubleArrayOf(0.5, 0.3, 0.2),
        durationMs = 30,
        envelopeExp = 3
    )
}

/**
 * 机械键盘声音引擎
 *
 * 支持青轴、红轴、茶轴三种轴体音效以及 ASMR 增强模式和环境雨声。
 * 所有音效通过正弦波合成生成，使用 AudioTrack (MODE_STATIC / MODE_STREAM) 播放。
 */
class SoundEngine {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    // ---------- 共用 AudioAttributes ----------

    /** 游戏类音频属性 —— 低延迟按键反馈 */
    private val gameAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    // ---------- 各轴体预分配的 AudioTrack (MODE_STATIC) ----------

    private val blueTrack: AudioTrack = buildStaticTrack(SwitchType.BLUE)
    private val redTrack: AudioTrack = buildStaticTrack(SwitchType.RED)
    private val brownTrack: AudioTrack = buildStaticTrack(SwitchType.BROWN)

    /** 轴体 → 对应预分配的 AudioTrack */
    private val trackMap: Map<SwitchType, AudioTrack> = mapOf(
        SwitchType.BLUE to blueTrack,
        SwitchType.RED to redTrack,
        SwitchType.BROWN to brownTrack
    )

    // ---------- ASMR 模式 ----------

    /** ASMR 增强音频 —— 包含延迟复制 + 低频隆隆声 + 高频空气感 */
    private val asmrTrack: AudioTrack = buildAsmrTrack()

    // ---------- 雨声 (MODE_STREAM 循环播放) ----------

    /** 雨声流式播放轨道，初始为空，rain() 时创建 */
    @Volatile
    private var rainTrack: AudioTrack? = null

    @Volatile
    private var rainThread: Thread? = null

    @Volatile
    private var isRaining = false

    // =====================================================================
    //  公开 API
    // =====================================================================

    /**
     * 播放指定轴体的键盘点击音
     * @param type 轴体类型
     */
    fun playClick(type: SwitchType) {
        val track = trackMap[type] ?: return
        try { track.stop() } catch (_: IllegalStateException) { }
        track.reloadStaticData()
        track.play()
    }

    /**
     * 播放 ASMR 增强版点击音（含多层延迟混响 + 低频 + 高频空气感）
     */
    fun playAsmrClick() {
        try { asmrTrack.stop() } catch (_: IllegalStateException) { }
        asmrTrack.reloadStaticData()
        asmrTrack.play()
    }

    /**
     * 播放环境雨声（循环白噪 → 棕噪）
     */
    fun playRain() {
        if (isRaining) return
        isRaining = true

        rainThread = Thread({
            // 创建流式 AudioTrack
            val bufferSize = SAMPLE_RATE * 2 // 1 秒的 16-bit mono 样本
            val track = AudioTrack.Builder()
                .setAudioAttributes(gameAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .setEncoding(AUDIO_FORMAT)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            rainTrack = track  // 先赋值，再播放（确保 stopRain 能找到 track）
            track.play()

            // 棕噪（随机行走滤波）：每 2 秒为一个循环块
            val blockSamples = SAMPLE_RATE * 2
            val block = ShortArray(blockSamples)
            var lastVal = 0.0

            while (isRaining) {
                // 生成 2 秒棕噪数据
                for (i in 0 until blockSamples) {
                    // 棕噪 = 白噪的积分（随机行走），加低通平滑
                    lastVal += (Random.nextDouble() - 0.5) * 0.3
                    // 衰减防止溢出
                    lastVal *= 0.998
                    val sample = (lastVal * 5000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    block[i] = sample.toShort()
                }
                // 写入流式轨道
                var offset = 0
                while (offset < blockSamples && isRaining) {
                    val written = track.write(block, offset, blockSamples - offset)
                    if (written < 0) break
                    offset += written
                }
            }

            track.stop()
            track.release()
            rainTrack = null
        }, "rain-engine").apply {
            isDaemon = true
            start()
        }
    }

    /**
     * 停止雨声播放
     */
    fun stopRain() {
        isRaining = false
        rainThread?.join(1000)
        rainThread = null
    }

    /**
     * 释放所有 AudioTrack 资源
     */
    fun release() {
        stopRain()
        trackMap.values.forEach { track ->
            try {
                track.stop()
            } catch (_: IllegalStateException) { }
            track.release()
        }
        try {
            asmrTrack.stop()
        } catch (_: IllegalStateException) { }
        asmrTrack.release()
    }

    // =====================================================================
    //  内部构建方法
    // =====================================================================

    /**
     * 为指定轴体构建 MODE_STATIC AudioTrack 并写入合成波形数据
     */
    private fun buildStaticTrack(type: SwitchType): AudioTrack {
        val samples = synthesizeClick(type.freqs, type.amps, type.durationMs, type.envelopeExp)
        val bufferSize = samples.size * 2 // 16-bit = 2 bytes per sample

        val track = AudioTrack.Builder()
            .setAudioAttributes(gameAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG)
                    .setEncoding(AUDIO_FORMAT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        return track
    }

    /**
     * 合成单次键盘点击波形
     *
     * @param freqs  各正弦波频率 (Hz)
     * @param amps   各正弦波振幅 (0~1)
     * @param durationMs 持续时间 (ms)
     * @param envelopeExp 包络指数，(1 - t/duration)^exp
     * @return 16-bit PCM 样本数组
     */
    private fun synthesizeClick(
        freqs: DoubleArray,
        amps: DoubleArray,
        durationMs: Int,
        envelopeExp: Int
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val durationSec = durationMs / 1000.0
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val tSec = i.toDouble() / SAMPLE_RATE
            val tNorm = tSec / durationSec
            val env = (1.0 - tNorm).pow(envelopeExp)
            var value = 0.0
            for (j in freqs.indices) {
                value += amps[j] * sin(2.0 * Math.PI * freqs[j] * tSec)
            }
            value *= env
            value = value.coerceIn(-1.0, 1.0)
            samples[i] = (value * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * 构建 ASMR 增强点击轨道
     *
     * 在青轴频率基础上：
     * - 添加 3 份延迟副本（20ms / 50ms / 80ms），振幅递减
     * - 添加 100Hz 低频隆隆声 (振幅 0.05)
     * - 添加 12000Hz 高频空气感 (振幅 0.03)
     * - 总时长 60ms
     */
    private fun buildAsmrTrack(): AudioTrack {
        val baseType = SwitchType.BLUE
        val totalDurationMs = 60
        val numSamples = (SAMPLE_RATE * totalDurationMs / 1000.0).toInt()
        val samples = ShortArray(numSamples)

        // 延迟时间点 (ms) 与对应振幅缩放因子
        val delays = intArrayOf(20, 50, 80)
        val delayAmps = doubleArrayOf(0.6, 0.4, 0.2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE           // 真实时间（秒）
            var value = 0.0

            // --- 主音：青轴包络 (1 - env)^4 ---
            val mainDurationSec = baseType.durationMs / 1000.0
            if (t <= mainDurationSec) {
                val tNorm = t / mainDurationSec          // 0~1
                val env = (1.0 - tNorm).pow(baseType.envelopeExp)
                for (j in baseType.freqs.indices) {
                    value += baseType.amps[j] * sin(2.0 * Math.PI * baseType.freqs[j] * t) * env
                }
            }

            // --- 三份延迟副本，振幅递减 ---
            for (d in delays.indices) {
                val delaySec = delays[d] / 1000.0
                val tDelayed = t - delaySec
                if (tDelayed in 0.0..mainDurationSec) {
                    val tNorm = tDelayed / mainDurationSec
                    val env = (1.0 - tNorm).pow(baseType.envelopeExp)
                    for (j in baseType.freqs.indices) {
                        value += baseType.amps[j] * delayAmps[d] * sin(2.0 * Math.PI * baseType.freqs[j] * tDelayed) * env
                    }
                }
            }

            // --- 100Hz 低频隆隆声（振幅 0.05）---
            value += 0.05 * sin(2.0 * Math.PI * 100.0 * t)

            // --- 12000Hz 高频空气感（振幅 0.03）---
            value += 0.03 * sin(2.0 * Math.PI * 12000.0 * t)

            // 全局柔和包络：尾部淡出
            val fadeStart = totalDurationMs * 0.6 / 1000.0
            if (t > fadeStart) {
                val fade = 1.0 - ((t - fadeStart) / (totalDurationMs / 1000.0 - fadeStart))
                value *= fade.coerceIn(0.0, 1.0)
            }

            value = value.coerceIn(-1.0, 1.0)
            samples[i] = (value * Short.MAX_VALUE).toInt().toShort()
        }

        val bufferSize = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(gameAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG)
                    .setEncoding(AUDIO_FORMAT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        return track
    }
}
