package com.example.cointrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 隐藏顶部的标题栏 (ActionBar)，让界面全屏更好看
        supportActionBar?.hide()

        // 延迟 2000 毫秒 (2秒) 后跳转
        Handler(Looper.getMainLooper()).postDelayed({
            // 1. 创建意图：从 Splash 跳到 Main
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // 2. 结束当前页面 (这样用户按返回键不会回到启动页)
            finish()
        }, 2000)
    }
}