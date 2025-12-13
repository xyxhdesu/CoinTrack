package com.example.cointrack.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    // 访问 /latest 路径，并带上 ?from=CNY 参数
    @GET("latest")
    suspend fun getRates(
        @Query("from") baseCurrency: String = "CNY"
    ): Response<ExchangeRateResponse>
}