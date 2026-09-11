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
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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

class NetworkManagerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "网络管理器"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        NetworkManagerScreen(padding)
    }

    @Composable
    private fun NetworkManagerScreen(padding: PaddingValues) {
        var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var filteredApps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var networkStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

        LaunchedEffect(Unit) {
            isLoading = true
            apps = getInstalledApps()
            filteredApps = apps
            networkStates = checkNetworkStates(apps)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E3A5C))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = Color(0xFF5AC8FA)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "应用网络控制",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "通过 appops 控制每个应用的网络访问权限",
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
                    Text("正在加载...", color = Color(0xFF9CA3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps) { appInfo ->
                        NetworkAppRow(
                            appInfo = appInfo,
                            hasInternet = networkStates[appInfo.packageName] ?: true,
                            onToggle = { allow ->
                                toggleNetwork(appInfo.packageName, allow)
                                networkStates = networkStates.toMutableMap().apply {
                                    this[appInfo.packageName] = allow
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun NetworkAppRow(
        appInfo: ApplicationInfo,
        hasInternet: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        val pm = packageManager
        val appName = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { appInfo.packageName }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!hasInternet) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasInternet) Color(0xFF1C2338) else Color(0xFF3D2B0E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasInternet) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = if (hasInternet) Color(0xFF4ADE80) else Color(0xFFFF6B6B),
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
                    text = if (hasInternet) "联网" else "已断网",
                    color = if (hasInternet) Color(0xFF4ADE80) else Color(0xFFFF6B6B),
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

    private fun checkNetworkStates(apps: List<ApplicationInfo>): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        if (!ShizukuHelper.isShizukuRunning()) return result
        for (app in apps) {
            try {
                val output = ShizukuHelper.execShellCommand("cmd appops get ${app.packageName} INTERNET")
                // 如果返回 "allow" 或默认，则允许联网
                val allowed = output.contains("allow", true) || output.contains("default", true) || output.isBlank()
                result[app.packageName] = allowed
            } catch (_: Exception) {
                result[app.packageName] = true
            }
        }
        return result
    }

    private fun toggleNetwork(packageName: String, allow: Boolean) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            val cmd = if (allow) {
                "cmd appops set $packageName INTERNET allow"
            } else {
                "cmd appops set $packageName INTERNET ignore"
            }
            ShizukuHelper.execShellCommand(cmd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
