package com.iolink.test.samplepost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.iolink.test.samplepost.Adapter.RecyclerViewAdapter;
import com.iolink.test.samplepost.Model.DataModel;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Page1Activity extends Activity{

    TextToSpeech t1;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    Button BtnMusic,BtnSettings,BtnCamera,BtnGallery;

    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page1);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(Page1Activity.this) {
            @Override
            public void onSwipeTop() {
                t1.speak("This is main screen", TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onSwipeBottom() {
                t1.speak("Open Next", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        BtnMusic = (Button) findViewById(R.id.btnMusic);

        //Swipe(BtnMusic);

        BtnMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.speak("selvan", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        BtnMusic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });






        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.UK);
            }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });
    }

    public void Swipe(Button btn){
        btn.setOnTouchListener(new OnSwipeTouchListener(Page1Activity.this) {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                t1.speak("Music", TextToSpeech.QUEUE_FLUSH, null);
                return super.onTouch(v, event);
            }

            @Override
            public void onSwipeTop() {
                t1.speak("This is main screen", TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onSwipeBottom() {
                t1.stop();
                OpenNextPage();
            }
        });
    }

    private void handleShakeEvent(int count) {
        t1.speak("Music,Settings,Camera,Gallery", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void OpenNextPage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        ArrayList<String> speech;
        if (resultcode == RESULT_OK) {
            if (requestCode == 1) {
                speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String resultSpeech = speech.get(0);

                if (!resultSpeech.startsWith("http://") && !resultSpeech.startsWith("https://"))
                    resultSpeech = "http://" + resultSpeech;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultSpeech));
                startActivity(browserIntent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}



