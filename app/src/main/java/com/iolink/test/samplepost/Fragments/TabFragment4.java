package com.iolink.test.samplepost.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.iolink.test.samplepost.Adapter.RecyclerViewAdapter;
import com.iolink.test.samplepost.BrowserActivity;
import com.iolink.test.samplepost.CallActivity;
import com.iolink.test.samplepost.CameraActivity;
import com.iolink.test.samplepost.ContactsActivity;
import com.iolink.test.samplepost.GalleryActivity;
import com.iolink.test.samplepost.HelpActivity;
import com.iolink.test.samplepost.MessageActivity;
import com.iolink.test.samplepost.Model.DataModel;
import com.iolink.test.samplepost.MusicActivity;
import com.iolink.test.samplepost.R;
import com.iolink.test.samplepost.Services.ShakeDetector;
import com.iolink.test.samplepost.SettingsActivity;
import com.iolink.test.samplepost.ShopActivity;
import com.iolink.test.samplepost.StoreActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.BATTERY_SERVICE;

public class TabFragment4 extends Fragment implements RecyclerViewAdapter.ItemListener{

    TextToSpeech t1;

    long mLastClickTime;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    RecyclerView recyclerView;
    ArrayList arrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_fragment_4, container, false);
        t1=new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        arrayList = new ArrayList();

        arrayList.add(new DataModel("Settings", R.drawable.settings, "#EEDD82"));
        arrayList.add(new DataModel("Date", R.drawable.date, "#F08080"));
        arrayList.add(new DataModel("Time", R.drawable.time, "#BA55D3"));
        arrayList.add(new DataModel("Battery", R.drawable.battery, "#9370DB"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), arrayList, this);
        recyclerView.setAdapter(adapter);

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        return rootView;
    }

    private void handleShakeEvent(int count) {
        //t1.speak("Internet,Settings,Store,Help", TextToSpeech.QUEUE_FLUSH, null);
    }

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
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
            }else if(toSpeak.equals("Date")){
                DateFormat df = new SimpleDateFormat("dd MMM yyyy");
                toSpeak = df.format(Calendar.getInstance().getTime());
            }else if(toSpeak.equals("Battery")){
                int batLevel = getBatteryPercentage(getContext());
                toSpeak = "Battery Level " + batLevel + "%";
            }
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
        mLastClickTime = currTime;
    }

    @Override
    public void onItemLongClick(DataModel item) {

        String toSpeak = item.text.toString();
        if(toSpeak.equals("Shopping")){
            Intent intent = new Intent(getActivity(), ShopActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Settings")){
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        ArrayList<String> speech;
        if (resultcode == getActivity().RESULT_OK) {
            if (requestCode == 1) {
                speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String resultSpeech = speech.get(0);

                if (!resultSpeech.startsWith("http://") && !resultSpeech.startsWith("https://"))
                    resultSpeech = "http://" + resultSpeech;

                Intent intentx = new Intent(getActivity(), BrowserActivity.class);
                intentx.putExtra("Path",resultSpeech);
                startActivity(intentx);
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
