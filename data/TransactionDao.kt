package com.example.cointrack.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // 插入一笔账
    @Insert
    suspend fun insert(transaction: Transaction)

    // 删除一笔账
    @Delete
    suspend fun delete(transaction: Transaction)

    // 查询所有账单，按时间倒序排列（新的在前面）
    // 返回 Flow 是为了让界面能"实时"收到数据更新
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // 统计本月支出 (type=0 是支出)
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 0")
    fun getTotalExpense(): Flow<Double?>

    // 统计本月收入 (type=1 是收入)
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 1")
    fun getTotalIncome(): Flow<Double?>
}