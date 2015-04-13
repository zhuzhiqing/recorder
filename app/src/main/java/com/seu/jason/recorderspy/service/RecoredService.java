package com.seu.jason.recorderspy.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.seu.jason.recorderspy.function.RecordFunc;
import com.seu.jason.recorderspy.util.OptMsg;
import com.seu.jason.recorderspy.util.UtilHelp;

/**
 * Created by Jason on 2015/4/12.
 */
public class RecoredService extends Service {
    private final String LOG_TAG = "RecordService";

    RecordFunc recordFunc = RecordFunc.getInstance();
    private boolean mIsRecording = false;
    private boolean mIsPlaying = false;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG,"onBind()");
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG,"onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG,"onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"onStartCommand()");
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG,"onDestroy()");
        super.onDestroy();
    }

    private int startRecord(){
        Log.d(LOG_TAG,"startRecord()");
        int result = OptMsg.MSG_STATE_UNKNOW;
        if(!mIsRecording){
            result = recordFunc.startRecord(UtilHelp.getTime());
            if(result!= OptMsg.MSG_STATE_RECORDING){
                mIsRecording = false;
            }else{
                mIsRecording = true;
            }
        }else{
            result = OptMsg.MSG_STATE_RECORDING;
        }
        return result;
    }

    private void stopRecord(){
        Log.d(LOG_TAG,"stopRecord()");
        if(mIsRecording){
            recordFunc.stopRecord();
        }
        mIsRecording = false;
    }

    Handler serviceHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case OptMsg.MSG_REQ_RECORD_TRIGGER:
                    handleTriggerRecord((Messenger)msg.replyTo);
                    break;
                default:;
            }
            super.handleMessage(msg);
        }

        private void handleTriggerRecord(Messenger replyMessenger){
            int rstCode = -1;
            if(mIsRecording){
                stopRecord();
                rstCode = OptMsg.MSG_STATE_NOT_RECORDING;
            }else{
                rstCode = startRecord();
            }
            if(messenger!=null){
                Message msg = new Message();
                msg.what = rstCode;
                try {
                    replyMessenger.send(msg);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
    };

    Messenger messenger = new Messenger(serviceHandler);


}
