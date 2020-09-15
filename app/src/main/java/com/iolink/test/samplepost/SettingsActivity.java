package com.iolink.test.samplepost;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.iolink.test.samplepost.Adapter.RecyclerViewAdapter;
import com.iolink.test.samplepost.Model.DataModel;
import com.iolink.test.samplepost.Services.OnSwipeTouchListener;
import com.iolink.test.samplepost.Services.ShakeDetector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    TextToSpeech t1;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    Context context;

    ArrayAdapter<String> adapter;

    RelativeLayout relativelayout;
    private boolean flashLightStatus = true;
    ImageView settingimg;

    WifiManager wifiManager;
    BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        settingimg = (ImageView) findViewById(R.id.settingimg);
        settingimg.setOnTouchListener(new OnSwipeTouchListener(SettingsActivity.this){
            @Override
            public void onSwipeBottom() {
                displayLocationSettingsRequest(SettingsActivity.this);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSwipeTop() {
                if(flashLightStatus){
                    t1.speak("Torch is on", TextToSpeech.QUEUE_FLUSH, null);
                    flashLight(1);
                    flashLightStatus = false;
                }else{
                    t1.speak("Torch is off", TextToSpeech.QUEUE_FLUSH, null);
                    flashLight(0);
                    flashLightStatus = true;
                }
            }

            @Override
            public void onSwipeRight() {
                boolean wifiEnabled = wifiManager.isWifiEnabled();
                if(wifiEnabled){
                    wifiManager.setWifiEnabled(false);
                    t1.speak("Wi-Fi Off", TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    wifiManager.setWifiEnabled(true);
                    t1.speak("Wi-Fi On", TextToSpeech.QUEUE_FLUSH, null);
                }
            }

            @Override
            public void onSwipeLeft() {
                if (mBluetoothAdapter.isEnabled()) {
                    t1.speak("Bluetooth Off", TextToSpeech.QUEUE_FLUSH, null);
                    mBluetoothAdapter.disable();
                }else{
                    t1.speak("Bluetooth On", TextToSpeech.QUEUE_FLUSH, null);
                    mBluetoothAdapter.enable();
                }
            }
        });

        relativelayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        context = this.getApplicationContext();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });
    }

    private void handleShakeEvent(int count) {
        t1.speak("Wi-Fi. Torch. Blue Tooth. GPS", TextToSpeech.QUEUE_FLUSH, null);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void flashLight(int i) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if(i == 1) {
                cameraManager.setTorchMode(cameraId, true);
            }else{
                cameraManager.setTorchMode(cameraId, false);
            }
        } catch (Exception e) {
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public static final String TAG = "GPS";
            public static final int REQUEST_CHECK_SETTINGS = 1;

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        t1.speak("GPS is already enabled", TextToSpeech.QUEUE_FLUSH, null);
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            t1.speak("Click ok to enable GPS", TextToSpeech.QUEUE_FLUSH, null);
                            status.startResolutionForResult(SettingsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
}



