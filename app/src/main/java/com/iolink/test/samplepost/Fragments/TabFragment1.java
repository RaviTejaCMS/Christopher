package com.iolink.test.samplepost.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
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
import com.iolink.test.samplepost.CallActivity;
import com.iolink.test.samplepost.CameraActivity;
import com.iolink.test.samplepost.ContactsActivity;
import com.iolink.test.samplepost.GalleryActivity;
import com.iolink.test.samplepost.HelpActivity;
import com.iolink.test.samplepost.MailLogin;
import com.iolink.test.samplepost.MailOptionActivity;
import com.iolink.test.samplepost.MailsActivity;
import com.iolink.test.samplepost.MessageActivity;
import com.iolink.test.samplepost.Model.DataModel;
import com.iolink.test.samplepost.MusicActivity;
import com.iolink.test.samplepost.R;
import com.iolink.test.samplepost.Services.ShakeDetector;
import com.iolink.test.samplepost.SettingsActivity;
import com.iolink.test.samplepost.StoreActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TabFragment1 extends Fragment implements RecyclerViewAdapter.ItemListener{

    TextToSpeech t1;

    long mLastClickTime;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    RecyclerView recyclerView;
    ArrayList arrayList;

    SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);
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
        arrayList.add(new DataModel("Call", R.drawable.call, "#20B2AA"));
        arrayList.add(new DataModel("Message", R.drawable.message, "#FFA07A"));
        arrayList.add(new DataModel("Contact", R.drawable.contact, "#90EE90"));
        arrayList.add(new DataModel("E-Mail", R.drawable.email, "#FFB6C1"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), arrayList, this);
        recyclerView.setAdapter(adapter);

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        return rootView;
    }

    private void handleShakeEvent(int count) {
        //t1.speak("Call,Message,Contact,Time", TextToSpeech.QUEUE_FLUSH, null);
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
            Intent intent = new Intent(getActivity(), CallActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("E-Mail")){
            SharedPreferences MailUsrPass = getActivity().getApplicationContext().getSharedPreferences("MailPassword", 0);
            String value = MailUsrPass.getString("Username",null);
            if (value != null) {
                Intent intent = new Intent(getActivity().getApplicationContext(), MailOptionActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(getActivity(), MailLogin.class);
                startActivity(intent);
            }
        }else if(toSpeak.equals("Camera")){
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtra("Path","");
            startActivity(intent);
        }else if(toSpeak.equals("Music")){
            Intent intent = new Intent(getActivity(), MusicActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Contact")){
            Intent intent = new Intent(getActivity(), ContactsActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Message")){
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Settings")){
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Gallery")){
            Intent intent = new Intent(getActivity(), GalleryActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Internet")){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say website address");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            startActivityForResult(intent, 1);
        }else if(toSpeak.equals("Store")){
            Intent intent = new Intent(getActivity(), StoreActivity.class);
            startActivity(intent);
        }else if(toSpeak.equals("Help")){
            Intent intent = new Intent(getActivity(), HelpActivity.class);
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
