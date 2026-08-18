package com.system.debugger.help

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.data.FeatureData
import com.system.debugger.data.FeatureInfo
import com.system.debugger.ui.theme.getCardColor

@Composable
fun HelpBottomSheet(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151B2E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .background(
                                color = Color(0xFF3A4058),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .width(36.dp)
                            .height(4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "功能说明",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color(0xFF9CA3B8),
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Divider(color = Color(0xFF2A3048), thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(FeatureData.features) { feature ->
                        FeatureHelpCard(feature)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHelpCard(feature: FeatureInfo) {
    val cardColor = getCardColor(feature.id)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(red = cardColor.red * 0.5f, green = cardColor.green * 0.5f, blue = cardColor.blue * 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getFeatureIcon(feature.id),
                    contentDescription = feature.name,
                    tint = Color.White,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                Text(
                    text = feature.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = feature.description,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF5AC8FA),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Text(
                    text = "所需权限",
                    color = Color(0xFF5AC8FA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = feature.requiredPermissions.joinToString(" · "),
                    color = Color(0xFF9CA3B8),
                    fontSize = 12.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Text(
                    text = "典型用例",
                    color = Color(0xFFFFB74D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = feature.useCases.joinToString(" · "),
                    color = Color(0xFF9CA3B8),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun getFeatureIcon(id: String): ImageVector {
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
