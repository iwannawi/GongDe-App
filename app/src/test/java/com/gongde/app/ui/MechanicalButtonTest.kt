package com.gongde.app.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MechanicalButtonTest {
    private val buttonSize = IntSize(220, 250)
    private val imageSize = IntSize(768, 768)

    @Test
    fun keycapTopPlane_acceptsPointInsideTopSurface() {
        assertTrue(isInsideKeycapTopPlane(Offset(112f, 96f), buttonSize, imageSize))
    }

    @Test
    fun keycapTopPlane_rejectsSwitchAndBase() {
        assertFalse(isInsideKeycapTopPlane(Offset(110f, 155f), buttonSize, imageSize))
        assertFalse(isInsideKeycapTopPlane(Offset(110f, 205f), buttonSize, imageSize))
    }

    @Test
    fun keycapTopPlane_rejectsTransparentImageEdges() {
        assertFalse(isInsideKeycapTopPlane(Offset(10f, 10f), buttonSize, imageSize))
        assertFalse(isInsideKeycapTopPlane(Offset(210f, 120f), buttonSize, imageSize))
    }
}
