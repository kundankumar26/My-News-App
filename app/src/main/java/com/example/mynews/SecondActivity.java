package com.example.mynews;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.Objects;

public class SecondActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        getSupportActionBar().hide();

        //SET UP WEB VIEWS TO VIEW NEWS ARTICLES
        TextView webviewUrlTextview = findViewById(R.id.second_activity_url);
        WebView webView = findViewById(R.id.news_webview);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        //CREATE INTENT AND GET THE PASSED STRING
        Intent intent = getIntent();
        String newsItemUrl = Objects.requireNonNull(intent.getExtras()).getString("url");
        assert newsItemUrl != null;
        String url = parseUrlString(newsItemUrl);

        webviewUrlTextview.setText(url);
        webView.loadUrl(newsItemUrl);
    }

    private String parseUrlString(String url) {
        int start = 0, end = url.length(), count = 0;
        for(int i = 0; i < url.length(); i++){
            if(url.charAt(i) == '/'){
                count++;
                start = end;
                end = i;
                if(count == 3){
                    break;
                }
            }
        }
        //Log.i("Second activity se", url);
        //Log.i("Second activity se", url.substring(start, end));
        return url.substring(start + 1, end);
    }
}