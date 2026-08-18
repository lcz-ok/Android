package com.system.debugger.feature

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
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

class AutomationSceneActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "自动化场景"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        AutomationScreen(padding)
    }

    @Composable
    private fun AutomationScreen(padding: PaddingValues) {
        val scenes = listOf(
            SceneItem(
                id = "game",
                name = "游戏模式",
                description = "关闭动画、调高性能、屏蔽通知",
                icon = Icons.Default.Build,
                color = Color(0xFFFF6B6B),
                commands = listOf(
                    "settings put global window_animation_scale 0",
                    "settings put global transition_animation_scale 0",
                    "settings put global animator_duration_scale 0"
                )
            ),
            SceneItem(
                id = "battery",
                name = "省电模式",
                description = "降低亮度、关闭动画、限制后台",
                icon = Icons.Default.BatteryChargingFull,
                color = Color(0xFF4ADE80),
                commands = listOf(
                    "settings put system screen_brightness 30",
                    "settings put global window_animation_scale 0.5",
                    "settings put global transition_animation_scale 0.5"
                )
            ),
            SceneItem(
                id = "night",
                name = "夜间模式",
                description = "最低亮度、静音、关闭自动同步",
                icon = Icons.Default.BrightnessLow,
                color = Color(0xFF5AC8FA),
                commands = listOf(
                    "settings put system screen_brightness 10"
                )
            ),
            SceneItem(
                id = "movie",
                name = "观影模式",
                description = "静音、自动旋转锁定、调高对比度",
                icon = Icons.Default.MusicNote,
                color = Color(0xFFAF52DE),
                commands = listOf(
                    "settings put system screen_brightness 150"
                )
            ),
            SceneItem(
                id = "flashlight",
                name = "快捷手电筒",
                description = "一键开启/关闭相机闪光灯",
                icon = Icons.Default.FlashlightOn,
                color = Color(0xFFFFB74D),
                commands = listOf(
                    "cmd statusbar set-expanded true"
                )
            ),
            SceneItem(
                id = "maxvolume",
                name = "最大音量",
                description = "将铃声媒体音量调至最大",
                icon = Icons.Default.VolumeUp,
                color = Color(0xFF34C759),
                commands = listOf(
                    "media volume --stream 3 --set 15",
                    "media volume --stream 2 --set 7"
                )
            )
        )

        var activatedScenes by remember { mutableStateOf(setOf<String>()) }
        var selectedCategory by remember { mutableStateOf("全部") }

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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A4A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFAF52DE)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "自动化场景",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "一键触发预设系统配置组合（基于 Shizuku）",
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
                listOf("全部", "性能", "显示", "声音").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(scenes) { scene ->
                    SceneCard(
                        scene = scene,
                        activated = activatedScenes.contains(scene.id),
                        onTrigger = {
                            activatedScenes = activatedScenes + scene.id
                            triggerScene(scene)
                        }
                    )
                }
            }
        }
    }

    private fun triggerScene(scene: SceneItem) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            scene.commands.forEach { cmd ->
                try {
                    ShizukuHelper.execShellCommand(cmd)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    private fun SceneCard(
        scene: SceneItem,
        activated: Boolean,
        onTrigger: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTrigger() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (activated) scene.color.copy(alpha = 0.35f) else Color(0xFF1C2338)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = scene.icon,
                    contentDescription = null,
                    tint = scene.color,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = scene.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (activated) {
                            Spacer(modifier = Modifier.padding(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.height(14.dp)
                            )
                        }
                    }
                    Text(
                        text = scene.description,
                        color = Color(0xFF9CA3B8),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "触发",
                    color = scene.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    private data class SceneItem(
        val id: String,
        val name: String,
        val description: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val color: Color,
        val commands: List<String>
    )
}
