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

// 👇 注意看这里：现在括号里有两个参数了！
class TransactionAdapter(
    private val onItemClicked: (Transaction) -> Unit,       // 参数1：短按
    private val onItemLongClicked: (Transaction) -> Unit    // 参数2：长按 (你缺的就是这个)
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            // 1. 设置显示内容
            binding.tvNote.text = if (transaction.note.isEmpty()) transaction.category else transaction.note
            binding.tvCategory.text = transaction.category

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(transaction.date))

            // 2. 设置金额颜色
            if (transaction.type == 0) {
                binding.tvAmount.text = "- ${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(Color.RED)
            } else {
                binding.tvAmount.text = "+ ${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(Color.parseColor("#4CAF50"))
            }

            // 3. 绑定短按事件
            binding.root.setOnClickListener {
                onItemClicked(transaction)
            }

            // 4. ✅ 绑定长按事件
            binding.root.setOnLongClickListener {
                onItemLongClicked(transaction)
                true // 返回 true 表示"我处理完了"，系统就不会再触发短按了
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
        }
    }
}