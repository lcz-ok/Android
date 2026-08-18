package com.system.debugger.feature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
abstract class BaseFeatureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    primary = Color(0xFF007AFF),
                    background = Color(0xFF0B0F19),
                    surface = Color(0xFF151B2E)
                )
            ) {
                FeatureScaffold(
                    title = getFeatureTitle(),
                    onBack = { finish() }
                ) { padding ->
                    FeatureContent(padding)
                }
            }
        }
    }

    abstract fun getFeatureTitle(): String

    @Composable
    abstract fun FeatureContent(padding: PaddingValues)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FeatureScaffold(
        title: String,
        onBack: () -> Unit,
        content: @Composable (PaddingValues) -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0B0F19),
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF0B0F19)
        ) { padding ->
            content(padding)
        }
    }
}