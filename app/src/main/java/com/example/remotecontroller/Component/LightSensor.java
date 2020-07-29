package com.example.remotecontroller.Component;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.ImageView;

public class LightSensor implements SensorEventListener {

    final static String TAG ="Light Sensor";
    private SensorManager sensorManager;
    private Sensor mPressure;
    private ImageView mask;
    private float theshold =300.0f;
    private boolean isEnable =true;
    public LightSensor(SensorManager sensorManager, ImageView mask)
    {
        this.sensorManager =sensorManager;
        this.mask=mask;
        mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        registerListener();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!isEnable) {
            mask.setAlpha(0.0f);
            return;
        }
        float lightValue = sensorEvent.values[0];
        Log.i(TAG,""+sensorEvent.values[0]);

        if (lightValue >theshold)
        {
            mask.setAlpha(0.0f);
        }
        else
        {
            mask.setAlpha((theshold-lightValue)/theshold);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void registerListener ()
    {
        sensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void unregisterListener()
    {

        sensorManager.unregisterListener(this);
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
