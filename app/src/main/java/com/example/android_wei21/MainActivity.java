package com.example.android_wei21;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;


//在頁面中顯示 http 的版面，並讓版面裡的觸發事件可以被 activity 抓到--->資料交換互動
public class MainActivity extends AppCompatActivity {
    private WebView webview;
    private EditText maxstr;
    private LocationManager lmgr;
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        } else{
            // Permission has already been granted
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init(){
        queue= Volley.newRequestQueue(this);
        lmgr=(LocationManager) getSystemService(Context.LOCATION_SERVICE);//取得系統服務
        if(!lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){//如果GPS沒開
            Intent intent=new Intent(Settings.ACTION_LOCALE_SETTINGS);//叫系統叫出設定給使用者要不要開Location
            //startActivity(intent);


        }

        webview=findViewById(R.id.webview);
        maxstr=findViewById(R.id.max);
        initwebview();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this,"please turn on GPS",Toast.LENGTH_SHORT);
        }
    }

    private void initwebview(){//你是view
        webview.setWebViewClient(new WebViewClient());//你是瀏覽器
        WebSettings Settings=webview.getSettings();//取得瀏覽器的一般設定
        Settings.setJavaScriptEnabled(true);//打開javascript功能
        Settings.setSupportZoom(true);
        Settings.setUseWideViewPort(true);
        Settings.setBuiltInZoomControls(true);
        Settings.setLoadWithOverviewMode(true);
        webview.loadUrl("file:///android_asset/brad.html");//本機的 HTML

        webview.addJavascriptInterface(new MyJavascriptOb(),"hahaha");//介紹這個物件給 webview 認識
        //認識後，就可以用javascript操作它
    };
    //因為發現 webview 裡無法上一頁，試著改寫按返回可以上一頁
    @Override
    public void onBackPressed() {
        if (webview.canGoBack()){
            webview.goBack();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        myLocationListener=new MyLocationListener();
        lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,myLocationListener);
        //取得提供位置的來源,第二個參數為最小更新時間,最後一個放監聽的物件
    }

    @Override
    protected void onPause() {//跳出時或結束時
        super.onPause();
        lmgr.removeUpdates(myLocationListener);
    }

    private MyLocationListener myLocationListener;//監聽的物件
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {//位置傳回後要執行的事情
            double lat=location.getLatitude();
            double lng=location.getLongitude();
            Log.v("wei","位置回傳"+lat+":"+lng);
            Message message=new Message();
            Bundle data= new Bundle();
            data.putString("yourname",lat+","+lng);
            message.setData(data);
            myhandler.sendMessage(message);
            //webview.loadUrl(String.format("javascript:moveTo(%f,%f)",lat,lng));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public void test1(View view) {
        String str=maxstr.getText().toString();
        webview.loadUrl(String.format("javascript:test1(%s)",str));
    }
    public void test2(View view) {//點擊後
        String url="https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyCLk8W31pUZyUEwd2z6Wzld99iipFvo85Y";
        String url2=String.format(url,maxstr.getText().toString());

        StringRequest request=new StringRequest(
                Request.Method.GET,
                url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        parseJSON(response);
                    }
                },
                null
        );
        queue.add(request);

    }

    private void parseJSON(String JSON){
        try {
            JSONObject root=new JSONObject(JSON);
            String status=root.getString("status");

            if (status.equals("OK")){//解JSON資料，目標是做出想要屬性的指向
                JSONArray results=root.getJSONArray("results");
                JSONObject result=results.getJSONObject(0);
                JSONObject geometry=result.getJSONObject("geometry");
                JSONObject location=geometry.getJSONObject("location");
                double lat=location.getDouble("lat");
                double lng=location.getDouble("lng");
                Log.v("wei","geocode==>"+lat+","+"lng");
                webview.loadUrl(String.format("javascript:moveTo(%f,%f)",lat,lng));
            }else{
                Log.v("wei","status"+status);
            }


        }catch (Exception e){
            Log.v("wei",e.toString());
        }
    }



    public void test3(View view) {
    }

    public class MyJavascriptOb {// 創造 webview 用來跟 activity 溝通的物件，之後
        // webview 有任何想傳給 acticity 的資訊就可以透過這個物件來達成
        @JavascriptInterface
        public void callvalue(String yourname){
            Log.v("wei","web called"+yourname);//確認一下有沒有被網頁的 JS 叫出來
            //但這個方法在 android 有些核心程式被廠商修改的情況下不見得都會成功
            maxstr.setText(yourname);
            /////////////////////////////////////////
            // 以下是一定都能運行的寫法，即透過 Handler
            Message message=new Message();
            Bundle data= new Bundle();
            data.putString("yourname",yourname);
            message.setData(data);
            myhandler.sendMessage(message);
        }
    }

    private Myhandler myhandler=new Myhandler();
    private class Myhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String yourname=msg.getData().getString("yourname");
           // maxstr.setText(yourname);
        }
    }
}
