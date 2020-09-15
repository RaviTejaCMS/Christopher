package com.iolink.test.samplepost;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MusicActivity extends AppCompatActivity {

    Context context;
    ListView listView;
    List<String> ListElementsArrayList,tempTitle;
    List<String> ListSongsPath,tempPath;
    String[] ListElements = new String[] { };
    ArrayAdapter<String> adapter;
    ContentResolver contentResolver;
    Uri uri;
    Cursor cursor;
    Integer CurrentSong = 0;
    //Integer SongCount = 0;
    Integer Steps = 0;

    TextToSpeech t1;

    private MediaPlayer mpintro;

    TextView txtSongTitle;

    public static final int RUNTIME_PERMISSION_CODE = 7;
    RelativeLayout relativeLayout;

    int SongPos = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        txtSongTitle = (TextView) findViewById(R.id.txtSongTitle);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        context = getApplicationContext();
        ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));
        ListSongsPath = new ArrayList<>(Arrays.asList(ListElements));

        AndroidRuntimePermission();
        GetAllMediaMp3Files("all");

        SongPos = 0;
        OpenSong();

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(MusicActivity.this) {

            @Override
            public void onSwipeLeft() {
                SongPos++;
                OpenSong();
            }

            @Override
            public void onSwipeRight() {
                SongPos--;
                OpenSong();
            }

            @Override
            public void onSwipeTop() {
                PlaySong();
            }

            @Override
            public void onSwipeBottom() {
                StopSong();
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

    private void handleShakeEvent(int count) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say song name");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        startActivityForResult(intent, 1);
    }

    public void OpenSong(){
        try {
            if (SongPos == -1) {
                SongPos = 0;
                t1.speak("This is first song", TextToSpeech.QUEUE_FLUSH, null);
                //return;
            }

            if (SongPos > ListElementsArrayList.size()) {
                SongPos = ListElementsArrayList.size() - 1;
                t1.speak("This is last song", TextToSpeech.QUEUE_FLUSH, null);
                //return;
            }
            txtSongTitle.setText(ListElementsArrayList.get(SongPos));
        }catch (Exception e){
            SongPos = 0;
        }
    }

    public void PlaySong(){
        try {
            if (mpintro != null) {
                mpintro.stop();
            }

            String path = ListSongsPath.get(SongPos);
            mpintro = MediaPlayer.create(this, Uri.parse(path));
            mpintro.setLooping(false);
            mpintro.start();
        }catch (Exception e){
            t1.speak("Something went wrong", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void StopSong(){
        if(mpintro != null){
            mpintro.stop();
        }
    }

    public  void SetAdapter(int startCount){

        tempTitle = new ArrayList<>(Arrays.asList(ListElements));
        tempPath = new ArrayList<>(Arrays.asList(ListElements));

        for(int i=startCount;i<startCount + 10;i++){
            try {
                tempTitle.add(ListElementsArrayList.get(i));
                tempPath.add(ListSongsPath.get(i));
            }catch (Exception e){

            }
        }

        adapter = new ArrayAdapter<String>(MusicActivity.this, android.R.layout.simple_list_item_1, tempTitle){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,25);
                return view;
            }
        };


        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaySong(position);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mpintro != null){
            mpintro.stop();
        }
        super.onBackPressed();
    }

    public void PlaySong(int position){
        if(mpintro != null){
            mpintro.stop();
            if(position == CurrentSong){
                CurrentSong = 0;
                return;
            }
        }

        CurrentSong = position;
        String path = tempPath.get(position);
        mpintro = MediaPlayer.create(this, Uri.parse(path));
        mpintro.setLooping(false);
        mpintro.start();
    }

    public void GetAllMediaMp3Files(String songname){

        ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));
        ListSongsPath = new ArrayList<>(Arrays.asList(ListElements));

        contentResolver = context.getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        cursor = contentResolver.query(
                uri, // Uri
                null,
                null,
                null,
                null
        );

        if (cursor == null) {
            Toast.makeText(MusicActivity.this,"Something Went Wrong.", Toast.LENGTH_LONG);

        } else if (!cursor.moveToFirst()) {

            Toast.makeText(MusicActivity.this,"No Music Found on SD Card.", Toast.LENGTH_LONG);
        }
        else {
            int Title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int song_index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                String SongTitle = cursor.getString(Title);
                String SongPath = cursor.getString(song_index);
                if(songname.equals("all")){
                    ListElementsArrayList.add(SongTitle);
                    ListSongsPath.add(SongPath);
                }else{
                    if(SongTitle.contains(songname)){
                        ListElementsArrayList.add(SongTitle);
                        ListSongsPath.add(SongPath);
                    }
                }

            } while (cursor.moveToNext());
        }
    }

    public void AndroidRuntimePermission(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){

                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(MusicActivity.this);
                    alert_builder.setMessage("External Storage Permission is Required.");
                    alert_builder.setTitle("Please Grant Permission.");
                    alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ActivityCompat.requestPermissions(MusicActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},RUNTIME_PERMISSION_CODE);
                        }
                    });

                    alert_builder.setNeutralButton("Cancel",null);

                    AlertDialog dialog = alert_builder.create();

                    dialog.show();

                }
                else {

                    ActivityCompat.requestPermissions(
                            MusicActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RUNTIME_PERMISSION_CODE
                    );
                }
            }else {

            }
        }
    }

    String resultSpeech = "";
    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        ArrayList<String> speech;
        if (resultcode == RESULT_OK) {
            if (requestCode == 1) {
                speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                resultSpeech = speech.get(0);
                GetAllMediaMp3Files(resultSpeech);
                SongPos = 0;
                OpenSong();
                //textmsg.setText(textmsg.getText() + " " + resultSpeech);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        switch(requestCode){

            case RUNTIME_PERMISSION_CODE:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {

                }
            }
        }
    }

}
