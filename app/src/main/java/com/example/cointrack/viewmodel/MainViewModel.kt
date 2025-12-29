package com.example.cointrack.viewmodel

import android.app.Application
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cointrack.data.AppDatabase
import com.example.cointrack.data.Transaction
import com.example.cointrack.data.TransactionRepository
import com.example.cointrack.data.network.BaiduClient
import com.example.cointrack.data.network.OcrResponse
import com.example.cointrack.data.network.RetrofitClient
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // === 原有代码保持不变 ===
    private val repository: TransactionRepository
    val allTransactions: LiveData<List<Transaction>>
    val totalExpense: LiveData<Double?>
    val totalIncome: LiveData<Double?>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions.asLiveData()
        totalExpense = repository.totalExpense.asLiveData()
        totalIncome = repository.totalIncome.asLiveData()
    }

    fun insert(transaction: Transaction) = viewModelScope.launch { repository.insert(transaction) }
    fun delete(transaction: Transaction) = viewModelScope.launch { repository.delete(transaction) }

    private val _exchangeRates = MutableLiveData<Map<String, Double>>()
    val exchangeRates: LiveData<Map<String, Double>> = _exchangeRates

    fun fetchRates() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getRates("CNY")
                if (response.isSuccessful && response.body() != null) {
                    _exchangeRates.value = response.body()!!.rates
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // === 👇 新增的 OCR 识别逻辑 (重点在这里) ===

    // 你的密钥 (已填入)
    private val API_KEY = "02vOeAsWxP4TpmCgWsod4fEF"
    private val SECRET_KEY = "mYX3vg2X0jrG9hBfvYVJ1hwR9xsYhNmN"

    // 识别结果：Pair(金额, 备注)
    val ocrResult = MutableLiveData<Pair<Double, String>?>()
    val ocrLoading = MutableLiveData<Boolean>()

    fun scanReceipt(imageData: ByteArray) {
        ocrLoading.value = true
        viewModelScope.launch {
            try {
                // 1. 获取 Token
                val tokenResp = BaiduClient.api.getAccessToken(
                    apiKey = API_KEY,
                    secretKey = SECRET_KEY
                )

                if (tokenResp.isSuccessful && tokenResp.body() != null) {
                    val token = tokenResp.body()!!.access_token

                    // 2. 图片转 Base64
                    val base64Img = Base64.encodeToString(imageData, Base64.NO_WRAP)

                    // 3. 识别小票
                    val ocrResp = BaiduClient.api.recognizeReceipt(token, base64Img)

                    if (ocrResp.isSuccessful && ocrResp.body() != null) {
                        parseOcrData(ocrResp.body()!!)
                    } else {
                        Toast.makeText(getApplication(), "识别失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                ocrLoading.value = false
            }
        }
    }

    // 解析百度返回的数据 (升级版)
    private fun parseOcrData(response: OcrResponse) {
        val list = response.words_result ?: return

        // 打印原始数据到日志，方便调试 (在 Logcat 搜 OCR_DEBUG)
        val rawText = list.joinToString { it.words }
        android.util.Log.d("OCR_DEBUG", "识别到的全部文字: $rawText")

        var foundAmount = 0.0
        var foundShopName = ""

        // 1. 寻找店名 (简单策略：包含特定关键字，或者是第一行)
        for (item in list) {
            val text = item.words
            if (foundShopName.isEmpty()) {
                if (text.contains("店") || text.contains("餐饮") || text.contains("超市") || text.contains("公司")) {
                    foundShopName = text
                }
            }
        }
        // 如果还没找到店名，就默认用第一行，但要排除掉纯数字或日期
        if (foundShopName.isEmpty() && list.isNotEmpty()) {
            val firstLine = list[0].words
            if (!firstLine.matches(Regex("^[0-9.\\-: ]+$"))) {
                foundShopName = firstLine
            }
        }

        // 2. 寻找金额 (智能策略)
        // 优先找含有 "合计"、"总金额"、"实收" 所在行的数字
        for (item in list) {
            val text = item.words
            if (text.contains("合计") || text.contains("总") || text.contains("实付") || text.contains("RMB") || text.contains("¥")) {
                // 提取这一行里的数字
                val num = extractPrice(text)
                if (num > 0 && num < 100000) { // 排除过大的异常值
                    foundAmount = num
                    break // 找到了合计，通常这就是最终结果，停止查找
                }
            }
        }

        // 如果没找到带关键字的金额，再用笨办法：找全文里看起来像价格的最大数字
        if (foundAmount == 0.0) {
            var maxNum = 0.0
            for (item in list) {
                val num = extractPrice(item.words)
                // 排除像手机号(11位)、日期(2023...)这样的数字
                // 价格通常有小数点，或者小于 10000
                if (num > 0) {
                    // 如果是整数且大于 19000000 (像日期 20240101) 跳过
                    if (num > 19000000) continue
                    // 如果是整数且大于 13000000000 (像手机号) 跳过
                    if (num > 10000000000) continue

                    if (num > maxNum) {
                        maxNum = num
                    }
                }
            }
            foundAmount = maxNum
        }

        ocrResult.value = Pair(foundAmount, foundShopName)
    }

    // 辅助工具：从字符串里提取价格数字
    private fun extractPrice(text: String): Double {
        // 把 "20.00元" 变成 "20.00"
        // 这里的正则意思是：匹配数字和小数点
        val regex = Regex("\\d+\\.\\d+|\\d+")
        val match = regex.find(text.replace(",", "")) // 去掉千分位逗号
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

}