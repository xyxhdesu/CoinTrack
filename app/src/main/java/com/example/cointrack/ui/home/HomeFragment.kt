package com.example.cointrack.ui.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cointrack.R
import com.example.cointrack.adapter.TransactionAdapter
import com.example.cointrack.data.Transaction
import com.example.cointrack.databinding.FragmentHomeBinding
import com.example.cointrack.viewmodel.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 获取 ViewModel (共享数据)
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ==========================================
        // 1. 初始化列表 (RecyclerView) & 适配器
        // ==========================================
        val adapter = TransactionAdapter(
            onItemClicked = { transaction ->
                // 短按：显示备注
                val msg = if (transaction.note.isNotEmpty()) transaction.note else "无备注"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onItemLongClicked = { transaction ->
                // ✅ 长按：弹出删除确认框
                showDeleteDialog(transaction)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        // ==========================================
        // 2. 观察数据 (LiveData)
        // ==========================================

        // 2.1 观察所有账单
        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // 临时变量，用于计算结余
        var currentIncome = 0.0
        var currentExpense = 0.0

        // 2.2 观察总收入
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            currentIncome = income ?: 0.0
            binding.tvTotalIncome.text = String.format("%.2f", currentIncome)
            updateBalance(currentIncome, currentExpense)
        }

        // 2.3 观察总支出
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            currentExpense = expense ?: 0.0
            binding.tvTotalExpense.text = String.format("%.2f", currentExpense)
            updateBalance(currentIncome, currentExpense)
        }

        // ==========================================
        // 3. 按钮点击事件 (导航跳转)
        // ==========================================

        // (1) 悬浮按钮 -> 记一笔
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }

        // (2) 汇率按钮
        binding.btnRate.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rateFragment)
        }

        // (3) 统计按钮
        binding.btnAnalysis.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_analysisFragment)
        }

        // (4) 设置按钮
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    // ==========================================
    // 辅助方法
    // ==========================================

    // 更新结余显示 & 检查超支
    private fun updateBalance(income: Double, expense: Double) {
        val balance = income - expense
        binding.tvTotalBalance.text = String.format("%.2f", balance)

        // 读取预算设置
        val prefs = requireContext().getSharedPreferences("CoinTrackPrefs", Context.MODE_PRIVATE)
        val budget = prefs.getFloat("budget_limit", 0f)

        if (budget > 0 && expense > budget) {
            // 🚨 警告：超支了！变红！
            binding.tvTotalExpense.setTextColor(Color.RED)
            binding.tvTotalBalance.setTextColor(Color.RED)
        } else {
            // 正常状态
            binding.tvTotalExpense.setTextColor(Color.parseColor("#F44336")) // 原来的红色
            binding.tvTotalBalance.setTextColor(Color.BLACK)
        }
    }

    // 显示删除确认弹窗
    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除记录")
            .setMessage("确定要删除这笔 \"${transaction.category}\" 记录吗？")
            .setPositiveButton("删除") { _, _ ->
                // 调用 ViewModel 删除
                viewModel.delete(transaction)
                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}