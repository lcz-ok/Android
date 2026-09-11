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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
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

class ComponentManagerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "组件管理器"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        ComponentManagerScreen(padding)
    }

    @Composable
    private fun ComponentManagerScreen(padding: PaddingValues) {
        var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var selectedApp by remember { mutableStateOf<ApplicationInfo?>(null) }
        var components by remember { mutableStateOf<List<ComponentInfo>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            apps = getInstalledApps()
        }

        if (selectedApp == null) {
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A3A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = Color(0xFFAF52DE)
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Column {
                            Text(
                                text = "应用组件管理",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "启用/禁用应用的具体组件（服务、广播接收器）",
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

                val filtered = if (searchQuery.isBlank()) apps else apps.filter {
                    val name = packageManager.getApplicationLabel(it).toString()
                    name.contains(searchQuery, true) || it.packageName.contains(searchQuery, true)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { appInfo ->
                        val appName = try { packageManager.getApplicationLabel(appInfo).toString() } catch (_: Exception) { appInfo.packageName }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedApp = appInfo
                                    isLoading = true
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    tint = Color(0xFF5AC8FA),
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                )
                                Spacer(modifier = Modifier.padding(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(appInfo.packageName, color = Color(0xFF9CA3B8), fontSize = 11.sp)
                                }
                                Text("查看组件", color = Color(0xFF5AC8FA), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            LaunchedEffect(selectedApp) {
                isLoading = true
                components = loadComponents(selectedApp!!.packageName)
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A3A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = Color(0xFFAF52DE))
                        Spacer(modifier = Modifier.padding(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = try { packageManager.getApplicationLabel(selectedApp!!).toString() } catch (_: Exception) { selectedApp!!.packageName },
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${components.size} 个组件",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "返回",
                            color = Color(0xFF5AC8FA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { selectedApp = null }
                        )
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("正在加载组件列表...", color = Color(0xFF9CA3B8))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(components) { comp ->
                            ComponentRow(
                                component = comp,
                                onToggle = { enable ->
                                    toggleComponent(selectedApp!!.packageName, comp.name, enable)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ComponentRow(
        component: ComponentInfo,
        onToggle: (Boolean) -> Unit
    ) {
        val isEnabled = component.enabled

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
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFF4ADE80) else Color(0xFFFFB74D),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = component.name.substringAfterLast("."),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = component.type,
                        color = Color(0xFF9CA3B8),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = if (isEnabled) "禁用" else "启用",
                    color = if (isEnabled) Color(0xFFFFB74D) else Color(0xFF4ADE80),
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

    private fun loadComponents(packageName: String): List<ComponentInfo> {
        if (!ShizukuHelper.isShizukuRunning()) return emptyList()
        val result = mutableListOf<ComponentInfo>()
        try {
            // 获取禁用的组件
            val disabledOutput = ShizukuHelper.execShellCommand("pm list packages -d -e $packageName")
            val disabledComponents = disabledOutput.lines().filter { it.startsWith("pkg:") }

            // 获取所有组件
            val dumpOutput = ShizukuHelper.execShellCommand("dumpsys package $packageName")
            val enabledSet = mutableSetOf<String>()
            val disabledSet = mutableSetOf<String>()

            // 解析 Activity 组件
            val activityRegex = Regex("([a-zA-Z0-9._]+/[a-zA-Z0-9._$]+)")
            val sectionRegex = Regex("(Activity Resolver Table|Receiver Resolver Table|Service Resolver Table)")

            var currentSection = ""
            dumpOutput.lines().forEach { line ->
                when {
                    line.contains("Activity Resolver Table") -> currentSection = "activity"
                    line.contains("Receiver Resolver Table") -> currentSection = "receiver"
                    line.contains("Service Resolver Table") -> currentSection = "service"
                    currentSection.isNotEmpty() -> {
                        val match = activityRegex.find(line)
                        if (match != null && match.groupValues[1].startsWith(packageName)) {
                            val componentName = match.groupValues[1].substringAfter("/")
                            val isDisabled = line.contains("disabled=") || line.contains("enabled=0")
                            result.add(ComponentInfo(
                                name = componentName,
                                type = currentSection,
                                enabled = !isDisabled
                            ))
                        }
                    }
                }
            }

            // 去重并限制数量
            return result.distinctBy { it.name }.take(50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun toggleComponent(packageName: String, componentName: String, enable: Boolean) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            val fullComponent = "$packageName/$componentName"
            val cmd = if (enable) {
                "pm enable $fullComponent"
            } else {
                "pm disable-user $fullComponent"
            }
            ShizukuHelper.execShellCommand(cmd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class ComponentInfo(
        val name: String,
        val type: String,
        val enabled: Boolean
    )
}
