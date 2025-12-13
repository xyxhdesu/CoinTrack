package com.example.cointrack.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cointrack.data.Transaction
import com.example.cointrack.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    // 点击事件回调：当用户点击某一行时触发
    private val onItemClicked: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    // 1. 创建视图 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    // 2. 绑定数据
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    // 3. 定义 ViewHolder 类
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            // 设置备注，如果没写备注就显示分类名
            binding.tvNote.text = if (transaction.note.isEmpty()) transaction.category else transaction.note
            binding.tvCategory.text = transaction.category.firstOrNull()?.toString() ?: "?" // 取首字做图标

            // 格式化日期
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(transaction.date))

            // 设置金额颜色：支出(0)红色，收入(1)绿色
            if (transaction.type == 0) {
                binding.tvAmount.text = "- ${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(Color.RED)
            } else {
                binding.tvAmount.text = "+ ${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(Color.parseColor("#4CAF50")) // 绿色
            }

            // 设置点击事件
            binding.root.setOnClickListener {
                onItemClicked(transaction)
            }
        }
    }

    // 4. DiffUtil 比较器（用来高效更新列表，不需要全部刷新）
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem == newItem
            }
        }
    }
}