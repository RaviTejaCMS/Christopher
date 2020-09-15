package com.iolink.test.samplepost;

import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

import java.util.Locale;

public class MailLogin extends AppCompatActivity {

    String Username;
    String Password;
    Button Submit;

    TextToSpeech t1;
    RelativeLayout relativeLayout;
    LinearLayout linearLayout;
    PopupWindow keyboardwindow;

    SharedPreferences pref,MailUsrPass;
    MyKeyBoard k;

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_login);

        imageView = (ImageView) findViewById(R.id.imgmaillogin);

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);
        MailUsrPass = getApplicationContext().getSharedPreferences("MailPassword", 0);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        imageView.setOnTouchListener(new OnSwipeTouchListener(MailLogin.this){
            @Override
            public void onSwipeBottom() {
                t1.speak("Enter password", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailLogin.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        Password = myText;
                    }
                });
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Enter email address", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailLogin.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        Username = myText;
                    }
                });
            }

            @Override
            public void onSwipeRight() {
                if (Username.equals("")) {
                    t1.speak("email address required", TextToSpeech.QUEUE_FLUSH, null);
                } else if (Password.equals("")) {
                    t1.speak("password required", TextToSpeech.QUEUE_FLUSH, null);
                } else{
                    SharedPreferences.Editor editor = MailUsrPass.edit();
                    editor.putString("Username", Username.toString());
                    editor.putString("Password", Password.toString());
                    editor.commit();

                    finish();
                    Intent intent = new Intent(getApplicationContext(), MailOptionActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onSwipeLeft() {
                onBackPressed();
            }
        });
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
