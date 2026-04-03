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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgroSensScreen(
    modifier: Modifier = Modifier,
    viewModel: AgroSensViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else Manifest.permission.ACCESS_FINE_LOCATION
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
    ) { /* state se recalcula con derivedStateOf */ }

    val permissionLauncherScan = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* state se recalcula con derivedStateOf */ }

    // Receiver para capturar dispositivos encontrados durante el scan (Bluetooth clásico).
    DisposableEffect(Unit) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            val device =
                                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                            try {
                                viewModel.addDiscoveredDevice(device)
                            } catch (_: SecurityException) {
                                // En Android 12+ esto puede fallar si faltan permisos.
                            } catch (_: Throwable) {
                                // Ignoramos dispositivos que no podamos procesar.
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
            onDispose { /* no-op */ }
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
            permissionLauncherConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }

        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        try {
            viewModel.clearDiscoveredDevices()
            viewModel.updateScanning(true)
            if (adapter.isDiscovering) adapter.cancelDiscovery()
            adapter.startDiscovery()
        } catch (t: Throwable) {
            viewModel.updateScanning(false)
            // Mostramos el error en la UI usando el mismo campo que conexión
            // (para no crear otro estado).
            // Nota: si es un error de permisos, el mensaje te ayuda a ajustar el AndroidManifest.
            // Se setea en el hilo principal por seguridad.
            // (El VM actualiza en UI mediante Compose state.)
            // Aprovechamos connect errorMessage.
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "AgroSens (ESP32)",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!hasBluetoothConnectPermission) {
            Text(
                text = "Se necesita permiso para conectarse al Bluetooth.",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = {
                    if (activity != null) {
                        permissionLauncherConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Habilitar Bluetooth")
            }
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (viewModel.isConnected) "Conectado" else if (viewModel.isConnecting) "Conectando..." else "Desconectado",
                    color = if (viewModel.isConnected) Color(0xFF4CAF50) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Color(0xFFF44336)
                        ,fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Dispositivos emparejados:", color = Color.White, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(8.dp))
                if (viewModel.bondedDevices.isEmpty()) {
                    Text(
                        text = "No hay dispositivos emparejados. Empareja el ESP32 desde Android y vuelve a intentar.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.bondedDevices.size) { idx ->
                            val device = viewModel.bondedDevices[idx]
                            DeviceRow(
                                device = device,
                                selected = viewModel.selectedDeviceAddress == device.address,
                                onClick = { viewModel.selectDevice(device.address) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.connect() },
                        enabled = viewModel.selectedDeviceAddress != null && !viewModel.isConnected && !viewModel.isConnecting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Conectar")
                    }
                    Button(
                        onClick = { viewModel.disconnect() },
                        enabled = viewModel.isConnected || viewModel.isConnecting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Desconectar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Datos del sensor",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val data = viewModel.sensorData
                SensorValueCard(label = "Humedad", value = data.humidityPercent?.let { "${it} %" })
                Spacer(modifier = Modifier.height(8.dp))
                SensorValueCard(label = "Temperatura", value = data.temperatureC?.let { "${it} C" })
                Spacer(modifier = Modifier.height(8.dp))
                SensorValueCard(label = "Conductividad", value = data.ecUSPerCm?.let { "${it} uS/cm" })
                Spacer(modifier = Modifier.height(8.dp))
                SensorValueCard(label = "Nitrogeno (N)", value = data.nitrogenMgPerKg?.let { "${it} mg/kg" })
                Spacer(modifier = Modifier.height(8.dp))
                SensorValueCard(label = "Fosforo (P)", value = data.phosphorusMgPerKg?.let { "${it} mg/kg" })
                Spacer(modifier = Modifier.height(8.dp))
                SensorValueCard(label = "Potasio (K)", value = data.potassiumMgPerKg?.let { "${it} mg/kg" })

                data.lastUpdateEpochMs?.let { epoch ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Última actualización: ${formatEpoch(epoch)}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Buscar dispositivos (sin emparejar)",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { startScan() },
                    enabled = !viewModel.isScanning && !viewModel.isConnected && !viewModel.isConnecting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text(if (viewModel.isScanning) "Buscando..." else "Escanear Bluetooth")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (viewModel.discoveredDevices.isEmpty()) {
                    Text(
                        text = if (viewModel.isScanning) "Escaneando..." else "Todavía no se encontró ningún dispositivo.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.discoveredDevices.size) { idx ->
                            val device = viewModel.discoveredDevices[idx]
                            DeviceRow(
                                device = device,
                                selected = viewModel.selectedDeviceAddress == device.address,
                                onClick = { viewModel.selectDevice(device.address) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: BluetoothDevice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFF4CAF50) else Color(0xFF1E2B3D)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = device.name ?: device.address,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Text(text = "Seleccionado", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SensorValueCard(label: String, value: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2B3D)),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value ?: "--",
                color = if (value == null) Color(0xFFB0BEC5) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

private fun formatEpoch(epochMs: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

