package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FAQItem(
    val question: String,
    val answer: String
)

@Composable
fun FAQScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    val faqs = listOf(
        FAQItem(
            question = "How do I configure F1 2025?",
            answer = "1. Open F1 2025\n2. Go to Settings → Telemetry\n3. Enable UDP Telemetry\n4. Set UDP Port to 20777\n5. Set UDP Format to 2025\n6. Enter your device's IP address in UDP Broadcast"
        ),
        FAQItem(
            question = "Why am I not receiving data?",
            answer = "Check these common issues:\n• F1 2025 is running and in a session\n• UDP Telemetry is enabled in game settings\n• Both devices are on the same WiFi network\n• Correct IP address is entered in F1 2025\n• Port 20777 is not blocked by firewall\n• Use Settings → Test Connection to verify"
        ),
        FAQItem(
            question = "How do I find my device's IP address?",
            answer = "Your device's IP address is shown in Settings → Network → Device IP Address. You need to enter this IP in F1 2025's UDP Broadcast setting."
        ),
        FAQItem(
            question = "Which dashboard should I use?",
            answer = "• Race: Full dashboard with position tracking, nearby drivers, ERS, DRS, and launch control\n• Qualifying: Focus on lap times, sector analysis, and personal bests\n• Time Trial: Compare against your rival with detailed sector breakdowns"
        ),
        FAQItem(
            question = "What does the Safety Car yellow border mean?",
            answer = "The yellow flashing border appears during Safety Car or Virtual Safety Car periods. The delta time shows your time difference from the required pace."
        ),
        FAQItem(
            question = "How does the Launch Control bar work?",
            answer = "During race starts, when in 1st gear with clutch engaged and speed below 10 km/h, a green bar appears showing optimal RPM range for the best launch."
        ),
        FAQItem(
            question = "What are the suggested gear arrows?",
            answer = "Red arrows (↑ or ↓) appear when F1 2025 suggests you should shift up or down for optimal performance."
        ),
        FAQItem(
            question = "Can I change units (km/h to mph)?",
            answer = "Yes! Go to Settings → Dashboard → Speed Unit to switch between km/h and mph. You can also change tyre information display and temperature units."
        ),
        FAQItem(
            question = "What's the difference between tyre wear and temperature?",
            answer = "In Settings → Dashboard → Tyre Information, you can choose:\n• Wear: Shows remaining tyre life as a percentage\n• Temperature: Shows tyre surface temperature (useful for setup and driving style)"
        ),
        FAQItem(
            question = "Why does the dashboard reset after a flashback?",
            answer = "The app now protects your best lap time during flashbacks. Your personal best should remain intact even after using flashback in F1 2025."
        ),
        FAQItem(
            question = "Can I use this with other F1 games?",
            answer = "This app is designed specifically for F1 2025 using the 2025 UDP telemetry format. It may not work correctly with older F1 games."
        ),
        FAQItem(
            question = "The app crashed or has a bug, what should I do?",
            answer = "Please report bugs via Settings → Report Bug. Include:\n• What you were doing when it crashed\n• Which dashboard you were using\n• Your device model and Android version"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "FAQ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                faqs.forEach { faq ->
                    FAQCard(faq)
                }

                // Extra space at bottom
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Question
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFFE94560)
                )
            }

            // Answer (expandable)
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}