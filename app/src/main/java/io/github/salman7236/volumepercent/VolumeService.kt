package io.github.salman7236.volumepercent

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat

class VolumeService : Service() {
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var updatePending = false
    private var lastPostMs = 0L
    private var lastPercent = -1

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getIntExtra(EXTRA_STREAM_TYPE, -1) == AudioManager.STREAM_MUSIC) {
                scheduleUpdate()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AudioManager::class.java)
        VolumeNotifier.ensureChannel(this)
        lastPercent = currentPercent()
        promoteToForeground(lastPercent)
        ContextCompat.registerReceiver(
            this, receiver, IntentFilter(ACTION), ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    // Not sticky for now; restart behavior gets revisited with boot start.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_REPOST) {
            // The user swiped the notification away; put it back.
            lastPercent = currentPercent()
            promoteToForeground(lastPercent)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(receiver)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun promoteToForeground(percent: Int) {
        val notification = VolumeNotifier.build(this, percent)
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                VolumeNotifier.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(VolumeNotifier.NOTIFICATION_ID, notification)
        }
    }

    // One post at a time, at most every MIN_INTERVAL_MS. Extra broadcasts in the
    // meantime are dropped, and the latest value is read when the post runs.
    private fun scheduleUpdate() {
        if (updatePending) return
        updatePending = true
        val wait = (lastPostMs + MIN_INTERVAL_MS - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        handler.postDelayed({
            updatePending = false
            val percent = currentPercent()
            if (percent != lastPercent) {
                lastPercent = percent
                lastPostMs = SystemClock.elapsedRealtime()
                VolumeNotifier.show(this, percent)
            }
        }, wait)
    }

    private fun currentPercent(): Int = VolumeMath.percent(
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    )

    companion object {
        const val ACTION_REPOST = "io.github.salman7236.volumepercent.ACTION_REPOST"
        private const val MIN_INTERVAL_MS = 250L
        private const val ACTION = "android.media.VOLUME_CHANGED_ACTION"
        private const val EXTRA_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, VolumeService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, VolumeService::class.java))
        }
    }
}