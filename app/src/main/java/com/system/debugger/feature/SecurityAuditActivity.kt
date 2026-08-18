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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class SecurityAuditActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "安全审计"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        SecurityAuditScreen(padding)
    }

    @Composable
    private fun SecurityAuditScreen(padding: PaddingValues) {
        var scanResults by remember { mutableStateOf<List<AuditResult>>(emptyList()) }
        var isScanning by remember { mutableStateOf(false) }
        var showOnlyHighRisk by remember { mutableStateOf(false) }

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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3D2B0E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D)
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Column {
                            Text(
                                text = "安全审计",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "扫描应用权限使用，识别潜在风险",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            isScanning = true
                            scanResults = performAudit()
                            isScanning = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Refresh else Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D)
                        )
                        Spacer(modifier = Modifier.padding(6.dp))
                        Text(
                            text = if (isScanning) "扫描中..." else "开始审计",
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }

            if (scanResults.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showOnlyHighRisk,
                        onClick = { showOnlyHighRisk = false },
                        label = { Text("全部 (${scanResults.size})") }
                    )
                    FilterChip(
                        selected = showOnlyHighRisk,
                        onClick = { showOnlyHighRisk = true },
                        label = { Text("高危 (${scanResults.count { it.riskLevel == RiskLevel.HIGH }})") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredResults = if (showOnlyHighRisk) {
                    scanResults.filter { it.riskLevel == RiskLevel.HIGH }
                } else {
                    scanResults
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResults) { result ->
                        AuditResultCard(result)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF3A4058),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "尚未执行审计",
                            color = Color(0xFF9CA3B8),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "点击上方按钮开始扫描",
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    private fun performAudit(): List<AuditResult> {
        val results = mutableListOf<AuditResult>()
        val pm = packageManager
        val highRiskPermissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        try {
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in apps) {
                if (app.packageName == packageName) continue

                val appInfo = try { pm.getApplicationInfo(app.packageName, 0) } catch (_: Exception) { continue }
                val grantedPerms = mutableListOf<String>()

                for (perm in highRiskPermissions) {
                    try {
                        if (pm.checkPermission(perm, app.packageName) == PackageManager.PERMISSION_GRANTED) {
                            grantedPerms.add(perm)
                        }
                    } catch (_: Exception) {}
                }

                if (grantedPerms.isNotEmpty()) {
                    val riskLevel = when {
                        grantedPerms.size >= 3 -> RiskLevel.HIGH
                        grantedPerms.size >= 2 -> RiskLevel.MEDIUM
                        else -> RiskLevel.LOW
                    }
                    results.add(
                        AuditResult(
                            appName = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { app.packageName },
                            packageName = app.packageName,
                            riskLevel = riskLevel,
                            riskyPermissions = grantedPerms
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results.sortedByDescending { it.riskLevel.ordinal }
    }

    @Composable
    private fun AuditResultCard(result: AuditResult) {
        val riskColor = when (result.riskLevel) {
            RiskLevel.HIGH -> Color(0xFFFF6B6B)
            RiskLevel.MEDIUM -> Color(0xFFFFB74D)
            RiskLevel.LOW -> Color(0xFF4ADE80)
        }
        val riskLabel = when (result.riskLevel) {
            RiskLevel.HIGH -> "高危"
            RiskLevel.MEDIUM -> "中等"
            RiskLevel.LOW -> "低危"
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.appName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = result.packageName,
                            color = Color(0xFF9CA3B8),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = riskColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = " $riskLabel ",
                            color = riskColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "高危权限: ${result.riskyPermissions.joinToString(", ") { it.substringAfterLast(".") }}",
                    color = Color(0xFF9CA3B8),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }

    private data class AuditResult(
        val appName: String,
        val packageName: String,
        val riskLevel: RiskLevel,
        val riskyPermissions: List<String>
    )

    private enum class RiskLevel {
        LOW, MEDIUM, HIGH
    }
}
