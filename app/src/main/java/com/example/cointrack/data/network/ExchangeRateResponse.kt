package com.example.cointrack.data.network

import com.google.gson.annotations.SerializedName

// 这个类对应网络返回的 JSON 结构
data class ExchangeRateResponse(
    val amount: Double,
    val base: String,
    val date: String,
    // Map<String, Double> 表示货币代码和汇率的键值对，比如 "USD": 0.14
    val rates: Map<String, Double>
)