package com.example.myapplication.model

data class SensorData(
    val humidityPercent: Float? = null,
    val temperatureC: Float? = null,
    val ecUSPerCm: Int? = null,
    val nitrogenMgPerKg: Int? = null,
    val phosphorusMgPerKg: Int? = null,
    val potassiumMgPerKg: Int? = null,
    val lastUpdateEpochMs: Long? = null,
)

