package tma.inf.upol.loveckydenik.classes

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import tma.inf.upol.loveckydenik.R


class Notification: BroadcastReceiver( ) {

    companion object {
        const val NOTIFICATION_ID_EXTRA = "notification_id"
        const val CHANEL_ID = "channel1"
        const val NOTIFICATION_CHANNEL_NAME = "notification channel"
        const val CHANNEL_DESCRIPTION = "Channel for pushing notifications of events from calendar"
        const val TITLE_EXTRA = "title"
        const val CONTENT_EXTRA = "content"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Vytvoření intentu, který zajistí otevření kalendáře po kliknutí na notifikaci
        val notificationID = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 1)
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.my_nav)
            .setDestination(R.id.calendarFragment)
            .createTaskStackBuilder()
            .getPendingIntent(
                notificationID,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        // Vytvoření notifikace
        val notification = NotificationCompat.Builder(context, CHANEL_ID)
            .setSmallIcon(R.drawable.ic_white_deer_logo)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.adapter_selector_color))
            .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
            .setContentText(intent.getStringExtra(CONTENT_EXTRA))
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }
}