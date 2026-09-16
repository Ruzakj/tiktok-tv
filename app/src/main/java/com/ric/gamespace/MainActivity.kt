package com.ric.gamespace

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(32,28,32,24); setBackgroundColor(bg) }
        root.addView(TextView(this).apply { text="RIC GAME SPACE"; textSize=25f; setTextColor(Color.WHITE); setTypeface(typeface,1) })
        root.addView(TextView(this).apply { text="iQOO Z9x • Embedded ADB gaming launcher"; textSize=13f; setTextColor(Color.LTGRAY); setPadding(0,5,0,20) })
        root.addView(statusCard())

        val row = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(0,18,0,8) }
        row.addView(button("ADB SETUP") { startActivity(adbEngine.wirelessDebuggingSettingsIntent()) }, LinearLayout.LayoutParams(0,56.dp,1f).apply { marginEnd=8.dp })
        row.addView(button("PAIR ADB") { showPairDialog() }, LinearLayout.LayoutParams(0,56.dp,1f).apply { marginStart=8.dp })
        root.addView(row)
        root.addView(button("REFRESH") { updateAdbStatus(); loadGames() }, LinearLayout.LayoutParams(-1,52.dp).apply { bottomMargin=12.dp })
        root.addView(TextView(this).apply { text="GAMES"; textSize=13f; setTextColor(Color.GRAY); setPadding(4,8,0,8) })
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) },LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root); updateAdbStatus(); loadGames()
    }

    override fun onResume() { super.onResume(); if(::adbEngine.isInitialized && ::adbStatus.isInitialized) updateAdbStatus() }

    private fun showPairDialog() {
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(48,8,48,0) }
        val host=EditText(this).apply { hint="Host (default 127.0.0.1)"; setText("127.0.0.1"); inputType=InputType.TYPE_CLASS_PHONE }
        val port=EditText(this).apply { hint="Pairing port"; inputType=InputType.TYPE_CLASS_NUMBER }
        val code=EditText(this).apply { hint="6-digit pairing code"; inputType=InputType.TYPE_CLASS_NUMBER; filters=arrayOf(android.text.InputFilter.LengthFilter(6)) }
        box.addView(host); box.addView(port); box.addView(code)
        val dialog=AlertDialog.Builder(this).setTitle("PAIR WIRELESS ADB").setMessage("Buka Wireless debugging > Pair device with pairing code. Biarkan dialog Settings tetap terbuka, lalu masukkan pairing port dan 6-digit code.").setView(box).setNegativeButton("BATAL",null).setPositiveButton("PAIR",null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val p=port.text.toString().toIntOrNull(); val c=code.text.toString(); val h=host.text.toString().ifBlank { "127.0.0.1" }
                if(p==null || p !in 1..65535 || c.length!=6) { Toast.makeText(this,"Port atau pairing code tidak valid",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=false
                adbStatus.text="ADB Engine: PAIRING…\n$h:$p"
                adbEngine.pair(h,p,c) { ok,msg -> runOnUiThread { dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=true; Toast.makeText(this,msg,Toast.LENGTH_LONG).show(); updateAdbStatus(); if(ok) dialog.dismiss() } }
            }
        }
        dialog.show()
    }

    private fun statusCard(): View=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(24,20,24,20);setBackgroundColor(card);addView(TextView(this@MainActivity).apply{text="AGGRESSIVE MODE";textSize=20f;setTextColor(red);setTypeface(typeface,1)});adbStatus=TextView(this@MainActivity).apply{textSize=14f;setTextColor(Color.WHITE);setPadding(0,8,0,0)};addView(adbStatus) }
    private fun updateAdbStatus(){ adbStatus.text=when(adbEngine.state()){ AdbEngine.State.UNSUPPORTED->"ADB Engine: UNSUPPORTED\nAndroid 11+ required.";AdbEngine.State.WIRELESS_DEBUGGING_OFF->"ADB Engine: SETUP REQUIRED\nEnable Wireless debugging.";AdbEngine.State.READY_FOR_PAIRING->"ADB Engine: READY TO PAIR\nTap PAIR ADB and enter pairing port + code.";AdbEngine.State.CONNECTED->"ADB Engine: PAIRED\nPairing saved. Connection probe is next." } }
    private fun loadGames(){list.removeAllViews();val pm=packageManager;val q=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);pm.queryIntentActivities(q,0).filter{it.activityInfo.packageName!=packageName}.sortedBy{it.loadLabel(pm).toString().lowercase()}.take(120).forEach{r->val pkg=r.activityInfo.packageName;val line=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(18,10,10,10);setBackgroundColor(card)};line.addView(TextView(this).apply{text=r.loadLabel(pm).toString();setTextColor(Color.WHITE);textSize=15f;gravity=Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,52.dp,1f));line.addView(button("BOOST & PLAY"){launch(pkg)},LinearLayout.LayoutParams(140.dp,48.dp));list.addView(line,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=8.dp})}}
    private fun launch(pkg:String){packageManager.getLaunchIntentForPackage(pkg)?.let{it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(it)}?:Toast.makeText(this,"Game cannot be launched",Toast.LENGTH_SHORT).show()}
    private fun button(label:String,action:()->Unit)=Button(this).apply{text=label;setTextColor(Color.WHITE);setBackgroundColor(red);setOnClickListener{action()}}
    private val Int.dp:Int get()=(this*resources.displayMetrics.density).toInt()
}
