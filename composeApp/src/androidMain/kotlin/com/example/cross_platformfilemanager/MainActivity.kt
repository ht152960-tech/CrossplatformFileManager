package com.example.cross_platformfilemanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Android 端入口 Activity。
 *
 * 这里只负责把共享层 Compose 应用挂载到 Android 窗口。
 */
class MainActivity : ComponentActivity() {
    private lateinit var browserReferencePicker: AndroidBrowserReferencePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        registerAndroidApplicationContext(applicationContext)

        val documentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            val consumed = browserReferencePicker.onDocumentPicked(uri)
            if (uri != null && !consumed) {
                Toast.makeText(
                    applicationContext,
                    "文件选择状态已失效，请重新选择",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
        browserReferencePicker = AndroidBrowserReferencePicker(
            contentResolver = contentResolver,
            launcher = documentLauncher,
        )
        AndroidBrowserReferencePickerHolder.register(browserReferencePicker)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        browserReferencePicker.cancelPendingPick()
        AndroidBrowserReferencePickerHolder.unregister(browserReferencePicker)
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
