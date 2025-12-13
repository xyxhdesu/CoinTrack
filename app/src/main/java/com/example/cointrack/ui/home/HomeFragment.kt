package com.example.cointrack.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cointrack.adapter.TransactionAdapter
import com.example.cointrack.databinding.FragmentHomeBinding
import com.example.cointrack.viewmodel.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 获取 ViewModel (注意这里用 activityViewModels，表示和 Activity 共享数据)
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
            // 点击某一行时的回调（比如点击查看详情），暂时先弹个 Toast
            Toast.makeText(context, "点击了: ${transaction.note}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        // 2. 观察数据变化 (观察者模式)

        // 2.1 观察列表数据
        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            // 当数据库数据变化时，自动刷新列表
            adapter.submitList(list)
        }

        // 2.2 观察收入和支出，计算结余
        // 这里我们用两个变量存临时数据，实际开发中可以在 ViewModel 里算好
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

        // 3. 点击悬浮按钮
        binding.fabAdd.setOnClickListener {
            // TODO: 跳转到记账页面
            Toast.makeText(context, "准备去记账", Toast.LENGTH_SHORT).show()
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