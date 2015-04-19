package com.seu.jason.recorder.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Jason on 2015/4/18.
 */
public class DemoService extends Service {
    final private String LOG_TAG = "DemoService";
    private boolean mIsRunning = false;
    public static final int HEART_BEAT = 1000;
    public static final int BOOT = 1001;

    Object lock = new Object();
    long interval = 0;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();
        mIsRunning = true;
        new Thread(new TimerThread()).start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind()");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        mIsRunning = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int what = bundle.getInt("CMD");
            switch (what) {
                case HEART_BEAT:
                    synchronized (lock) {
                        interval = 0;       //清interval
                    }
                    break;
                case BOOT:
                    break;
                default:
                    ;
            }
        }
        return START_STICKY;//super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return null;
    }


    class TimerThread implements Runnable {
        @Override
        public void run() {
            while (mIsRunning) {
                try {
                    Thread.sleep(1000);
                    synchronized (lock) {
                        if (interval > 30) {        //1分钟没有收到心跳,录音服务出错
                            Log.e(LOG_TAG, "重启服务");
                            interval = 0;
                            Intent intent = new Intent(DemoService.this, RecordService.class);
                            stopService(intent);        //先终止
                            startService(intent);       //再重启
                        }
                        interval++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
