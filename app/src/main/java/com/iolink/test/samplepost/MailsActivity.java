package com.iolink.test.samplepost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iolink.test.samplepost.Services.GMailSender;
import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;


public class MailsActivity extends AppCompatActivity {
    TextView textmsg;

    ListView list;
    List<String> listSender = new ArrayList<String>();
    List<String> listSubject = new ArrayList<String>();
    List<String> listMessage = new ArrayList<String>();

    TextToSpeech t1;

    LinearLayout linearLayout1;
    Context context;


    TextView txtSender,txtSubject,txtMail;
    int MsgPos = 0;

    SharedPreferences pref;
    MyKeyBoard k;

    PopupWindow keyboardwindow,ComposeWindow;

    ImageView imgNewMail;
    String mailto,mailsubject,mailmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mails);

        context = getApplicationContext();

        linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.UK);
            }
            }
        });

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        txtSender=(TextView) findViewById(R.id.txtSender);
        txtSubject =(TextView) findViewById(R.id.txtSubject);
        txtMail = (TextView) findViewById(R.id.txtMessage);

        linearLayout1.setOnTouchListener(new OnSwipeTouchListener(MailsActivity.this) {
            @Override
            public void onSwipeLeft() {
                MsgPos++;
                ReadMail();
            }

            @Override
            public void onSwipeRight() {
                MsgPos--;
                ReadMail();
            }

            @Override
            public void onSwipeTop() {
                if(mailto.equals("")){
                    t1.speak("No mails found", TextToSpeech.QUEUE_FLUSH, null);
                }else {
                    ComposeMessage();
                }
            }

            @Override
            public void onSwipeBottom() {
                ReadContent();
            }
        });
        new MyAsynk().execute();
    }

    public void ReadMail(){
        try {
            if (MsgPos == -1) {
                MsgPos = 0;
            }

            if (MsgPos > listSender.size()) {
                MsgPos = listSender.size() - 1;
            }
            String name = "Sender - " + listSender.get(MsgPos) + ", Subject - " + listSubject.get(MsgPos);
            t1.speak(name, TextToSpeech.QUEUE_FLUSH, null);

            mailto = listSender.get(MsgPos);

            txtSender.setText(listSender.get(MsgPos));
            txtSubject.setText(listSubject.get(MsgPos));
            txtMail.setText(listMessage.get(MsgPos));
        }catch (Exception ex){
            mailto = "";
            t1.speak("No mails found", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void ReadContent(){
        try{
            String name = "Sender - " + listSender.get(MsgPos) + ", Subject - " + listSubject.get(MsgPos) + ", Message - " + listMessage.get(MsgPos);
            t1.speak(name, TextToSpeech.QUEUE_FLUSH, null);
        }catch (Exception e){

        }
    }


    public void ComposeMessage(){
        LayoutInflater layoutInflater = (LayoutInflater) MailsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_compose,null);

        imgNewMail = (ImageView) customView.findViewById(R.id.newmailimg);
        imgNewMail.setOnTouchListener(new OnSwipeTouchListener(MailsActivity.this){
            @Override
            public void onSwipeBottom() {
                t1.speak("Enter message", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailsActivity.this,t1,null,linearLayout1,pref,9001);
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
                t1.speak(mailto, TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onSwipeRight() {
                SendMail(mailto,mailsubject,mailmessage);
            }

            @Override
            public void onSwipeLeft() {
                t1.speak("Enter subject", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(MailsActivity.this,t1,null,linearLayout1,pref,9001);
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
        ComposeWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, 0);
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

    private class MyAsynk extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");

            SharedPreferences MailUsrPass = getApplicationContext().getSharedPreferences("MailPassword", 0);
            String username = MailUsrPass.getString("Username",null);
            String password = MailUsrPass.getString("Password",null);
            //                                           ;
            try {
                Session session  = Session.getDefaultInstance(props, null);
                Store store      = session.getStore("imaps");
                store.connect("imap.gmail.com", username, password);
                Folder inbox     = store.getFolder("Inbox");
                inbox.open(Folder.READ_WRITE);
                Message msgs[]   = inbox.getMessages();
                if (msgs.length > 0)
                {
                    for (int i = 0; i < msgs.length ; i++)
                    {
                        listSender.add(((InternetAddress) msgs[i].getFrom()[0]).getAddress());
                        listSubject.add(msgs[i].getSubject().toString());
                        if (msgs[i].isMimeType("text/plain")) {
                            listMessage.add(msgs[i].getContent().toString());
                        } else if (msgs[i].isMimeType("multipart/*")) {
                            MimeMultipart mimeMultipart = (MimeMultipart) msgs[i].getContent();
                            listMessage.add(getTextFromMimeMultipart(mimeMultipart));
                        } else{
                            listMessage.add("");
                        }
                    }
                }
                store.close();
            }
            catch (Exception e) {
                Log.e("MAILMAIL",e.toString());
                return null;
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            MsgPos = 0;
            ReadMail();
        }
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart){
        try {
            String result = "";
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result = result + "\n" + bodyPart.getContent();
                    break; // without break same text appears twice in my tests
                } else if (bodyPart.isMimeType("text/html")) {
                    String html = (String) bodyPart.getContent();
                    result = html;
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
                }
            }
            return result;
        }catch (Exception e){
            return  "";
        }
    }
}
