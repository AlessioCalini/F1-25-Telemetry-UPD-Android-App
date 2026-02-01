package com.example.myapplication

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onResetOnboarding: () -> Unit,
    onOpenFAQ: () -> Unit,  // ⭐ NUOVO parametro
    isReceivingData: Boolean
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPortDialog by remember { mutableStateOf(false) }
    var showTestDialog by remember { mutableStateOf(false) }
    var showSpeedUnitDialog by remember { mutableStateOf(false) }
    var showTyreDisplayDialog by remember { mutableStateOf(false) }
    var showTempUnitDialog by remember { mutableStateOf(false) }

    val sharedPrefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    var udpPort by remember {
        mutableStateOf(sharedPrefs.getInt("udp_port", 20777))
    }

    // Stati per unità
    var speedUnit by remember {
        mutableStateOf(UnitsPreferences.getSpeedUnit(context))
    }
    var tyreDisplay by remember {
        mutableStateOf(UnitsPreferences.getTyreDisplay(context))
    }
    var tempUnit by remember {
        mutableStateOf(UnitsPreferences.getTempUnit(context))
    }

    val deviceIp = remember { getDeviceIpAddress(context) }

    val appVersion: String = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            SettingsHeader(onBack = onBack)

            // Network Section
            SettingsSection(title = "Network") {
                SettingsItem(
                    icon = Icons.Default.Wifi,
                    title = "Device IP Address",
                    subtitle = deviceIp,
                    onClick = { }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "UDP Port",
                    subtitle = udpPort.toString(),
                    onClick = { showPortDialog = true }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Test Connection",
                    subtitle = "Press any controller button",
                    onClick = { showTestDialog = true }
                )
            }

            // Dashboard Section
            SettingsSection(title = "Dashboard") {
                SettingsItem(
                    icon = Icons.Default.Speed,
                    title = "Speed Unit",
                    subtitle = UnitsPreferences.getSpeedUnitLabel(speedUnit),
                    onClick = { showSpeedUnitDialog = true }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Circle,
                    title = "Tyre Information",
                    subtitle = UnitsPreferences.getTyreDisplayLabel(tyreDisplay),
                    onClick = { showTyreDisplayDialog = true }
                )

                // Temperature unit (solo se temperatura è selezionata)
                if (tyreDisplay == UnitsPreferences.TyreDisplay.TEMPERATURE) {
                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    SettingsItem(
                        icon = Icons.Default.Thermostat,
                        title = "Temperature Unit",
                        subtitle = UnitsPreferences.getTempUnitLabel(tempUnit),
                        onClick = { showTempUnitDialog = true }
                    )
                }
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = appVersion,
                    onClick = { }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Tutorial",
                    subtitle = "View onboarding again",
                    onClick = {
                        onResetOnboarding()
                        onBack()
                    }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                // ⭐ NUOVO - FAQ
                SettingsItem(
                    icon = Icons.Default.HelpOutline,
                    title = "FAQ",
                    subtitle = "Frequently asked questions",
                    onClick = onOpenFAQ
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                // ⭐ NUOVO - Privacy Policy
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = { /* Link esterno */ }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                // ⭐ NUOVO - Report Bug
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Report Bug",
                    subtitle = "Send us feedback",
                    onClick = { /* Email */ }
                )
            }
        }
    }

    // Port Dialog
    if (showPortDialog) {
        PortDialog(
            currentPort = udpPort,
            onDismiss = { showPortDialog = false },
            onConfirm = { newPort ->
                udpPort = newPort
                sharedPrefs.edit().putInt("udp_port", newPort).apply()
                showPortDialog = false
            }
        )
    }

    // Test Connection Dialog
    if (showTestDialog) {
        ConnectionTestDialog(
            isReceivingData = isReceivingData,
            onDismiss = { showTestDialog = false }
        )
    }

    // Speed Unit Dialog
    if (showSpeedUnitDialog) {
        SpeedUnitDialog(
            currentSelection = speedUnit,
            onDismiss = { showSpeedUnitDialog = false },
            onSelect = { selected ->
                speedUnit = selected
                UnitsPreferences.setSpeedUnit(context, selected)
                showSpeedUnitDialog = false
            }
        )
    }

    // Tyre Display Dialog
    if (showTyreDisplayDialog) {
        TyreDisplayDialog(
            currentSelection = tyreDisplay,
            onDismiss = { showTyreDisplayDialog = false },
            onSelect = { selected ->
                tyreDisplay = selected
                UnitsPreferences.setTyreDisplay(context, selected)
                showTyreDisplayDialog = false
            }
        )
    }

    // Temp Unit Dialog
    if (showTempUnitDialog) {
        TempUnitDialog(
            currentSelection = tempUnit,
            onDismiss = { showTempUnitDialog = false },
            onSelect = { selected ->
                tempUnit = selected
                UnitsPreferences.setTempUnit(context, selected)
                showTempUnitDialog = false
            }
        )
    }
}

@Composable
fun SettingsHeader(onBack: () -> Unit) {
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
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFFE94560),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Open",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SpeedUnitDialog(
    currentSelection: UnitsPreferences.SpeedUnit,
    onDismiss: () -> Unit,
    onSelect: (UnitsPreferences.SpeedUnit) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Speed Unit") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(UnitsPreferences.SpeedUnit.KMH) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSelection == UnitsPreferences.SpeedUnit.KMH,
                        onClick = { onSelect(UnitsPreferences.SpeedUnit.KMH) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "km/h", fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(UnitsPreferences.SpeedUnit.MPH) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSelection == UnitsPreferences.SpeedUnit.MPH,
                        onClick = { onSelect(UnitsPreferences.SpeedUnit.MPH) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "mph", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TyreDisplayDialog(
    currentSelection: UnitsPreferences.TyreDisplay,
    onDismiss: () -> Unit,
    onSelect: (UnitsPreferences.TyreDisplay) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tyre Information") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(UnitsPreferences.TyreDisplay.WEAR) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSelection == UnitsPreferences.TyreDisplay.WEAR,
                        onClick = { onSelect(UnitsPreferences.TyreDisplay.WEAR) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Wear (%)", fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(UnitsPreferences.TyreDisplay.TEMPERATURE) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSelection == UnitsPreferences.TyreDisplay.TEMPERATURE,
                        onClick = { onSelect(UnitsPreferences.TyreDisplay.TEMPERATURE) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Temperature", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TempUnitDialog(
    currentSelection: UnitsPreferences.TempUnit,
    onDismiss: () -> Unit,
    onSelect: (UnitsPreferences.TempUnit) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Temperature Unit") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(UnitsPreferences.TempUnit.CELSIUS) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSelection == UnitsPreferences.TempUnit.CELSIUS,
                        onClick = { onSelect(UnitsPreferences.TempUnit.CELSIUS) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Celsius (°C)", fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(UnitsPreferences.TempUnit.FAHRENHEIT) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSelection == UnitsPreferences.TempUnit.FAHRENHEIT,
                        onClick = { onSelect(UnitsPreferences.TempUnit.FAHRENHEIT) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Fahrenheit (°F)", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PortDialog(
    currentPort: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var portText by remember { mutableStateOf(currentPort.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("UDP Port") },
        text = {
            Column {
                Text(
                    text = "Enter UDP port (1024-65535)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = portText,
                    onValueChange = {
                        portText = it
                        errorMessage = null
                    },
                    label = { Text("Port") },
                    singleLine = true,
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val port = portText.toIntOrNull()
                    when {
                        port == null -> errorMessage = "Invalid port number"
                        port < 1024 || port > 65535 -> errorMessage = "Port must be between 1024-65535"
                        else -> onConfirm(port)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getDeviceIpAddress(context: Context): String {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipInt = wifiInfo.ipAddress

        if (ipInt != 0) {
            return String.format(
                "%d.%d.%d.%d",
                ipInt and 0xff,
                ipInt shr 8 and 0xff,
                ipInt shr 16 and 0xff,
                ipInt shr 24 and 0xff
            )
        }

        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses

            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                    return address.hostAddress ?: "Unknown"
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return "Not connected"
}