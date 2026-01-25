package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip


@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState()
    val pages = listOf(
        OnboardingPage.Welcome,
        OnboardingPage.Setup,
        OnboardingPage.Connect,
        OnboardingPage.SelectDashboard
    )

    // ⭐ RESPONSIVE - Dimensioni schermo
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )

            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager con le slide
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page], screenHeight, screenWidth)
            }

            // Indicatori + Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = screenHeight * 0.04f),  // ⭐ 4% altezza
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dots indicator
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    activeColor = Color(0xFFE94560),
                    inactiveColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(screenHeight * 0.02f)  // ⭐ 2% altezza
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))  // ⭐ 3% altezza

                // Button
                val isLastPage = pagerState.currentPage == pages.size - 1
                val coroutineScope = rememberCoroutineScope()

                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            coroutineScope.launch {  // ✅ CORRETTO
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(screenHeight * 0.07f),  // ⭐ 7% altezza
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94560)
                    ),
                    shape = RoundedCornerShape(screenHeight * 0.035f)  // ⭐ Rounded relativo
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        fontSize = (screenHeight.value * 0.022f).sp,  // ⭐ 2.2% altezza
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLastPage) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(screenHeight * 0.03f)  // ⭐ Icona responsive
                        )
                    }
                }

                // Skip button
                if (!isLastPage) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.padding(top = screenHeight * 0.01f)
                    ) {
                        Text(
                            text = "Skip",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = (screenHeight.value * 0.018f).sp  // ⭐ 1.8% altezza
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = screenWidth * 0.08f),  // ⭐ 8% larghezza
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon/Image
        Surface(
            modifier = Modifier.size(screenHeight * 0.15f),  // ⭐ 15% altezza
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = page.iconRes),  // ⭐ USA ICONA PNG
                    contentDescription = page.title,
                    modifier = Modifier
                        .size(screenHeight * 0.1f)
                        .clip(RoundedCornerShape(screenHeight*0.015f)),  // Dimensione icona
                )
            }
        }

        Spacer(modifier = Modifier.height(screenHeight * 0.06f))  // ⭐ 6% altezza

        // Title
        Text(
            text = page.title,
            fontSize = (screenHeight.value * 0.035f).sp,  // ⭐ 3.5% altezza
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.02f))  // ⭐ 2% altezza

        // Description
        Text(
            text = page.description,
            fontSize = (screenHeight.value * 0.02f).sp,  // ⭐ 2% altezza
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = (screenHeight.value * 0.03f).sp  // ⭐ Line height responsive
        )

        // Extra content
        if (page.extraContent.isNotEmpty()) {
            Spacer(modifier = Modifier.height(screenHeight * 0.04f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(screenHeight * 0.02f),  // ⭐ Border radius responsive
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(screenHeight * 0.025f)  // ⭐ Padding responsive
                ) {
                    page.extraContent.forEach { line ->
                        Row(
                            modifier = Modifier.padding(vertical = screenHeight * 0.005f),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                color = Color(0xFFE94560),
                                fontSize = (screenHeight.value * 0.02f).sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = line,
                                color = Color.White,
                                fontSize = (screenHeight.value * 0.018f).sp,  // ⭐ Font responsive
                                lineHeight = (screenHeight.value * 0.025f).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class OnboardingPage(
    val iconRes: Int,
    val title: String,
    val description: String,
    val extraContent: List<String> = emptyList()
) {
    object Welcome : OnboardingPage(
        iconRes = R.drawable.ic_logo_app,
        title = "Welcome to F1 Dashboard Pro",
        description = "Transform your device into a professional F1 telemetry dashboard with real-time data from F1 2025"
    )

    object Setup : OnboardingPage(
        iconRes = R.drawable.ic_setting,
        title = "Configure F1 2025",
        description = "Enable UDP telemetry in your game settings",
        extraContent = listOf(
            "Open F1 2025 Game",
            "Go to Settings → Telemetry",
            "Enable UDP Telemetry",
            "Set UDP Port to 20777",
            "Set UDP Format to 2025"
        )
    )

    object Connect : OnboardingPage(
        iconRes = R.drawable.ic_connection,
        title = "Connect Your Device",
        description = "Enter this device's IP address in F1 2025 UDP settings",
        extraContent = listOf(
            "Find your device IP in Settings",
            "Enter it in F1 2025 UDP Broadcast",
            "Make sure both devices are on the same WiFi",
            "Start a session to test connection"
        )
    )

    object SelectDashboard : OnboardingPage(
        iconRes = R.drawable.ic_steering,
        title = "Choose Your Dashboard",
        description = "Select the perfect dashboard for your racing session",
        extraContent = listOf(
            "Race: Full dashboard with position tracking",
            "Qualifying: Best lap times and sectors",
            "Time Trial: Beat your rival's time"
        )
    )
}