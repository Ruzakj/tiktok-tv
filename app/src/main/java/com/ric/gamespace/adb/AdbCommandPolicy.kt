package com.ric.gamespace.adb

/**
 * Only reversible/read-only commands are allowed through the gaming engine.
 * Kernel, thermal bypass and destructive package commands are intentionally excluded.
 */
object AdbCommandPolicy {
    private val readOnlyPrefixes = listOf(
        "getprop ", "settings get ", "dumpsys battery", "dumpsys thermalservice",
        "dumpsys display", "wm size", "wm density", "cat /proc/meminfo"
    )

    fun isReadOnlyAllowed(command: String): Boolean =
        readOnlyPrefixes.any { command.trim().startsWith(it) }
}
