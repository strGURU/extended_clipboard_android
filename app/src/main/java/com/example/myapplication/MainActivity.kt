package com.example.myapplication

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var clipboardManager: ClipboardManager

    private fun openAccessibilitySettings() {
        startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

//        val intent = Intent(this, ClipboardAccessibilityService::class.java)
//        startForegroundService(intent)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ClipboardScreen(clipboardManager = clipboardManager)
                }
            }
        }
    }
}



@Composable
fun ClipboardScreen(clipboardManager: ClipboardManager) {
    val context = LocalContext.current
    var clipboardText by remember { mutableStateOf(getClipboardText(clipboardManager, context)) }
    var connectionResult by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = {
        // запускаємо мережевий виклик в фоновій корутині
        scope.launch(Dispatchers.IO) {
            try {
                val client = ClipboardSyncClient(host = "192.168.1.108", port = 7777)
                val response = client.sendMessage("Привіт з телефону")
                connectionResult = "Відповідь: $response"
            } catch (e: Exception) {
                connectionResult = "Помилка: ${e.message}"
            }
        }
    }) {
        Text("Тест з'єднання")
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(text = connectionResult)

    // цикл опитування - кожну секунду перевіряє буфер і оновлює стан
    LaunchedEffect(Unit) {
        while (true) {
            val current = getClipboardText(clipboardManager, context)
            if (current != clipboardText) {
                clipboardText = current
            }
            delay(1000L) // пауза 1 секунда між перевірками
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Вміст буфера обміну:", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = clipboardText, fontSize = 20.sp)
    }
}

private fun getClipboardText(clipboardManager: ClipboardManager, context: Context): String {
    android.util.Log.d("ClipboardDebug", "Clip: ${clipboardManager.primaryClip}")
    val clip = clipboardManager.primaryClip
    if (clip == null || clip.itemCount == 0) return "(порожньо)"

    return clip.getItemAt(0)
        .coerceToText(context)
        .toString()
        .ifBlank { "(порожньо)" }
}