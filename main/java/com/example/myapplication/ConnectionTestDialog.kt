package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestStatus {
    WAITING,
    SUCCESS,
    TIMEOUT
}

@Composable
fun ConnectionTestDialog(
    isReceivingData: Boolean,
    onDismiss: () -> Unit
) {
    var testStatus by remember { mutableStateOf(TestStatus.WAITING) }
    var countdown by remember { mutableStateOf(60) }
    var testKey by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Avvia il countdown
    LaunchedEffect(testKey) {
        testStatus = TestStatus.WAITING
        countdown = 60

        coroutineScope.launch {
            for (i in 60 downTo 1) {
                countdown = i
                delay(1000)
            }
            if (testStatus == TestStatus.WAITING) {
                testStatus = TestStatus.TIMEOUT
            }
        }
    }

    // Controlla se ricevi dati
    LaunchedEffect(isReceivingData, testKey) {
        if (isReceivingData && testStatus == TestStatus.WAITING) {
            testStatus = TestStatus.SUCCESS
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon & Status
                    when (testStatus) {
                        TestStatus.WAITING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = Color(0xFF2196F3)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Testing Connection",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Press any button on your controller",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Timeout in ${countdown}s",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        TestStatus.SUCCESS -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Connection OK!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Receiving data from F1 2025",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        TestStatus.TIMEOUT -> {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Timeout",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "No Connection",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "No data received. Check:\n• F1 2025 is running\n• UDP Telemetry enabled\n• Same WiFi network\n• Correct IP address",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons in base allo stato
                    when (testStatus) {
                        TestStatus.TIMEOUT -> {
                            Button(
                                onClick = { testKey++ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            ) {
                                Text("Retry")
                            }
                        }

                        TestStatus.SUCCESS -> {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Close")
                            }
                        }

                        else -> {
                            // Nessun pulsante durante test
                        }
                    }
                }

                // X in alto a destra
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}