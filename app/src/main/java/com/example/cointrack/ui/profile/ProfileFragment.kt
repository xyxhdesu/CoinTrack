package com.example.cointrack.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.cointrack.databinding.FragmentProfileBinding
import com.example.cointrack.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // === 1. 预算功能逻辑 ===
        // 读取已保存的预算
        val prefs = requireContext().getSharedPreferences("CoinTrackPrefs", Context.MODE_PRIVATE)
        val savedBudget = prefs.getFloat("budget_limit", 0f)
        if (savedBudget > 0) {
            binding.etBudget.setText(savedBudget.toString())
        }

        // 保存预算
        binding.btnSaveBudget.setOnClickListener {
            val input = binding.etBudget.text.toString().toFloatOrNull()
            if (input != null) {
                prefs.edit().putFloat("budget_limit", input).apply()
                Toast.makeText(context, "预算已设置！超支时将在首页提醒。", Toast.LENGTH_SHORT).show()
            }
        }

        // === 2. 导出 Excel (CSV) 逻辑 ===
        binding.btnExport.setOnClickListener {
            exportToCsv()
        }
    }

    private fun exportToCsv() {
        // 观察一次数据（不能用 observe，因为这里是点击触发，要直接获取当前值）
        val list = viewModel.allTransactions.value ?: return

        // 1. 构建 CSV 内容
        val sb = StringBuilder()
        sb.append("ID,日期,类型,分类,金额,备注\n") // 表头

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        for (item in list) {
            val typeStr = if (item.type == 1) "收入" else "支出"
            sb.append("${item.id},${sdf.format(Date(item.date))},$typeStr,${item.category},${item.amount},${item.note}\n")
        }

        try {
            // 2. 写入缓存文件
            val fileName = "CoinTrack_Backup_${System.currentTimeMillis()}.csv"
            val file = File(requireContext().cacheDir, fileName)
            file.writeText(sb.toString())

            // 3. 调用系统分享 (最简单的导出方式，不用申请文件权限！)
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "导出账单到..."))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}