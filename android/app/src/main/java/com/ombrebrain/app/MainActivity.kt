package com.ombrebrain.app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var assetLoader: WebViewAssetLoader? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 服务器地址配置对话框 ---
        val prefs = getSharedPreferences("ombrebrain_prefs", MODE_PRIVATE)
        val savedUrl = prefs.getString("server_url", null)

        if (savedUrl == null) {
            showServerUrlDialog()
        }

        // --- 创建 WebView ---
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode =
                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // 设置 User-Agent 让前端识别为移动端
            settings.userAgentString =
                settings.userAgentString + " OmbreBrain-Android"

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    url: String?
                ): Boolean {
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    // 如果加载远程地址失败，显示重试按钮
                    if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT_FAILED) {
                        // 不显示错误页面，让前端自行处理
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    supportActionBar?.title = title ?: "OmbreBrain"
                }
            }
        }

        setContentView(webView)

        // --- 加载页面 ---
        loadContent()
    }

    private fun loadContent() {
        val prefs = getSharedPreferences("ombrebrain_prefs", MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", null)

        if (serverUrl != null && serverUrl.isNotEmpty()) {
            // 远程服务器模式
            webView.loadUrl(serverUrl)
        } else {
            // 本地 Assets 模式 - 加载 v2/index.html
            assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
                .build()

            webView.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): android.webkit.WebResourceResponse? {
                    return assetLoader?.shouldInterceptRequest(request?.url!!)
                }
            }

            // 尝试加载 v2/index.html，如果存在
            val v2Exists = try {
                assets.list("v2")?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }

            if (v2Exists) {
                webView.loadUrl("https://appassets.androidplatform.net/assets/v2/index.html")
            } else {
                webView.loadUrl("https://appassets.androidplatform.net/assets/dashboard.html")
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    private fun showServerUrlDialog() {
        val input = EditText(this).apply {
            hint = "https://your-server.onrender.com/v2/"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        AlertDialog.Builder(this)
            .setTitle("配置服务器地址")
            .setMessage("请输入你的 OmbreBrain 服务器地址（留空使用本地离线界面）")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    val prefs = getSharedPreferences("ombrebrain_prefs", MODE_PRIVATE)
                    prefs.edit().putString("server_url", url).apply()
                    Toast.makeText(this, "服务器地址已保存", Toast.LENGTH_SHORT).show()
                    loadContent()
                } else {
                    // 留空表示本地模式
                    Toast.makeText(this, "将使用本地离线界面", Toast.LENGTH_SHORT).show()
                    loadContent()
                }
            }
            .setNegativeButton("跳过") { _, _ ->
                loadContent()
            }
            .setCancelable(false)
            .show()
    }
}
