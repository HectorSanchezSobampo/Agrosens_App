package com.example.myapplication.data.bluetooth

import com.example.myapplication.domain.model.SensorData

sealed class SensorFieldUpdate {
    data class Humidity(val value: Float) : SensorFieldUpdate()
    data class Temperature(val value: Float) : SensorFieldUpdate()
    data class Ec(val value: Int) : SensorFieldUpdate()
    data class Nitrogen(val value: Int) : SensorFieldUpdate()
    data class Phosphorus(val value: Int) : SensorFieldUpdate()
    data class Potassium(val value: Int) : SensorFieldUpdate()
}

private val humidityRegex = Regex("^Humedad:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%\\s*$", RegexOption.IGNORE_CASE)
private val temperatureRegex = Regex("^Temperatura:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*C\\s*$", RegexOption.IGNORE_CASE)
private val ecRegex = Regex("^Conductividad:\\s*([0-9]+)\\s*uS/cm\\s*$", RegexOption.IGNORE_CASE)
private val nitrogenRegex = Regex("^Nitrogeno\\s*\\(N\\):\\s*([0-9]+)\\s*mg/kg\\s*$", RegexOption.IGNORE_CASE)
private val phosphorusRegex = Regex("^Fosforo\\s*\\(P\\):\\s*([0-9]+)\\s*mg/kg\\s*$", RegexOption.IGNORE_CASE)
private val potassiumRegex = Regex("^Potasio\\s*\\(K\\):\\s*([0-9]+)\\s*mg/kg\\s*$", RegexOption.IGNORE_CASE)

/**
 * Parsea las líneas que tu Arduino envía con `SerialBT.println(...)`.
 * Si la línea no coincide con ningún dato, retorna `null`.
 */
fun parseSensorLine(line: String): SensorFieldUpdate? {
    val trimmed = line.trim()

    humidityRegex.matchEntire(trimmed)?.let { return SensorFieldUpdate.Humidity(it.groupValues[1].toFloat()) }
    temperatureRegex.matchEntire(trimmed)?.let { return SensorFieldUpdate.Temperature(it.groupValues[1].toFloat()) }
    ecRegex.matchEntire(trimmed)?.let { return SensorFieldUpdate.Ec(it.groupValues[1].toInt()) }
    nitrogenRegex.matchEntire(trimmed)?.let { return SensorFieldUpdate.Nitrogen(it.groupValues[1].toInt()) }
    phosphorusRegex.matchEntire(trimmed)?.let { return SensorFieldUpdate.Phosphorus(it.groupValues[1].toInt()) }
    potassiumRegex.matchEntire(trimmed)?.let { return SensorFieldUpdate.Potassium(it.groupValues[1].toInt()) }

    return null
}

fun SensorData.applyUpdate(update: SensorFieldUpdate): SensorData {
    val now = System.currentTimeMillis()
    return when (update) {
        is SensorFieldUpdate.Humidity -> copy(humidityPercent = update.value, lastUpdateEpochMs = now)
        is SensorFieldUpdate.Temperature -> copy(temperatureC = update.value, lastUpdateEpochMs = now)
        is SensorFieldUpdate.Ec -> copy(ecUSPerCm = update.value, lastUpdateEpochMs = now)
        is SensorFieldUpdate.Nitrogen -> copy(nitrogenMgPerKg = update.value, lastUpdateEpochMs = now)
        is SensorFieldUpdate.Phosphorus -> copy(phosphorusMgPerKg = update.value, lastUpdateEpochMs = now)
        is SensorFieldUpdate.Potassium -> copy(potassiumMgPerKg = update.value, lastUpdateEpochMs = now)
    }
}

