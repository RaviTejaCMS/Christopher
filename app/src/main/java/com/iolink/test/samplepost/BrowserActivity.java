package com.iolink.test.samplepost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.iolink.test.samplepost.Services.MyKeyBoard;

import java.util.ArrayList;
import java.util.Locale;

public class BrowserActivity extends AppCompatActivity {

    WebView wv;
    TextToSpeech t1;
    RelativeLayout relativeLayout;
    PopupWindow keyboardwindow;

    SharedPreferences pref;
    MyKeyBoard k;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        wv=(WebView)findViewById(R.id.webview);
        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        t1.speak("Enter website", TextToSpeech.QUEUE_FLUSH, null);
                        keyboardwindow = k.MyKeyBoards(BrowserActivity.this, t1, relativeLayout, null, pref,9001);
                        keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                String myText = pref.getString("mytext", null);
                                OpenURL(myText);
                            }
                        });
                    }
                }, 100);
    }

    public void OpenURL(String url){
        WebSettings webSettings = wv.getSettings();
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);
        wv.setWebViewClient(new BrowserActivity.myWebClient());
        wv.loadUrl("http://" + url);
        Log.e("MYURL","http://" + url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        try {
            if (resultcode == RESULT_OK) {
                if(requestCode == 9001){
                    k.onActivityResult(requestCode, resultcode, intent);
                }
            }
        }catch (Exception ex){

        }
    }


    int a,b,c,d,e,f = 0;
    public void buttonClicked(View view) {
        if(a == 0 && b == 0 && c == 0 && d == 0 && e == 0 && f == 0){
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            k.CheckLetter(a,b,c,d,e,f);
                            a = b = c= d= e= f= 0;
                        }
                    }, 1000);
        }
        if (view.getId() == R.id.BtnOne) {
            a++;
        } else if (view.getId() == R.id.BtnTwo) {
            b++;
        } else if (view.getId() == R.id.BtnThree) {
            c = 1;
        } else if (view.getId() == R.id.BtnFour) {
            d = 1;
        } else if (view.getId() == R.id.BtnFive) {
            e++;
        } else if (view.getId() == R.id.BtnSix) {
            f++;
        }
    }




    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            view.loadUrl(url);
            return true;

        }
    }



}


