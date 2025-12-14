package com.example.cointrack.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.cointrack.databinding.FragmentAnalysisBinding
import com.example.cointrack.viewmodel.MainViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 观察数据库里的所有账单
        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            // 1. 筛选出支出 (type == 0)
            val expenseList = list.filter { it.type == 0 }

            if (expenseList.isEmpty()) {
                binding.pieChart.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                return@observe
            } else {
                binding.pieChart.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            }

            // 2. 按分类分组并求和
            // 结果类似：{"吃饭": 50.0, "交通": 20.0}
            val map = HashMap<String, Double>()
            for (item in expenseList) {
                val current = map.getOrDefault(item.category, 0.0)
                map[item.category] = current + item.amount
            }

            // 3. 准备图表数据
            val entries = ArrayList<PieEntry>()
            for ((category, amount) in map) {
                entries.add(PieEntry(amount.toFloat(), category))
            }

            // 4. 设置样式
            val dataSet = PieDataSet(entries, "支出分类")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // 使用多彩颜色
            dataSet.valueTextSize = 16f
            dataSet.valueTextColor = Color.WHITE

            val data = PieData(dataSet)

            // 5. 显示图表
            binding.pieChart.data = data
            binding.pieChart.description.isEnabled = false // 不显示描述
            binding.pieChart.centerText = "总支出"
            binding.pieChart.setCenterTextSize(20f)
            binding.pieChart.animateY(1000) // 动画效果
            binding.pieChart.invalidate() // 刷新显示
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}