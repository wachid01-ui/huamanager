package com.huamanager.modem.router.app.data.model

data class DeviceInfo(
    val deviceName: String = "Huawei Router",
    val hardwareVersion: String = "-",
    val softwareVersion: String = "-",
    val webUIVersion: String = "-",
    val imei: String = "-",
    val imsi: String = "-",
    val macAddress1: String = "-",
    val serialNumber: String = "-"
)
