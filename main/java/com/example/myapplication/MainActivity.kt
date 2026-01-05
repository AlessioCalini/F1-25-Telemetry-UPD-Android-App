package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle


class MainActivity : ComponentActivity() {
    private lateinit var udpReceiver: UdpReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.BLACK

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //Mantiene lo schermo sempre acceso
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


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

        //Rimuovi il flag quando chiudi l'app
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun F1DashboardPro(state: TelemetryState) {
    // ⭐ Switch automatico tra dashboard
    when (state.sessionType) {
        SessionType.QUALIFYING_1,
        SessionType.QUALIFYING_2,
        SessionType.QUALIFYING_3,
        SessionType.SHORT_QUALIFYING,
        SessionType.ONE_SHOT_QUALIFYING,
        SessionType.PRACTICE_1,
        SessionType.PRACTICE_2,
        SessionType.PRACTICE_3,
        SessionType.SHORT_PRACTICE,
        SessionType.TIME_TRIAL -> QualifyingDashboard(state)

        SessionType.RACE,
        SessionType.RACE_2,
        SessionType.RACE_3 -> RaceDashboard(state)

        else -> RaceDashboard(state)  // Default
    }
}

@Composable
fun RaceDashboard(state: TelemetryState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .systemBarsPadding()
    ) {
        // Background gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            Color(0xFF000000),
                            Color(0xFF0A0A0A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // TOP ROW - Last Lap, RPM, Delta
            TopRowSection(state)

            // CENTER SECTION - Speed, Gear, Tires, Drivers
            Box(modifier = Modifier.weight(1f, fill = true)) {
                CenterSection(state)
            }

            // BOTTOM SECTION - ERS Bar
            ERSBarSection(state)
        }
    }
}

@Composable
fun QualifyingDashboard(state: TelemetryState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .systemBarsPadding()
    ) {
        // Background gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            Color(0xFF000000),
                            Color(0xFF0A0A0A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // TOP ROW - Last Lap, Best Lap, Current Lap, Delta
            QualifyingTopRow(state)

            // CENTER SECTION - Speed/Pos, Gear, Tires, Sectors
            Box(modifier = Modifier.weight(1f, fill = true)) {
                QualifyingCenterSection(state)
            }

            // BOTTOM SECTION - ERS Bar
            ERSBarSection(state)
        }
    }
}

@Composable
fun QualifyingTopRow(state: TelemetryState) {
    // Delta color
    val deltaColor = remember(state.deltaToSessionBest) {
        when {
            state.deltaToSessionBest.startsWith("-") -> Color(0xFF4CAF50)
            state.deltaToSessionBest.startsWith("+") -> Color(0xFFE53935)
            else -> Color(0xFFFFA726)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⭐ Left Card - Last & Best (SENZA Current)
        Surface(
            modifier = Modifier
                .weight(0.9f)
                .padding(end = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                /*Text(
                    text = "LAP TIMES",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )*/

                // Last Lap
                LapTimeRow(
                    label = "LAST",
                    time = state.lastLapTime,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Best Lap
                LapTimeRow(
                    label = "BEST",
                    time = state.bestLapTime,
                    color = Color(0xFF9C27B0)
                )
            }
        }

        // ⭐ Center - CURRENT LAP (al posto degli RPM)
        Surface(
            modifier = Modifier
                .weight(1.2f)
                .padding(horizontal = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                /*Text(
                    text = "CURRENT",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )*/
                Text(
                    text = state.currentLapTime,
                    fontSize = 50.sp,  // ⭐ Grande e visibile
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFA726)
                )
            }
        }

        // Right Card - Delta to Session Best
        Surface(
            modifier = Modifier
                .weight(0.9f)
                .padding(start = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "DELTA",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = state.deltaToSessionBest,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = deltaColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }
    }
}

@Composable
fun LapTimeRow(label: String, time: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),  // ⭐ RIDOTTO da 8dp/6dp a 6dp/4dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = time,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun QualifyingCenterSection(state: TelemetryState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side - Speed & Position (no Lap)
        Box(modifier = Modifier.weight(0.55f)) {
            SpeedPosCard(state)
        }

        // Center - Gear and Tires
        GearAndTiresSection(state)

        // Right Side - Best Sectors
        Box(modifier = Modifier.weight(0.55f)) {
            BestSectorsCard(state)
        }
    }
}

@Composable
fun SpeedPosCard(state: TelemetryState) {
    // Animated speed
    val animatedSpeed by animateIntAsState(
        targetValue = state.speed.toInt(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "speed_animation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speed
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "$animatedSpeed",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "KM/H",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                modifier = Modifier.fillMaxWidth(0.6f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Position
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POS",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${state.pos}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun BestSectorsCard(state: TelemetryState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            /*Text(
                text = "BEST SECTORS",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 2.dp)
            )*/

            // Sector 1
            SectorRow(
                label = "S1",
                time = state.bestSector1,
                color = Color(0xFF9C27B0)
            )

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Sector 2
            SectorRow(
                label = "S2",
                time = state.bestSector2,
                color = Color(0xFF9C27B0)
            )

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Sector 3
            SectorRow(
                label = "S3",
                time = state.bestSector3,
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
fun SectorRow(label: String, time: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "${time}s",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun TopRowSection(state: TelemetryState) {
    // Animated RPM value
    val animatedRPM by animateIntAsState(
        targetValue = state.rpm,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm_animation"
    )

    // Animated RPM color
    val rpmColor by animateColorAsState(
        targetValue = getRPMColor(state.rpm, state.maxRPM),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm_color"
    )

    // Delta color
    val deltaColor = remember(state.deltaToPersonalBest) {
        when {
            state.deltaToPersonalBest.startsWith("-") -> Color(0xFF4CAF50)
            state.deltaToPersonalBest.startsWith("+") -> Color(0xFFE53935)
            else -> Color(0xFFFFA726)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⭐ Last Lap Card - MODIFICATO
        Surface(
            modifier = Modifier
                .weight(0.9f)
                .padding(end = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // ⭐ Label "LAST LAP" in alto a sinistra
                Text(
                    text = "LAST LAP",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // ⭐ Valore centrale e grande
                Text(
                    text = state.lastLapTime,
                    fontSize = 30.sp,  // ⭐ INGRANDITO da 14.sp a 22.sp
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        // RPM Display
        Surface(
            modifier = Modifier
                .weight(1.2f)
                .padding(horizontal = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "RPM",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "$animatedRPM",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = rpmColor
                )
            }
        }

        // Delta Card - ⭐ MODIFICATO
        Surface(
            modifier = Modifier
                .weight(0.9f)
                .padding(start = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // ⭐ Label "DELTA" in alto a destra
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "DELTA",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // ⭐ Valore centrale a sinistra (CenterStart) e ingrandito
                Text(
                    text = state.deltaToPersonalBest,
                    fontSize = 35.sp,  // ⭐ INGRANDITO da 14.sp a 22.sp
                    fontWeight = FontWeight.Bold,
                    color = deltaColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }
    }
}

@Composable
fun CenterSection(state: TelemetryState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side - Speed, Lap, Position - ⭐ RIDOTTO ANCORA
        Box(modifier = Modifier.weight(0.4f)) {
            SpeedLapCard(state)
        }

        // Center - Gear and Tires - ⭐ INGRANDITO
        GearAndTiresSection(state)

        // Right Side - Driver Positions - ⭐ RIDOTTO ANCORA
        Box(modifier = Modifier.weight(0.4f)) {
            DriverPositionsCard(state)
        }
    }
}

@Composable
fun SpeedLapCard(state: TelemetryState) {
    // Animated speed
    val animatedSpeed by animateIntAsState(
        targetValue = state.speed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "speed_animation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),  // ⭐ RIDOTTO da 8.dp a 6.dp
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Speed - ⭐ RIDOTTO ANCORA
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text = "$animatedSpeed",
                    fontSize = 30.sp,  // ⭐ RIDOTTO da 42.sp a 36.sp
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "KM/H",
                    fontSize = 8.sp,  // ⭐ RIDOTTO da 11.sp a 10.sp
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Divider(
                modifier = Modifier.fillMaxWidth(0.6f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Lap - ⭐ RIDOTTO ANCORA
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LAP",
                    fontSize = 8.sp,  // ⭐ RIDOTTO da 10.sp a 9.sp
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${state.currentLap}/${state.totalLaps}",
                    fontSize = 26.sp,  // ⭐ RIDOTTO da 18.sp a 16.sp
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(
                modifier = Modifier.fillMaxWidth(0.6f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Position
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POS",
                    fontSize = 8.sp,  // ⭐ RIDOTTO da 10.sp a 9.sp
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(0.8.dp))
                Text(
                    text = "${state.pos}",
                    fontSize = 26.sp,  // ⭐ RIDOTTO da 18.sp a 16.sp
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun GearAndTiresSection(state: TelemetryState) {
    // ⭐ Banner DRS: Mostra quando DRS è attivato
    var showDRSBanner by remember { mutableStateOf(false) }

    // ⭐ Nuova logica: mostra quando activated è true
    LaunchedEffect(state.drsAllowed) {

        if (state.drsAllowed) {
            // DRS attivato → Mostra banner verde
            showDRSBanner = true
        }else {
            // DRS non attivo → Nascondi banner
            showDRSBanner = false
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Layout normale (Gomme + Gear)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gomme SINISTRA
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                TyreIndicator("FL", state.tyreWearFL.toInt(), getTyreColor(state.tyreWearFL))
                TyreIndicator("RL", state.tyreWearRL.toInt(), getTyreColor(state.tyreWearRL))
            }

            // GEAR al centro
            GearDisplay(state.gear)

            // Gomme DESTRA
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 6.dp)
            ) {
                TyreIndicator("FR", state.tyreWearFR.toInt(), getTyreColor(state.tyreWearFR))
                TyreIndicator("RR", state.tyreWearRR.toInt(), getTyreColor(state.tyreWearRR))
            }
        }

        // ⭐ BANNER DRS
        androidx.compose.animation.AnimatedVisibility(
            visible = showDRSBanner,
            enter = androidx.compose.animation.scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = androidx.compose.animation.scaleOut(
                animationSpec = tween(300)
            )
        ) {
            DRSBanner()
        }
    }
}


@Composable
fun GearDisplay(gear: Int) {
    val gearText = getGearText(gear)
    val gearColor = when (gear) {
        -1 -> Color(0xFFE53935)
        0 -> Color(0xFFFFA726)
        else -> Color(0xFF4CAF50)
    }

    val animatedColor by animateColorAsState(
        targetValue = gearColor,
        animationSpec = tween(300),
        label = "gear_color"
    )

    Surface(
        modifier = Modifier.size(150.dp),  // ⭐ INGRANDITO da 90dp a 120dp
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(3.dp, animatedColor),
        tonalElevation = 8.dp,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = gearText,
                    fontSize = 100.sp,  // ⭐ INGRANDITO da 48sp a 64sp
                    fontWeight = FontWeight.Black,
                    color = animatedColor
                )
            }
        }
    }
}

@Composable
fun DriverPositionsCard(state: TelemetryState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),  // ⭐ RIDOTTO da 6.dp a 4.dp
            verticalArrangement = Arrangement.spacedBy(0.8.dp)  // ⭐ RIDOTTO da 2.dp a 1.dp
        ) {

            // Driver 2 positions ahead
            DriverInfoRow(
                driver = state.driver1Ahead,
                color = Color(0xFF4CAF50)
            )

            // Driver 1 position ahead
            DriverInfoRow(
                driver = state.driver2Ahead,
                color = Color(0xFF81C784)
            )
            Spacer(modifier = Modifier.height(5.dp))

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 1.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Driver 1 position behind
            DriverInfoRow(
                driver = state.driver1Behind,
                color = Color(0xFFFFFFFF)
            )

            // Driver 2 positions behind
            DriverInfoRow(
                driver = state.driver2Behind,
                color = Color(0xFFBDBDBD)
            )
        }
    }
}

@Composable
fun DriverInfoRow(driver: DriverInfo, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),  // ⭐ RIDOTTO da 6.dp/3.dp a 4.dp/2.dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (driver.name != "---") {
                    Text(
                        text= "${driver.name} : ${driver.lastLapTime}",
                        fontSize = 26.sp,  // ⭐ RIDOTTO da 11.sp a 10.sp
                        color = color.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text= "--- : --:--.---",
                        fontSize = 26.sp,  // ⭐ RIDOTTO da 11.sp a 10.sp
                        color = color.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ERSBarSection(state: TelemetryState) {
    val ersPercentage = (state.ersStoreEnergy / 4000000f * 100).coerceIn(0f, 100f)

    val animatedPercentage by animateFloatAsState(
        targetValue = ersPercentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ers_animation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(35.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPercentage / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    getERSGradientColor(animatedPercentage),
                                    getERSGradientColor(animatedPercentage).copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                Text(
                    text = "${animatedPercentage.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (animatedPercentage > 30f) Color.White else Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun TyreIndicator(label: String, percentage: Int, color: Color) {
    val animatedPercentage by animateIntAsState(
        targetValue = percentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tyre_wear_animation"
    )

    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(300),
        label = "tyre_color"
    )

    Surface(
        modifier = Modifier.size(width = 70.dp, height = 62.dp),  // ⭐ INGRANDITO da 48x40 a 62x54
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, animatedColor),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,  // ⭐ INGRANDITO da 9sp a 11sp
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "$animatedPercentage%",
                fontSize = 25.sp,  // ⭐ INGRANDITO da 13sp a 16sp
                fontWeight = FontWeight.Bold,
                color = animatedColor
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

fun getERSGradientColor(percentage: Float): Color {
    return when {
        percentage > 75f -> Color(0xFF00FF00)
        percentage > 50f -> Color(0xFF88FF00)
        percentage > 25f -> Color(0xFFFFAA00)
        else -> Color.Red
    }
}

fun getTyreColor(wear: Float): Color{
    return when{
        wear < 20f -> Color(0xFF00FF00)
        wear < 40f -> Color(0xFF88FF00)
        wear < 60f -> Color(0xFFFFAA00)
        wear < 80f -> Color(0xFFFF6600)
        else -> Color.Red
    }
}

@Composable
fun DRSBanner() {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF4CAF50),  // VERDE
        tonalElevation = 12.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DRS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}