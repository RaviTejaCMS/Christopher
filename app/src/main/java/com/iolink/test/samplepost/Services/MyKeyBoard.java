package com.iolink.test.samplepost.Services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.iolink.test.samplepost.BrowserActivity;
import com.iolink.test.samplepost.ContactsActivity;
import com.iolink.test.samplepost.R;

import java.util.ArrayList;

public class MyKeyBoard {
    ImageButton BtnCaps,BtnNumberOn,BtnRemove,BtnSpace;
    String mytext = "";
    Boolean isKeyNum = false;
    Boolean isCaps = false;
    PopupWindow keyboard;

    Context mycontext;

    TextToSpeech t1;

    RelativeLayout relativeLayout;
    LinearLayout linearLayout;

    SharedPreferences pref;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    Activity activity;
    int IntentId = 0;

    public PopupWindow MyKeyBoards(Context context, TextToSpeech t, RelativeLayout r, LinearLayout l, SharedPreferences sp,int Intent){
        mycontext = context;
        t1 = t;
        relativeLayout = r;
        linearLayout = l;
        pref = sp;
        mytext = "";
        isKeyNum = false;
        isCaps = false;
        activity = (Activity)context;
        IntentId = Intent;

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                OpenGoogleMic();
            }
        });

        return OpenKeyboard();
    }

    public void OpenGoogleMic(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say anything");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        activity.startActivityForResult(intent, IntentId);
    }


    public PopupWindow OpenKeyboard(){

        LayoutInflater layoutInflater = (LayoutInflater) mycontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_keyboard,null);

        BtnCaps = (ImageButton) customView.findViewById(R.id.BtnCaps);
        SwipeControl(BtnCaps,mycontext);

        BtnNumberOn = (ImageButton) customView.findViewById(R.id.BtnNumberOn);
        SwipeControl(BtnNumberOn,mycontext);

        BtnRemove = (ImageButton) customView.findViewById(R.id.BtnRemove);
        SwipeControl(BtnRemove,mycontext);

        BtnSpace = (ImageButton) customView.findViewById(R.id.BtnFive);
        BtnSpace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mytext = mytext  + " ";
                t1.speak("space added", TextToSpeech.QUEUE_FLUSH, null);
                return false;
            }
        });

        keyboard = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        if(relativeLayout != null) {
            keyboard.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
        }else{
            keyboard.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
        }

        return keyboard;
    }

    public void onActivityResult(int requestCode, int resultcode, Intent intent) {
        ArrayList<String> speech;
        if (resultcode == Activity.RESULT_OK) {
            speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String resultSpeech = speech.get(0);
            mytext = mytext + ""  + resultSpeech;
        }
    }

    public void SwipeControl(ImageButton button,Context context){
        button.setOnTouchListener(new OnSwipeTouchListener(context){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                t1.speak(mytext, TextToSpeech.QUEUE_FLUSH, null);
                return super.onTouch(v, event);
            }

            public void onSwipeBottom() {
                if(isKeyNum) {
                    isKeyNum = false;
                    t1.speak("number mode disabled", TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    isKeyNum = true;
                    t1.speak("number mode enabled", TextToSpeech.QUEUE_FLUSH, null);
                }
            }

            @Override
            public void onSwipeTop() {
                if(isCaps) {
                    isCaps = false;
                    t1.speak("caps mode disabled", TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    isCaps = true;
                    t1.speak("caps mode enabled", TextToSpeech.QUEUE_FLUSH, null);
                }
            }

            @Override
            public void onSwipeRight() {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("mytext", mytext);
                editor.commit();
                keyboard.dismiss();
            }

            @Override
            public void onSwipeLeft() {
                mytext = mytext.substring(0, mytext.length() - 1);
                t1.speak("last letter removed", TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    public void CheckLetter(int a, int b, int c, int d, int e, int f){
        String letter = "";
        if(a == 1 && b == 0 && c == 0 && d == 0 && e == 0 && f == 0){
            letter = "a";
        } else if(a == 1 && b == 0 && c == 1 && d == 0 && e == 0 && f == 0){
            letter = "b";
        } else if(a == 1 && b == 1 && c == 0 && d == 0 && e == 0 && f == 0){
            letter = "c";
        } else if(a == 1 && b == 1 && c == 0 && d == 1 && e == 0 && f == 0){
            letter = "d";
        } else if(a == 1 && b == 0 && c == 0 && d == 1 && e == 0 && f == 0){
            letter = "e";
        } else if(a == 1 && b == 1 && c == 1 && d == 0 && e == 0 && f == 0){
            letter = "f";
        } else if(a == 1 && b == 1 && c == 1 && d == 1 && e == 0 && f == 0){
            letter = "g";
        } else if(a == 1 && b == 0 && c == 1 && d == 1 && e == 0 && f == 0){
            letter = "h";
        } else if(a == 0 && b == 1 && c == 1 && d == 0 && e == 0 && f == 0){
            letter = "i";
        } else if(a == 0 && b == 1 && c == 1 && d == 1 && e == 0 && f == 0){
            letter = "j";
        } else if(a == 1 && b == 0 && c == 0 && d == 0 && e == 1 && f == 0){
            letter = "k";
        } else if(a == 1 && b == 0 && c == 1 && d == 0 && e == 1 && f == 0){
            letter = "l";
        } else if(a == 1 && b == 1 && c == 0 && d == 0 && e == 1 && f == 0){
            letter = "m";
        } else if(a == 1 && b == 1 && c == 0 && d == 1 && e == 1 && f == 0){
            letter = "n";
        } else if(a == 1 && b == 0 && c == 0 && d == 1 && e == 1 && f == 0){
            letter = "o";
        } else if(a == 1 && b == 1 && c == 1 && d == 0 && e == 1 && f == 0){
            letter = "p";
        } else if(a == 1 && b == 1 && c == 1 && d == 1 && e == 1 && f == 0){
            letter = "q";
        } else if(a == 1 && b == 0 && c == 1 && d == 1 && e == 1 && f == 0){
            letter = "r";
        } else if(a == 0 && b == 1 && c == 1 && d == 0 && e == 1 && f == 0){
            letter = "s";
        } else if(a == 0 && b == 1 && c == 1 && d == 1 && e == 1 && f == 0){
            letter = "t";
        } else if(a == 1 && b == 0 && c == 0 && d == 0 && e == 1 && f == 1){
            letter = "u";
        } else if(a == 1 && b == 0 && c == 1 && d == 0 && e == 1 && f == 1){
            letter = "v";
        } else if(a == 0 && b == 1 && c == 1 && d == 1 && e == 0 && f == 1){
            letter = "w";
        } else if(a == 1 && b == 1 && c == 0 && d == 0 && e == 1 && f == 1){
            letter = "x";
        } else if(a == 1 && b == 1 && c == 0 && d == 1 && e == 1 && f == 1){
            letter = "y";
        } else if(a == 1 && b == 0 && c == 0 && d == 1 && e == 1 && f == 1){
            letter = "z";
        }else if(a == 2){
            letter = "@";
        }else if(b == 2){
            letter = ".";
        }else if(e == 2){
            letter = " ";
        }else if(f == 2){
            OpenGoogleMic();
        }

        if(isKeyNum && letter == "a")
        {
            letter = "1";
        }else if(isKeyNum && letter == "b")
        {
            letter = "2";
        }else if(isKeyNum && letter == "c")
        {
            letter = "3";
        }else if(isKeyNum && letter == "d")
        {
            letter = "4";
        }else if(isKeyNum && letter == "e")
        {
            letter = "5";
        }else if(isKeyNum && letter == "f")
        {
            letter = "6";
        }else if(isKeyNum && letter == "g")
        {
            letter = "7";
        }else if(isKeyNum && letter == "h")
        {
            letter = "8";
        }else if(isKeyNum && letter == "i")
        {
            letter = "9";
        }else if(isKeyNum && letter == "j")
        {
            letter = "0";
        }

        if(letter.equals("") && f != 2){
            t1.speak("Invalid letter", TextToSpeech.QUEUE_FLUSH, null);
        }else if(f != 2 ){
            if(letter.equals(" ")) {
                t1.speak("Space", TextToSpeech.QUEUE_FLUSH, null);
            }else {
                t1.speak(letter, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        if(isCaps){
            letter = letter.toUpperCase();
        }

        mytext = mytext + ""  + letter;
        a = 0; b = 0; c = 0; d = 0 ;e =0; f =0;
    }
}
