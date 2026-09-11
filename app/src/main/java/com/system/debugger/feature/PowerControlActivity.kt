package com.system.debugger.feature

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class PowerControlActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "电源控制"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        PowerControlScreen(padding)
    }

    @Composable
    private fun PowerControlScreen(padding: PaddingValues) {
        var confirmDialog by remember { mutableStateOf<PowerAction?>(null) }
        var resultMessage by remember { mutableStateOf<String?>(null) }
        var airplaneMode by remember { mutableStateOf(false) }
        var wifiEnabled by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            airplaneMode = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1A1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "电源与快速操作",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "通过 Shell 执行系统级电源操作",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            resultMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E3D2E))
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF4ADE80),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    PowerActionCard(
                        title = "快速重启",
                        description = "立即重启系统（soft reboot）",
                        icon = Icons.Default.Refresh,
                        color = Color(0xFF5AC8FA),
                        onClick = { confirmDialog = PowerAction("快速重启", "reboot") }
                    )
                }
                item {
                    PowerActionCard(
                        title = "重启到恢复模式",
                        description = "重启进入 Recovery 模式",
                        icon = Icons.Default.Refresh,
                        color = Color(0xFFAF52DE),
                        onClick = { confirmDialog = PowerAction("重启到恢复模式", "reboot recovery") }
                    )
                }
                item {
                    PowerActionCard(
                        title = "重启到引导模式",
                        description = "重启进入 Bootloader/Fastboot 模式",
                        icon = Icons.Default.Refresh,
                        color = Color(0xFFFFB74D),
                        onClick = { confirmDialog = PowerAction("重启到引导模式", "reboot bootloader") }
                    )
                }
                item {
                    PowerActionCard(
                        title = "关机",
                        description = "立即关闭设备电源",
                        icon = Icons.Default.PowerSettingsNew,
                        color = Color(0xFFFF6B6B),
                        onClick = { confirmDialog = PowerAction("关机", "shutdown -p") }
                    )
                }
                item {
                    PowerActionCard(
                        title = if (airplaneMode) "关闭飞行模式" else "开启飞行模式",
                        description = "一键切换飞行模式状态",
                        icon = Icons.Default.AirplanemodeActive,
                        color = Color(0xFFFFB74D),
                        onClick = {
                            val newState = !airplaneMode
                            toggleAirplaneMode(newState)
                            airplaneMode = newState
                            resultMessage = if (newState) "飞行模式已开启" else "飞行模式已关闭"
                        }
                    )
                }
                item {
                    PowerActionCard(
                        title = "打开电池信息",
                        description = "查看详细电池状态与健康",
                        icon = Icons.Default.BatteryStd,
                        color = Color(0xFF4ADE80),
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                                startActivity(intent)
                            } catch (_: Exception) {
                                resultMessage = "无法打开电池信息页面"
                            }
                        }
                    )
                }
            }
        }

        confirmDialog?.let { action ->
            AlertDialog(
                onDismissRequest = { confirmDialog = null },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFF1C2338),
                title = {
                    Text("确认${action.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Text(
                        text = "即将执行: ${action.command}\n\n此操作将立即生效，请确认设备状态。",
                        color = Color(0xFF9CA3B8),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        executePowerAction(action)
                        confirmDialog = null
                    }) {
                        Text("确认执行", color = Color(0xFFFF6B6B), fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDialog = null }) {
                        Text("取消", color = Color(0xFF9CA3B8))
                    }
                }
            )
        }
    }

    @Composable
    private fun PowerActionCard(
        title: String,
        description: String,
        icon: ImageVector,
        color: Color,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        color = Color(0xFF9CA3B8),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "执行",
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    private fun executePowerAction(action: PowerAction) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            ShizukuHelper.execShellCommand(action.command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleAirplaneMode(enable: Boolean) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            val value = if (enable) 1 else 0
            // 切换飞行模式设置
            ShizukuHelper.execShellCommand("settings put global airplane_mode_on $value")
            // 广播状态变化
            ShizukuHelper.execShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class PowerAction(
        val title: String,
        val command: String
    )
}
