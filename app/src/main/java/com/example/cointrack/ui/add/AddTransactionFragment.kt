package com.example.cointrack.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cointrack.R
import com.example.cointrack.data.Transaction
import com.example.cointrack.databinding.FragmentAddTransactionBinding
import com.example.cointrack.viewmodel.MainViewModel
import java.util.Date

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    // 复用同一个 MainViewModel
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 点击保存按钮
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString()
        val category = binding.etCategory.text.toString()
        val note = binding.etNote.text.toString()

        // 1. 简单校验
        if (amountStr.isEmpty()) {
            binding.layoutAmount.error = "请输入金额"
            return
        }
        if (category.isEmpty()) {
            binding.layoutCategory.error = "请输入分类"
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0

        // 2. 判断是收入还是支出
        // 获取选中的按钮ID
        val checkedId = binding.toggleGroup.checkedButtonId
        val type = if (checkedId == R.id.btnIncome) 1 else 0 // 1=收入, 0=支出

        // 3. 创建数据对象
        val transaction = Transaction(
            amount = amount,
            type = type,
            category = category,
            note = note,
            date = System.currentTimeMillis()
        )

        // 4. 存入数据库
        viewModel.insert(transaction)

        // 5. 提示并返回首页
        Toast.makeText(context, "保存成功！", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp() // 返回上一页
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}