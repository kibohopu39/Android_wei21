package com.example.android_wei21;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.util.WeakHashMap;

//在頁面中顯示 http 的版面，並讓版面裡的觸發事件可以被 activity 抓到--->資料交換互動
public class MainActivity extends AppCompatActivity {
    private WebView webview;
    private EditText maxstr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview=findViewById(R.id.webview);
        maxstr=findViewById(R.id.max);
        initwebview();
    }

    private void initwebview(){//你是view
        webview.setWebViewClient(new WebViewClient());//你是瀏覽器
        WebSettings Settings=webview.getSettings();//取得設定
        Settings.setJavaScriptEnabled(true);//打開javascript
        Settings.setSupportZoom(true);
        Settings.setUseWideViewPort(true);
        Settings.setBuiltInZoomControls(true);
        Settings.setLoadWithOverviewMode(true);
        webview.loadUrl("file:///android_asset/brad.html");//本機的HTML
    };
    //因為發現webview裡無法上一頁，試著改寫按返回
    @Override
    public void onBackPressed() {
        if (webview.canGoBack()){
            webview.goBack();
        }else{
            super.onBackPressed();
        }

    }

    public void test1(View view) {
        String str=maxstr.getText().toString();
        webview.loadUrl(String.format("javascript:test1(%s)",str));
    }
    public void test2(View view) {
    }
    public void test3(View view) {
    }
}
