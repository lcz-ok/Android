package com.system.debugger.feature

import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.os.Environment
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class DeviceInfoActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "设备信息查看器"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        DeviceInfoScreen(padding)
    }

    @Composable
    private fun DeviceInfoScreen(padding: PaddingValues) {
        var deviceInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            isLoading = true
            deviceInfo = collectDeviceInfo()
            isLoading = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A5C))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color(0xFF5AC8FA)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "设备完整信息",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "通过 Shell 命令获取系统级设备参数",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("正在获取设备信息...", color = Color(0xFF9CA3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        InfoGroupCard(
                            title = "基本信息",
                            icon = Icons.Default.Info,
                            color = Color(0xFF5AC8FA),
                            items = listOf(
                                "设备型号" to (Build.MODEL ?: "未知"),
                                "品牌" to (Build.BRAND ?: "未知"),
                                "制造商" to (Build.MANUFACTURER ?: "未知"),
                                "产品名" to (Build.PRODUCT ?: "未知"),
                                "设备名" to (Build.DEVICE ?: "未知")
                            )
                        )
                    }
                    item {
                        InfoGroupCard(
                            title = "系统版本",
                            icon = Icons.Default.Info,
                            color = Color(0xFF4ADE80),
                            items = listOf(
                                "Android 版本" to (Build.VERSION.RELEASE ?: "未知"),
                                "API Level" to Build.VERSION.SDK_INT.toString(),
                                "安全补丁" to (Build.VERSION.SECURITY_PATCH ?: "无"),
                                "构建编号" to (Build.ID ?: "未知"),
                                "构建类型" to (Build.TYPE ?: "未知")
                            )
                        )
                    }
                    item {
                        InfoGroupCard(
                            title = "CPU 处理器",
                            icon = Icons.Default.Memory,
                            color = Color(0xFFFFB74D),
                            items = listOf(
                                "CPU 架构" to (Build.SUPPORTED_ABIS?.firstOrNull() ?: "未知"),
                                "处理器" to (deviceInfo["ro.product.processor"] ?: "未知"),
                                "核心数" to (Runtime.getRuntime().availableProcessors().toString()),
                                "硬件" to (deviceInfo["ro.hardware"] ?: "未知")
                            )
                        )
                    }
                    item {
                        InfoGroupCard(
                            title = "内存与存储",
                            icon = Icons.Default.Storage,
                            color = Color(0xFFAF52DE),
                            items = listOf(
                                "总内存" to formatMemorySize(deviceInfo["mem.total"] ?: ""),
                                "可用内存" to formatMemorySize(Runtime.getRuntime().maxMemory().toString()),
                                "内部存储总量" to getTotalStorage(),
                                "内部存储可用" to getAvailableStorage()
                            )
                        )
                    }
                    item {
                        InfoGroupCard(
                            title = "Shell 获取的属性",
                            icon = Icons.Default.Memory,
                            color = Color(0xFF5AC8FA),
                            items = listOf(
                                "基带版本" to (deviceInfo["ro.build.version.baseband"] ?: "未知"),
                                "内核版本" to (deviceInfo["kernel"] ?: "未知"),
                                "引导加载器" to (Build.BOOTLOADER ?: "未知"),
                                "显示版本" to (deviceInfo["ro.build.display.id"] ?: "未知")
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun InfoGroupCard(
        title: String,
        icon: ImageVector,
        color: Color,
        items: List<Pair<String, String>>
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                items.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            color = Color(0xFF9CA3B8),
                            fontSize = 13.sp
                        )
                        Text(
                            text = value,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }

    private fun collectDeviceInfo(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            // 通过 Shizuku 执行 getprop 获取系统属性
            if (ShizukuHelper.isShizukuRunning()) {
                val propOutput = ShizukuHelper.execShellCommand("getprop")
                propOutput.lines().forEach { line ->
                    // 格式: [key]: [value]
                    val match = Regex("\\[([^\\]]+)\\]:\\s*\\[([^\\]]*)\\]").find(line)
                    if (match != null) {
                        result[match.groupValues[1]] = match.groupValues[2]
                    }
                }
                // 获取内核版本
                result["kernel"] = ShizukuHelper.execShellCommand("uname -r").trim()
                // 获取内存信息
                result["mem.total"] = ShizukuHelper.execShellCommand("cat /proc/meminfo | grep MemTotal").trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun formatMemorySize(raw: String): String {
        if (raw.isBlank()) return "未知"
        val match = Regex("(\\d+)").find(raw)
        if (match != null) {
            val kb = match.value.toLong()
            val mb = kb / 1024
            return if (mb > 1024) "${mb / 1024} GB" else "$mb MB"
        }
        return raw
    }

    private fun getTotalStorage(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes
            val gb = total / (1024 * 1024 * 1024)
            "$gb GB"
        } catch (e: Exception) {
            "未知"
        }
    }

    private fun getAvailableStorage(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val avail = stat.availableBytes
            val gb = avail / (1024 * 1024 * 1024)
            "$gb GB"
        } catch (e: Exception) {
            "未知"
        }
    }
}
