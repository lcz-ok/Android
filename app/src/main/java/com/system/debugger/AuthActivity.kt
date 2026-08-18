package com.system.debugger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.system.debugger.ui.theme.SystemDebuggerTheme

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SystemDebuggerTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen(
                        onAuthSuccess = {
                            startActivity(
                                android.content.Intent(
                                    this@AuthActivity,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var cardKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    val correctKey = "20141230"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF007AFF),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "卡密认证",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "请输入您的卡密以继续使用系统调试器",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3B8),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = cardKey,
                onValueChange = { input ->
                    cardKey = input
                    errorMessage = ""
                },
                placeholder = {
                    Text(
                        text = "请输入卡密",
                        color = Color(0xFF6B7280)
                    )
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007AFF),
                    unfocusedBorderColor = Color(0xFF3A4058),
                    cursorColor = Color(0xFF007AFF)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        verifyKey(cardKey, correctKey, onAuthSuccess) { error ->
                            errorMessage = error
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isVerifying = true
                    verifyKey(cardKey, correctKey, onAuthSuccess) { error ->
                        errorMessage = error
                        isVerifying = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    contentColor = Color.White
                ),
                enabled = !isVerifying
            ) {
                Text(
                    text = if (isVerifying) "验证中..." else "验证并进入",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1C2338),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "系统调试器 v1.0\n基于 Shizuku 的系统级调试工具（无需 Root）",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

private fun verifyKey(
    input: String,
    correct: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (input.trim() == correct) {
        onSuccess()
    } else {
        onError("卡密错误，请重新输入")
    }
}