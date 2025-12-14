package com.example.cointrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cointrack.data.AppDatabase
import com.example.cointrack.data.Transaction
import com.example.cointrack.data.TransactionRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    // 将 Flow 转换为 LiveData，方便界面观察
    val allTransactions: LiveData<List<Transaction>>
    val totalExpense: LiveData<Double?>
    val totalIncome: LiveData<Double?>

    init {
        // 初始化数据库和仓库
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)

        allTransactions = repository.allTransactions.asLiveData()
        totalExpense = repository.totalExpense.asLiveData()
        totalIncome = repository.totalIncome.asLiveData()
    }

    // 插入数据（通过 viewModelScope 开启一个协程，不卡顿主线程）
    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    // 删除数据
    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }


    // 用于存放网络返回的汇率信息
    private val _exchangeRates = androidx.lifecycle.MutableLiveData<Map<String, Double>>()
    val exchangeRates: LiveData<Map<String, Double>> = _exchangeRates

    // 获取汇率的方法
    fun fetchRates() {
        viewModelScope.launch {
            try {
                // 在后台线程发起网络请求
                val response = com.example.cointrack.data.network.RetrofitClient.api.getRates("CNY")
                if (response.isSuccessful && response.body() != null) {
                    // 成功拿到数据，更新 LiveData
                    _exchangeRates.value = response.body()!!.rates
                }
            } catch (e: Exception) {
                // 网络出错（比如没网），这里可以打印日志或忽略
                e.printStackTrace()
            }
        }
    }
}