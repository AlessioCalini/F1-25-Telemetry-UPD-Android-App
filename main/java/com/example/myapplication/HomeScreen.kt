package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton



enum class DashboardType {
    NONE,
    RACE,
    QUALIFYING,
    TIME_TRIAL,
    SETTINGS,
    FAQ
}

@Composable
fun HomeScreen(
    onDashboardSelected: (DashboardType) -> Unit,
    onOpenSettings: () -> Unit,

) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backgroundColor = Color(0xFF050505)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
    ) {

            // ⭐ Resto del contenuto (tutto dentro questa Column)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = screenWidth * 0.05f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Settings button in alto a destra
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = screenHeight * 0.02f)
                ) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(screenHeight * 0.035f)
                        )
                    }
                }

                // Logo con sfumatura
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.35f)
                ) {
                    // Immagine del logo
                    Image(
                        painter = painterResource(id = R.drawable.logo_f1_dashboard),
                        contentDescription = "F1 Dashboard Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Sfumatura sinistra
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(screenWidth * 0.15f)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        backgroundColor,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Sfumatura destra
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(screenWidth * 0.15f)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        backgroundColor
                                    )
                                )
                            )
                    )

                    // Sfumatura in alto
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(screenHeight * 0.04f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        backgroundColor,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Sfumatura in basso
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(screenHeight * 0.04f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        backgroundColor
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = "Select Dashboard Mode",
                    fontSize = (screenHeight.value * 0.022f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.04f))

                // Tre card per le dashboard
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f)
                ) {
                    DashboardCard(
                        title = "Race",
                        description = "Full race dashboard with live position tracking",
                        iconRes = R.drawable.ic_race,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        onClick = { onDashboardSelected(DashboardType.RACE) }
                    )

                    DashboardCard(
                        title = "Qualifying",
                        description = "Best lap times and sector analysis",
                        iconRes = R.drawable.ic_qualifying,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        onClick = { onDashboardSelected(DashboardType.QUALIFYING) }
                    )

                    DashboardCard(
                        title = "Time Trial",
                        description = "Beat your rival with sector comparison",
                        iconRes = R.drawable.ic_time_trial,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        onClick = { onDashboardSelected(DashboardType.TIME_TRIAL) }
                    )
                }
            }
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    iconRes: Int,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val iconBoxSize = screenWidth * 0.18f
    val iconSize = screenWidth * 0.17f
    val cardPadding = screenWidth * 0.04f
    val cornerRadius = screenWidth * 0.04f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        color = Color(0xFF1A1A1A),  // ⭐ Card leggermente più chiare dello sfondo
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(iconBoxSize),
                shape = RoundedCornerShape(cornerRadius * 0.75f),
                color = Color(0xFFFFFFFF)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = (screenWidth.value * 0.055f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(screenHeight * 0.005f))
                Text(
                    text = description,
                    fontSize = (screenWidth.value * 0.035f).sp,
                    color = Color.Gray,
                    lineHeight = (screenWidth.value * 0.045f).sp
                )
            }
        }
    }
}