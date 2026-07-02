package com.example.myapplication

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private val channelId = "clipboard_sync_channel"

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Extended Clipboard")
            .setContentText("Синхронізація буфера активна")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .build()

        startForeground(1, notification)

        clipboardManager.addPrimaryClipChangedListener {
            val text = clipboardManager.primaryClip
                ?.getItemAt(0)?.coerceToText(this)?.toString()
            // тут будеш слати текст на ПК через мережу
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Clipboard Sync", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}