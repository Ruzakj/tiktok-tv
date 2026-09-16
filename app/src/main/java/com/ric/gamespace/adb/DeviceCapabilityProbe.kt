package com.ric.gamespace.adb

/** Safe probe list. Execution is enabled after the embedded ADB transport connects. */
object DeviceCapabilityProbe {
    val commands = listOf(
        "getprop ro.product.manufacturer",
        "getprop ro.product.model",
        "getprop ro.build.version.release",
        "getprop ro.build.version.sdk",
        "settings get system peak_refresh_rate",
        "settings get system min_refresh_rate",
        "dumpsys battery",
        "dumpsys thermalservice",
        "dumpsys display",
        "cat /proc/meminfo"
    )
}
