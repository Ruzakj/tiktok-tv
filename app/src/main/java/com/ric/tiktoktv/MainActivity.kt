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
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

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
                val host = request.url.host.orEmpty().lowercase()
                return !(host == "tiktok.com" || host.endsWith(".tiktok.com") ||
                    host.endsWith(".tiktokcdn.com") || host.endsWith(".tiktokv.com"))
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                installTvMode(view)
            }
        }
        web.isFocusable = true
        web.isFocusableInTouchMode = true
        web.requestFocus()
        web.loadUrl("https://www.tiktok.com/explore")
    }

    private fun installTvMode(view: WebView) {
        val js = """
        (() => {
          if (window.__RIC_TV__) { window.__RIC_TV__.clean(); return; }
          const css=document.createElement('style');
          css.id='ric-tv-css';
          css.textContent='html,body{background:#000!important;overflow-x:hidden!important}video{max-height:100vh!important}*:focus{outline:3px solid #fff!important;outline-offset:3px!important}';
          (document.head||document.documentElement).appendChild(css);

          const norm=e=>(e?.innerText||e?.textContent||'').replace(/\\s+/g,' ').trim().toLowerCase();
          const clean=()=>{
            document.querySelectorAll('[role="dialog"],[aria-modal="true"]').forEach(d=>{
              const t=norm(d);
              if(/buka tiktok|buka aplikasi|dapatkan pengalaman|open tiktok|get the full experience|continue in app/.test(t)){
                const close=[...d.querySelectorAll('button,[role="button"],a,div')].find(x=>/nanti saja|not now|close|tutup|continue watching/i.test(norm(x)));
                if(close) close.click(); else d.remove();
              }
            });
            document.querySelectorAll('[data-e2e="top-login-button"],a[href*="/login"]').forEach(e=>e.style.setProperty('display','none','important'));
            document.documentElement.style.setProperty('overflow-y','auto','important');
            document.body.style.setProperty('overflow-y','auto','important');
          };
          const visible=()=>[...document.querySelectorAll('video')].filter(v=>{const r=v.getBoundingClientRect();return r.bottom>innerHeight*.25&&r.top<innerHeight*.75}).sort((a,b)=>Math.abs(a.getBoundingClientRect().top)-Math.abs(b.getBoundingClientRect().top))[0];
          const play=()=>{const v=visible();document.querySelectorAll('video').forEach(x=>{if(x!==v)x.pause()});if(v)v.play().catch(()=>{});};
          let lastMove=0;
          const next=dir=>{
            clean();
            const v=visible();
            const all=[...document.querySelectorAll('video')];
            if(v && all.length>1){
              const idx=all.indexOf(v), candidate=all[idx+dir];
              if(candidate){candidate.scrollIntoView({behavior:'smooth',block:'center'});setTimeout(play,550);return;}
            }
            const scroller=document.scrollingElement||document.documentElement;
            const before=scroller.scrollTop;
            const amount=Math.max(innerHeight*.88,320)*dir;
            window.scrollBy({top:amount,behavior:'smooth'});
            document.querySelectorAll('[data-e2e*="recommend"],[class*="DivVideoFeed"],[class*="video-feed"]').forEach(e=>{if(dir>0&&e.scrollHeight>e.clientHeight)e.scrollTop+=amount});
            setTimeout(()=>{
              clean();play();
              const root=document.scrollingElement||document.documentElement;
              if(dir>0 && Math.abs(root.scrollTop-before)<4 && Date.now()-lastMove>900){
                lastMove=Date.now();
                window.dispatchEvent(new Event('scroll'));
                root.scrollTop=Math.min(root.scrollTop+innerHeight*.8,Math.max(0,root.scrollHeight-innerHeight));
              }
            },650);
          };
          const observer=new MutationObserver(()=>clean());
          observer.observe(document.documentElement,{childList:true,subtree:true});
          window.__RIC_TV__={clean,next,play};
          clean(); setInterval(clean,1000);
          window.addEventListener('scroll',()=>{clearTimeout(window.__ricPlayTimer);window.__ricPlayTimer=setTimeout(play,250)},{passive:true});
        })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }

    private fun js(script: String) = web.evaluateJavascript(script, null)

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> { js("window.__RIC_TV__?.next(1)"); return true }
                KeyEvent.KEYCODE_DPAD_UP -> { js("window.__RIC_TV__?.next(-1)"); return true }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    js("(()=>{const v=[...document.querySelectorAll('video')].find(x=>{const r=x.getBoundingClientRect();return r.bottom>innerHeight*.2&&r.top<innerHeight*.8});if(v){v.paused?v.play():v.pause()}})()")
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY -> { js("window.__RIC_TV__?.play()"); return true }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> { js("document.querySelectorAll('video').forEach(v=>v.pause())"); return true }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
