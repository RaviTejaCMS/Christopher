package com.iolink.test.samplepost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iolink.test.samplepost.Services.GMailSender;
import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

import java.net.URL;
import java.util.Locale;

/**
 * Created by Peach on 8/28/2018.
 */

public class MailOptionActivity extends AppCompatActivity {

    Button BtnCompose,BtnInbox,BtnLogout;
    PopupWindow ComposeWindow,keyboardwindow;

    LinearLayout linearLayout;

    MyKeyBoard k;
    SharedPreferences pref;
    TextToSpeech t1;

    ImageView optImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_option);

        optImageView = (ImageView) findViewById(R.id.imageviewmail);
        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        optImageView.setOnTouchListener(new OnSwipeTouchListener(MailOptionActivity.this){
            @Override
            public void onSwipeBottom() {
                Intent intent = new Intent(getApplicationContext(), MailsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSwipeTop() {
                ComposeMessage();
            }

            @Override
            public void onSwipeLeft() {
                onBackPressed();
            }

            @Override
            public void onSwipeRight() {
                SharedPreferences MailUsrPass = getApplicationContext().getSharedPreferences("MailPassword", 0);
                MailUsrPass.edit().clear().commit();

                finish();
                Intent intent = new Intent(getApplicationContext(), MailLogin.class);
                startActivity(intent);
            }
        });
    }

    ImageView imgNewMail;
    String mailto,mailsubject,mailmessage;
    public void ComposeMessage(){
        LayoutInflater layoutInflater = (LayoutInflater) MailOptionActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_compose,null);

        imgNewMail = (ImageView) customView.findViewById(R.id.newmailimg);
        imgNewMail.setOnTouchListener(new OnSwipeTouchListener(MailOptionActivity.this){
            @Override
            public void onSwipeBottom() {
                t1.speak("Enter message", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailOptionActivity.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        mailmessage = myText;
                    }
                });
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Enter to address", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailOptionActivity.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        mailto = myText;
                    }
                });
            }

            @Override
            public void onSwipeRight() {
                SendMail(mailto,mailsubject,mailmessage);
            }

            @Override
            public void onSwipeLeft() {
                t1.speak("Enter subject", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailOptionActivity.this,t1,null,linearLayout,pref,9001);
                keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String myText = pref.getString("mytext", null);
                        mailsubject = myText;
                    }
                });
            }
        });

        ComposeWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ComposeWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    public void SendMail(final String email,final String subject, final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    t1.speak("Sending message", TextToSpeech.QUEUE_FLUSH, null);

                    SharedPreferences MailUsrPass = getApplicationContext().getSharedPreferences("MailPassword", 0);
                    String username = MailUsrPass.getString("Username",null);
                    String password = MailUsrPass.getString("Password",null);

                    GMailSender sender = new GMailSender(username, password);
                    sender.sendMail(subject,message,username,email);
                }catch (Exception ex){
                    t1.speak("message sent successfully", TextToSpeech.QUEUE_FLUSH, null);
                    Log.e("MAIL_ERROR",ex.toString());
                }
            }
        }).start();
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
