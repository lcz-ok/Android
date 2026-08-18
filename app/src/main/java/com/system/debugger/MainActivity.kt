package com.system.debugger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.data.FeatureData
import com.system.debugger.data.FeatureInfo
import com.system.debugger.help.HelpBottomSheet
import com.system.debugger.shizuku.ShizukuHelper
import com.system.debugger.ui.theme.getCardColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorScheme = darkColorScheme(
                primary = Color(0xFF007AFF),
                background = Color(0xFF0B0F19),
                surface = Color(0xFF151B2E)
            )
            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    val showShizukuDialog = remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        if (!ShizukuHelper.isShizukuRunning()) {
                            showShizukuDialog.value = true
                        }
                    }

                    MainScreen(
                        onFeatureClick = { featureId ->
                            FeatureRouter.openFeature(this, featureId)
                        }
                    )

                    if (showShizukuDialog.value) {
                        ShizukuNotRunningDialog(
                            onOpenShizuku = {
                                ShizukuHelper.openShizukuApp(this)
                            },
                            onDismiss = {
                                showShizukuDialog.value = false
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onFeatureClick: (String) -> Unit
) {
    val context = LocalContext.current
    var showHelp by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "系统调试器",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0E3D2E)
                ) {
                    Text(
                        text = " Shizuku 就绪 ",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "选择一个功能模块开始使用",
                color = Color(0xFF9CA3B8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            FeatureGrid(onFeatureClick = onFeatureClick)
        }

        FloatingHelpButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 32.dp)
        ) {
            showHelp = true
        }

        if (showHelp) {
            HelpBottomSheet(onDismiss = { showHelp = false })
        }
    }
}

@Composable
private fun FeatureGrid(onFeatureClick: (String) -> Unit) {
    val features = FeatureData.features

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        for (rowIndex in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                for (colIndex in 0 until 3) {
                    val index = rowIndex * 3 + colIndex
                    if (index < features.size) {
                        FeatureCard(
                            feature = features[index],
                            onClick = { onFeatureClick(features[index].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: FeatureInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = getIconForFeature(feature.id)
    val cardColor = getCardColor(feature.id)

    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = feature.name,
                tint = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(10.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = feature.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = feature.description.take(22) + if (feature.description.length > 22) "…" else "",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun FloatingHelpButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = Color(0xFF007AFF),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 22.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Help,
            contentDescription = "帮助",
            tint = Color.White,
            modifier = Modifier.padding(2.dp)
        )
    }
}

@Composable
private fun getIconForFeature(id: String): ImageVector {
    return when (id) {
        "freezer" -> Icons.Default.Block
        "privacy" -> Icons.Default.Lock
        "file" -> Icons.Default.Folder
        "tuner" -> Icons.Default.Settings
        "audit" -> Icons.Default.VerifiedUser
        "automation" -> Icons.Default.Build
        else -> Icons.Default.Star
    }
}

@Composable
fun ShizukuNotRunningDialog(
    onOpenShizuku: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF1C2338),
        title = {
            Text(
                text = "Shizuku 未就绪",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Shizuku 服务未运行或权限未授予。您需要先开启 Shizuku 服务，然后重新打开应用才能正常使用所有功能。",
                color = Color(0xFF9CA3B8),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenShizuku) {
                Text(
                    text = "打开 Shizuku",
                    color = Color(0xFF007AFF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "退出",
                    color = Color(0xFF9CA3B8)
                )
            }
        }
    )
}