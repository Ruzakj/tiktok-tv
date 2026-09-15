package com.ric.tiktoktv

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        web = WebView(this)
        setContentView(web)
        web.setBackgroundColor(0xFF000000.toInt())
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            userAgentString = userAgentString.replace("; wv", "")
        }
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val host = request.url.host.orEmpty()
                return if (host.endsWith("tiktok.com") || host.endsWith("tiktokcdn.com") || host.endsWith("tiktokv.com")) false else true
            }
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                injectTvCss(view)
            }
        }
        web.isFocusable = true
        web.isFocusableInTouchMode = true
        web.requestFocus()
        web.loadUrl("https://www.tiktok.com/explore")
    }

    private fun injectTvCss(view: WebView) {
        val js = """
            (function(){
              if(document.getElementById('ric-tv-css')) return;
              const s=document.createElement('style'); s.id='ric-tv-css';
              s.textContent=`html,body{background:#000!important;overflow-x:hidden!important} video{max-height:100vh!important} [data-e2e='top-login-button'],a[href*='/login']{display:none!important} *:focus{outline:3px solid #fff!important;outline-offset:3px!important}`;
              document.head.appendChild(s);
            })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> { web.evaluateJavascript("window.scrollBy({top:window.innerHeight*0.88,behavior:'smooth'})", null); return true }
                KeyEvent.KEYCODE_DPAD_UP -> { web.evaluateJavascript("window.scrollBy({top:-window.innerHeight*0.88,behavior:'smooth'})", null); return true }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { web.evaluateJavascript("(()=>{let v=[...document.querySelectorAll('video')].find(x=>{let r=x.getBoundingClientRect();return r.top<innerHeight&&r.bottom>0});if(v){v.paused?v.play():v.pause()}})()", null); return true }
                KeyEvent.KEYCODE_MEDIA_PLAY -> { web.evaluateJavascript("document.querySelector('video')?.play()", null); return true }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> { web.evaluateJavascript("document.querySelector('video')?.pause()", null); return true }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
