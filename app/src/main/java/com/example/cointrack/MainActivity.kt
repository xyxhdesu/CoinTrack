package com.example.cointrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cointrack.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // 1. 定义 Binding 变量
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. 初始化 Binding (这是 ViewBinding 的标准写法)
        // 它会自动根据 activity_main.xml 生成 ActivityMainBinding 类
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. 设置内容视图
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        // App 关闭时停止音乐
        MusicHelper.stop()
    }

}