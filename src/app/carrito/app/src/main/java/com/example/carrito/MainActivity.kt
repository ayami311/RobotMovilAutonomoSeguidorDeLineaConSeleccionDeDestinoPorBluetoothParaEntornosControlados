package com.example.carrito

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carrito.ui.theme.CarritoTheme
import okhttp3.*
import java.util.Locale

class MainActivity : ComponentActivity() {
    // ¡Ojo! La IP ahora es para WebSocket (ws://) y el puerto es 81
    private val espIp = "ws://192.168.18.25:80"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarritoTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    ControlScreen(espIp)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ControlScreen(espIp: String) {
    val context = LocalContext.current
    var isTtsInitialized by remember { mutableStateOf(false) }

    // Estados para el WebSocket
    var webSocket by remember { mutableStateOf<WebSocket?>(null) }
    var connectionStatus by remember { mutableStateOf("Desconectado") }

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) isTtsInitialized = true
        }
    }

    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized) {
            tts.language = Locale.Builder().setLanguage("es").build()
        }
    }

    // Listener para los mensajes del WebSocket
    val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            (context as ComponentActivity).runOnUiThread { connectionStatus = "Conectado" }
            tts.speak("Conectado al carrito", TextToSpeech.QUEUE_FLUSH, null, null)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // "Llegada:Sala" -> parts[0]="Llegada", parts[1]="Sala"
            val parts = text.split(":")
            val messageType = parts.getOrNull(0)
            val messagePayload = parts.getOrNull(1)

            val textToSpeak = when(messageType) {
                "Llegada" -> "He llegado a: $messagePayload"
                "Alerta" -> "¡Cuidado! Obstáculo detectado."
                else -> "Mensaje desconocido"
            }
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            (context as ComponentActivity).runOnUiThread { connectionStatus = "Desconectando" }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            (context as ComponentActivity).runOnUiThread { connectionStatus = "Error de Conexión" }
        }
    }

    // Efecto para conectar y desconectar el WebSocket
    DisposableEffect(Unit) {
        val client = OkHttpClient()
        val request = Request.Builder().url(espIp).build()
        webSocket = client.newWebSocket(request, webSocketListener)

        onDispose {
            webSocket?.close(1000, "Cerrando la app")
            tts.stop()
            tts.shutdown()
        }
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun handleButtonClick(command: String, label: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }

        if (isTtsInitialized) {
            tts.speak(label, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        // Enviar comando por WebSocket
        webSocket?.send(command)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Estado: $connectionStatus", color = if (connectionStatus == "Conectado") Color.Green else Color.Red)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { handleButtonClick("/ruta1", "Haz escogido ir a la sala") }, modifier = Modifier.fillMaxWidth()) {
            Text("SALA")
        }
        Button(onClick = { handleButtonClick("/ruta2", "Haz escogido ir a la cocina") }, modifier = Modifier.fillMaxWidth()) {
            Text("COCINA")
        }
        Button(onClick = { handleButtonClick("/ruta3", "Haz escogido ir al baño") }, modifier = Modifier.fillMaxWidth()) {
            Text("BAÑO")
        }
    }
}