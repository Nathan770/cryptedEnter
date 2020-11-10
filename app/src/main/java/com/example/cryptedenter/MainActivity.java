package com.example.cryptedenter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.renderscript.Element;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "nathan";

    private TextView main_LBL_steps;
    private MaterialButton main_BTN_vibration;
    private MaterialButton main_BTN_enter;
    private EditText main_EDT_input;


    private SensorManager sensorManager;
    private Boolean running = false;
    private String step = "0";

    private int i = 0;
    private int random = 0;

    private int levelBattery = 0;
    private int currentHour = 0;
    private int currentMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        askForPermission();
        getTime();
        vibrationAction();
        checkEnter();
    }

    private void checkEnter() {
        // check if the input is good
        main_BTN_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String check = main_EDT_input.getText().toString();

                if(check.equals(String.valueOf(currentHour)+String.valueOf(currentMinute)+String.valueOf(levelBattery)+String.valueOf(random+1))){
                    if(Float.parseFloat(step.toString()) >= 30.0){
                       toast("Success enter");
                       goToSuccess();
                    }else {
                        toast("please make 30 step or more");
                    }

                }else {
                    toast("NO success");
                }
            }
        });
    }

    private void goToSuccess() {
        // go to other activity if success
        Intent goToNextActivity = new Intent(getApplicationContext(), MainActivity2.class);
        startActivity(goToNextActivity);
    }

    private void toast(String succes_enter) {
        Toast.makeText(this, succes_enter, Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Receiver for battery level
            levelBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };

    private void vibrationAction() {
        //do vibration like the random number
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        main_BTN_vibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 0;
                int rand = new Random().nextInt(3) + 1;
                random = rand;
                Thread thread;
                thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (i != rand) {
                                Thread.sleep(1000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        doVibration(vibrator);
                                        changeCount();
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };

                thread.start();
            }
        });
    }

    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        //getBattery
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void changeCount() {
        this.i = (this.i + 1);
    }


    private void doVibration(Vibrator vibrator) {

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(500);
        }

    }


    private void getTime() {
        // update time every second
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar rightNow = Calendar.getInstance();
                                currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
                                currentMinute = rightNow.get(Calendar.MINUTE);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();

    }

    private void findViews() {
        main_LBL_steps = findViewById(R.id.main_LBL_steps);
        main_BTN_vibration = findViewById(R.id.main_BTN_vibration);
        main_BTN_enter = findViewById(R.id.main_BTN_enter);
        main_EDT_input = findViewById(R.id.main_EDT_input);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //count your number step
        if (running) {
            step = String.valueOf(event.values[0]);
            main_LBL_steps.setText("Your step number is : " + step);
        } else {
            event.values[0] = (float) 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}