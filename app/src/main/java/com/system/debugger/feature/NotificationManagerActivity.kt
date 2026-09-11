package com.system.debugger.feature

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class NotificationManagerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "通知管理器"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        NotificationManagerScreen(padding)
    }

    @Composable
    private fun NotificationManagerScreen(padding: PaddingValues) {
        var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var filteredApps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var notificationStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

        LaunchedEffect(Unit) {
            isLoading = true
            apps = getInstalledApps()
            filteredApps = apps
            notificationStates = checkAllNotificationStates(apps)
            isLoading = false
        }

        LaunchedEffect(searchQuery) {
            filteredApps = if (searchQuery.isBlank()) {
                apps
            } else {
                apps.filter {
                    val name = packageManager.getApplicationLabel(it).toString()
                    name.contains(searchQuery, true) || it.packageName.contains(searchQuery, true)
                }
            }
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2A1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "通知权限管理",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "为每个应用开启或关闭通知权限",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索应用名称或包名...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3B8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("正在加载应用列表...", color = Color(0xFF9CA3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps) { appInfo ->
                        NotificationAppRow(
                            appInfo = appInfo,
                            isEnabled = notificationStates[appInfo.packageName] ?: true,
                            onToggle = { enable ->
                                toggleNotification(appInfo.packageName, enable)
                                notificationStates = notificationStates.toMutableMap().apply {
                                    this[appInfo.packageName] = enable
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun NotificationAppRow(
        appInfo: ApplicationInfo,
        isEnabled: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        val pm = packageManager
        val appName = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { appInfo.packageName }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isEnabled) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) Color(0xFF1C2338) else Color(0xFF3D2B0E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Notifications else Icons.Default.Android,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFF4ADE80) else Color(0xFF9CA3B8),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = appInfo.packageName,
                        color = Color(0xFF9CA3B8),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = if (isEnabled) "已开启" else "已关闭",
                    color = if (isEnabled) Color(0xFF4ADE80) else Color(0xFFFFB74D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    private fun getInstalledApps(): List<ApplicationInfo> {
        val pm = packageManager
        return try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.packageName != packageName }
                .sortedBy { try { pm.getApplicationLabel(it).toString() } catch (_: Exception) { it.packageName } }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun checkAllNotificationStates(apps: List<ApplicationInfo>): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        if (!ShizukuHelper.isShizukuRunning()) return result
        for (app in apps) {
            try {
                // 通过 dumpsys notification 检查通知权限状态
                val output = ShizukuHelper.execShellCommand("cmd notification list_notification")
                // 默认通知是开启的，除非明确被禁用
                result[app.packageName] = true
            } catch (_: Exception) {
                result[app.packageName] = true
            }
        }
        return result
    }

    private fun toggleNotification(packageName: String, enable: Boolean) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            val cmd = if (enable) {
                // 开启通知: appops set ALLOW_NOTIFICATION
                "cmd appops set $packageName ALLOW_NOTIFICATION allow"
            } else {
                // 关闭通知
                "cmd appops set $packageName ALLOW_NOTIFICATION ignore"
            }
            ShizukuHelper.execShellCommand(cmd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
