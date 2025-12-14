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
    }

    private fun updateBalance(income: Double, expense: Double) {
        val balance = income - expense
        binding.tvTotalBalance.text = String.format("%.2f", balance)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}