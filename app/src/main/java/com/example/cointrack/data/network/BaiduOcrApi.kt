package com.example.cointrack.data.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

// 定义百度 API 接口
interface BaiduOcrApi {

    // 1. 获取 Access Token
    @POST("oauth/2.0/token")
    suspend fun getAccessToken(
        @Query("grant_type") grantType: String = "client_credentials",
        @Query("client_id") apiKey: String,
        @Query("client_secret") secretKey: String
    ): Response<TokenResponse>

    // 2. 识别票据
    @FormUrlEncoded
    @POST("rest/2.0/ocr/v1/receipt")
    suspend fun recognizeReceipt(
        @Query("access_token") accessToken: String,
        @Field("image") imageBase64: String
    ): Response<OcrResponse>
}

// 响应数据结构
data class TokenResponse(val access_token: String)

data class OcrResponse(val words_result: List<OcrWord>?)
data class OcrWord(val words: String)

// 独立的网络客户端
object BaiduClient {
    private const val BASE_URL = "https://aip.baidubce.com/"

    val api: BaiduOcrApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BaiduOcrApi::class.java)
    }
}