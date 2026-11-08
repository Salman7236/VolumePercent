package io.github.salman7236.volumepercent

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.createBitmap

object VolumeNotifier {
    const val NOTIFICATION_ID = 1
    private const val CHANNEL_ID = "volume"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Volume percentage", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun build(context: Context, percent: Int): Notification {
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )
        val repost = PendingIntent.getService(
            context, 1,
            Intent(context, VolumeService::class.java).setAction(VolumeService.ACTION_REPOST),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(IconCompat.createWithBitmap(drawNumber(percent)))
            .setContentTitle("Volume $percent%")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openApp)
            .setDeleteIntent(repost)
            .build()
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, percent: Int) {
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, build(context, percent))
    }

    private fun drawNumber(value: Int): Bitmap {
        val size = 96
        val bitmap = createBitmap(size, size)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = size * 0.8f
        }
        val text = value.toString()
        val width = paint.measureText(text)
        if (width > size) paint.textSize *= size / width
        val y = size / 2f - (paint.descent() + paint.ascent()) / 2f
        Canvas(bitmap).drawText(text, size / 2f, y, paint)
        return bitmap
    }
}