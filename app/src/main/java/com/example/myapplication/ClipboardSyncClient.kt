package com.example.myapplication

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class ClipboardSyncClient(private val host: String, private val port: Int) {

    // відправляє один рядок тексту з length-prefix і повертає відповідь сервера (якщо є)
    fun sendMessage(message: String): String {
        Socket(host, port).use { socket ->
            val output = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())

            val messageBytes = message.toByteArray(Charsets.UTF_8)

            // length-prefix: спочатку 4 байти з довжиною, потім сам текст
            output.writeInt(messageBytes.size)
            output.write(messageBytes)
            output.flush()

            // читаємо відповідь у тому ж форматі: 4 байти довжини + текст
            val responseLength = input.readInt()
            val responseBytes = ByteArray(responseLength)
            input.readFully(responseBytes)

            return String(responseBytes, Charsets.UTF_8)
        }
    }
}