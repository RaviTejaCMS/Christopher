package com.iolink.test.samplepost;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iolink.test.samplepost.Adapter.RecyclerViewAdapter;
import com.iolink.test.samplepost.Model.DataModel;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreActivity extends AppCompatActivity {

    TextToSpeech t1;
    Context context;
    LinearLayout linearLayout;
    public static final int progress_bar_type = 0;
    ProgressDialog pDialog;
    String TAG = "FIREBASE";

    List<String> TitleList = new ArrayList<String>();
    List<String> PathList = new ArrayList<String>();
    List<String> DescList = new ArrayList<String>();
    List<String> ImgList = new ArrayList<String>();

    public int ListPos = 0;
    public String CurrentApk = "";

    ImageView ApkImage;
    TextView txtBookName;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        mediaPlayer = new MediaPlayer();

        ApkImage = (ImageView) findViewById(R.id.ApkImage);
        txtBookName = (TextView) findViewById(R.id.txtBookName);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(StoreActivity.this){
            @Override
            public void onSwipeLeft() {
                ListPos++;
                OpenAPK();
            }

            @Override
            public void onSwipeRight() {
                ListPos--;
                OpenAPK();
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Opening book, Plese wait", TextToSpeech.QUEUE_FLUSH, null);
                if(CurrentApk.equals("")){
                    t1.speak("No apk found", TextToSpeech.QUEUE_FLUSH, null);
                }else {
                    if(CurrentApk.contains(".mp3")) {
                        try{
                            PlayBook(CurrentApk);
                    }catch(Exception e){
                    }
                    }else {
                        new DownloadFileFromURL().execute();
                    }
                }
            }

            @Override
            public void onSwipeBottom() {
                OpenAPK();
            }
        });

        try {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TitleList = new ArrayList<String>();
                    PathList = new ArrayList<String>();
                    DescList = new ArrayList<String>();
                    ImgList = new ArrayList<String>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String Name = snapshot.child("Name").getValue().toString();
                        String Path = snapshot.child("Path").getValue().toString();
                        String Desc = snapshot.child("Desc").getValue().toString();
                        String Img = snapshot.child("Image").getValue().toString();
                        TitleList.add(Name);
                        PathList.add(Path);
                        DescList.add(Desc);
                        ImgList.add(Img);
                    }
                    ListPos = 0;
                    OpenAPK();

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e(TAG, "Failed to read value.", error.toException());
                }
            });


        }catch (Exception ex){
            Log.e("FIREBASE",ex.toString());
        }

        context = this.getApplicationContext();
    }

    public void PlayBook(String path){
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }

    public void OpenAPK(){
        try {

            if(mediaPlayer != null){
                mediaPlayer.stop();
            }

            Log.e("TAPSIZE",ListPos + "," + PathList.size());
            if (ListPos <= 0) {
                ListPos = 0;
            }

            if (ListPos >= PathList.size()) {
                ListPos = PathList.size() - 1;
            }

            CurrentApk = PathList.get(ListPos);
            String desc = DescList.get(ListPos);
            txtBookName.setText(TitleList.get(ListPos));
            new ImageLoadTask(ImgList.get(ListPos), ApkImage).execute();
            t1.speak("Book Name " + TitleList.get(ListPos) + ", Description " + desc, TextToSpeech.QUEUE_FLUSH, null);
        }catch (Exception ex){
            CurrentApk = "";
            ApkImage.setImageBitmap(null);
            t1.speak("No apk found", TextToSpeech.QUEUE_FLUSH, null);
            ListPos = 0;
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(CurrentApk);
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream("/sdcard/app.apk");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                //Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

            // Displaying downloaded image into image view
            // Reading image path from sdcard
            t1.speak("Downloading Completed", TextToSpeech.QUEUE_FLUSH, null);
            String appPath = Environment.getExternalStorageDirectory().toString() + "/app.apk";
            // setting downloaded into image view


            //my_image.setImageDrawable(Drawable.createFromPath(imagePath));

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(appPath)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // without this flag android returned a intent error!
            startActivity(intent);
        }


    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }
}



