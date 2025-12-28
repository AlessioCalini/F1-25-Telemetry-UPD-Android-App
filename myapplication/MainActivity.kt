package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var udpReceiver: UdpReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.BLACK

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        udpReceiver = UdpReceiver(20777)
        udpReceiver.start()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val telemetryState by udpReceiver.telemetryState.collectAsStateWithLifecycle()
                F1DashboardPro(telemetryState)
            }
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        udpReceiver.stop()
    }
}

@Composable
fun F1DashboardPro(state: TelemetryState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        // RIGA SUPERIORE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LAST LAP (sinistra) - ⭐ AGGIORNATO
            Text(
                text = "LAST LAP  ${state.lastLapTime}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // RPM (centro)
            Text(
                text = "${state.rpm}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = getRPMColor(state.rpm, state.maxRPM)
            )

            // DELTA (destra) - ⭐ AGGIORNATO CON CALCOLO REALE
            Text(
                text = "${state.deltaToPersonalBest}  DELTA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    state.deltaToPersonalBest.startsWith("-") -> Color(0xFF00FF00)  // Verde se negativo (migliorato)
                    state.deltaToPersonalBest.startsWith("+") -> Color.Red           // Rosso se positivo (peggiorato)
                    else -> Color(0xFFFFAA00)                                        // Giallo se neutro
                }
            )
        }

        // SEZIONE CENTRALE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = (-20).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SINISTRA - Velocità e Lap
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Text(
                    text = "${state.speed}",
                    fontSize = 30.sp,  //MODIFICATO, PRIMA ERA 64
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "KM/H",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(20.dp)) //MODIFICATO, PRIMA ERA 32
                // LAP - ⭐ AGGIORNATO
                Text(
                    text = "LAP ${state.currentLap}/${state.totalLaps}",
                    fontSize = 25.sp, //modificato prima era 18
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))
                // POSITION
                Text(
                    text = "POS ${state.pos}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // CENTRO - Gomme + Marcia
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gomme SINISTRA
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    TyreIndicator("FL", state.tyreWearFL.toInt(), getTyreColor(state.tyreWearFL))
                    TyreIndicator("RL", state.tyreWearRL.toInt(), getTyreColor(state.tyreWearRL))
                }

                // MARCIA
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GEAR",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color(0xFF1A1A1A))
                            .border(4.dp, Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getGearText(state.gear),
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Red
                        )
                    }
                }

                // Gomme DESTRA
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    TyreIndicator("FR", state.tyreWearFR.toInt(), getTyreColor(state.tyreWearFR))
                    TyreIndicator("RR", state.tyreWearRR.toInt(), getTyreColor(state.tyreWearRR))
                }
            }

            // DESTRA - Posizioni 4 piloti (2 davanti + 2 dietro)
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 20.dp)
            ) {

                // Piloti davanti (verde = più vicino)
                Text(
                    text = "VER +0.23",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF00)
                )
                Text(
                    text = "LEC +0.45",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF88FF88)
                )

                // Piloti dietro (bianco)
                Text(
                    text = "NOR -0.67",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "PIA -1.23",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCCCCCC)
                )
            }
        }

        // BARRA ERS IN BASSO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val ersPercentage = (state.ersStoreEnergy / 4000000f * 100).coerceIn(0f, 100f)

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(45.dp)
                    .background(Color(0xFF0D0D0D))
            ) {
                // Barra con gradiente verde-rosso
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ersPercentage / 100f)
                        .fillMaxHeight()
                        .background(getERSGradientColor(ersPercentage))
                )

                Text(
                    text = "${ersPercentage.toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun TyreIndicator(label: String, percentage: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(60.dp, 50.dp)
            .border(2.dp, color)
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "$percentage%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun getGearText(gear: Int): String {
    return when (gear) {
        -1 -> "R"
        0 -> "N"
        else -> "$gear"
    }
}

fun getRPMColor(rpm: Int, maxRPM: Int): Color {
    val percentage = if (maxRPM > 0) rpm.toFloat() / maxRPM else 0f
    return when {
        percentage > 0.95f -> Color.Red
        percentage > 0.85f -> Color(0xFFFFAA00)
        percentage > 0.75f -> Color.Yellow
        else -> Color(0xFF00FF00)
    }
}

// Colore ERS con gradiente verde-rosso
fun getERSGradientColor(percentage: Float): Color {
    return when {
        percentage > 75f -> Color(0xFF00FF00)  // Verde brillante
        percentage > 50f -> Color(0xFF88FF00)  // Verde-giallo
        percentage > 25f -> Color(0xFFFFAA00)  // Arancione
        else -> Color.Red                       // Rosso
    }
}

fun getTyreColor(wear: Float): Color{
    return when{
        wear < 20f -> Color(0xFF00FF00)   // Verde - Gomma nuova
        wear < 40f -> Color(0xFF88FF00)   // Verde chiaro
        wear < 60f -> Color(0xFFFFAA00)   // Arancione
        wear < 80f -> Color(0xFFFF6600)   // Arancione scuro
        else -> Color.Red
    }
}