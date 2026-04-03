package com.example.myapplication.ui.sensors

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.bluetooth.SensorFieldUpdate
import com.example.myapplication.bluetooth.applyUpdate
import com.example.myapplication.bluetooth.parseSensorLine
import com.example.myapplication.model.SensorData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgroSensViewModel(application: Application) : AndroidViewModel(application) {

    private val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    var bondedDevices: List<BluetoothDevice> by mutableStateOf(emptyList())
        private set

    var discoveredDevices: List<BluetoothDevice> by mutableStateOf(emptyList())
        private set

    var selectedDeviceAddress: String? by mutableStateOf(null)
        private set

    var isConnecting: Boolean by mutableStateOf(false)
        private set

    var isConnected: Boolean by mutableStateOf(false)
        private set

    var isScanning: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    var sensorData: SensorData by mutableStateOf(SensorData())
        private set

    private var socket: BluetoothSocket? = null
    private var readJob: Job? = null

    fun refreshBondedDevices() {
        bondedDevices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()
        if (selectedDeviceAddress == null) {
            selectedDeviceAddress = bondedDevices.firstOrNull()?.address
        }
    }

    fun selectDevice(address: String) {
        selectedDeviceAddress = address
    }

    fun clearDiscoveredDevices() {
        discoveredDevices = emptyList()
    }

    fun addDiscoveredDevice(device: BluetoothDevice) {
        if (discoveredDevices.any { it.address == device.address }) return
        discoveredDevices = discoveredDevices + device
    }

    fun connect() {
        val address = selectedDeviceAddress ?: return
        connectToAddress(address)
    }

    private fun connectToAddress(address: String) {
        val adapter = bluetoothAdapter ?: run {
            errorMessage = "Bluetooth no disponible en este dispositivo."
            return
        }

        val device = try {
            adapter.getRemoteDevice(address)
        } catch (t: Throwable) {
            errorMessage = t.message ?: "No se pudo preparar el dispositivo Bluetooth."
            return
        }

        if (isConnecting || isConnected) return

        // Evita problemas: mientras está haciendo discovery, a veces falla RFCOMM.
        if (adapter.isDiscovering) {
            try {
                adapter.cancelDiscovery()
            } catch (_: Throwable) {
            }
        }

        errorMessage = null
        isConnecting = true
        readJob?.cancel()
        try {
            socket?.close()
        } catch (_: Throwable) {
        }
        socket = null
        isConnected = false

        readJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Muchas apps de "Serial Bluetooth" usan RFCOMM inseguro para evitar problemas
                // si el dispositivo no está emparejado (bonded) en Android.
                val createdSocket = try {
                    device.createInsecureRfcommSocketToServiceRecord(sppUUID)
                } catch (_: Throwable) {
                    device.createRfcommSocketToServiceRecord(sppUUID)
                }

                createdSocket.connect()

                socket = createdSocket
                withContext(Dispatchers.Main) {
                    isConnecting = false
                    isConnected = true
                }

                val reader = BufferedReader(InputStreamReader(createdSocket.inputStream))
                while (isActive && createdSocket.isConnected) {
                    val line = reader.readLine() ?: break
                    val update: SensorFieldUpdate? = parseSensorLine(line)
                    if (update != null) {
                        withContext(Dispatchers.Main) {
                            sensorData = sensorData.applyUpdate(update)
                        }
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    errorMessage = t.message ?: "Error al conectar/leer datos"
                    isConnecting = false
                    isConnected = false
                }
            } finally {
                try {
                    socket?.close()
                } catch (_: Throwable) {
                }
                socket = null
                withContext(Dispatchers.Main) {
                    isConnecting = false
                    isConnected = false
                }
            }
        }
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null
        try {
            socket?.close()
        } catch (_: Throwable) {
        }
        socket = null

        isConnecting = false
        isConnected = false
    }

    fun updateScanning(scanning: Boolean) {
        isScanning = scanning
    }
}

