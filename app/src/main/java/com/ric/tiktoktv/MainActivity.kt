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
    private var cursorMode = false

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
          if (window.__RIC_TV__) { window.__RIC_TV__.clean(); window.__RIC_TV__.setMode('scroll'); return; }
          const css=document.createElement('style');
          css.textContent='html,body{background:#000!important;overflow-x:hidden!important}video{max-height:100vh!important}*:focus{outline:3px solid #fff!important;outline-offset:3px!important}';
          (document.head||document.documentElement).appendChild(css);
          const norm=e=>(e?.innerText||e?.textContent||'').replace(/\s+/g,' ').trim().toLowerCase();
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
          const cursor=document.createElement('div');
          cursor.style.cssText='position:fixed;z-index:2147483647;left:50%;top:50%;width:22px;height:22px;border:3px solid white;border-radius:50%;box-shadow:0 0 0 2px #111,0 1px 8px #000;background:#ff3350;pointer-events:none;display:none;transform:translate(-50%,-50%);';
          document.documentElement.appendChild(cursor);
          const hint=document.createElement('div');
          hint.style.cssText='position:fixed;z-index:2147483647;left:22px;top:18px;padding:9px 14px;border-radius:8px;background:#171717e8;color:white;font:600 16px sans-serif;display:none;pointer-events:none;';
          document.documentElement.appendChild(hint);
          let mode='scroll',cx=innerWidth/2,cy=innerHeight/2,hintTimer=0;
          const target=()=>document.elementFromPoint(cx,cy);
          const mouse=type=>{const el=target();if(el)el.dispatchEvent(new MouseEvent(type,{bubbles:true,cancelable:true,view:window,clientX:cx,clientY:cy,buttons:type==='mousedown'?1:0}));};
          const showHint=()=>{hint.style.display='block';clearTimeout(hintTimer);hintTimer=setTimeout(()=>hint.style.display='none',1700);};
          const setMode=m=>{mode=m==='cursor'?'cursor':'scroll';cursor.style.display=mode==='cursor'?'block':'none';hint.textContent=mode==='cursor'?'MODE KURSOR · ↑↓←→ gerak · OK klik':'MODE SCROLL · ↑↓ pindah video · 1 ganti mode';showHint();};
          const moveCursor=(dx,dy)=>{cx=Math.max(8,Math.min(innerWidth-8,cx+dx));cy=Math.max(8,Math.min(innerHeight-8,cy+dy));cursor.style.left=cx+'px';cursor.style.top=cy+'px';mouse('mousemove');showHint();};
          const clickCursor=()=>{const el=target();if(!el)return;mouse('mouseover');mouse('mousedown');mouse('mouseup');const clickable=el.closest('a,button,[role="button"],[tabindex]');(clickable||el).click();};
          let lastMove=0;
          const next=dir=>{
            clean();
            const v=visible(),all=[...document.querySelectorAll('video')];
            if(v&&all.length>1){const candidate=all[all.indexOf(v)+dir];if(candidate){candidate.scrollIntoView({behavior:'smooth',block:'center'});setTimeout(play,550);return;}}
            const scroller=document.scrollingElement||document.documentElement,before=scroller.scrollTop,amount=Math.max(innerHeight*.88,320)*dir;
            window.scrollBy({top:amount,behavior:'smooth'});
            document.querySelectorAll('[data-e2e*="recommend"],[class*="DivVideoFeed"],[class*="video-feed"]').forEach(e=>{if(dir>0&&e.scrollHeight>e.clientHeight)e.scrollTop+=amount});
            setTimeout(()=>{clean();play();const root=document.scrollingElement||document.documentElement;if(dir>0&&Math.abs(root.scrollTop-before)<4&&Date.now()-lastMove>900){lastMove=Date.now();window.dispatchEvent(new Event('scroll'));root.scrollTop=Math.min(root.scrollTop+innerHeight*.8,Math.max(0,root.scrollHeight-innerHeight));}},650);
          };
          const observer=new MutationObserver(()=>clean());observer.observe(document.documentElement,{childList:true,subtree:true});
          window.__RIC_TV__={clean,next,play,setMode,moveCursor,clickCursor,getMode:()=>mode};
          clean();setInterval(clean,1000);
          window.addEventListener('scroll',()=>{clearTimeout(window.__ricPlayTimer);window.__ricPlayTimer=setTimeout(play,250)},{passive:true});
          setMode('scroll');
        })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }

    private fun js(script: String) = web.evaluateJavascript(script, null)
    private fun toggleMode() {
        cursorMode = !cursorMode
        js("window.__RIC_TV__?.setMode('${if (cursorMode) "cursor" else "scroll"}')")
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> { toggleMode(); return true }
                KeyEvent.KEYCODE_DPAD_DOWN -> { if (cursorMode) js("window.__RIC_TV__?.moveCursor(0,55)") else js("window.__RIC_TV__?.next(1)"); return true }
                KeyEvent.KEYCODE_DPAD_UP -> { if (cursorMode) js("window.__RIC_TV__?.moveCursor(0,-55)") else js("window.__RIC_TV__?.next(-1)"); return true }
                KeyEvent.KEYCODE_DPAD_LEFT -> { if (cursorMode) { js("window.__RIC_TV__?.moveCursor(-55,0)"); return true } }
                KeyEvent.KEYCODE_DPAD_RIGHT -> { if (cursorMode) { js("window.__RIC_TV__?.moveCursor(55,0)"); return true } }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    if (cursorMode) js("window.__RIC_TV__?.clickCursor()")
                    else js("(()=>{const v=[...document.querySelectorAll('video')].find(x=>{const r=x.getBoundingClientRect();return r.bottom>innerHeight*.2&&r.top<innerHeight*.8});if(v){v.paused?v.play():v.pause()}})()")
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { js("(()=>{const v=[...document.querySelectorAll('video')].find(x=>{const r=x.getBoundingClientRect();return r.bottom>innerHeight*.2&&r.top<innerHeight*.8});if(v){v.paused?v.play():v.pause()}})()"); return true }
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
