package com.ric.gamespace

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ric.gamespace.adb.AdbEngine

class MainActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private lateinit var adbStatus: TextView
    private lateinit var adbEngine: AdbEngine
    private val bg = Color.rgb(9, 11, 16)
    private val card = Color.rgb(20, 23, 31)
    private val red = Color.rgb(255, 51, 79)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adbEngine = AdbEngine(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 24)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "RIC GAME SPACE"
            textSize = 25f
            setTextColor(Color.WHITE)
            setTypeface(typeface, 1)
        })
        root.addView(TextView(this).apply {
            text = "iQOO Z9x • Embedded ADB gaming launcher"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, 5, 0, 20)
        })
        root.addView(statusCard())

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 18, 0, 12)
        }
        row.addView(button("ADB SETUP") {
            startActivity(adbEngine.wirelessDebuggingSettingsIntent())
        }, LinearLayout.LayoutParams(0, 56.dp, 1f).apply { marginEnd = 8.dp })
        row.addView(button("REFRESH") {
            updateAdbStatus(); loadGames()
        }, LinearLayout.LayoutParams(0, 56.dp, 1f).apply { marginStart = 8.dp })
        root.addView(row)

        root.addView(TextView(this).apply {
            text = "GAMES"
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(4, 8, 0, 8)
        })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        updateAdbStatus()
        loadGames()
    }

    override fun onResume() {
        super.onResume()
        if (::adbEngine.isInitialized && ::adbStatus.isInitialized) updateAdbStatus()
    }

    private fun statusCard(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 20, 24, 20)
        setBackgroundColor(card)
        addView(TextView(this@MainActivity).apply {
            text = "AGGRESSIVE MODE"
            textSize = 20f
            setTextColor(red)
            setTypeface(typeface, 1)
        })
        adbStatus = TextView(this@MainActivity).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 0)
        }
        addView(adbStatus)
    }

    private fun updateAdbStatus() {
        adbStatus.text = when (adbEngine.state()) {
            AdbEngine.State.UNSUPPORTED -> "ADB Engine: UNSUPPORTED\nAndroid 11+ required for Wireless Debugging."
            AdbEngine.State.WIRELESS_DEBUGGING_OFF -> "ADB Engine: SETUP REQUIRED\nEnable Wireless debugging."
            AdbEngine.State.READY_FOR_PAIRING -> "ADB Engine: READY TO PAIR\n${adbEngine.pairingInstructions()}"
            AdbEngine.State.CONNECTED -> "ADB Engine: CONNECTED\nShell transport ready."
        }
    }

    private fun loadGames() {
        list.removeAllViews()
        val pm = packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(query, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        apps.take(120).forEach { info ->
            val pkg = info.activityInfo.packageName
            val name = info.loadLabel(pm).toString()
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(18, 10, 10, 10)
                setBackgroundColor(card)
            }
            line.addView(TextView(this).apply {
                text = name
                setTextColor(Color.WHITE)
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
            }, LinearLayout.LayoutParams(0, 52.dp, 1f))
            line.addView(button("BOOST & PLAY") { launch(pkg) }, LinearLayout.LayoutParams(140.dp, 48.dp))
            list.addView(line, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8.dp })
        }
    }

    private fun launch(pkg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else Toast.makeText(this, "Game cannot be launched", Toast.LENGTH_SHORT).show()
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setTextColor(Color.WHITE)
        setBackgroundColor(red)
        setOnClickListener { action() }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
