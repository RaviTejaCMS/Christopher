package com.iolink.test.samplepost;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iolink.test.samplepost.Services.MyKeyBoard;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    ImageView imageView;
    Uri cameraImageUri;
    private static int REQUEST_CODE = 1;
    TextToSpeech t1;

    public static String mCurrentPhotoPath;

    Button BtnBlue, BtnWhatsapp, BtnClosex;

    PopupWindow popupWindow,keyboard;

    RelativeLayout relativeLayout;

    private static final int CAMERA_REQUEST = 1888;

    SharedPreferences pref;
    MyKeyBoard k;
    PopupWindow keyboardwindow;

    File ShareFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        k = new MyKeyBoard();
        pref = getApplicationContext().getSharedPreferences("KeyBoard", 0);

        AndroidRuntimePermission();

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        Intent intentv = getIntent();
        String path = intentv.getStringExtra("Path");

        imageView = (ImageView) findViewById(R.id.imageView3);

        imageView.setOnTouchListener(new OnSwipeTouchListener(CameraActivity.this) {
            @Override
            public void onSwipeTop() {
                ShareImage();
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the name of photo");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        startActivityForResult(intent, 1);

        /*if(path.equals("")) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            t1.speak("Enter the name of photo", TextToSpeech.QUEUE_FLUSH, null);
                            keyboardwindow = k.MyKeyBoards(CameraActivity.this,t1,relativeLayout,null,pref);
                            keyboardwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    String myText = pref.getString("mytext", null);
                                    cameraImageUri = getOutputMediaFileUri(1,myText,getApplicationContext());
                                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                                    //cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivityForResult(cameraIntent, 2);
                                }
                            });

                        }
                    }, 500);
        }else {
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(myBitmap);
        }*/
    }

    public static final int RUNTIME_PERMISSION_CODE = 7;
    public void AndroidRuntimePermission(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(CameraActivity.this);
                    alert_builder.setMessage("Camera Permission is Required.");
                    alert_builder.setTitle("Please Grant Permission.");
                    alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.CAMERA},RUNTIME_PERMISSION_CODE);
                        }
                    });
                    alert_builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = alert_builder.create();
                    dialog.show();
                }
                else {
                    ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.CAMERA},RUNTIME_PERMISSION_CODE);
                }
            }else {

            }
        }
    }

    public void OpenSharePopup(){
        LayoutInflater layoutInflater = (LayoutInflater) CameraActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_share, null);

        BtnBlue = (Button) customView.findViewById(R.id.BtnBluetooth);
        BtnWhatsapp = (Button) customView.findViewById(R.id.BtnWhatsApp);
        BtnClosex = (Button) customView.findViewById(R.id.BtnClose);

        BtnClosex.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popupWindow.dismiss();
                return false;
            }
        });

        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
    }

    private static Uri getOutputMediaFileUri(int type, String filename,Context context) {
        //return Uri.fromFile(getOutputMediaFile(type,filename));
        return FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile(type,filename));

    }

    private static File getOutputMediaFile(int type, String filename) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + filename + ".jpg");
        } else {
            return null;
        }
        mCurrentPhotoPath = "file:" + mediaFile.getAbsolutePath();
        return mediaFile;
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

                String myText = resultSpeech;
                cameraImageUri = getOutputMediaFileUri(1,myText,getApplicationContext());
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                //cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(cameraIntent, 2);
            }else if(requestCode == 2){

                Uri imageUri = Uri.parse(mCurrentPhotoPath);
                File file = new File(imageUri.getPath());
                try {
                    InputStream ims = new FileInputStream(file);
                    imageView.setImageBitmap(BitmapFactory.decodeStream(ims));
                    SaveImage(file);
                } catch (FileNotFoundException e) {
                    return;
                }

                /*Log.e("PHOTO_PATH",cameraImageUri.getPath());
                Bitmap myBitmap = BitmapFactory.decodeFile("file:" + cameraImageUri.getPath());
                imageView.setImageBitmap(myBitmap);
                //*/
                t1.speak("Image Saved", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private void SaveImage(File ofile) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String fname = ofile.getName();
        File file = new File(myDir, fname);
        ShareFile = file;
        if (file.exists())
            file.delete();
        try {
            FileInputStream in = new FileInputStream(ofile);
            FileOutputStream out = new FileOutputStream(file);byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            ofile.delete();
        } catch (Exception e) {
            Log.e("APP_ERROR",e.toString());
            e.printStackTrace();
        }
        addImageGallery(file);
    }

    private void addImageGallery( File file ) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // or image/png
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public  void ShareImage(){
        try {

            Uri uri = Uri.parse("file://" + ShareFile.getAbsolutePath());

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            Intent new_intent = Intent.createChooser(shareIntent, "Share via");
            new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(new_intent);

        }catch (Exception ex){
            Log.e("SHARE_ERROR",ex.toString());
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
