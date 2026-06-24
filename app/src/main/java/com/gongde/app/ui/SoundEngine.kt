package com.gongde.app.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
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
 * 支持青轴、红轴、茶轴三种声音主题，以及可复用的环境雨声能力。
 * 所有音效通过正弦波合成生成，使用 AudioTrack (MODE_STATIC / MODE_STREAM) 播放。
 */
class SoundEngine {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private val gameAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val tracks = mutableMapOf<SwitchType, AudioTrack>()

    /**
     * 后台预热：提前创建最常用的 AudioTrack，避免首次点击卡顿
     * 应在 app 启动时通过协程调用
     */
    fun warmUp(defaultType: SwitchType = SwitchType.BLUE) {
        Thread({
            try { getTrack(defaultType) } catch (e: Exception) { Log.w("SoundEngine", "warmUp track failed", e) }
        }, "audio-warmup").apply { isDaemon = true; start() }
    }

    @Synchronized
    private fun getTrack(type: SwitchType): AudioTrack? {
        tracks[type]?.let { return it }
        return try {
            buildStaticTrack(type).also { tracks[type] = it }
        } catch (_: Exception) { null }
    }

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
    @Synchronized
    fun playClick(type: SwitchType) {
        val track = getTrack(type) ?: return
        try { track.stop() } catch (_: IllegalStateException) { }
        track.reloadStaticData()
        track.play()
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
                    if (!isRaining) break
                    lastVal += (Random.nextDouble() - 0.5) * 0.3
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

            try { track.stop() } catch (_: IllegalStateException) { }
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
        rainTrack?.let { track ->
            try { track.pause() } catch (_: IllegalStateException) { }
            try { track.flush() } catch (_: IllegalStateException) { }
        }
        val thread = rainThread
        if (thread != null && thread !== Thread.currentThread()) {
            try {
                thread.join(150)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        if (rainThread?.isAlive != true) {
            rainThread = null
        }
    }

    /**
     * 释放所有 AudioTrack 资源
     */
    @Synchronized
    fun release() {
        stopRain()
        tracks.values.forEach { track ->
            try { track.stop() } catch (_: IllegalStateException) { }
            track.release()
        }
        tracks.clear()
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

}
