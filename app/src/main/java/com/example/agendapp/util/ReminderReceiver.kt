package com.example.agendapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("eventName") ?: "Evento"
        val eventDate = intent.getStringExtra("eventDate") ?: ""
        val eventTime = intent.getStringExtra("eventTime") ?: "00:00"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val eventDateTime = try {
            sdf.parse("$eventDate $eventTime")
        } catch (e: Exception) {
            null
        }

        val now = Calendar.getInstance().time
        val message = when {
            eventDateTime == null -> "Recordatorio: $eventName"
            eventDateTime.before(now) -> "$eventName ha empezado!"
            (eventDateTime.time - now.time) <= 30 * 60 * 1000 -> "$eventName comienza en menos de 30 min!"
            else -> "Recuerda: $eventName"
        }

        // Canal de notificación
        val channelId = "event_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Eventos", NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // icono del sistema
            .setContentTitle("Recordatorio")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
