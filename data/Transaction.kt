package com.example.cointrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity 告诉 Room 这是一个数据库表，表名叫 transactions
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, //自增ID，不用管

    val amount: Double,    // 金额
    val type: Int,         // 类型：0=支出, 1=收入
    val category: String,  // 分类：比如 "吃饭", "交通"
    val note: String,      // 备注
    val date: Long         // 时间戳
)