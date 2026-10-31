package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.widget.CountdownWidgetProvider

class CountdownAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_REMINDER) {
            val eventId = intent.getLongExtra("event_id", 0L)
            val title = intent.getStringExtra("event_title") ?: "Upcoming Event"
            val targetTime = intent.getLongExtra("target_time", System.currentTimeMillis())

            val remainingHours = ((targetTime - System.currentTimeMillis()) / (3600 * 1000L)).coerceAtLeast(0)
            val msg = if (remainingHours <= 1) {
                "The countdown for \"$title\" has reached the final hour! Prepare now."
            } else if (remainingHours < 48) {
                "Only $remainingHours hours remaining until \"$title\"!"
            } else {
                val days = remainingHours / 24
                "Only $days days remaining until \"$title\"!"
            }

            val soundEffect = intent.getStringExtra("sound_effect") ?: "doomsday_alarm"
            com.example.util.SoundEffectHelper.playSoundEffect(soundEffect)

            NotificationHelper.showEventNotification(
                context = context,
                eventId = eventId,
                title = title,
                message = msg
            )

            // Also refresh widgets
            CountdownWidgetProvider.updateAllWidgets(context)
        }
    }
}
