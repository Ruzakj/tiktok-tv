package com.ric.gamespace

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private val bg = Color.rgb(9,11,16)
    private val card = Color.rgb(20,23,31)
    private val red = Color.rgb(255,51,79)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,28,32,24);setBackgroundColor(bg)}
        root.addView(TextView(this).apply{text="RIC GAME SPACE";textSize=25f;setTextColor(Color.WHITE);setTypeface(typeface,1)})
        root.addView(TextView(this).apply{text="iQOO Z9x • Non-root performance launcher";textSize=13f;setTextColor(Color.LTGRAY);setPadding(0,5,0,20)})
        root.addView(statusCard())
        val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,18,0,12)}
        row.addView(button("WIRELESS ADB") { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }, LinearLayout.LayoutParams(0,56.dp,1f).apply{marginEnd=8.dp})
        row.addView(button("REFRESH") { loadGames() }, LinearLayout.LayoutParams(0,56.dp,1f).apply{marginStart=8.dp})
        root.addView(row)
        root.addView(TextView(this).apply{text="GAMES";textSize=13f;setTextColor(Color.GRAY);setPadding(4,8,0,8)})
        list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        root.addView(ScrollView(this).apply{addView(list)},LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root); loadGames()
    }

    private fun statusCard(): View = LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL;setPadding(24,20,24,20);setBackgroundColor(card)
        addView(TextView(this@MainActivity).apply{text="AGGRESSIVE MODE";textSize=20f;setTextColor(red);setTypeface(typeface,1)})
        addView(TextView(this@MainActivity).apply{text="ADB Engine: setup required\nTap Wireless ADB, enable Wireless debugging, then return here.";textSize=14f;setTextColor(Color.WHITE);setPadding(0,8,0,0)})
    }

    private fun loadGames(){
        list.removeAllViews()
        val pm=packageManager
        val q=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps=pm.queryIntentActivities(q,0).filter{it.activityInfo.packageName!=packageName}.sortedBy{it.loadLabel(pm).toString().lowercase()}
        apps.take(120).forEach { r ->
            val pkg=r.activityInfo.packageName; val name=r.loadLabel(pm).toString()
            val line=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(18,10,10,10);setBackgroundColor(card)}
            line.addView(TextView(this).apply{text=name;setTextColor(Color.WHITE);textSize=15f},LinearLayout.LayoutParams(0,52.dp,1f))
            line.addView(button("BOOST & PLAY") { launch(pkg) },LinearLayout.LayoutParams(140.dp,48.dp))
            list.addView(line,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=8.dp})
        }
    }

    private fun launch(pkg:String){
        // v0.1 deliberately applies only safe app-level orchestration. Embedded ADB transport follows after device pairing validation.
        val i=packageManager.getLaunchIntentForPackage(pkg)
        if(i!=null){ i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i) } else Toast.makeText(this,"Game cannot be launched",Toast.LENGTH_SHORT).show()
    }
    private fun button(t:String, f:()->Unit)=Button(this).apply{text=t;setTextColor(Color.WHITE);setBackgroundColor(red);setOnClickListener{f()}}
    private val Int.dp:Int get()=(this*resources.displayMetrics.density).toInt()
}
