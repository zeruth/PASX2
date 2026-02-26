package com.pasx2

import android.content.Context
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pasx2.ui.Colors
import com.pasx2.ui.WindowImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kr.co.iefriends.pcsx2.MainActivity
import kr.co.iefriends.pcsx2.NativeApp
import kr.co.iefriends.pcsx2.SDLControllerManager
import kr.co.iefriends.pcsx2.SDLSurface
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class Main: ComponentActivity() {
    companion object {
        private val eDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        private val eScope = CoroutineScope(eDispatcher + SupervisorJob())

        val surface = mutableStateOf<SDLSurface?>(null)

        private var m_szGamefile = ""

        fun invoke(task: suspend () -> Unit) {
            eScope.launch {
                task()
            }
        }

        fun startEmuThread() {
            invoke {
                NativeApp.runVMThread(m_szGamefile)
            }
        }

        fun copyAssetAll(p_context: Context, srcPath: String) {
            val assetMgr = p_context.assets
            try {
                val destPath =
                    p_context.getExternalFilesDir(null).toString() + File.separator + srcPath
                assetMgr.list(srcPath)?.let {
                    if (it.isEmpty()) {
                        MainActivity.copyFile(p_context, srcPath, destPath)
                    } else {
                        val dir = File(destPath)
                        if (!dir.exists()) dir.mkdir()
                        for (element in it) {
                            copyAssetAll(p_context, srcPath + File.separator + element)
                        }
                    }
                }
            } catch (ignored: IOException) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

       // Default resources
        copyAssetAll(applicationContext, "bios");
        copyAssetAll(applicationContext, "resources");

        invoke {
            Initialize()
            surface.value = SDLSurface(this)
        }

        setContent {
            WindowImpl.Window {
                Box(Modifier
                    .fillMaxSize()
                    .background(Colors.surface.value))
                surface.value?.let { s ->
                    AndroidView(factory = {
                        s
                    }, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    override fun onPause() {
        NativeApp.pause()
        super.onPause()
    }

    override fun onResume() {
        NativeApp.resume()
        super.onResume()
    }

    override fun onDestroy() {
        NativeApp.shutdown()
        super.onDestroy()

        val appPid = Process.myPid()
        Process.killProcess(appPid)
    }

    fun Initialize() {
        NativeApp.initializeOnce(applicationContext)

        // Set up JNI
        SDLControllerManager.nativeSetupJNI()

        // Initialize state
        SDLControllerManager.initialize()
    }
}