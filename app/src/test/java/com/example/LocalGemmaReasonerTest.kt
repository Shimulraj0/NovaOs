package com.example

import com.example.ai.LocalGemmaReasoner
import com.example.data.model.AppTarget
import com.example.data.model.SystemTask
import com.example.data.model.VolumeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalGemmaReasonerTest {

    private val reasoner = LocalGemmaReasoner()

    @Test
    fun testTorchParsing() {
        val onResult = reasoner.reason("turn on flashlight")
        assertEquals(1, onResult.parsedTasks.size)
        val task = onResult.parsedTasks.first() as SystemTask.ToggleTorch
        assertEquals(true, task.enable)
        assertFalse(onResult.requiresConfirmation)

        val offResult = reasoner.reason("turn off torch")
        val offTask = offResult.parsedTasks.first() as SystemTask.ToggleTorch
        assertEquals(false, offTask.enable)
    }

    @Test
    fun testVolumeParsing() {
        val muteResult = reasoner.reason("mute volume")
        val muteTask = muteResult.parsedTasks.first() as SystemTask.AdjustVolume
        assertTrue(muteTask.direction is VolumeDirection.Mute)
        assertFalse(muteResult.requiresConfirmation)

        val pctResult = reasoner.reason("set volume to 80%")
        val pctTask = pctResult.parsedTasks.first() as SystemTask.AdjustVolume
        val dir = pctTask.direction as VolumeDirection.SetAbsolute
        assertEquals(80, dir.percent)
    }

    @Test
    fun testBatteryAndStorage() {
        val batResult = reasoner.reason("how much battery do I have")
        assertTrue(batResult.parsedTasks.first() is SystemTask.CheckBattery)

        val storageResult = reasoner.reason("check storage and memory")
        assertTrue(storageResult.parsedTasks.first() is SystemTask.CheckStorage)
    }

    @Test
    fun testCompoundTaskDecomposition() {
        val compoundResult = reasoner.reason("mute volume and turn on flashlight")
        assertEquals(2, compoundResult.parsedTasks.size)
        assertTrue(compoundResult.parsedTasks[0] is SystemTask.AdjustVolume)
        assertTrue(compoundResult.parsedTasks[1] is SystemTask.ToggleTorch)
        assertFalse(compoundResult.requiresConfirmation)
    }

    @Test
    fun testSensitiveTaskFlagged() {
        val appResult = reasoner.reason("open camera")
        val appTask = appResult.parsedTasks.first() as SystemTask.OpenApp
        assertEquals(AppTarget.Camera, appTask.target)
        assertTrue(appResult.requiresConfirmation)
    }
}
