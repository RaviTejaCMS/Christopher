package com.iolink.test.samplepost;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;
import com.iolink.test.samplepost.Services.WhatsappAccessibilityService;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WhatsappActivity extends AppCompatActivity {

    String txtNumber, txtMessage;

    TextToSpeech t1;
    LinearLayout linearLayout;
    PopupWindow keyboardwindow;

    SharedPreferences pref;
    MyKeyBoard k;

    ImageView ImgWhatsapp;
    TextView contactName,contactNumber;

    List<String> NameList = new ArrayList<String>();
    List<String> NumberList = new ArrayList<String>();

    int ListPos = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whatsapp);

        ImgWhatsapp = (ImageView) findViewById(R.id.ImgWhatsapp);
        contactName = (TextView) findViewById(R.id.contactName);
        contactNumber = (TextView) findViewById(R.id.contactNumber);

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
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


        GetWhatsAppContact("all");

        /*if (!isAccessibilityOn (WhatsappActivity.this, WhatsappAccessibilityService.class)) {
            Intent intent = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity (intent);
        }*/

        linearLayout.setOnTouchListener(new OnSwipeTouchListener(WhatsappActivity.this){
            @Override
            public void onSwipeBottom() {
                txtNumber = NumberList.get(ListPos);
                contactName.setVisibility(View.GONE);
                contactNumber.setVisibility(View.GONE);
                ImgWhatsapp.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSwipeTop() {
                txtNumber = NumberList.get(ListPos);
                contactName.setVisibility(View.GONE);
                contactNumber.setVisibility(View.GONE);
                ImgWhatsapp.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSwipeRight() {
                ListPos--;
                ReadContact(ListPos);
            }

            @Override
            public void onSwipeLeft() {
                ListPos++;
                ReadContact(ListPos);
            }
        });


        ImgWhatsapp.setOnTouchListener(new OnSwipeTouchListener(WhatsappActivity.this){
            @Override
            public void onSwipeBottom() {
                t1.speak("Enter message", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(WhatsappActivity.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        txtMessage = myText;
                    }
                });
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Enter number", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(WhatsappActivity.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        txtNumber = myText;
                    }
                });
            }

            @Override
            public void onSwipeLeft() {
                contactName.setVisibility(View.VISIBLE);
                contactNumber.setVisibility(View.VISIBLE);
                ImgWhatsapp.setVisibility(View.GONE);
                GetWhatsAppContact("all");
            }

            @Override
            public void onSwipeRight() {
                sendMessageToWhatsAppContact(txtNumber,txtMessage);
            }
        });
    }

    private void handleShakeEvent(int count) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say first letter of contact");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        startActivityForResult(intent, 1);
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
                GetWhatsAppContact(resultSpeech);
            }else if(requestCode == 9001){
                k.onActivityResult(requestCode, resultcode, intent);
            }
        }
    }

    public void ReadContact(int ListPos){
        try {



            if (ListPos < 0) {
                ListPos = 0;
            }



            if (ListPos > NameList.size()) {
                ListPos = NameList.size();
            }

            t1.speak(NameList.get(ListPos).toString(), TextToSpeech.QUEUE_FLUSH, null);
            contactName.setText(NameList.get(ListPos));
            contactNumber.setText(NumberList.get(ListPos));
        }catch (Exception ex){
            contactName.setText("");
            contactNumber.setText("");
            ListPos = 0;
            t1.speak("No whatsapp contact found", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public  void GetWhatsAppContact(String query){
        try{
            NameList = new ArrayList<String>();
            NumberList = new ArrayList<String>();

            Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI
                    ,new String[] {ContactsContract.Data._ID
                            ,ContactsContract.Data.DISPLAY_NAME
                            ,ContactsContract.CommonDataKinds.Phone.NUMBER
                            ,ContactsContract.CommonDataKinds.Phone.TYPE}
                    ,ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                            + "' AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?"
                    ,new String[] { "com.whatsapp" }
                    , null);

            int contactNumberColumn     = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int contactNameColumn       = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
            while (c.moveToNext())
            {
                String name = c.getString(contactNameColumn);
                if(query.contains("all")) {
                    NameList.add(name);
                    NumberList.add(c.getString(contactNumberColumn));
                }else{
                    if(name.substring(0, 1).toLowerCase().equals(query.toLowerCase())){
                        NameList.add(name);
                        NumberList.add(c.getString(contactNumberColumn));
                    }
                }
            }

            ListPos = 0;
            ReadContact(ListPos);
            if(NumberList.size() != 0) {
                t1.speak("Please select the contact", TextToSpeech.QUEUE_FLUSH, null);

            }
        }catch (Exception ex){
            Log.e("WHATSPP",ex.toString());
        }
    }

    private void sendMessageToWhatsAppContact(String number,String msg) {
        t1.speak("Click right bottom to send message", TextToSpeech.QUEUE_FLUSH, null);
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            String url = "https://api.whatsapp.com/send?phone=" + number + "&text=" + URLEncoder.encode(msg, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                getApplicationContext().startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAccessibilityOn (Context context, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName () + "/" + clazz.getCanonicalName ();
        try {
            accessibilityEnabled = Settings.Secure.getInt (context.getApplicationContext ().getContentResolver (), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {  }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter (':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString (context.getApplicationContext ().getContentResolver (), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                colonSplitter.setString (settingValue);
                while (colonSplitter.hasNext ()) {
                    String accessibilityService = colonSplitter.next ();

                    if (accessibilityService.equalsIgnoreCase (service)) {
                        return true;
                    }
                }
            }
        }

        return false;
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
