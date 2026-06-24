package com.gongde.app.ui

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class SoundEngineTest {

    @Test
    fun `SwitchType 枚举包含三种轴体`() {
        assertEquals(3, SwitchType.entries.size)
        assertNotNull(SwitchType.BLUE)
        assertNotNull(SwitchType.RED)
        assertNotNull(SwitchType.BROWN)
    }

    @Test
    fun `BLUE 轴参数正确`() {
        assertEquals("青轴", SwitchType.BLUE.label)
        assertEquals(35, SwitchType.BLUE.durationMs)
        assertEquals(4, SwitchType.BLUE.envelopeExp)
        assertEquals(3, SwitchType.BLUE.freqs.size)
        assertEquals(3, SwitchType.BLUE.amps.size)
        assertEquals(4000.0, SwitchType.BLUE.freqs[0], 0.1)
        assertEquals(8000.0, SwitchType.BLUE.freqs[1], 0.1)
    }

    @Test
    fun `RED 轴参数正确`() {
        assertEquals("红轴", SwitchType.RED.label)
        assertEquals(25, SwitchType.RED.durationMs)
        assertEquals(2, SwitchType.RED.envelopeExp)
        assertEquals(1200.0, SwitchType.RED.freqs[0], 0.1)
    }

    @Test
    fun `BROWN 轴参数正确`() {
        assertEquals("茶轴", SwitchType.BROWN.label)
        assertEquals(30, SwitchType.BROWN.durationMs)
        assertEquals(3, SwitchType.BROWN.envelopeExp)
        assertEquals(2800.0, SwitchType.BROWN.freqs[0], 0.1)
    }

    @Test
    fun `振幅在合法范围内`() {
        SwitchType.entries.forEach { type ->
            type.amps.forEach { amp ->
                assertTrue("$type 振幅 $amp 应在 0~1 之间", amp in 0.0..1.0)
            }
        }
    }

    @Test
    fun `频率为正值`() {
        SwitchType.entries.forEach { type ->
            type.freqs.forEach { freq ->
                assertTrue("$type 频率 $freq 应为正值", freq > 0)
            }
        }
    }

    @Test
    fun `SoundEngine 实例可创建`() {
        val engine = SoundEngine()
        assertNotNull(engine)
        engine.release()
    }

    @Test
    fun `SoundEngine release 不抛异常`() {
        val engine = SoundEngine()
        engine.release() // 不应崩溃
    }

    @Test
    fun `SoundEngine warmUp 不抛异常`() {
        val engine = SoundEngine()
        engine.warmUp()
        Thread.sleep(100) // 给预热线程一点时间
        engine.release()
    }

    @Test
    fun `SoundEngine playClick 不在主线程阻塞`() {
        val engine = SoundEngine()
        val start = System.currentTimeMillis()
        engine.playClick(SwitchType.BLUE)
        val elapsed = System.currentTimeMillis() - start
        engine.release()
        // 播放操作不应阻塞超过 100ms
        assertTrue("playClick 耗时 ${elapsed}ms", elapsed < 100)
    }

    @Test
    fun `SoundEngine 对未预热的轴体安全处理`() {
        val engine = SoundEngine()
        // 直接播放未预热的轴体，不应崩溃
        engine.playClick(SwitchType.RED)
        engine.playClick(SwitchType.BROWN)
        engine.release()
    }

    @Test
    fun `SoundEngine 雨声生命周期正确`() {
        val engine = SoundEngine()
        engine.playRain()
        Thread.sleep(50)
        engine.stopRain()
        engine.release()
    }

    @Test
    fun `SoundEngine 重复 release 不崩溃`() {
        val engine = SoundEngine()
        engine.release()
        engine.release()
    }
}
