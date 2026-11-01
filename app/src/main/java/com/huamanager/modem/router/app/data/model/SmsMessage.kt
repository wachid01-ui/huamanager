package com.huamanager.modem.router.app.data.model

data class SmsMessage(
    val index: Int,
    val phone: String,
    val content: String,
    val date: String,
    val isRead: Boolean = true
)
