package com.iolink.test.samplepost;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;


public class CallActivity extends Activity {

    ImageButton btn1234;
    ImageButton btn5678;
    ImageButton btn90;
    ImageButton btnopt;

    Button BtnCall, BtnMsg, BtnClose;
    Button delMsgBtn, playMsgBtn, closePopupBtn, sendMsgBtn;

    int REQUEST_CODE = 1;
    String DIALOG_TEXT = "Speech recognition demo";

    TextView textView, textmsg;
    TextToSpeech t1;

    String resultSpeech = "";

    PopupWindow popupWindow;

    RelativeLayout relativeLayout;

    String PhoneNumber = "";

    SharedPreferences pref;
    MyKeyBoard k;
    PopupWindow keyboardwindow;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        context = getApplicationContext();

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        textView = (TextView) findViewById(R.id.textdial);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        btn1234 = (ImageButton) findViewById(R.id.btn1234);
        btn1234.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.speak("1,2,3,4", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        btn1234.setOnTouchListener(new OnSwipeTouchListener(CallActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("1", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("1");
            }

            @Override
            public void onSwipeTop() {
                t1.speak("2", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("2");
            }

            @Override
            public void onSwipeRight() {
                t1.speak("3", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("3");
            }

            @Override
            public void onSwipeBottom() {
                t1.speak("4", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("4");
            }
        });

        btn5678 = (ImageButton) findViewById(R.id.btn5678);

        btn5678.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.speak("5,6,7,8", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        btn5678.setOnTouchListener(new OnSwipeTouchListener(CallActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("5", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("5");
            }

            @Override
            public void onSwipeTop() {
                t1.speak("6", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("6");
            }

            @Override
            public void onSwipeRight() {
                t1.speak("7", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("7");
            }

            @Override
            public void onSwipeBottom() {
                t1.speak("8", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("8");
            }
        });

        btn90 = (ImageButton) findViewById(R.id.btn90);
        btn90.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.speak("9,0,*,#", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        btn90.setOnTouchListener(new OnSwipeTouchListener(CallActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("9", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("9");
            }

            @Override
            public void onSwipeTop() {
                t1.speak("0", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("0");
            }

            @Override
            public void onSwipeRight() {
                t1.speak("*", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("*");
            }

            @Override
            public void onSwipeBottom() {
                t1.speak("#", TextToSpeech.QUEUE_FLUSH, null);
                DialNumber("#");
            }
        });

        btnopt = (ImageButton) findViewById(R.id.btncall);
        btnopt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.speak("Delete,Replay,Call,Save", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        btnopt.setOnTouchListener(new OnSwipeTouchListener(CallActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("Remove last number", TextToSpeech.QUEUE_FLUSH, null);
                RemoveNumber();
            }

            @Override
            public void onSwipeRight() {
                if(PhoneNumber.equals("")){
                    t1.speak("Phone number needed for call", TextToSpeech.QUEUE_FLUSH, null);
                }else {
                    t1.speak("Call or Message", TextToSpeech.QUEUE_FLUSH, null);
                    PhoneCall();
                }
            }

            @Override
            public void onSwipeTop() {
                t1.speak(PhoneNumber.replaceAll(".(?=.)", "$0 "), TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onSwipeBottom() {
                if(PhoneNumber.equals("")){
                    t1.speak("Phone number needed to save", TextToSpeech.QUEUE_FLUSH, null);
                }else {
                    t1.speak("opening keyboard for new contact", TextToSpeech.QUEUE_FLUSH, null);
                    keyboardwindow = k.MyKeyBoards(CallActivity.this, t1, relativeLayout, null, pref,9001);
                    keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            String myText = pref.getString("mytext", null);
                            WritePhoneContact(myText,PhoneNumber);
                            t1.speak("Contact added successfully", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                }
            }
        });

    }

    public void WritePhoneContact(String displayName, String number) {
        Context contetx = context; //Application's context or Activity's context
        String strDisplayName = displayName; // Name of the Person to add
        String strNumber = number; //number of the person to add with the Contact
        System.out.println("NAME> " + strDisplayName + "    NUMBER ====>  " + strNumber);
        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
        int contactIndex = cntProOper.size();//ContactSize

        //Newly Inserted contact
        // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Display name will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
                .build());
        //Mobile number will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, strNumber) // Number to be added
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
        try {
            // We will do batch operation to insert all above data
            //Contains the output of the app of a ContentProviderOperation.
            //It is sure to have exactly one of uri or count set
            ContentProviderResult[] contentProresult = null;
            contentProresult = contetx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
        } catch (RemoteException exp) {
            //logs;
        } catch (OperationApplicationException exp) {
            //logs
        }
    }


    public void DialNumber(String number) {
        //String numbers = textView.getText() + "" + number;
        PhoneNumber = PhoneNumber + "" + number;
        //textView.setText(numbers);
    }

    public void RemoveNumber() {
        PhoneNumber = PhoneNumber.substring(0, PhoneNumber.length() - 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        ArrayList<String> speech;
        if (resultcode == RESULT_OK) {
            speech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            resultSpeech = speech.get(0);
            if (requestCode == REQUEST_CODE) {
                //t1.speak(resultSpeech, TextToSpeech.QUEUE_FLUSH, null);
                t1.speak(PhoneNumber, TextToSpeech.QUEUE_FLUSH, null);

                Log.e("ERROR_APP",PhoneNumber+ "aaa");

                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                int rawContactInsertIndex = ops.size();

                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, PhoneNumber) // Name of the person
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, resultSpeech) // Name of the person
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) // Name of the person
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, resultSpeech) // Name of the person
                        .build());

                try {
                    ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                t1.speak("Contact added successfully", TextToSpeech.QUEUE_FLUSH, null);

            } else if (requestCode == 2) {
                t1.speak("Contact added successfully", TextToSpeech.QUEUE_FLUSH, null);
            } else if (requestCode == 3) {
                textmsg.setText(textmsg.getText() + " " + resultSpeech);
            } else if(requestCode == 9001){
                k.onActivityResult(requestCode, resultcode, intent);
            }
        }
    }

    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
    LinearLayout popup_option_layout;
    public void PhoneCall() {

        LayoutInflater layoutInflater = (LayoutInflater) CallActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_option, null);

        popup_option_layout = (LinearLayout) customView.findViewById(R.id.linearLayout);
        popup_option_layout.setOnTouchListener(new OnSwipeTouchListener(CallActivity.this) {
            @Override
            public void onSwipeLeft() {
                popupWindow.dismiss();
            }

            @Override
            public void onSwipeTop() {
                phoneIntent.setData(Uri.parse("tel:" + PhoneNumber));
                startActivity(phoneIntent);
            }

            @Override
            public void onSwipeRight() {
                t1.speak("Opening new message window", TextToSpeech.QUEUE_FLUSH, null);
                OpenMessage();
            }

            @Override
            public void onSwipeBottom() {
                phoneIntent.setData(Uri.parse("tel:" + PhoneNumber));
                startActivity(phoneIntent);
            }
        });

        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
    }


    ImageView MsgImageView;
    String msgContent;
    PopupWindow MsgWindow;
    public void OpenMessage(){
        LayoutInflater layoutInflater = (LayoutInflater) CallActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_message,null);
        MsgImageView = (ImageView) customView.findViewById(R.id.MsgImageView);
        MsgWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        MsgWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
        MsgImageView.setOnTouchListener(new OnSwipeTouchListener(CallActivity.this) {
            @Override
            public void onSwipeLeft() {
                t1.speak("Call OR Message", TextToSpeech.QUEUE_FLUSH, null);
                MsgWindow.dismiss();
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Opening keyboard for compose message", TextToSpeech.QUEUE_FLUSH, null);
                keyboardwindow = k.MyKeyBoards(CallActivity.this,t1,relativeLayout,null,pref,9001);
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
                onBackPressed();
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
