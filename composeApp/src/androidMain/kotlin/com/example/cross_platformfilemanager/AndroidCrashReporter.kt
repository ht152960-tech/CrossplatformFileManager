package com.example.cross_platformfilemanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Process
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

object AndroidCrashReporter {
    private const val CrashFileName = "taggo_last_crash.txt"
    private val installed = AtomicBoolean(false)

    fun install(context: Context) {
        if (!installed.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                writeCrashReport(appContext, thread, throwable)
            }
            try {
                previousHandler?.uncaughtException(thread, throwable) ?: run {
                    Process.killProcess(Process.myPid())
                    exitProcess(10)
                }
            } catch (_: Throwable) {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }

    fun readLastCrash(context: Context): String? {
        val crashFile = crashFile(context.applicationContext)
        return runCatching {
            if (crashFile.exists()) crashFile.readText(Charsets.UTF_8) else null
        }.getOrNull()
    }

    fun clearLastCrash(context: Context) {
        runCatching {
            crashFile(context.applicationContext).delete()
        }
    }

    fun copyCrashToClipboard(context: Context, crashText: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Taggo crash report", crashText))
    }

    fun shareCrash(context: Context, crashText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "taggo_last_crash.txt")
            putExtra(Intent.EXTRA_TEXT, crashText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share crash report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(chooser)
        }
    }

    private fun writeCrashReport(context: Context, thread: Thread, throwable: Throwable) {
        val crashText = buildCrashReport(context, thread, throwable)
        val crashFile = crashFile(context)
        crashFile.parentFile?.mkdirs()
        crashFile.writeText(crashText, Charsets.UTF_8)
    }

    private fun crashFile(context: Context): File = File(context.filesDir, CrashFileName)

    private fun buildCrashReport(context: Context, thread: Thread, throwable: Throwable): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val stackTraceWriter = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTraceWriter))
        val builder = StringBuilder()
        builder.appendLine("time: $timestamp")
        builder.appendLine("package: ${context.packageName}")
        builder.appendLine("thread: ${thread.name}")
        builder.appendLine("throwableClass: ${throwable::class.java.name}")
        builder.appendLine("throwableMessage: ${throwable.message.orEmpty()}")
        builder.appendLine()
        builder.appendLine("fullStackTrace:")
        builder.appendLine(stackTraceWriter.toString().trimEnd())
        builder.appendLine()
        builder.appendLine("causeChain:")
        appendCauseChain(builder, throwable.cause, 1)
        return builder.toString().trimEnd()
    }

    private fun appendCauseChain(builder: StringBuilder, cause: Throwable?, depth: Int) {
        if (cause == null) {
            builder.appendLine("  <none>")
            return
        }
        var current: Throwable? = cause
        var currentDepth = depth
        while (current != null) {
            val causeWriter = StringWriter()
            current.printStackTrace(PrintWriter(causeWriter))
            builder.appendLine("  #$currentDepth ${current::class.java.name}: ${current.message.orEmpty()}")
            builder.appendLine(causeWriter.toString().trimEnd().prependIndent("    "))
            current = current.cause
            currentDepth++
            if (current != null) {
                builder.appendLine()
            }
        }
    }
}