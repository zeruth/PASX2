package com.pasx2.ui.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pasx2.ext.ModifierExt.onPress
import com.pasx2.ui.ToolbarImpl

open class ToolbarButton {
    open var icon = mutableStateOf<ImageVector?>(null)
    open var drawerSize = mutableIntStateOf(200)

    var background = mutableStateOf(Color.Companion.Cyan)
    var expanded = mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Button() {
        Box(
            Modifier.Companion.clip(RoundedCornerShape(4.dp)).size(34.dp)
                .background(background.value)
                .onPress { action() }) {
            Content()
        }
    }

    @Composable
    open fun BoxScope.Content() {
        Box(
            Modifier.size(30.dp).background(Color.Companion.Transparent)
                .align(Alignment.Companion.Center)
        ) {
            Icon()
        }
    }

    @Composable
    fun BoxScope.Icon() {
        Box(
            Modifier.background(Color.Companion.Transparent)
                .align(Alignment.Companion.Center)
        ) {
            icon.value?.let {
                Image(it, "")
            }
        }
    }

    open fun action() {
        if (!expanded.value) {
            if (ToolbarImpl.expanded.value != drawerSize.intValue) {
                ToolbarImpl.expanded.value = drawerSize.intValue
            }
            ToolbarImpl.drawerContext.value = this
            expanded.value = true
        } else {
            if (ToolbarImpl.drawerContext.value == this) {
                ToolbarImpl.expanded.value = 0
                expanded.value = false
            } else {
                if (ToolbarImpl.expanded.value != drawerSize.intValue) {
                    ToolbarImpl.expanded.value = drawerSize.intValue
                }
                ToolbarImpl.drawerContext.value = this
                expanded.value = true
            }
        }
    }

    @Composable
    open fun DrawerContent() {}
}