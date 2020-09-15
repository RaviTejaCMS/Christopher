package com.iolink.test.samplepost;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.iolink.test.samplepost.Adapter.PagerAdapter;

import java.util.Locale;

public class TabActivity extends AppCompatActivity {

    TextToSpeech t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);


        t2=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t2.setLanguage(Locale.UK);
                }
            }
        });

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        t2.speak("Call, Message, Contact, E-Mail", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }, 500);


        Log.e("HERE_COME","act");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.getSupportActionBar().hide();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Phone"));
        tabLayout.addTab(tabLayout.newTab().setText("Media"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.addTab(tabLayout.newTab().setText("Extra"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));



        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    t2.speak("Call, Message, Contact, E-Mail", TextToSpeech.QUEUE_FLUSH, null);
                }else if(tab.getPosition() == 1){
                    t2.speak("Camera, Gallery, Music, Whatsapp", TextToSpeech.QUEUE_FLUSH, null);
                }else if(tab.getPosition() == 2){
                    t2.speak("Internet, Shopping, Book, Help", TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    t2.speak("Settings, Date, Time, Battery", TextToSpeech.QUEUE_FLUSH, null);
                }
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
