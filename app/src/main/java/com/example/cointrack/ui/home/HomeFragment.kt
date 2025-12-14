package com.example.cointrack.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController // <--- 关键引用
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cointrack.R
import com.example.cointrack.adapter.TransactionAdapter
import com.example.cointrack.databinding.FragmentHomeBinding
import com.example.cointrack.viewmodel.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // 1. 初始化 RecyclerView
        val adapter = TransactionAdapter { transaction ->
            Toast.makeText(context, "点击了: ${transaction.note}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        // 2. 观察数据
        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        var currentIncome = 0.0
        var currentExpense = 0.0

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            currentIncome = income ?: 0.0
            binding.tvTotalIncome.text = String.format("%.2f", currentIncome)
            updateBalance(currentIncome, currentExpense)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            currentExpense = expense ?: 0.0
            binding.tvTotalExpense.text = String.format("%.2f", currentExpense)
            updateBalance(currentIncome, currentExpense)
        }

        // ==========================================
        // 重点在这里：点击悬浮按钮 (+) 的逻辑
        // ==========================================
        binding.fabAdd.setOnClickListener {
            // 执行跳转动作
            // 注意：R.id.action_homeFragment_to_addTransactionFragment 必须和你 nav_graph.xml 里的 id 一致
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }
        // 汇率按钮点击事件
        binding.btnRate.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rateFragment)
        }

        // 统计按钮点击事件
        binding.btnAnalysis.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_analysisFragment)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    // 修改这个方法
    private fun updateBalance(income: Double, expense: Double) {
        val balance = income - expense
        binding.tvTotalBalance.text = String.format("%.2f", balance)

        // === ✅ 新增：超支检查逻辑 ===
        val prefs = requireContext().getSharedPreferences("CoinTrackPrefs", android.content.Context.MODE_PRIVATE)
        val budget = prefs.getFloat("budget_limit", 0f) // 获取设定的预算

        if (budget > 0) {
            if (expense > budget) {
                // 如果超支：支出显示红色，并弹窗提示
                binding.tvTotalExpense.setTextColor(android.graphics.Color.RED)
                // 也可以把"本月结余"这几个字改成警告
                binding.tvTotalBalance.setTextColor(android.graphics.Color.RED)
                // 甚至弹个 Toast
                // Toast.makeText(context, "警告：本月已超支！", Toast.LENGTH_LONG).show()
            } else {
                // 没超支：恢复正常颜色
                binding.tvTotalExpense.setTextColor(android.graphics.Color.parseColor("#F44336")) // 原来的红色
                binding.tvTotalBalance.setTextColor(android.graphics.Color.BLACK)
            }
        }
        // ============================
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}