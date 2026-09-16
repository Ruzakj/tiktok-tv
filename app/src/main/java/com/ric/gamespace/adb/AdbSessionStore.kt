package com.ric.gamespace.adb

import android.content.Context

/** Stores only non-secret connection state. Pairing credentials/keys will use secure storage. */
class AdbSessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("ric_adb_state", Context.MODE_PRIVATE)

    var paired: Boolean
        get() = prefs.getBoolean("paired", false)
        set(value) = prefs.edit().putBoolean("paired", value).apply()

    fun clear() = prefs.edit().clear().apply()
}
