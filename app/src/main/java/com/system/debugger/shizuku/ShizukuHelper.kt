package com.system.debugger.shizuku

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuHelper {

    private const val REQUEST_CODE = 1001

    fun isShizukuRunning(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun checkAndRequestPermission(
        context: Context,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            try {
                Shizuku.requestPermission(REQUEST_CODE)
                onDenied()
            } catch (e: Exception) {
                onDenied()
            }
        }
    }

    fun openShizukuApp(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                val marketIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"))
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(marketIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun execShellCommand(command: String): String {
        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", command),
                null,
                null
            ) ?: return ""
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
