package io.github.salman7236.volumepercent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val enabled = Prefs.isServiceEnabled(context)
        val shouldStart = when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> enabled && Prefs.startOnBoot(context)
            Intent.ACTION_MY_PACKAGE_REPLACED -> enabled
            else -> false
        }
        if (shouldStart) VolumeService.start(context)
    }
}