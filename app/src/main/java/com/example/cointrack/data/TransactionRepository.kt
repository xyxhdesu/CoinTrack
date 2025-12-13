package com.example.cointrack.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    // 获取所有账单（直接从 DAO 拿）
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // 获取统计数据
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()

    // 插入数据（挂起函数，因为要在后台线程执行）
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    // 删除数据
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}