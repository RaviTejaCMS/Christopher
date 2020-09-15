package com.iolink.test.samplepost;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;

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

public class ShopActivity extends AppCompatActivity {

    TextToSpeech t1;
    Context context;
    LinearLayout linearLayout;

    List<String> ItemList = new ArrayList<String>();
    List<String> ImgList = new ArrayList<String>();

    public int ListPos = 0;

    ImageView ItemImage;
    TextView txtItemName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        ItemList.add("Head Phone");
        ItemList.add("Shoes");
        ItemList.add("Shirt");
        ItemList.add("Bag");
        ItemList.add("Phone");

        ImgList.add("https://images-na.ssl-images-amazon.com/images/I/61s8OvmioKL._SY355_.jpg");
        ImgList.add("https://atlas-content-cdn.pixelsquid.com/stock-images/nike-running-shoes-Va12nQ5-600.jpg");
        ImgList.add("http://www.pngpix.com/wp-content/uploads/2016/10/PNGPIX-COM-Red-T-Shirt-PNG-Transparent-Image-500x673.png");
        ImgList.add("http://www.pngpix.com/wp-content/uploads/2016/07/PNGPIX-COM-Travel-Bag-PNG-Transparent-Image-1-500x541.png");
        ImgList.add("https://cdn4.iconfinder.com/data/icons/samsung-galaxy-s6-set/512/galaxyS6_BlackSapphire1.png");

        ItemImage = (ImageView) findViewById(R.id.ItemImage);
        txtItemName = (TextView) findViewById(R.id.txtItemName);

        OpenItem();

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(ShopActivity.this){
            @Override
            public void onSwipeLeft() {
                ListPos++;
                OpenItem();
            }

            @Override
            public void onSwipeRight() {
                ListPos--;
                OpenItem();
            }

            @Override
            public void onSwipeTop() {
                t1.speak("Say your price range", TextToSpeech.QUEUE_FLUSH, null);
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Price Range");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                startActivityForResult(intent, 1);
            }

            @Override
            public void onSwipeBottom() {
                OpenItem();
            }
        });
        context = this.getApplicationContext();
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
                t1.speak("Your order has been placed", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void OpenItem(){
        try {
            if (ListPos <= 0) {
                ListPos = 0;
            }

            if (ListPos >= ItemList.size()) {
                ListPos = ItemList.size() - 1;
            }
            txtItemName.setText(ItemList.get(ListPos));
            new ImageLoadTask(ImgList.get(ListPos), ItemImage).execute();
            t1.speak("Item Name " + ItemList.get(ListPos), TextToSpeech.QUEUE_FLUSH, null);
        }catch (Exception ex){
            txtItemName.setText("");
            ItemImage.setImageBitmap(null);
            t1.speak("No item found", TextToSpeech.QUEUE_FLUSH, null);
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
}



