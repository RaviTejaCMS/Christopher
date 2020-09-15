package com.iolink.test.samplepost;

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
import android.view.ViewConfiguration;

import com.iolink.test.samplepost.Adapter.RecyclerViewAdapter;
import com.iolink.test.samplepost.Model.DataModel;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemListener {

    TextToSpeech t1;

    long mLastClickTime;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    RecyclerView recyclerView;
    ArrayList arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        arrayList = new ArrayList();
        arrayList.add(new DataModel("Call", R.drawable.call, "#808080"));
        arrayList.add(new DataModel("Message", R.drawable.message, "#808080"));
        arrayList.add(new DataModel("Contact", R.drawable.contact, "#808080"));
        arrayList.add(new DataModel("Time", R.drawable.time, "#808080"));

        arrayList.add(new DataModel("Music", R.drawable.music, "#808080"));
        arrayList.add(new DataModel("Settings", R.drawable.settings, "#808080"));
        arrayList.add(new DataModel("Camera", R.drawable.camera, "#808080"));
        arrayList.add(new DataModel("Gallery", R.drawable.gallery, "#808080"));

        arrayList.add(new DataModel("Internet", R.drawable.internet, "#808080"));
        arrayList.add(new DataModel("Settings", R.drawable.whatsapp, "#808080"));
        arrayList.add(new DataModel("Store", R.drawable.store, "#808080"));
        arrayList.add(new DataModel("Help", R.drawable.help, "#808080"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, arrayList, this);
        recyclerView.setAdapter(adapter);

        GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
    }

    private void handleShakeEvent(int count) {
        t1.speak("Call,Message,Time,Music,Settings,Contact,Camera,Internet,Whatsapp,Store,Help", TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onItemClick(DataModel item) {
        long currTime = System.currentTimeMillis();
        if (currTime - mLastClickTime < ViewConfiguration.getDoubleTapTimeout()) {
            //onItemLongClick(item);
        }else {
            String toSpeak = item.text.toString();
            if(toSpeak.equals("Time")){
                DateFormat df = new SimpleDateFormat("h:mm a");
                toSpeak = df.format(Calendar.getInstance().getTime());
            }else if(toSpeak.equals("Internet")){
                toSpeak = "press long and say website name after the alert";
            }
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
        mLastClickTime = currTime;
    }

    @Override
    public void onItemLongClick(DataModel item) {

        String toSpeak = item.text.toString();
        if(toSpeak.equals("Call")){
            Intent intent = new Intent(this, CallActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Time")){
            DateFormat df = new SimpleDateFormat("h:mm a");
            toSpeak = df.format(Calendar.getInstance().getTime());
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }else if(toSpeak.equals("Camera")){
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("Path","");
            startActivity(intent);
        }else if(toSpeak.equals("Music")){
            Intent intent = new Intent(this, MusicActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Contact")){
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Message")){
            Intent intent = new Intent(this, MessageActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Settings")){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Gallery")){
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Internet")){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say website address");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            startActivityForResult(intent, 1);
        }else if(toSpeak.equals("Store")){
            Intent intent = new Intent(this, StoreActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Help")){
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }
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



