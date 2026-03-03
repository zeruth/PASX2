package com.pasx2

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.view.SurfaceHolder
import android.view.SurfaceView
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
import kotlinx.coroutines.launch
import kr.co.iefriends.pcsx2.MainActivity
import kr.co.iefriends.pcsx2.NativeApp
import kr.co.iefriends.pcsx2.SDLControllerManager
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class SurfaceCallbacks(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    init {
        holder.addCallback(this)
    }
    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        NativeApp.onNativeSurfaceChanged(holder.surface, width, height)
        Main.start()
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        NativeApp.onNativeSurfaceChanged(null, 0, 0)
    }
}

class Main: ComponentActivity() {
    companion object {
        private val eDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        private val eScope = CoroutineScope(eDispatcher + SupervisorJob())

        val surface = mutableStateOf<SurfaceView?>(null)

        val eState = mutableStateOf(EmuState.STOPPED)

        private var m_szGamefile = ""

        fun invoke(task: suspend () -> Unit) {
            eScope.launch {
                task()
            }
        }

        fun start() {
            invoke {
                NativeApp.runVMThread(m_szGamefile)
                eState.value = EmuState.RUNNING
            }
        }

        fun pause() {
            invoke {
                NativeApp.pause()
                eState.value = EmuState.PAUSED
            }
        }

        fun resume() {
            invoke {
                NativeApp.resume()
                eState.value = EmuState.RUNNING
            }
        }

        fun stop() {
            invoke {
                NativeApp.shutdown()
                eState.value = EmuState.STOPPED
            }
        }

        fun renderOpenGL() {
            invoke {
                NativeApp.renderGpu(12)
            }
        }

        fun renderVulkan() {
            invoke {
                NativeApp.renderGpu(14)
            }
        }

        fun renderSoftware() {
            invoke {
                NativeApp.renderGpu(13)
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

        fun getSupportedGLESVersion(context: Context): Double {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = am.deviceConfigurationInfo
            return info.glEsVersion.toDouble()
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

        SurfaceCallbacks(this)

       // Default resources
        copyAssetAll(applicationContext, "bios");
        copyAssetAll(applicationContext, "resources");

        invoke {
            NativeApp.initializeOnce(applicationContext)

            // Set up JNI
            SDLControllerManager.nativeSetupJNI()

            // Initialize state
            SDLControllerManager.initialize()
        }



        val glVersion = getSupportedGLESVersion(this)

        if (glVersion < 3.2) {
            println("ERROR: GL VERSION: $glVersion")
            //renderSoftware()
/*            eState.value = EmuState.GL_UNSUPPORTED
            return*/
        }

        setContent {
            WindowImpl.Window {
                Box(Modifier
                    .fillMaxSize()
                    .background(Colors.surface.value))
                surface.value?.let { surfaceView ->
                    AndroidView(factory = {
                        surfaceView
                    }, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    override fun onPause() {
        if (eState.value == EmuState.RUNNING)
            NativeApp.pause()
        super.onPause()
    }

    override fun onResume() {
        if (eState.value == EmuState.PAUSED)
            NativeApp.resume()
        super.onResume()
    }

    override fun onDestroy() {
        NativeApp.shutdown()
        super.onDestroy()

        val appPid = Process.myPid()
        Process.killProcess(appPid)
    }
}