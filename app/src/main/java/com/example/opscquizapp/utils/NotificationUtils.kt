// File: app/src/main/java/com/example/opscquizapp/utils/NotificationUtils.kt

package com.example.opscquizapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.opscquizapp.R
import com.example.opscquizapp.ContinueGameActivity
import android.app.PendingIntent
import android.content.Intent

object NotificationUtils {

    const val CHANNEL_ID = "continue_game_channel"
    const val CHANNEL_NAME = "Continue Game Notifications"
    const val CHANNEL_DESCRIPTION = "Notifies users about saved games to continue"
    const val NOTIFICATION_ID = 1001

    /**
     * Creates a notification channel for Android 8.0+ devices.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and displays the "Continue Game" notification.
     */
    fun showContinueGameNotification(context: Context) {
        // Intent to open ContinueGameActivity when notification is tapped
        val intent = Intent(context, ContinueGameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // PendingIntent to wrap the intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_continue_game))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification on tap

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }


    fun cancelContinueGameNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(NOTIFICATION_ID)
        }
    }
}

