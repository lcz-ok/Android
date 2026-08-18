package com.system.debugger.feature

import android.os.Build
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper

class SystemTunerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "系统调谐器"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        SystemTunerScreen(padding)
    }

    @Composable
    private fun SystemTunerScreen(padding: PaddingValues) {
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80)
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Column {
                        Text(
                            text = "系统参数调优",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "调整动画速度、字体大小等系统参数",
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
                item {
                    TunerSlider(
                        title = "窗口动画缩放",
                        description = "控制窗口打开/关闭动画速度",
                        icon = Icons.Default.Lightbulb,
                        min = 0f,
                        max = 3f,
                        steps = 9,
                        unit = "x",
                        initialValue = getAnimationScale("window_animation_scale")
                    ) { value ->
                        setAnimationScale("window_animation_scale", value)
                    }
                }
                item {
                    TunerSlider(
                        title = "过渡动画缩放",
                        description = "控制页面切换动画速度",
                        icon = Icons.Default.SwapHoriz,
                        min = 0f,
                        max = 3f,
                        steps = 9,
                        unit = "x",
                        initialValue = getAnimationScale("transition_animation_scale")
                    ) { value ->
                        setAnimationScale("transition_animation_scale", value)
                    }
                }
                item {
                    TunerSlider(
                        title = "动画持续时间缩放",
                        description = "控制所有动画的持续时间",
                        icon = Icons.Default.Timer,
                        min = 0f,
                        max = 3f,
                        steps = 9,
                        unit = "x",
                        initialValue = getAnimationScale("animator_duration_scale")
                    ) { value ->
                        setAnimationScale("animator_duration_scale", value)
                    }
                }
                item {
                    QuickActionCard(
                        title = "恢复默认动画",
                        description = "一键重置所有动画缩放为 1.0x",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            resetAnimations()
                        }
                    )
                }
                item {
                    DensityCard()
                }
                item {
                    ScreenInfoCard()
                }
            }
        }
    }

    private fun getAnimationScale(key: String): Float {
        return try {
            val value = Settings.Global.getFloat(contentResolver, key, 1.0f)
            value
        } catch (e: Exception) {
            1.0f
        }
    }

    private fun setAnimationScale(key: String, value: Float) {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            Settings.Global.putFloat(contentResolver, key, value)
        } catch (e: Exception) {
            ShizukuHelper.execShellCommand("settings put global $key $value")
        }
    }

    private fun resetAnimations() {
        if (!ShizukuHelper.isShizukuRunning()) return
        try {
            setAnimationScale("window_animation_scale", 1.0f)
            setAnimationScale("transition_animation_scale", 1.0f)
            setAnimationScale("animator_duration_scale", 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    private fun TunerSlider(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        min: Float,
        max: Float,
        steps: Int,
        unit: String,
        initialValue: Float,
        onValueChange: (Float) -> Unit
    ) {
        var sliderValue by remember { mutableStateOf(initialValue) }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
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
                        text = String.format("%.1f%s", sliderValue, unit),
                        color = Color(0xFF4ADE80),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onValueChange(sliderValue) },
                    valueRange = min..max,
                    steps = steps,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4ADE80),
                        activeTrackColor = Color(0xFF4ADE80),
                        inactiveTrackColor = Color(0xFF3A4058)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun QuickActionCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    tint = Color(0xFFFFB74D),
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
                    color = Color(0xFFFFB74D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    @Composable
    private fun DensityCard() {
        val densityDpi = resources.displayMetrics.densityDpi
        val density = resources.displayMetrics.density

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "屏幕密度信息",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DPI", color = Color(0xFF9CA3B8), fontSize = 13.sp)
                    Text("$densityDpi dpi", color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("密度", color = Color(0xFF9CA3B8), fontSize = 13.sp)
                    Text("${String.format("%.2f", density)}x", color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("屏幕尺寸", color = Color(0xFF9CA3B8), fontSize = 13.sp)
                    Text(
                        "${resources.displayMetrics.widthPixels} x ${resources.displayMetrics.heightPixels} px",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun ScreenInfoCard() {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "系统信息",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Android 版本", color = Color(0xFF9CA3B8), fontSize = 13.sp)
                    Text(Build.VERSION.RELEASE, color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("API Level", color = Color(0xFF9CA3B8), fontSize = 13.sp)
                    Text("${Build.VERSION.SDK_INT}", color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("设备型号", color = Color(0xFF9CA3B8), fontSize = 13.sp)
                    Text(Build.MODEL, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}
