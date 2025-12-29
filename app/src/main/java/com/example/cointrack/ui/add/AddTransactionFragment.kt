package com.example.cointrack.ui.add

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private val viewModel: MainViewModel by activityViewModels()

    // 1. [修改] 改为从相册选择内容 (GetContent)
    // 这样可以直接获取原图，不会被模拟器摄像头压缩变糊
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                // 读取原始高清文件流
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    // 提示用户
                    Toast.makeText(context, "已选择图片，正在识别...", Toast.LENGTH_SHORT).show()
                    // 发送给 ViewModel 进行识别
                    viewModel.scanReceipt(bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "读取图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener { saveTransaction() }

        // 2. [修改] 点击相机图标 -> 打开相册
        binding.layoutAmount.setEndIconOnClickListener {
            // 传入 "image/*" 表示只显示图片
            pickImageLauncher.launch("image/*")

            // 提示：模拟器上用相册选图最清晰
            // 真机上如果想换回相机，以后改回 takePictureLauncher 即可
        }

        // 3. 观察识别结果
        viewModel.ocrResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                // 填入金额
                val amountText = if (result.first == 0.0) "" else result.first.toString()
                binding.etAmount.setText(amountText)

                // 填入备注 (店名)
                if (result.second.isNotEmpty()) {
                    binding.etNote.setText(result.second)
                }

                // 选中支出
                binding.toggleGroup.check(R.id.btnExpense)

                if (result.first > 0) {
                    Toast.makeText(context, "识别成功！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "文字已识别，但未找到金额，请手动输入", Toast.LENGTH_LONG).show()
                }

                // 清空，避免重复触发
                viewModel.ocrResult.value = null
            }
        }

        // 4. 加载状态
        viewModel.ocrLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.layoutAmount.isEnabled = !isLoading
            binding.layoutAmount.error = if (isLoading) "正在连接百度云识别..." else null
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString()
        val category = binding.etCategory.text.toString()
        val note = binding.etNote.text.toString()

        if (amountStr.isEmpty()) {
            binding.layoutAmount.error = "请输入金额"
            return
        }
        if (category.isEmpty()) {
            binding.layoutCategory.error = "请输入分类"
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val checkedId = binding.toggleGroup.checkedButtonId
        val type = if (checkedId == R.id.btnIncome) 1 else 0

        val transaction = Transaction(
            amount = amount,
            type = type,
            category = category,
            note = note,
            date = System.currentTimeMillis()
        )

        viewModel.insert(transaction)
        Toast.makeText(context, "保存成功！", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}