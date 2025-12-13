package com.example.cointrack.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.cointrack.databinding.FragmentRateBinding
import com.example.cointrack.viewmodel.MainViewModel

class RateFragment : Fragment() {

    private var _binding: FragmentRateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 观察汇率数据
        viewModel.exchangeRates.observe(viewLifecycleOwner) { rates ->
            if (rates != null) {
                // 计算 100 人民币等于多少外币
                val usd = (rates["USD"] ?: 0.0) * 100
                val eur = (rates["EUR"] ?: 0.0) * 100
                val jpy = (rates["JPY"] ?: 0.0) * 100

                binding.tvUsd.text = String.format("%.2f", usd)
                binding.tvEur.text = String.format("%.2f", eur)
                binding.tvJpy.text = String.format("%.0f", jpy) // 日元通常没有小数
            }
        }

        // 2. 按钮点击刷新
        binding.btnRefresh.setOnClickListener {
            Toast.makeText(context, "正在刷新...", Toast.LENGTH_SHORT).show()
            viewModel.fetchRates()
        }

        // 3. 页面刚进来时，自动加载一次
        viewModel.fetchRates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}