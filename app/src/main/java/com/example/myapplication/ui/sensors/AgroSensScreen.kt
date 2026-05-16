package com.example.myapplication.ui.sensors

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.domain.model.SensorData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.tooling.preview.Preview

private val ScreenBg = Color(0xFFF8F9FA)
private val BrandGreen = Color(0xFF2E7D32)
private val StatusBadgeBg = Color(0xFFE3F2FD)
private val StatusBadgeText = Color(0xFF1976D2)

@Composable
fun AgroSensScreen(
    modifier: Modifier = Modifier,
    viewModel: AgroSensViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var currentTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Historial, 2: Ajustes
    val history = remember { mutableStateListOf<SensorData>().apply { addAll(viewModel.history) } }

    val hasBluetoothConnectPermission by remember {
        derivedStateOf {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@derivedStateOf true
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val requiredScanPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }
    }

    val hasBluetoothScanPermission by remember {
        derivedStateOf {
            ContextCompat.checkSelfPermission(
                context,
                requiredScanPermission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionLauncherConnect = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val permissionLauncherScan = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    DisposableEffect(Unit) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            val device =
                                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                                    ?: return
                            try {
                                viewModel.addDiscoveredDevice(device)
                            } catch (_: SecurityException) {
                            } catch (_: Throwable) {
                            }
                        }

                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            viewModel.updateScanning(false)
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            context.registerReceiver(receiver, filter)

            onDispose {
                try {
                    viewModel.updateScanning(false)
                    adapter.cancelDiscovery()
                } catch (_: Throwable) {
                }
                context.unregisterReceiver(receiver)
            }
        } else {
            onDispose { }
        }
    }

    LaunchedEffect(hasBluetoothConnectPermission) {
        if (hasBluetoothConnectPermission) viewModel.refreshBondedDevices()
    }

    fun startScan() {
        if (!hasBluetoothScanPermission) {
            permissionLauncherScan.launch(requiredScanPermission)
            return
        }
        if (!hasBluetoothConnectPermission) {
            permissionLauncherConnect.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Manifest.permission.BLUETOOTH_CONNECT
                } else {
                    // En versiones anteriores no se requiere este permiso,
                    // pero teóricamente hasBluetoothConnectPermission sería true.
                    ""
                }
            )
            return
        }

        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        try {
            viewModel.clearDiscoveredDevices()
            viewModel.updateScanning(true)
            if (adapter.isDiscovering) adapter.cancelDiscovery()
            adapter.startDiscovery()
        } catch (_: Throwable) {
            viewModel.updateScanning(false)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                0 -> DashboardTab(
                    viewModel = viewModel,
                    hasBluetoothConnectPermission = hasBluetoothConnectPermission,
                    activity = activity,
                    permissionLauncherConnect = permissionLauncherConnect,
                    onScan = { startScan() },
                    onSaveCurrentData = { savedReading -> history.add(savedReading) }
                )
                1 -> HistoryTab(history = history)
                2 -> SettingsTab()
            }
        }

        BottomNavBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}

@Composable
private fun DashboardTab(
    viewModel: AgroSensViewModel,
    hasBluetoothConnectPermission: Boolean,
    activity: Activity?,
    permissionLauncherConnect: androidx.activity.result.ActivityResultLauncher<String>,
    onScan: () -> Unit,
    onSaveCurrentData: (SensorData) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        DashboardHeader(
            isConnected = viewModel.isConnected,
            isConnecting = viewModel.isConnecting,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!hasBluetoothConnectPermission) {
            BluetoothPermissionCard(
                onRequestPermission = {
                    if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncherConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                },
            )
        } else {
            BluetoothPanel(
                viewModel = viewModel,
                onScan = onScan,
            )

            Spacer(modifier = Modifier.height(20.dp))

            val data = viewModel.sensorData
            SensorDataGrid(data = data)

            data.lastUpdateEpochMs?.let { epoch ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Última actualización: ${formatEpoch(epoch)}",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val snapshot = data.copy()
                    viewModel.saveCurrentData()
                    onSaveCurrentData(snapshot)
                    Toast.makeText(
                        context,
                        "Datos guardados en el historial",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Text(
                    text = "Guardar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun HistoryTab(history: List<SensorData>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = "Historial de Datos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay datos guardados aún.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                history.forEach { data ->
                    HistoryItemCard(data)
                }
            }
        }
    }
}


@Composable
private fun SettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Ajustes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bluetooth",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Permisos, conexión y dispositivos guardados del sensor.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Historial",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Listado de lecturas guardadas desde el panel principal.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aplicación",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Versión, estado general y opciones básicas de la pantalla.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(data: SensorData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.lastUpdateEpochMs?.let { formatFullDate(it) } ?: "Sin fecha",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandGreen
                )
                Text(
                    text = data.lastUpdateEpochMs?.let { formatEpoch(it) } ?: "--:--",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                HistoryMetric(label = "Hum.", value = "${data.humidityPercent?.let { formatOneDecimal(it) } ?: "--"}%", modifier = Modifier.weight(1f))
                HistoryMetric(label = "Temp.", value = "${data.temperatureC?.let { formatOneDecimal(it) } ?: "--"}°C", modifier = Modifier.weight(1f))
                HistoryMetric(label = "EC", value = "${data.ecUSPerCm ?: "--"}", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                HistoryMetric(label = "N", value = "${data.nitrogenMgPerKg ?: "--"}", modifier = Modifier.weight(1f))
                HistoryMetric(label = "P", value = "${data.phosphorusMgPerKg ?: "--"}", modifier = Modifier.weight(1f))
                HistoryMetric(label = "K", value = "${data.potassiumMgPerKg ?: "--"}", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HistoryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
    }
}

@Composable
private fun DashboardHeader(
    isConnected: Boolean,
    isConnecting: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🌱", fontSize = 50.sp)
        Text(
            text = "AgroSens",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
        )
        Spacer(modifier = Modifier.height(10.dp))
        val (badgeLabel, dot) = when {
            isConnected -> "Sensor conectado" to "●"
            isConnecting -> "Conectando…" to "◐"
            else -> "Sensor desconectado" to "○"
        }
        Row(
            modifier = Modifier
                .background(StatusBadgeBg, RoundedCornerShape(20.dp))
                .padding(horizontal = 15.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = dot,
                color = StatusBadgeText,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 6.dp),
            )
            Text(
                text = badgeLabel,
                color = StatusBadgeText,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun BluetoothPermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Bluetooth",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Se necesita permiso para conectarse al sensor por Bluetooth.",
                color = Color(0xFF666666),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) {
                Text("Habilitar Bluetooth")
            }
        }
    }
}

@Composable
private fun BluetoothPanel(
    viewModel: AgroSensViewModel,
    onScan: () -> Unit,
) {
    // Estado para controlar si la lista está visible o no
    var expandList by remember { mutableStateOf(true) }

    // Si se conecta exitosamente, ocultamos la lista automáticamente
    LaunchedEffect(viewModel.isConnected) {
        if (viewModel.isConnected) {
            expandList = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            // --- 1. CABECERA Y ESTADO ---
            Text(
                text = "Conexión Bluetooth",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    viewModel.isConnected -> "Conectado"
                    viewModel.isConnecting -> "Conectando…"
                    else -> "Desconectado"
                },
                color = when {
                    viewModel.isConnected -> Color(0xFF4CAF50)
                    else -> Color(0xFF666666)
                },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )

            viewModel.errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = err, color = Color(0xFFF44336), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. BOTONES PRINCIPALES (Siempre visibles arriba) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { viewModel.connect() },
                    enabled = viewModel.selectedDeviceAddress != null &&
                            !viewModel.isConnected &&
                            !viewModel.isConnecting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                ) {
                    Text("Conectar")
                }
                Button(
                    onClick = {
                        viewModel.disconnect()
                        expandList = true // Volver a mostrar la lista al desconectar
                    },
                    enabled = viewModel.isConnected || viewModel.isConnecting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                ) {
                    Text("Desconectar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. CONTROL DE EXPANSIÓN (Mostrar/Ocultar) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandList = !expandList }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dispositivos disponibles",
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Text(
                    text = if (expandList) "Ocultar ▲" else "Mostrar ▼",
                    color = BrandGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }

            // --- 4. LISTA DE DISPOSITIVOS (Colapsable) ---
            if (expandList) {
                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.bondedDevices.isEmpty()) {
                    Text(
                        text = "No hay dispositivos emparejados. Empareja el ESP32 desde Ajustes de Android.",
                        color = Color(0xFF666666),
                        fontSize = 13.sp,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.bondedDevices.forEach { device ->
                            DeviceRowLight(
                                device = device,
                                selected = viewModel.selectedDeviceAddress == device.address,
                                onClick = { viewModel.selectDevice(device.address) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Buscar dispositivos (sin emparejar)",
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScan,
                    enabled = !viewModel.isScanning && !viewModel.isConnected && !viewModel.isConnecting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                ) {
                    Text(if (viewModel.isScanning) "Buscando…" else "Escanear Bluetooth")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (viewModel.discoveredDevices.isEmpty()) {
                    Text(
                        text = if (viewModel.isScanning) "Escaneando…" else "Todavía no se encontró ningún dispositivo nuevo.",
                        color = Color(0xFF666666),
                        fontSize = 13.sp,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.discoveredDevices.forEach { device ->
                            DeviceRowLight(
                                device = device,
                                selected = viewModel.selectedDeviceAddress == device.address,
                                onClick = { viewModel.selectDevice(device.address) },
                            )
                        }
                    }
                }
            } else {
                // --- 5. VISTA CONTRAÍDA (Muestra solo el seleccionado) ---
                if (viewModel.selectedDeviceAddress != null) {
                    val selectedDevice = viewModel.bondedDevices.find { it.address == viewModel.selectedDeviceAddress }
                        ?: viewModel.discoveredDevices.find { it.address == viewModel.selectedDeviceAddress }

                    if (selectedDevice != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DeviceRowLight(
                            device = selectedDevice,
                            selected = true,
                            onClick = { expandList = true } // Permite expandir tocando el dispositivo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorDataGrid(data: SensorData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardSensorCard(
                label = "Humedad",
                valueText = data.humidityPercent?.let { formatOneDecimal(it) } ?: "--",
                unit = "%",
                icon = "💧",
                accent = Color(0xFF2196F3),
                status = statusHumidity(data.humidityPercent),
                modifier = Modifier.weight(1f),
            )
            DashboardSensorCard(
                label = "Temperatura",
                valueText = data.temperatureC?.let { formatOneDecimal(it) } ?: "--",
                unit = "°C",
                icon = "🌡️",
                accent = Color(0xFFFF9800),
                status = statusTemperature(data.temperatureC),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardSensorCard(
                label = "Cond. (EC)",
                valueText = data.ecUSPerCm?.toString() ?: "--",
                unit = "µS/cm",
                icon = "⚡",
                accent = Color(0xFF9E9E9E),
                status = statusEc(data.ecUSPerCm),
                modifier = Modifier.weight(1f),
            )
            DashboardSensorCard(
                label = "Nitrógeno (N)",
                valueText = data.nitrogenMgPerKg?.toString() ?: "--",
                unit = "mg/kg",
                icon = "🌿",
                accent = Color(0xFF4CAF50),
                status = statusNpk(data.nitrogenMgPerKg, low = 25, high = 50),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardSensorCard(
                label = "Fósforo (P)",
                valueText = data.phosphorusMgPerKg?.toString() ?: "--",
                unit = "mg/kg",
                icon = "🌱",
                accent = Color(0xFF1B5E20),
                status = statusNpk(data.phosphorusMgPerKg, low = 20, high = 45),
                modifier = Modifier.weight(1f),
            )
            DashboardSensorCard(
                label = "Potasio (K)",
                valueText = data.potassiumMgPerKg?.toString() ?: "--",
                unit = "mg/kg",
                icon = "🌾",
                accent = Color(0xFFFBC02D),
                status = statusNpk(data.potassiumMgPerKg, low = 30, high = 60),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardSensorCard(
    label: String,
    valueText: String,
    unit: String,
    icon: String,
    accent: Color,
    status: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(modifier = Modifier.padding(15.dp)) {
                Text(text = icon, fontSize = 24.sp)
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(vertical = 2.dp),
                ) {
                    Text(
                        text = valueText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                    )
                    Text(
                        text = " $unit",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                    )
                }
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun DeviceRowLight(
    device: BluetoothDevice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val border = if (selected) BorderStroke(2.dp, Color(0xFF4CAF50)) else BorderStroke(1.dp, Color(0xFFE0E0E0))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
        border = border,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = device.name ?: device.address,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Text(
                    text = "Seleccionado",
                    color = BrandGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFEEEEEE),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItem(
                label = "Dashboard",
                isSelected = currentTab == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                label = "Historial",
                isSelected = currentTab == 1,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                label = "Ajustes",
                isSelected = currentTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) BrandGreen else Color(0xFFAAAAAA),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private fun formatOneDecimal(value: Float): String {
    return String.format(Locale.getDefault(), "%.1f", value)
}

private fun statusHumidity(h: Float?): String = when {
    h == null -> "Sin datos"
    h >= 40f -> "Óptimo"
    h >= 25f -> "Normal"
    else -> "Bajo"
}

private fun statusTemperature(t: Float?): String = when {
    t == null -> "Sin datos"
    t in 20f..30f -> "Normal"
    t > 30f -> "Alto"
    else -> "Bajo"
}

private fun statusEc(ec: Int?): String = when {
    ec == null -> "Sin datos"
    ec == 0 -> "Sin señal"
    ec < 300 -> "Bajo"
    ec <= 900 -> "Estable"
    else -> "Alto"
}

private fun statusNpk(value: Int?, low: Int, high: Int): String = when {
    value == null -> "Sin datos"
    value < low -> "Bajo - Fertilizar"
    value <= high -> "Normal"
    else -> "Bueno"
}

private fun formatEpoch(epochMs: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

private fun formatFullDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

@Preview(showBackground = true)
@Composable
fun HistoryTabPreview() {
    val mockHistory = listOf(
        SensorData(
            humidityPercent = 45.5f,
            temperatureC = 24.2f,
            ecUSPerCm = 450,
            nitrogenMgPerKg = 30,
            phosphorusMgPerKg = 25,
            potassiumMgPerKg = 40,
            lastUpdateEpochMs = System.currentTimeMillis()
        ),
        SensorData(
            humidityPercent = 38.0f,
            temperatureC = 26.5f,
            ecUSPerCm = 500,
            nitrogenMgPerKg = 28,
            phosphorusMgPerKg = 22,
            potassiumMgPerKg = 38,
            lastUpdateEpochMs = System.currentTimeMillis() - 3600000 // Hace una hora
        )
    )
    Box(modifier = Modifier.background(ScreenBg)) {
        HistoryTab(history = mockHistory)
    }
}