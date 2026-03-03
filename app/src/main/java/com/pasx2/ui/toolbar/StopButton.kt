package com.pasx2.ui.toolbar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.pasx2.EmuState
import com.pasx2.Main
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.PauseSolid
import compose.icons.lineawesomeicons.PlaySolid
import compose.icons.lineawesomeicons.StopSolid

class StopButton : ToolbarButton() {
    override var icon = mutableStateOf<ImageVector?>(LineAwesomeIcons.StopSolid)
    override fun isVisible(): Boolean {
        return Main.eState.value != EmuState.STOPPED
    }
    override fun action() {
        Main.stop()
    }
}