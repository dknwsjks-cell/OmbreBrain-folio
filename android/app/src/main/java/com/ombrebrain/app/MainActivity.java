package com.ombrebrain.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private WebViewAssetLoader assetLoader;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Server URL config dialog
        SharedPreferences prefs = getSharedPreferences("ombrebrain_prefs", MODE_PRIVATE);
        String savedUrl = prefs.getString("server_url", null);

        if (savedUrl == null) {
            showServerUrlDialog();
        }

        // Create WebView
        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setMixedContentMode(
                android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        );
        webView.getSettings().setUserAgentString(
                webView.getSettings().getUserAgentString() + " OmbreBrain-Android"
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title != null ? title : "OmbreBrain");
                }
            }
        });

        setContentView(webView);
        loadContent();
    }

    private void loadContent() {
        SharedPreferences prefs = getSharedPreferences("ombrebrain_prefs", MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", null);

        if (serverUrl != null && !serverUrl.isEmpty()) {
            webView.loadUrl(serverUrl);
        } else {
            assetLoader = new WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/",
                            new WebViewAssetLoader.AssetsPathHandler(this))
                    .build();

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(
                        WebView view, WebResourceRequest request) {
                    return assetLoader.shouldInterceptRequest(request.getUrl());
                }
            });

            boolean v2Exists = false;
            try {
                v2Exists = getAssets().list("v2").length > 0;
            } catch (Exception ignored) {}

            if (v2Exists) {
                webView.loadUrl("https://appassets.androidplatform.net/assets/v2/index.html");
            } else {
                webView.loadUrl("https://appassets.androidplatform.net/assets/dashboard.html");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    private void showServerUrlDialog() {
        EditText input = new EditText(this);
        input.setHint("https://your-server.onrender.com/v2/");
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_URI);

        new AlertDialog.Builder(this)
                .setTitle("配置服务器地址")
                .setMessage("请输入你的 OmbreBrain 服务器地址（留空使用本地离线界面）")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        SharedPreferences prefs = getSharedPreferences(
                                "ombrebrain_prefs", MODE_PRIVATE);
                        prefs.edit().putString("server_url", url).apply();
                        Toast.makeText(this, "服务器地址已保存",
                                Toast.LENGTH_SHORT).show();
                        loadContent();
                    } else {
                        Toast.makeText(this, "将使用本地离线界面",
                                Toast.LENGTH_SHORT).show();
                        loadContent();
                    }
                })
                .setNegativeButton("跳过", (dialog, which) -> loadContent())
                .setCancelable(false)
                .show();
    }
}
