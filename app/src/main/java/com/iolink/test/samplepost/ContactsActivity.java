package com.iolink.test.samplepost;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iolink.test.samplepost.Adapter.ContactsAdapter;
import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ContactsActivity extends AppCompatActivity {

    ListView list;
    List<String> maintitle = new ArrayList<String>();
    List<String> subtitle = new ArrayList<String>();

    Button BtnCall, BtnMsg, BtnClose;
    Button delMsgBtn, playMsgBtn, closePopupBtn, sendMsgBtn;

    Intent phoneIntent = new Intent(Intent.ACTION_CALL);

    TextToSpeech t1;

    PopupWindow popupWindow1;
    PopupWindow popupWindow2;

    LinearLayout linearLayout;

    String CurrentNumber = "";

    TextView txtName, txtNubmer;

    int ContactPos = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    String DIALOG_TEXT = "Say any name";

    SharedPreferences pref,MailUsrPass;
    MyKeyBoard k;
    PopupWindow keyboardwindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        txtName  = (TextView) findViewById(R.id.txtName);
        txtNubmer  = (TextView) findViewById(R.id.txtNumber);

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

        getContactList("none");

        linearLayout.setOnTouchListener(new OnSwipeTouchListener(ContactsActivity.this) {

            @Override
            public void onSwipeLeft() {
                ContactPos++;
                OpenContact();
            }

            @Override
            public void onSwipeRight() {
                ContactPos--;
                OpenContact();
            }

            @Override
            public void onSwipeTop() {
                PhoneCall(ContactPos);
            }

            @Override
            public void onSwipeBottom() {
                ComposeMessage(ContactPos);
            }

        });
    }

    private void handleShakeEvent(int count) {

        t1.speak("Shaking", TextToSpeech.QUEUE_FLUSH, null);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, DIALOG_TEXT);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        startActivityForResult(intent, 5);
    }

    public void OpenContact(){
        try {
            if (ContactPos == -1) {
                ContactPos = 0;
                t1.speak("This is first contact", TextToSpeech.QUEUE_FLUSH, null);
                //return;
            }

            if (ContactPos > maintitle.size()) {
                ContactPos = maintitle.size();
                t1.speak("This is last contact", TextToSpeech.QUEUE_FLUSH, null);
                //return;
            }
            String name = maintitle.get(ContactPos);
            t1.speak(name, TextToSpeech.QUEUE_FLUSH, null);
            txtName.setText(subtitle.get(ContactPos));
            txtNubmer.setText(maintitle.get(ContactPos));
        }catch (Exception ex){
            t1.speak("No Contacts found, Please try again", TextToSpeech.QUEUE_FLUSH, null);
            txtName.setText("");
            txtNubmer.setText("");
        }
    }

    public void ComposeMessage(int position){
        String name = maintitle.get(position);
        String number = subtitle.get(position);
        CurrentNumber = number;
        t1.speak("Opening new message window", TextToSpeech.QUEUE_FLUSH, null);
        NewMessage();
    }

    public void PhoneCall(int position){
        String number = subtitle.get(position);
        CurrentNumber = number;
        phoneIntent.setData(Uri.parse("tel:" + CurrentNumber));
        startActivity(phoneIntent);
    }

    private void getContactList(String query) {
        try {

            maintitle = new ArrayList<String>();
            subtitle = new ArrayList<String>();
            ContactPos = 0;

            ContentResolver cr = getContentResolver();
            Cursor cur = null;
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);

            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));

                            if(query.equals("none")){
                                maintitle.add(name);
                                subtitle.add(phoneNo);
                            }else{
                                if(name.contains(query)){
                                    maintitle.add(name);
                                    subtitle.add(phoneNo);
                                }
                            }


                            Log.i("Name", "Name: " + name);
                            Log.i("Phone", "Phone Number: " + phoneNo);
                        }
                        pCur.close();
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            OpenContact();
        }catch (Exception ex){
            Log.e("APP_ERROR",ex.toString());
        }
    }

    ImageView MsgImageView;
    String msgContent;
    PopupWindow MsgWindow;
    public void NewMessage(){
        LayoutInflater layoutInflater = (LayoutInflater) ContactsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_message,null);
        MsgImageView = (ImageView) customView.findViewById(R.id.MsgImageView);
        MsgWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        MsgWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
        MsgImageView.setOnTouchListener(new OnSwipeTouchListener(ContactsActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("Closing message window", TextToSpeech.QUEUE_FLUSH, null);
                MsgWindow.dismiss();
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Opening keyboard for compose message", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(ContactsActivity.this,t1,null,linearLayout,pref,9001);
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
            String sendNumber = CurrentNumber;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sendNumber,null,message.toString(),null,null);
            t1.speak("Message sent successfully", TextToSpeech.QUEUE_FLUSH, null);
        }catch (Exception ex){
            Log.e("ERROR_APP",ex.toString());
        }
    }

    String resultSpeech = "";
    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        ArrayList<String> speech;
        if (resultcode == RESULT_OK) {
            if (requestCode == 3) {
                speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                resultSpeech = speech.get(0);
            }else if (requestCode == 5) {
                speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                resultSpeech = speech.get(0);
                getContactList(resultSpeech);
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
