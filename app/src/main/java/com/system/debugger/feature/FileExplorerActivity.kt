package com.system.debugger.feature

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.shizuku.ShizukuHelper
import java.io.File

class FileExplorerActivity : BaseFeatureActivity() {
    override fun getFeatureTitle() = "文件探险家"

    @Composable
    override fun FeatureContent(padding: PaddingValues) {
        FileExplorerScreen(padding)
    }

    @Composable
    private fun FileExplorerScreen(padding: PaddingValues) {
        var currentPath by remember { mutableStateOf("/") }
        var files by remember { mutableStateOf<List<File>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(currentPath) {
            isLoading = true
            files = listFiles(currentPath)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A4A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFAF52DE)
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            text = "文件探险家",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "上级目录",
                            tint = Color(0xFFAF52DE),
                            modifier = Modifier.clickable {
                                val parent = File(currentPath).parent
                                if (parent != null) currentPath = parent
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = currentPath,
                            onValueChange = { currentPath = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 13.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1C2338),
                                unfocusedContainerColor = Color(0xFF1C2338),
                                focusedIndicatorColor = Color(0xFFAF52DE),
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFFAF52DE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中...", color = Color(0xFF9CA3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(files) { file ->
                        FileItemRow(file) {
                            if (file.isDirectory) {
                                currentPath = file.absolutePath
                            }
                        }
                    }
                }
            }
        }
    }

    private fun listFiles(path: String): List<File> {
        return try {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.sortedByDescending { it.isDirectory } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Composable
    private fun FileItemRow(file: File, onClick: () -> Unit) {
        val isDir = file.isDirectory
        val size = if (isDir) {
            "${file.listFiles()?.size ?: 0} 项"
        } else {
            formatFileSize(file.length())
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2338))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDir) Icons.Default.Folder else getFileIcon(file.name),
                    contentDescription = null,
                    tint = if (isDir) Color(0xFFFFB74D) else Color(0xFF5AC8FA),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = size,
                        color = Color(0xFF9CA3B8),
                        fontSize = 11.sp
                    )
                }
                if (isDir) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF6B7280)
                    )
                }
            }
        }
    }

    private fun getFileIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
        return when {
            name.endsWith(".jpg", true) || name.endsWith(".png", true) || name.endsWith(".gif", true) -> Icons.Default.Image
            name.endsWith(".mp4", true) || name.endsWith(".avi", true) -> Icons.Default.Movie
            name.endsWith(".pdf", true) -> Icons.Default.PictureAsPdf
            name.endsWith(".apk", true) -> Icons.Default.Android
            name.endsWith(".txt", true) || name.endsWith(".log", true) -> Icons.Default.Description
            else -> Icons.Default.InsertDriveFile
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
