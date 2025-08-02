package com.example.onetapalarm // MAKE SURE this matches your other files

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

// The class name MUST be exactly "AlarmReceiver"
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the alarm fires.
        // We will show a notification.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel (required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm!")
            .setContentText("Your alarm is ringing.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show the notification. Use a unique ID for each notification to allow multiple.
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}