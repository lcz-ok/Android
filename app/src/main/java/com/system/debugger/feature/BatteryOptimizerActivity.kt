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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
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

class BatteryOptimizerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "电池优化管理"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        BatteryOptimizerScreen(padding)
    }

    @Composable
    private fun BatteryOptimizerScreen(padding: PaddingValues) {
        var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var filteredApps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var whitelistStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

        LaunchedEffect(Unit) {
            isLoading = true
            apps = getInstalledApps()
            filteredApps = apps
            whitelistStates = checkWhitelistStates(apps)
            isLoading = false
        }

        LaunchedEffect(searchQuery) {
            filteredApps = if (searchQuery.isBlank()) apps
            else apps.filter {
                val name = packageManager.getApplicationLabel(it).toString()
                name.contains(searchQuery, true) || it.packageName.contains(searchQuery, true)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E3D2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "电池优化白名单",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "管理应用电池优化白名单，控制后台运行策略",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索应用...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3B8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("正在加载电池优化状态...", color = Color(0xFF9CA3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps) { appInfo ->
                        BatteryAppRow(
                            appInfo = appInfo,
                            inWhitelist = whitelistStates[appInfo.packageName] ?: false,
                            onToggle = { add ->
                                toggleWhitelist(appInfo.packageName, add)
                                whitelistStates = whitelistStates.toMutableMap().apply {
                                    this[appInfo.packageName] = add
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BatteryAppRow(
        appInfo: ApplicationInfo,
        inWhitelist: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        val pm = packageManager
        val appName = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { appInfo.packageName }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!inWhitelist) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (inWhitelist) Color(0xFF0E3D2E) else Color(0xFF1C2338)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (inWhitelist) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                    contentDescription = null,
                    tint = if (inWhitelist) Color(0xFF4ADE80) else Color(0xFF9CA3B8),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(appInfo.packageName, color = Color(0xFF9CA3B8), fontSize = 11.sp)
                }
                Text(
                    text = if (inWhitelist) "已白名单" else "已优化",
                    color = if (inWhitelist) Color(0xFF4ADE80) else Color(0xFFFFB74D),
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

    private fun checkWhitelistStates(apps: List<ApplicationInfo>): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        if (!ShizukuHelper.isShizukuRunning()) return result
        try {
            // 获取电池优化白名单列表
            val whitelistOutput = ShizukuHelper.execShellCommand("dumpsys deviceidle whitelist")
            val whitelistPackages = whitelistOutput.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith(" ") }

            for (app in apps) {
                result[app.packageName] = whitelistPackages.any { it.contains(app.packageName) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun toggleWhitelist(packageName: String, add: Boolean) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            val cmd = if (add) {
                "dumpsys deviceidle whitelist +$packageName"
            } else {
                "dumpsys deviceidle whitelist -$packageName"
            }
            ShizukuHelper.execShellCommand(cmd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
