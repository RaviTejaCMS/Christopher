package com.iolink.test.samplepost;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iolink.test.samplepost.Adapter.ContactsAdapter;
import com.iolink.test.samplepost.Adapter.GalleryAdapter;
import com.iolink.test.samplepost.R;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GalleryActivity extends AppCompatActivity {

    ListView list;
    List<String> images = new ArrayList<String>();
    List<String> title = new ArrayList<String>();

    TextToSpeech t1;

    ContentResolver contentResolver;
    Uri uri;
    Cursor cursor;
    Context context;

    RelativeLayout relativeLayout;

    int CurrentImage = 0;

    TextView txtFileName;
    ImageView ImageViewImg;

    public static final int RUNTIME_PERMISSION_CODE = 7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        context = getApplicationContext();

        txtFileName = (TextView) findViewById(R.id.txtFileName);
        ImageViewImg = (ImageView) findViewById(R.id.ImageViewImg);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(GalleryActivity.this){
            @Override
            public void onSwipeBottom() {
                //deleteImage();
            }

            @Override
            public void onSwipeTop() {
                ShareImage();
            }

            @Override
            public void onSwipeRight() {
                CurrentImage--;
                OpenPrevImage();
            }

            @Override
            public void onSwipeLeft() {
                CurrentImage++;
                OpenNextImage();
            }
        });

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        AndroidRuntimePermission();
        GetAllImagesFiles();
        OpenPrevImage();
    }

    public void OpenNextImage(){

        if(CurrentImage > title.size()){
            CurrentImage = title.size();
            t1.speak("This is last image", TextToSpeech.QUEUE_FLUSH, null);
        }

        String titles = title.get(CurrentImage);
        t1.speak(titles, TextToSpeech.QUEUE_FLUSH, null);
        txtFileName.setText(titles);


        String path = images.get(CurrentImage);
        Bitmap myBitmap = BitmapFactory.decodeFile(path);
        ImageViewImg.setImageBitmap(myBitmap);
    }

    public void OpenPrevImage(){
        if(title.size() == 0){
            return;
        }
        if(CurrentImage == -1){
            CurrentImage = 0;
            t1.speak("This is first image", TextToSpeech.QUEUE_FLUSH, null);
        }

        String titles = title.get(CurrentImage);
        t1.speak(titles, TextToSpeech.QUEUE_FLUSH, null);
        txtFileName.setText(titles);

        String path = images.get(CurrentImage);
        Bitmap myBitmap = BitmapFactory.decodeFile(path);
        ImageViewImg.setImageBitmap(myBitmap);
    }

    public  void ShareImage(){
        try {
            String path = images.get(CurrentImage);
            File f = new File(path);
            Uri uri = Uri.parse("file://" + f.getAbsolutePath());

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

    public void deleteImage(){
        String path = images.get(CurrentImage);
        new File(path).getAbsoluteFile().delete();
        t1.speak("Image deleted successfully", TextToSpeech.QUEUE_FLUSH, null);
    }



    public void SayName(int position){
        String imageName = title.get(position);
        t1.speak(imageName, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void OpenImageActivity(int position){
        String path = images.get(position);
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("Path",path);
        startActivity(intent);
    }

    public void AndroidRuntimePermission(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(GalleryActivity.this);
                    alert_builder.setMessage("External Storage Permission is Required.");
                    alert_builder.setTitle("Please Grant Permission.");
                    alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(GalleryActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},RUNTIME_PERMISSION_CODE);
                        }
                    });
                    alert_builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = alert_builder.create();
                    dialog.show();
                }
                else {
                    ActivityCompat.requestPermissions(GalleryActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},RUNTIME_PERMISSION_CODE);
                }
            }else {

            }
        }
    }

    public void GetAllImagesFiles(){

        contentResolver = context.getContentResolver();
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        cursor = contentResolver.query(
                uri, // Uri
                null,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[] {"%saved_images%"},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " Desc"
        );

        if (cursor == null) {
            Toast.makeText(GalleryActivity.this,"Something Went Wrong.", Toast.LENGTH_LONG);

        } else if (!cursor.moveToFirst()) {

            Toast.makeText(GalleryActivity.this,"No Images Found on SD Card.", Toast.LENGTH_LONG);
        }
        else {
            int Title = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
            int song_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                String SongTitle = cursor.getString(Title);
                title.add(SongTitle);

                String SongPath = cursor.getString(song_index);
                images.add(SongPath);

            } while (cursor.moveToNext());
        }
    }

}
