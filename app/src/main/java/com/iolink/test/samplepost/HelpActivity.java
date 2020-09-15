package com.iolink.test.samplepost;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

public class HelpActivity extends AppCompatActivity {
    TextToSpeech t1;
    Context context;
    LinearLayout linearLayout;
    ImageView videopg1,videopg2;

    int type = 0;

    SharedPreferences videoCallPref,pref;
    PopupWindow keyboardwindow;
    MyKeyBoard k;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.UK);
            }
            }
        });

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        videoCallPref = getApplicationContext().getSharedPreferences("VIDEOCALL", 0);

        videopg1 = (ImageView) findViewById(R.id.videopg1);
        videopg1.setOnTouchListener(new OnSwipeTouchListener(HelpActivity.this){
            @Override
            public void onSwipeBottom() {
                type = 0;
                videopg1.setVisibility(View.GONE);
                videopg2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSwipeTop() {
                type = 0;
                videopg1.setVisibility(View.GONE);
                videopg2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSwipeRight() {
                type = 1;
                videopg1.setVisibility(View.GONE);
                videopg2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSwipeLeft() {
                onBackPressed();
            }
        });

        videopg2 = (ImageView) findViewById(R.id.videopg2);
        videopg2.setOnTouchListener(new OnSwipeTouchListener(HelpActivity.this){
            @Override
            public void onSwipeBottom() {
                if(type == 0){
                    t1.speak("Enter number", TextToSpeech.QUEUE_FLUSH, null);
                    keyboardwindow = k.MyKeyBoards(HelpActivity.this,t1,null,linearLayout,pref,9001);
                    keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String myText = pref.getString("mytext", null);
                            SaveNumber(4,myText);
                        }
                    });


                }else {
                    String number = videoCallPref.getString("CONTACT_4", null);
                    SendSms(number);
                }
            }

            @Override
            public void onSwipeTop() {
                if(type == 0){
                    t1.speak("Enter number", TextToSpeech.QUEUE_FLUSH, null);
                    keyboardwindow = k.MyKeyBoards(HelpActivity.this,t1,null,linearLayout,pref,9001);
                    keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String myText = pref.getString("mytext", null);
                            SaveNumber(2,myText);
                        }
                    });

                }else {
                    String number = videoCallPref.getString("CONTACT_2", null);
                    SendSms(number);
                }
            }

            @Override
            public void onSwipeRight() {
                if(type == 0){
                    t1.speak("Enter number", TextToSpeech.QUEUE_FLUSH, null);
                    keyboardwindow = k.MyKeyBoards(HelpActivity.this,t1,null,linearLayout,pref,9001);
                    keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String myText = pref.getString("mytext", null);
                            SaveNumber(3,myText);
                        }
                    });

                }else {
                    String number = videoCallPref.getString("CONTACT_3", null);
                    SendSms(number);
                }
            }

            @Override
            public void onSwipeLeft() {
                if(type == 0){
                    t1.speak("Enter number", TextToSpeech.QUEUE_FLUSH, null);
                    keyboardwindow = k.MyKeyBoards(HelpActivity.this,t1,null,linearLayout,pref,9001);
                    keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String myText = pref.getString("mytext", null);
                            SaveNumber(1,myText);
                        }
                    });

                }else {
                    String number = videoCallPref.getString("CONTACT_1", null);
                    SendSms(number);
                }
            }
        });
        context = this.getApplicationContext();
    }

    public void SaveNumber(int direction,String number){
        SharedPreferences.Editor editor = videoCallPref.edit();
        editor.putString("CONTACT_" + direction, number);
        editor.commit();
        t1.speak("Contact updated", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void SendSms(String PhoneNumber){
        try{
            Log.e("NUMBER",PhoneNumber);
            String sendNumber = PhoneNumber;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sendNumber,null,"Please come to video call".toString(),null,null);
            t1.speak("Request sent successfully", TextToSpeech.QUEUE_FLUSH, null);

            Intent intent = new Intent(HelpActivity.this, VideoChatViewActivity.class);
            startActivity(intent);
        }catch (Exception ex){
            Log.e("ERROR_APP",ex.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        if (resultcode == RESULT_OK) {
            if (requestCode == 9001) {
                k.onActivityResult(requestCode, resultcode, intent);
            }
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
}