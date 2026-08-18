package com.system.debugger.feature

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class PrivacyShieldActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "隐私盾"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        PrivacyShieldScreen(padding)
    }

    @Composable
    private fun PrivacyShieldScreen(padding: PaddingValues) {
        val context = this
        val permissions = listOf(
            PermissionItem("CAMERA", "摄像头", "防止应用偷拍", Icons.Default.Videocam, Color(0xFFFF6B6B)),
            PermissionItem("RECORD_AUDIO", "麦克风", "禁止后台录音", Icons.Default.Mic, Color(0xFFFFB74D)),
            PermissionItem("ACCESS_FINE_LOCATION", "定位", "隐藏地理位置", Icons.Default.LocationOn, Color(0xFF5AC8FA)),
            PermissionItem("READ_CONTACTS", "通讯录", "保护联系人隐私", Icons.Default.Contacts, Color(0xFFAF52DE)),
            PermissionItem("READ_EXTERNAL_STORAGE", "存储", "限制文件访问", Icons.Default.Storage, Color(0xFF4ADE80)),
            PermissionItem("READ_SMS", "短信", "防止短信泄露", Icons.Default.Message, Color(0xFFE879F9))
        )

        var permissionStates by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

        LaunchedEffect(Unit) {
            permissions.forEach { perm ->
                permissionStates[perm.key] = checkPermission(perm.key)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E3A3D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF5AC8FA)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "隐私权限管理",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "一键控制各项敏感权限的授予状态",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(permissions) { perm ->
                    PermissionRow(
                        perm = perm,
                        isGranted = permissionStates[perm.key] ?: false,
                        onToggle = { granted ->
                            permissionStates = permissionStates.toMutableMap().apply {
                                this[perm.key] = granted
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return try {
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    private fun requestPermissionForAll(permission: String, grant: Boolean) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            val command = if (grant) "pm grant" else "pm revoke"
            val fullPermission = when (permission) {
                "CAMERA" -> Manifest.permission.CAMERA
                "RECORD_AUDIO" -> Manifest.permission.RECORD_AUDIO
                "ACCESS_FINE_LOCATION" -> Manifest.permission.ACCESS_FINE_LOCATION
                "READ_CONTACTS" -> Manifest.permission.READ_CONTACTS
                "READ_EXTERNAL_STORAGE" -> Manifest.permission.READ_EXTERNAL_STORAGE
                "READ_SMS" -> Manifest.permission.READ_SMS
                else -> permission
            }
            val pm = packageManager
            val apps = pm.getInstalledApplications(0)
            for (app in apps) {
                try {
                    ShizukuHelper.execShellCommand("$command ${app.packageName} $fullPermission")
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    private fun PermissionRow(
        perm: PermissionItem,
        isGranted: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    imageVector = perm.icon,
                    contentDescription = null,
                    tint = perm.color,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = perm.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = perm.description,
                        color = Color(0xFF9CA3B8),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isGranted,
                    onCheckedChange = { newState ->
                        onToggle(newState)
                        requestPermissionForAll(perm.key, newState)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF007AFF)
                    )
                )
            }
        }
    }

    private data class PermissionItem(
        val key: String,
        val name: String,
        val description: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val color: Color
    )
}
