package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.data.db.DBService
import com.example.myapplication.ui.AppNavigator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DBService.init(this)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8F9FA)
                ) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun TimerApp() {
    var isStopwatchTab by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Title
        Text(
            text = "PROYECTO",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF16213E))
                .padding(8.dp)
        ) {
            Button(
                onClick = { isStopwatchTab = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStopwatchTab) Color(0xFF4CAF50) else Color(0xFF2C3B52)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("⏱️ Cronómetro")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isStopwatchTab = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isStopwatchTab) Color(0xFF4CAF50) else Color(0xFF2C3B52)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("⏲️ Temporizador")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (isStopwatchTab) {
                Stopwatch()
            } else {
                Timer()
            }
        }

        // Footer
        Text(
            text = "Desarrollado Por : Hector Sanchez",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Stopwatch() {
    var milliseconds by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<Long>()) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(10)
            milliseconds += 10
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Motivational Text
        Text(
            text = if (!isRunning) "¡Listo para comenzar!" else "¡Sigue así!",
            color = Color(0xFF4CAF50),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Time Display
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3B5E), Color(0xFF16213E))
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = formatTime(milliseconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isRunning) Color(0xFF4CAF50) else Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(120.dp)
            ) {
                Text(if (!isRunning) "▶ Iniciar" else "⏸️ Pausar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { 
                    if (milliseconds > 0) {
                        laps = laps + milliseconds
                    }
                },
                enabled = isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🏁 Vuelta")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { 
                    isRunning = false
                    milliseconds = 0
                    laps = emptyList()
                },
                enabled = milliseconds > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🔄 Reiniciar")
            }
        }

        if (laps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Vueltas:",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2B3D))
                    .padding(8.dp)
            ) {
                laps.reversed().take(3).forEachIndexed { index, time ->
                    Text(
                        text = "Vuelta ${laps.size - index}: ${formatTime(time)}",
                        color = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Timer() {
    var seconds by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingTime > 0) {
            delay(1000)
            remainingTime -= 1000
            if (remainingTime <= 0) {
                isRunning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Text
        Text(
            text = when {
                remainingTime <= 0 -> "¡Configura tu tiempo!"
                isRunning -> "Tiempo restante"
                else -> "En pausa"
            },
            color = when {
                remainingTime <= 0 -> Color(0xFF4CAF50)
                isRunning -> Color(0xFF2196F3)
                else -> Color(0xFFFFA000)
            },
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Time Display
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3B5E), Color(0xFF16213E))
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = formatTime(remainingTime),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Time Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2B3D))
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TimeSelector(
                label = "Minutos",
                value = minutes,
                onIncrement = { if (minutes < 59) minutes++ },
                onDecrement = { if (minutes > 0) minutes-- }
            )

            Spacer(modifier = Modifier.width(32.dp))

            TimeSelector(
                label = "Segundos",
                value = seconds,
                onIncrement = { if (seconds < 59) seconds++ },
                onDecrement = { if (seconds > 0) seconds-- }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { 
                    if (!isRunning) {
                        remainingTime = (minutes * 60 + seconds) * 1000L
                    }
                    isRunning = !isRunning
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isRunning) Color(0xFF4CAF50) else Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(120.dp)
            ) {
                Text(if (!isRunning) "▶ Iniciar" else "⏸️ Pausar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { 
                    isRunning = false
                    remainingTime = 0
                    minutes = 0
                    seconds = 0
                },
                enabled = minutes > 0 || seconds > 0 || remainingTime > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🔄 Reiniciar")
            }
        }

        // Quick Presets
        if (!isRunning) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Presets rápidos:",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PresetButton("1 min") {
                    minutes = 1
                    seconds = 0
                }
                PresetButton("3 min") {
                    minutes = 3
                    seconds = 0
                }
                PresetButton("5 min") {
                    minutes = 5
                    seconds = 0
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C3B52))
        ) {
            Text("▲", fontSize = 24.sp, color = Color.White)
        }
        Text(
            text = "%02d".format(value),
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C3B52))
        ) {
            Text("▼", fontSize = 24.sp, color = Color.White)
        }
    }
}

@Composable
fun PresetButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2C3B52)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text)
    }
}

fun formatTime(timeInMillis: Long): String {
    val hours = timeInMillis / (1000 * 60 * 60)
    val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (timeInMillis % (1000 * 60)) / 1000
    val centiseconds = (timeInMillis % 1000) / 10

    return if (hours > 0) {
        "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, centiseconds)
    } else {
        "%02d:%02d.%02d".format(minutes, seconds, centiseconds)
    }
}