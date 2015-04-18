package com.seu.jason.recorder.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.seu.jason.recorder.function.RecordFunction;
import com.seu.jason.recorder.util.OptMsg;
import com.seu.jason.recorder.util.UtilHelp;


/**
 * Created by Jason on 2015/4/17.
 */
public class RecordService extends Service {
    private final String LOG_TAG = "RecordService";

    public static final String RECEIVER_CMD = "receiver_cmd";
    public static final int RECEIVER_CMD_BOOT = 1001;
    public static final int RECEIVER_CMD_ALARM_START = 1002;
    public static final int RECEIVER_CMD_ALARM_END = 1003;
    public static final String ALARM_RECORD_START_ACTION = "com.seu.jason.recorderdspy.alarm_record_start_action";
    public static final String ALARM_RECORD_STOP_ACTION = "com.seu.jason.recorderdspy.alarm_record_stop_action";

    RecordFunction recordFunc;
    SharedPreferences mSharedPreferences;   //获取系统定时设置
    Messenger replyTo;
    Messenger messenger;

    //状态
    private boolean mIsRecording = false;
    private boolean mIsInScheduleRecording = false;
    private boolean mIsSetAlarm = false;
    private boolean mIsBackgroundRecord = false;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();
        messenger = new Messenger(serviceHandler);
        recordFunc = RecordFunction.getInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
//        return super.onStartCommand(intent, flags, startId);


        startCheck();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
        messenger = null;
        recordFunc = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind()");
        super.onUnbind(intent);
        replyTo = null;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind()");
        super.onRebind(intent);
    }

    public void sendStateUpdate() {
        Log.d(LOG_TAG, "sendStateUpdate()");

        if (replyTo == null)
            return;
        Message msg = new Message();
        msg.what = OptMsg.MSG_RST_CHECK_STATE;
        Bundle b = new Bundle();
        b.putBoolean("mIsRecording", mIsRecording);
        b.putBoolean("mIsInScheduleRecording", mIsInScheduleRecording);
        b.putBoolean("mIsSetAlarm", mIsSetAlarm);
        b.putBoolean("mIsBackgroundRecord", mIsBackgroundRecord);
        b.putLong("interval", getSharedPreferencesInterval());
        msg.setData(b);

        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 启动正常录音
     */
    public void startNormalRecord() {
        Log.d(LOG_TAG, "startNormalRecord()");

        if (mIsInScheduleRecording) return;          //如果正在后台录音，则不能开始手动录音
        if (mIsRecording) return;                     //如果已经在录音，也不能开始录音
        if (mIsBackgroundRecord) return;

        int result = recordFunc.startRecord(UtilHelp.getTime());    //启动录音机
        if (result == OptMsg.STATE_SUCCESS) {
            mIsRecording = true;
            sendStateUpdate();                          //状态变更
        }
    }

    /**
     * 停止正常录音
     */
    public void stopNormalRecord() {
        Log.d(LOG_TAG, "stopNormalRecord()");

        if (mIsRecording) {
            recordFunc.stopRecord();
            mIsRecording = false;
            sendStateUpdate();
        }
    }

    public void startBackgroundRecord() {
        Log.d(LOG_TAG, "startBackgroundRecord()");

        if (mIsRecording)        //如果正在进行普通录音，则停止普通录音
            startNormalRecord();

        int result = OptMsg.STATE_ERROR_UNKNOW;
        if (!mIsBackgroundRecord) {
            result = recordFunc.startRecord(UtilHelp.getTime());
            if (result == OptMsg.STATE_SUCCESS) {
                mIsBackgroundRecord = true;
                sendStateUpdate();                  //状态变更
                new Thread(new RecordTimerThread()).start();      //启动定时器
            }
        }
    }

    public void stopBackgroundRecord() {
        Log.d(LOG_TAG, "stopBackgroundRecord()");

        if (mIsBackgroundRecord) {
            recordFunc.stopRecord();
            mIsBackgroundRecord = false;
            sendStateUpdate();
        }
    }

    //因为定时器，所以重新启动
    private void timerRestart() {
        Log.d(LOG_TAG, "timerRestart()");
        if (!mIsBackgroundRecord)
            return;
        recordFunc.stopRecord();
        recordFunc.startRecord(UtilHelp.getTime());
    }

    private void startCheck() {
        Log.d(LOG_TAG, "startCheck()");
        if (!getSharedPreferencesBackgroundRecord())        //是否设置了后台录音
            return;

        if (!mIsBackgroundRecord) {
            startBackgroundRecord();
        }
    }

    private void setBackgroundRecord() {
        setSharedPreferencesBackgroundRecord(true);
        startBackgroundRecord();
    }

    private void cancelBackgroundRecord() {
        setSharedPreferencesBackgroundRecord(false);
        stopBackgroundRecord();
    }

    Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OptMsg.MSG_REQ_RECORD_TRIGGER:
                    if (!mIsBackgroundRecord) {               //没有启动后台录音
                        if (mIsRecording)
                            stopNormalRecord();
                        else
                            startNormalRecord();
                    }
                    break;
                case OptMsg.MSG_REQ_BACKGROUND_START:       //启动后台录音
                    setBackgroundRecord();
                    break;
                case OptMsg.MSG_REQ_BACKGROUND_STOP:        //停止后台录音
                    stopBackgroundRecord();
                    break;
                case OptMsg.MSG_REQ_SCHEDULE_RECORD_START:
                    break;
                case OptMsg.MSG_REQ_SCHEDULE_RECORD_STOP:
                    break;
                case OptMsg.MSG_INTERVAL_UP:            //定时器到期
                    timerRestart();
                    break;
                case OptMsg.MSG_REQ_CHECK_STATE:
                    replyTo = (Messenger) msg.replyTo;
                    sendStateUpdate();
                    break;
                case OptMsg.MSG_REQ_SET_TIME_INTERVAL:
                    setSharedPreferencesInterval(msg.getData().getLong("interval"));
                    break;
                default:
                    ;

            }
            super.handleMessage(msg);
        }
    };

    public class RecordTimerThread implements Runnable {
        @Override
        public void run() {
            Log.d(LOG_TAG, "启动定时器");
            while (mIsBackgroundRecord || mIsInScheduleRecording) {        //
                try {
                    // Thread.sleep(30*60*1000);      //线程暂停，但是是毫秒
                    Thread.sleep(getSharedPreferencesInterval());      //线程暂停，但是是毫秒
                    Message message = new Message();
                    message.what = OptMsg.MSG_INTERVAL_UP;
                    serviceHandler.sendMessage(message);
                    Log.d(LOG_TAG, "定时时间到");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public long getSharedPreferenceAlarmStartTime() {
        return mSharedPreferences.getLong("alarmStartTime", 0);
    }

    public long getSharedPreferenceAlarmEndTime() {
        return mSharedPreferences.getLong("alarmEndTime", 0);
    }

    public boolean getSharedPreferenceAlarmEnable() {
        return mSharedPreferences.getBoolean("isAlarmEnable", false);
    }

    public boolean getSharedPreferencesBackgroundRecord() {
        return mSharedPreferences.getBoolean("isBackgroundRecordEnable", false);
    }

    public long getSharedPreferencesInterval() {
        return mSharedPreferences.getLong("interval", 0);
    }

    public void setSharedPreferenceAlarmStartTime(long date) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong("alarmStartTime", date);
        mEditor.commit();
    }

    public void setSharedPreferenceAlarmEndTime(long date) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong("alarmEndTime", date);
        mEditor.commit();
    }

    public void setSharedPreferencesAlarmEnable(boolean enable) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("isAlarmEnable", enable);
        mEditor.commit();
    }

    public void setSharedPreferencesBackgroundRecord(boolean enable) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("isBackgroundRecordEnable", enable);
        mEditor.commit();
    }

    public void setSharedPreferencesInterval(long interval) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong("interval", interval);
        mEditor.commit();
    }


}
