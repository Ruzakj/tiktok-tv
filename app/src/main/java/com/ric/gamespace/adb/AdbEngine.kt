package com.ric.gamespace.adb

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * Embedded ADB facade for RIC Game Space.
 * Phase 1 keeps the app buildable while the transport is integrated.
 * No external LADB application is launched or required.
 */
class AdbEngine(private val context: Context) {
    enum class State { UNSUPPORTED, WIRELESS_DEBUGGING_OFF, READY_FOR_PAIRING, CONNECTED }

    fun state(): State {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return State.UNSUPPORTED
        val enabled = Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0) == 1
        if (!enabled) return State.WIRELESS_DEBUGGING_OFF
        return if (AdbSessionStore(context).paired) State.READY_FOR_PAIRING else State.READY_FOR_PAIRING
    }

    fun wirelessDebuggingSettingsIntent(): Intent =
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun pairingInstructions(): String =
        "Wireless debugging aktif. Pilih Pair device with pairing code, lalu masukkan port dan kode ke RIC Game Space."
}
