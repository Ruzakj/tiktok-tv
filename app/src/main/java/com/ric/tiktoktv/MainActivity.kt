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
                return !(host.endsWith("tiktok.com") || host.endsWith("tiktokcdn.com") || host.endsWith("tiktokv.com"))
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
        (()=>{
          if(window.__RIC_TV__) { window.__RIC_TV__.clean(); return; }
          const css=document.createElement('style');
          css.id='ric-tv-css';
          css.textContent=`
            html,body{background:#000!important;overflow-x:hidden!important}
            video{max-height:100vh!important}
            [data-e2e='top-login-button'],a[href*='/login']{display:none!important}
            *:focus{outline:3px solid #fff!important;outline-offset:3px!important}
          `;
          document.head.appendChild(css);

          const textOf=e=>(e?.innerText||e?.textContent||'').trim().toLowerCase();
          const clean=()=>{
            document.querySelectorAll('[role="dialog"],div[class*="DivModal"],div[class*="ModalContainer"]').forEach(d=>{
              const t=textOf(d);
              if(t.includes('buka tiktok') || t.includes('buka aplikasi') || t.includes('dapatkan pengalaman') || t.includes('nanti saja') || t.includes('open tiktok') || t.includes('continue in app')) {
                const later=[...d.querySelectorAll('button,a,div')].find(x=>/nanti saja|not now|continue watching|lanjutkan menonton/i.test(textOf(x)));
                if(later) later.click(); else d.remove();
              }
            });
            document.querySelectorAll('body > div').forEach(e=>{
              const t=textOf(e);
              if((t.includes('dapatkan pengalaman lebih lengkap di aplikasi') || t.includes('get the full experience in the app')) && t.length<800) e.remove();
            });
            document.documentElement.style.overflowY='auto';
            document.body.style.overflowY='auto';
          };

          const visibleVideo=()=>[...document.querySelectorAll('video')].find(v=>{const r=v.getBoundingClientRect();return r.bottom>innerHeight*.2&&r.top<innerHeight*.8});
          const playVisible=()=>{document.querySelectorAll('video').forEach(v=>{if(v!==visibleVideo()) v.pause()}); const v=visibleVideo(); if(v) v.play().catch(()=>{});};
          const next=(dir)=>{
            clean();
            const videos=[...document.querySelectorAll('video')];
            const current=visibleVideo();
            let target=null;
            if(current && videos.length){const i=videos.indexOf(current);target=videos[i+dir]}
            if(target){target.scrollIntoView({behavior:'smooth',block:'center'});setTimeout(playVisible,450)}
            else {
              window.scrollBy({top:dir*innerHeight*.92,behavior:'smooth'});
              if(dir>0){
                window.dispatchEvent(new Event('scroll'));
                document.dispatchEvent(new Event('scroll'));
                const root=document.scrollingElement||document.documentElement;
                root.scrollTop=Math.max(root.scrollTop,root.scrollHeight-innerHeight*1.2);
                window.dispatchEvent(new Event('scroll'));
              }
              setTimeout(()=>{clean();playVisible()},700);
            }
          };
          const obs=new MutationObserver(()=>clean());
          obs.observe(document.documentElement,{childList:true,subtree:true});
          window.__RIC_TV__={clean,next,playVisible};
          clean(); setInterval(clean,1200);
        })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }

    private fun js(code: String) = web.evaluateJavascript(code, null)

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> { js("window.__RIC_TV__?.next(1)"); return true }
                KeyEvent.KEYCODE_DPAD_UP -> { js("window.__RIC_TV__?.next(-1)"); return true }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    js("(()=>{const v=[...document.querySelectorAll('video')].find(x=>{const r=x.getBoundingClientRect();return r.bottom>innerHeight*.2&&r.top<innerHeight*.8});if(v){v.paused?v.play():v.pause()}})()")
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY -> { js("window.__RIC_TV__?.playVisible()"); return true }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> { js("document.querySelectorAll('video').forEach(v=>v.pause())"); return true }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
