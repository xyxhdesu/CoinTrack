package com.example.cointrack

import android.content.Context
import android.media.MediaPlayer

object MusicHelper {
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var isPaused = false

    // 🎵 在这里填入你的音乐文件 ID
    private val musicList = listOf(
        R.raw.music1,
        R.raw.music2,
        R.raw.music3
    )

    fun play(context: Context) {
        if (mediaPlayer == null) {
            // 初始化播放器
            mediaPlayer = MediaPlayer.create(context, musicList[currentIndex])
            mediaPlayer?.setOnCompletionListener { next(context) } // 播完自动下一首
            mediaPlayer?.start()
            isPaused = false
        } else if (isPaused) {
            // 如果是暂停状态，继续播放
            mediaPlayer?.start()
            isPaused = false
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
    }

    fun next(context: Context) {
        // 释放当前的
        stop()

        // 计算下一首的索引
        currentIndex = (currentIndex + 1) % musicList.size

        // 播放新的
        play(context)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPaused = false
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}