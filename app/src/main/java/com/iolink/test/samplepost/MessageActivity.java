package com.iolink.test.samplepost;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iolink.test.samplepost.Adapter.ContactsAdapter;
import com.iolink.test.samplepost.R;
import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.iolink.test.samplepost.R.id.relativeLayout;


public class MessageActivity extends AppCompatActivity {

    Button delMsgBtn,playMsgBtn,closePopupBtn,sendMsgBtn;
    TextView textmsg;
    String sendNumber = "";

    ListView list;
    List<String> contactname = new ArrayList<String>();
    List<String> maintitle = new ArrayList<String>();
    List<String> subtitle = new ArrayList<String>();

    TextToSpeech t1;

    long mLastClickTime;

    PopupWindow popupWindow,keyboard;

    LinearLayout linearLayout1;

    Context context;

    int REQUEST_CODE = 1;
    String DIALOG_TEXT = "Speech recognition demo";

    TextView readMsg,MsgTitle;

    int MsgPos = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    SharedPreferences pref;
    MyKeyBoard k;
    PopupWindow keyboardwindow;

    String PhoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        context = getApplicationContext();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });

        linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.UK);
            }
            }
        });

        readMsg=(TextView) findViewById(R.id.readtxtmsg);
        MsgTitle =(TextView) findViewById(R.id.MsgTitle);
        linearLayout1.setOnTouchListener(new OnSwipeTouchListener(MessageActivity.this) {

            @Override
            public void onSwipeLeft() {
                MsgPos++;
                OpenMessage();
            }

            @Override
            public void onSwipeRight() {
                MsgPos--;
                OpenMessage();
            }

            @Override
            public void onSwipeTop() {
               NewMessage();
            }

            @Override
            public void onSwipeBottom() {
                SingleTab(MsgPos);
            }
        });

        getMessageList("none");
    }

    private void handleShakeEvent(int count) {

        t1.speak("Shaking", TextToSpeech.QUEUE_FLUSH, null);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, REQUEST_CODE);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        startActivityForResult(intent, 1);
    }

    public void OpenMessage(){
        try {
            if (MsgPos == -1) {
                MsgPos = 0;
                t1.speak("This is first message", TextToSpeech.QUEUE_FLUSH, null);
                //return;
            }

            if (MsgPos > maintitle.size()) {
                MsgPos = maintitle.size();
                t1.speak("This is last message", TextToSpeech.QUEUE_FLUSH, null);
                //return;
            }

            PhoneNumber = maintitle.get(MsgPos);
            String name = "Sender - " + contactname.get(MsgPos);

            t1.speak(name, TextToSpeech.QUEUE_FLUSH, null);
            readMsg.setText(subtitle.get(MsgPos));
            MsgTitle.setText(contactname.get(MsgPos));
        }catch (Exception ex){
            t1.speak("No Messages Found", TextToSpeech.QUEUE_FLUSH, null);
            readMsg.setText("");
            MsgTitle.setText("");
        }
    }

    public void OpenContacts(){
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
    }

    public void SingleTab(int position){
        String name = "Sender - " +  contactname.get(position) + ". Message - " + subtitle.get(position);
        t1.speak(name, TextToSpeech.QUEUE_FLUSH, null);
    }

    ImageView MsgImageView;
    String msgContent;
    PopupWindow MsgWindow;
    public void NewMessage(){
        LayoutInflater layoutInflater = (LayoutInflater) MessageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_message,null);
        MsgImageView = (ImageView) customView.findViewById(R.id.MsgImageView);
        MsgWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        MsgWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, 0);
        MsgImageView.setOnTouchListener(new OnSwipeTouchListener(MessageActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("Closing message window", TextToSpeech.QUEUE_FLUSH, null);
                MsgWindow.dismiss();
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Opening keyboard for compose message", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MessageActivity.this,t1,null,linearLayout1,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        msgContent = myText;
                    }
                });
            }

            @Override
            public void onSwipeRight() {
                SendSms(msgContent);
                MsgWindow.dismiss();
            }

            @Override
            public void onSwipeBottom() {
                t1.speak(msgContent, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

    }

    public void SendSms(String message){
        try{
            String sendNumber = PhoneNumber;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sendNumber,null,message.toString(),null,null);
            t1.speak("Message sent successfully", TextToSpeech.QUEUE_FLUSH, null);
        }catch (Exception ex){
            Log.e("ERROR_APP",ex.toString());
        }
    }

    private void getMessageList(String name) {
        try {
             contactname = new ArrayList<String>();
             maintitle = new ArrayList<String>();
             subtitle = new ArrayList<String>();


            Uri uriSMSURI = Uri.parse("content://sms/inbox");
            Cursor cur = null;

            cur = getContentResolver().query(uriSMSURI, null, null, null, null);

            while (cur != null && cur.moveToNext()) {
                String address = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndexOrThrow("body"));

                String cname = "";
                boolean atleastOneAlpha = address.matches(".*[a-zA-Z]+.*");
                if(atleastOneAlpha){
                    cname = address;
                }else{
                    cname = getContactName(address);
                }

                Log.e("SEARCHNAME",name);
                if(name.equals("none")){
                    maintitle.add(address);
                    subtitle.add(body);
                    contactname.add(cname);
                }else{
                    if(cname != null && cname.contains(name)){
                        maintitle.add(address);
                        subtitle.add(body);
                        contactname.add(cname);
                    }
                }
            }

            if (cur != null) {
                cur.close();
            }
            MsgPos = 0;
            OpenMessage();
        }catch (Exception ex){
            Log.e("APP_ERROR",ex.toString());
        }
    }

    public String getContactName(String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    String resultSpeech = "";
    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        ArrayList<String> speech;
        if (resultcode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                resultSpeech = speech.get(0);
                getMessageList(resultSpeech);
                //textmsg.setText(textmsg.getText() + " " + resultSpeech);
            }else if(requestCode == 9001){
                k.onActivityResult(requestCode, resultcode, intent);
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
