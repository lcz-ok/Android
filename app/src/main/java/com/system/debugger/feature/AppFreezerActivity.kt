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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class AppFreezerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "应用冻结器"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        AppFreezerScreen(padding)
    }

    @Composable
    private fun AppFreezerScreen(padding: PaddingValues) {
        val context = this
        var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var filteredApps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var showFrozenOnly by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            isLoading = true
            apps = getInstalledApps()
            filteredApps = apps
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A50))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "已安装应用",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${apps.size} 个应用 · 点击可冻结/解冻",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showFrozenOnly,
                    onClick = { showFrozenOnly = false; filteredApps = apps },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = showFrozenOnly,
                    onClick = { showFrozenOnly = true; filteredApps = apps.filter { it.enabled.not() } },
                    label = { Text("已冻结") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中...", color = Color(0xFF9CA3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps) { appInfo ->
                        AppItemRow(
                            appInfo = appInfo,
                            onFreeze = {
                                toggleFreeze(appInfo)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AppItemRow(
        appInfo: ApplicationInfo,
        onFreeze: () -> Unit
    ) {
        val context = this
        val pm = context.packageManager
        val appName = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { appInfo.packageName }
        val isFrozen = !appInfo.enabled

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFreeze() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFrozen) Color(0xFF3D2B0E) else Color(0xFF1C2338)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFrozen) Icons.Default.Block else Icons.Default.Android,
                    contentDescription = null,
                    tint = if (isFrozen) Color(0xFFFFB74D) else Color(0xFF4ADE80),
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
                    text = if (isFrozen) "解冻" else "冻结",
                    color = if (isFrozen) Color(0xFFFFB74D) else Color(0xFF4ADE80),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    private fun getInstalledApps(): List<ApplicationInfo> {
        val pm = packageManager
        val flags = PackageManager.GET_META_DATA
        return try {
            val apps = pm.getInstalledApplications(flags)
            apps.filter { it.packageName != packageName }
                .sortedBy { try { pm.getApplicationLabel(it).toString() } catch (_: Exception) { it.packageName } }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun toggleFreeze(appInfo: ApplicationInfo) {
        if (!ShizukuHelper.isShizukuRunning()) {
            return
        }
        try {
            val isCurrentlyFrozen = !appInfo.enabled
            val pm = packageManager
            pm.setApplicationEnabledSetting(
                appInfo.packageName,
                if (isCurrentlyFrozen) PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0
            )
            recreate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
