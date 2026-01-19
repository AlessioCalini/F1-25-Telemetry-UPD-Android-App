package com.example.myapplication

import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import android.content.pm.ActivityInfo
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler



class MainActivity : ComponentActivity() {
    private lateinit var udpReceiver: UdpReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.WHITE  // ⭐ Bianco per home

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //Mantiene lo schermo sempre acceso
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        udpReceiver = UdpReceiver(20777)
        udpReceiver.start()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // ⭐ State per controllare quale dashboard mostrare
                var selectedDashboard by rememberSaveable { mutableStateOf(DashboardType.NONE) }

                // ⭐ Cambia orientamento in base alla selezione
                LaunchedEffect(selectedDashboard) {
                    requestedOrientation = when (selectedDashboard) {
                        DashboardType.NONE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }

                when (selectedDashboard) {
                    DashboardType.NONE -> {
                        // ⭐ Mostra Home Screen
                        HomeScreen(onDashboardSelected = { dashboard ->
                            selectedDashboard = dashboard
                            // Cambia colore status bar quando entra in una dashboard
                            window.statusBarColor = android.graphics.Color.BLACK
                        })
                    }

                    DashboardType.RACE -> {
                        val telemetryState by udpReceiver.telemetryState.collectAsState()
                        key(telemetryState.safetyCarStatus) {
                            RaceDashboard(state = telemetryState,
                                onBack = { selectedDashboard = DashboardType.NONE}
                            )
                        }
                    }

                    DashboardType.QUALIFYING -> {
                        val telemetryState by udpReceiver.telemetryState.collectAsState()
                        QualifyingDashboard(state = telemetryState,
                            onBack = { selectedDashboard = DashboardType.NONE})
                    }

                    DashboardType.TIME_TRIAL -> {
                        val telemetryState by udpReceiver.telemetryState.collectAsState()
                        TimeTrialDashboard(state = telemetryState,
                            onBack = { selectedDashboard = DashboardType.NONE})
                    }
                }
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

/*@Composable
fun F1DashboardPro(state: TelemetryState) {
    // ⭐ Switch automatico tra dashboard
    when (state.sessionType) {
        SessionType.QUALIFYING_1,
        SessionType.QUALIFYING_2,
        SessionType.QUALIFYING_3,
        SessionType.SHORT_QUALIFYING,
        SessionType.ONE_SHOT_QUALIFYING,
        SessionType.SPRINT_SHOOTOUT_1,           // ⭐ NUOVO
        SessionType.SPRINT_SHOOTOUT_2,           // ⭐ NUOVO
        SessionType.SPRINT_SHOOTOUT_3,           // ⭐ NUOVO
        SessionType.SHORT_SPRINT_SHOOTOUT,       // ⭐ NUOVO
        SessionType.ONE_SHOT_SPRINT_SHOOTOUT,    // ⭐ NUOVO
        SessionType.PRACTICE_1,
        SessionType.PRACTICE_2,
        SessionType.PRACTICE_3,
        SessionType.SHORT_PRACTICE -> QualifyingDashboard(state)

        SessionType.TIME_TRIAL -> TimeTrialDashboard(state)

        SessionType.RACE,
        SessionType.RACE_2,
        SessionType.RACE_3 -> RaceDashboard(state)

        else -> {
            RaceDashboard(state)
        }  // Default
    }
}*/

@Composable
fun RaceDashboard(state: TelemetryState, onBack: (() -> Unit)? = null) {
    // ⭐ Gestisci il back button
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }
    val isSafetyCarActive = state.safetyCarStatus in listOf(1, 2)  // 1=full, 2=virtual
    val safetyCarColor = Color(0xFFFFEB3B)  // Giallo


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .systemBarsPadding()

    ) {
        // ⭐ 1. Background gradient (PRIMO - in fondo)
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

        // ⭐ 2. Contenuto della dashboard (SECONDO)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TopRowSection(state, isSafetyCarActive)

            Box(modifier = Modifier.weight(1f, fill = true)) {
                CenterSection(state)
            }

            ERSBarSection(state)
        }

        // ⭐ 3. BORDO GIALLO (ULTIMO - in cima a tutto)
        if (isSafetyCarActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 8.dp,  // ⭐ Aumentato a 8dp per renderlo più visibile
                        color = safetyCarColor,
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        }
    }
}

@Composable
fun QualifyingDashboard(state: TelemetryState, onBack: (() -> Unit)? = null) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }
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
fun TimeTrialDashboard(state: TelemetryState, onBack: (() -> Unit)? = null) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    // ⭐ State locale per controllare la visibilità del banner
    var showInvalidBanner by remember { mutableStateOf(false) }

    // ⭐ Quando currentLapInvalid diventa true, mostra il banner per 3 secondi
    LaunchedEffect(state.currentLapInvalid) {
        if (state.currentLapInvalid) {
            showInvalidBanner = true
            delay(5000)  // Mostra per 5 secondi
            showInvalidBanner = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .systemBarsPadding()
    ) {
        // Background gradient
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
            // TOP ROW
            TimeTrialTopRow(state)

            // CENTER SECTION
            Box(modifier = Modifier.weight(1f, fill = true)) {
                TimeTrialCenterSection(state)
            }

            // BOTTOM SECTION - ERS
            ERSBarSection(state)
        }
        // ⭐ INVALID LAP BANNER (sopra tutto)
        if (showInvalidBanner) {
            InvalidLapBanner()
        }
    }
}

// ⭐ NUOVO - Banner lap invalido
@Composable
fun InvalidLapBanner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 12.dp,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // ⭐ Diagonale bianco/nero
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Triangolo nero (in basso a sinistra)
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        },
                        color = Color.Black
                    )

                    // Triangolo bianco (in alto a destra)
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height)
                            close()
                        },
                        color = Color.White
                    )
                }

                // Testo INVALID
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "INVALID",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Red,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = "LAP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TimeTrialTopRow(state: TelemetryState) {
    // Delta color
    val deltaColor = remember(state.ttDeltaToRival) {
        when {
            state.ttDeltaToRival.startsWith("-") -> Color(0xFF4CAF50)  // Verde se più veloce del rival
            state.ttDeltaToRival.startsWith("+") -> Color(0xFFE53935)  // Rosso se più lento
            else -> Color(0xFFFFA726)
        }
    }

    val animatedRPM by animateIntAsState(
        targetValue = state.rpm.toInt(),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm_animation"
    )

    val rpmColor by animateColorAsState(
        targetValue = getRPMColor(state.rpm.toInt(), state.maxRPM.toInt()),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm_color"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ⭐ Left Card - Personal Best & Rival
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
                        text = "TIME TRIAL",
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )*/

                    // Personal Best
                    LapTimeRow(
                        label = "BEST",
                        time = state.ttPersonalBestLap,
                        color = Color(0xFF4CAF50)
                    )

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Rival Time
                    LapTimeRow(
                        label = "RIVAL",
                        time = state.ttRivalLap,
                        color = Color(0xFFE53935)
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

            // ⭐ Right Card - Delta to Rival
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
                        text = state.ttDeltaToRival,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = deltaColor,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeTrialCenterSection(state: TelemetryState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⭐ Left Side - Speed & Laps Completed
        Box(modifier = Modifier.weight(0.65f)) {
            TimeTrialSpeedLapsCard(state)
        }

        // Center - Gear and Tires
        GearAndTiresSection(state)

        // ⭐ Right Side - Best Sectors (Session)
        Box(modifier = Modifier.weight(0.7f)) {
            TimeTrialBestSectorsCard(state)
        }
    }
}

@Composable
fun TimeTrialSpeedLapsCard(state: TelemetryState) {
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

            // ⭐ Laps Completed
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LAPS",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${state.ttLapsCompleted}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun TimeTrialBestSectorsCard(state: TelemetryState) {
    // ⭐ Colore viola per player, rosso per rival
    val playerColor = Color(0xFF9C27B0)  // Viola
    val rivalColor = Color(0xFFE53935)   // Rosso
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

            // ⭐ Sector 1 con nome
            TimeTrialSectorRow(
                label = "S1",
                time = state.ttSessionBestSector1,
                color = if (state.ttBestS1Owner == "YOU") playerColor else rivalColor
            )

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // ⭐ Sector 2 con nome
            TimeTrialSectorRow(
                label = "S2",
                time = state.ttSessionBestSector2,
                color = if (state.ttBestS2Owner == "YOU") playerColor else rivalColor
            )

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // ⭐ Sector 3 con nome
            TimeTrialSectorRow(
                label = "S3",
                time = state.ttSessionBestSector3,
                color = if (state.ttBestS3Owner == "YOU") playerColor else rivalColor
            )
        }
    }
}

// ⭐ NUOVA funzione semplificata senza owner text
@Composable
fun TimeTrialSectorRow(label: String, time: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label (S1, S2, S3)
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // ⭐ Solo il tempo, con colore che indica owner
            Text(
                text = "${time}s",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = color,  // Viola = Player, Rosso = Rival
                textAlign = TextAlign.Start
            )
        }
    }
}

// ⭐ NUOVA funzione per row con owner
/*@Composable
fun TimeTrialSectorRow(label: String, time: String, owner: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            // Owner + Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = owner,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.7f)
                )
                // ⭐ Due punti
                Text(
                    text = ":",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.7f)
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
}*/

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
fun TopRowSection(state: TelemetryState, isSafetyCarActive: Boolean = false) {
    // ⭐ Mostra Launch Control solo in partenza
    val showLaunchControl = state.sessionType in listOf(SessionType.RACE, SessionType.RACE_2, SessionType.RACE_3) &&
            state.gear == 1 &&
            state.clutch > 0.5f &&
            state.speed < 10

    // ⭐ MODIFICATO - Delta cambia in base a Safety Car
    val (deltaText, deltaColor) = if (isSafetyCarActive) {
        // Durante Safety Car: mostra delta dalla SC
        val scDelta = state.safetyCarDelta
        val deltaStr = if (scDelta >= 0) {
            "+${String.format("%.3f", scDelta)}"
        } else {
            String.format("%.3f", scDelta)
        }
        val color = if (scDelta >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)  // Verde se avanti, rosso se dietro
        deltaStr to color
    } else {
        // Normale: delta al personal best
        val deltaStr = state.deltaToPersonalBest
        val color = when {
            deltaStr.startsWith("-") -> Color(0xFF4CAF50)
            deltaStr.startsWith("+") -> Color(0xFFE53935)
            else -> Color(0xFFFFA726)
        }
        deltaStr to color
    }

    val animatedRPM by animateIntAsState(
        targetValue = state.rpm.toInt(),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm_animation"
    )

    val rpmColor by animateColorAsState(
        targetValue = getRPMColor(state.rpm.toInt(), state.maxRPM.toInt()),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm_color"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ⭐ ROW ORIGINALE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Card - Last Lap
            Surface(
                modifier = Modifier
                    .weight(1f)
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
                    Column(
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "LAST LAP",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = state.lastLapTime,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }

            // Center - RPM Display
            Surface(
                modifier = Modifier
                    .weight(1f)
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

            // Right Card - Delta
            Surface(
                modifier = Modifier
                    .weight(1f)
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
                            text = if (isSafetyCarActive) "SC DELTA" else "DELTA",  // ⭐ Label cambia
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = deltaText,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = deltaColor,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }

        // ⭐ LAUNCH CONTROL BAR (sotto gli RPM)
        if (showLaunchControl) {
            LaunchControlBar(state)
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
    // ⭐ State per controllare la visibilità del banner
    var showDRSBanner by remember { mutableStateOf(false) }

    // ⭐ Logica semplice
    LaunchedEffect(state.drsAllowed, state.drsActivated) {
        showDRSBanner = state.drsAllowed && !state.drsActivated
    }

    // ⭐ CORRETTO - Rimosso fillMaxSize(), usa solo lo spazio assegnato
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Layout normale (Gomme + Gear) - Sempre visibile
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
            GearDisplay(state.gear, state.suggestedGear)
            //GearDisplay(state.gear)

            // Gomme DESTRA
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 6.dp)
            ) {
                TyreIndicator("FR", state.tyreWearFR.toInt(), getTyreColor(state.tyreWearFR))
                TyreIndicator("RR", state.tyreWearRR.toInt(), getTyreColor(state.tyreWearRR))
            }
        }

        // ⭐ BANNER DRS - Posizionato IN ALTO
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-90).dp),  // ⭐ Sposta ancora più in alto
            contentAlignment = Alignment.Center
        ) {
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
}


@Composable
fun GearDisplay(gear: Int, suggestedGear: Int) {
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
        modifier = Modifier.size(150.dp),
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
            // ⭐ Gear principale al centro
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = gearText,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Black,
                    color = animatedColor
                )
            }

            // ⭐ NUOVO - Suggested Gear nell'angolo in basso a sinistra
            if (suggestedGear > 0 && suggestedGear != gear) {
                val arrow = when {
                    suggestedGear > gear -> "▲"  // Aumenta marcia
                    suggestedGear < gear -> "▼"  // Diminuisci marcia
                    else -> ""
                }

                val suggestedColor = when {
                    suggestedGear > gear -> Color(0xFF4CAF50)  // Verde per shift up
                    suggestedGear < gear -> Color(0xFFE53935)  // Rosso per shift down
                    else -> Color.White
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = arrow,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = suggestedColor
                    )
                    Text(
                        text = "$suggestedGear",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = suggestedColor
                    )
                }
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
    val ersPercentage = (state.ersStoreEnergy / 4_000_000f).coerceIn(0f, 1f)

    val isOvertakeMode = state.ersDeployMode == 3

    val ersColor by animateColorAsState(
        targetValue = if (isOvertakeMode) {
            Color(0xFF4CAF50)
        } else {
            Color(0xFF00BCD4)
        },
        animationSpec = tween(300),
        label = "ers_color"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = ersPercentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ers_animation"
    )

    // ⭐ Box per centrare la Surface
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)  // 90% della larghezza
                .height(35.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart  // ⭐ Lascia CenterStart per il contenuto interno
            ) {
                // Background filler
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ersColor.copy(alpha = 0.3f),
                                    ersColor
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                )

                // ERS Label and percentage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ⭐ SINISTRA - Solo "ERS"
                    Text(
                        text = "ERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // ⭐ CENTRO - Badge "OVERTAKE"
                    if (isOvertakeMode) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50)
                        ) {
                            Text(
                                text = "OVERTAKE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        // ⭐ Spacer invisibile per mantenere il layout bilanciato
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // ⭐ DESTRA - Percentuale
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
            .width(220.dp)
            .height(80.dp),
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

@Composable
fun LaunchControlBar(state: TelemetryState) {
    // ⭐ Determina il range ideale di RPM
    val (minRPM, maxRPM) = remember(state.actualTyreCompound, state.weather) {
        when {
            state.weather >= 3 -> 8000 to 9000
            state.actualTyreCompound in 16..18 -> 10000 to 11000
            state.actualTyreCompound in 19..20 -> 9800 to 10500
            state.actualTyreCompound in 21..22 -> 9000 to 10000
            else -> 9500 to 10500
        }
    }

    val currentRPM = state.rpm.toInt()
    val maxEngineRPM = 15000  // ⭐ Massimo fisso (non usare state.maxRPM che può essere sbagliato)

    // ⭐ Progress della barra (quanto si riempie)
    val rpmProgress = (currentRPM.toFloat() / maxEngineRPM).coerceIn(0f, 1f)

    // ⭐ Posizioni delle stanghette del range ideale
    val minRPMPosition = (minRPM.toFloat() / maxEngineRPM).coerceIn(0f, 1f)
    val maxRPMPosition = (maxRPM.toFloat() / maxEngineRPM).coerceIn(0f, 1f)

    // Colore: verde se dentro il range, arancione fuori
    val isInRange = currentRPM in minRPM..maxRPM
    val barColor by animateColorAsState(
        targetValue = if (isInRange) Color(0xFF4CAF50) else Color(0xFFFFA726),
        animationSpec = tween(200),
        label = "launch_color"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ⭐ BARRA RIEMPIMENTO (RPM correnti)
            Box(
                modifier = Modifier
                    .fillMaxWidth(rpmProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                barColor.copy(alpha = 0.4f),
                                barColor
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            // ⭐ STANGHETTA SINISTRA (Min RPM)
            Box(
                modifier = Modifier
                    .fillMaxWidth(minRPMPosition)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(0.8f)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .align(Alignment.CenterEnd)
                )
            }

            // ⭐ STANGHETTA DESTRA (Max RPM)
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxRPMPosition)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(0.8f)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .align(Alignment.CenterEnd)
                )
            }

            // ⭐ ZONA IDEALE (rettangolo trasparente tra le due stanghette)
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val minX = size.width * minRPMPosition
                    val maxX = size.width * maxRPMPosition

                    drawRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = Offset(minX, 0f),
                        size = Size(maxX - minX, size.height)
                    )
                }
            }

            // ⭐ TESTO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAUNCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$minRPM-$maxRPM",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "$currentRPM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )

                    if (isInRange) {
                        Text(
                            text = "✓",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}