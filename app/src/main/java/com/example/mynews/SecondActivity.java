package com.example.mynews;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SecondActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        //SET UP WEB VIEWS TO VIEW NEWS ARTICLES
        WebView webView = findViewById(R.id.news_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        //CREATE INTENT AND GET THE PASSED STRING
        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");
        if(url != null)
            webView.loadUrl(url);
    }
}