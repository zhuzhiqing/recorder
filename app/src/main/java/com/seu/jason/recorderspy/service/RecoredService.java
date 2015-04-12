package com.seu.jason.recorderspy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.seu.jason.recorderspy.function.RecordFunc;

/**
 * Created by Jason on 2015/4/12.
 */
public class RecoredService extends Service {
    String Tag = "RecordService";
    RecordFunc recordFunc = RecordFunc.getInstance();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startRecorder(){

    }


}
