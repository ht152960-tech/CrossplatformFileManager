package com.example.cross_platformfilemanager

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Android 端入口 Activity。
 *
 * 这里只负责把共享层 Compose 应用挂载到 Android 窗口。
 */
class MainActivity : ComponentActivity() {
    private val filePickerState: AndroidFilePickerViewModel by viewModels()
    private lateinit var browserReferencePicker: AndroidBrowserReferencePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        registerAndroidApplicationContext(applicationContext)
        if (filePickerState.clearStalePendingAfterProcessRestore()) {
            showFilePickerStateExpiredToast()
        }

        val documentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val uri = result.data?.data.takeIf { result.resultCode == Activity.RESULT_OK }
            val consumed = browserReferencePicker.onDocumentPicked(uri)
            if (uri != null && !consumed) {
                showFilePickerStateExpiredToast()
            }
        }
        browserReferencePicker = AndroidBrowserReferencePicker(
            contentResolver = contentResolver,
            launcher = documentLauncher,
            pickerState = filePickerState,
        )
        AndroidBrowserReferencePickerHolder.register(browserReferencePicker)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            browserReferencePicker.cancelPendingPick()
        }
        AndroidBrowserReferencePickerHolder.unregister(browserReferencePicker)
        super.onDestroy()
    }

    private fun showFilePickerStateExpiredToast() {
        Toast.makeText(
            applicationContext,
            "文件选择状态已失效，请重新选择",
            Toast.LENGTH_LONG,
        ).show()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
