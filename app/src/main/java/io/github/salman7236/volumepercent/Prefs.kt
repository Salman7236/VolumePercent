package io.github.salman7236.volumepercent

import android.content.Context
import androidx.core.content.edit

object Prefs {
    private const val FILE = "settings"
    private const val KEY_ENABLED = "service_enabled"
    private const val KEY_BOOT = "start_on_boot"

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isServiceEnabled(context: Context) = prefs(context).getBoolean(KEY_ENABLED, false)
    fun setServiceEnabled(context: Context, value: Boolean) =
        prefs(context).edit { putBoolean(KEY_ENABLED, value) }

    fun startOnBoot(context: Context) = prefs(context).getBoolean(KEY_BOOT, true)
    fun setStartOnBoot(context: Context, value: Boolean) =
        prefs(context).edit { putBoolean(KEY_BOOT, value) }
}