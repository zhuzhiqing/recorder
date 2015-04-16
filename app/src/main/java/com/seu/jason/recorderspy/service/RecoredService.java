package com.seu.jason.recorderspy.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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

import com.seu.jason.recorderspy.function.RecordFunc;
import com.seu.jason.recorderspy.util.OptMsg;
import com.seu.jason.recorderspy.util.UtilHelp;

import java.util.Date;

/**
 * Created by Jason on 2015/4/12.
 */
public class RecoredService extends Service {
    public static final String RECEIVER_CMD = "receiver_cmd";
    public static final int RECEIVER_CMD_BOOT = 1001;
    public static final int RECEIVER_CMD_ALARM_START = 1002;
    public static final int RECEIVER_CMD_ALARM_END = 1003;
    public static final String ALARM_RECORD_START_ACTION = "com.seu.jason.recorderdspy.alarm_record_start_action";
    public static final String ALARM_RECORD_STOP_ACTION = "com.seu.jason.recorderdspy.alarm_record_stop_action";
    private final String LOG_TAG = "RecordService";
    ;
    RecordFunc recordFunc = RecordFunc.getInstance();
    SharedPreferences mSharedPreferences;   //获取系统定时设置
    Messenger replyTo;
    private boolean mIsRecording = false;
    private boolean mIsPlaying = false;
    private boolean mIsInScheduleRecording = false;
    private boolean mIsSetAlarm = false;

    private void printState() {
//        Log.d(LOG_TAG+"mIsRecording",String.valueOf(mIsRecording));
//        Log.d(LOG_TAG+"mIsInScheduleRecording",String.valueOf(mIsInScheduleRecording));
//        Log.d(LOG_TAG+"mIsSetAlarm",String.valueOf(mIsSetAlarm));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
//        replyTo = (Messenger) intent.getExtras().getParcelable("messenger");
        return messenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind()");
//        replyTo = (Messenger) intent.getExtras().getParcelable("messenger");
//        if(replyTo == null)
//            Log.e(LOG_TAG,"onRebind():replyTo==null");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind()");
        replyTo = null;
        super.onUnbind(intent);
        return true;
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //检查现有定时是否过期
//        Date alarm = new Date(getSharedPreferenceAlarm());
//        if (!alarm.after(new Date())) {
//            setSharedPreferenceAlarm(0);
//            mIsSetAlarm = false;
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
        //return super.onStartCommand(intent, flags, startId);
        Bundle b = intent.getExtras();
        if (b != null) {
            int opt = b.getInt(RECEIVER_CMD);
            if (opt == RECEIVER_CMD_BOOT) {
                bootCheck();
            } else if (opt == RECEIVER_CMD_ALARM_START) {
                startAlarmRecord();
            } else if (opt == RECEIVER_CMD_ALARM_END) {
                stopScheduleRecord(true);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    public void sendStateUpdate() {
        Log.d(LOG_TAG, "sendStateUpdate()");
        if (replyTo != null) {
            Message msg = new Message();
            msg.what = OptMsg.MSG_RST_CHECK_STATE;
            Bundle b = new Bundle();
            b.putBoolean("mIsRecording", mIsRecording);
            b.putBoolean("mIsInScheduleRecording", mIsInScheduleRecording);
            b.putBoolean("mIsSetAlarm", mIsSetAlarm);
            b.putLong("starttime", getSharedPreferenceAlarmStartTime());
            b.putLong("endtime", getSharedPreferenceAlarmEndTime());
            msg.setData(b);
            try {
                Log.e(LOG_TAG, "sendStateUpdate() watch this!");
                replyTo.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "sendStateUpdate() error!");
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "sendStateUpdate():replyTo == null");
        }
    }

    private int startRecord() {
        Log.d(LOG_TAG, "startRecord()");
        int result = OptMsg.MSG_STATE_UNKNOW;
        if (!mIsRecording) {
            result = recordFunc.startRecord(UtilHelp.getTime());
            if (result != OptMsg.MSG_STATE_RECORDING) {
                mIsRecording = false;
            } else {
                mIsRecording = true;
            }
        } else {
            result = OptMsg.MSG_STATE_RECORDING;
        }
        sendStateUpdate();
        printState();
        return result;
    }

    private void stopRecord() {
        Log.d(LOG_TAG, "stopRecord()");
        if (mIsRecording) {
            recordFunc.stopRecord();
        }
        mIsRecording = false;
        sendStateUpdate();
        printState();
    }

    /**
     * 定时录音开始
     * 先关闭普通录音
     */
    private void startAlarmRecord() {
        Log.d(LOG_TAG, "startAlarmRecord()");
        if (mIsRecording) {
            stopRecord();
        }

        int result = OptMsg.MSG_STATE_UNKNOW;
        if (!mIsInScheduleRecording) {
            result = recordFunc.startRecord(UtilHelp.getTime());
            if (result != OptMsg.MSG_STATE_RECORDING) {
                mIsInScheduleRecording = false;
            } else {
                mIsInScheduleRecording = true;
                new Thread(new TimerThread()).start();
            }
        } else {
            result = OptMsg.MSG_STATE_RECORDING;
        }
        sendStateUpdate();
        printState();
    }

    //因为定时器，所以重新启动
    private void alarmRecordTimerRestart() {
        Log.d(LOG_TAG, "alarmRecordTimerRestart()");
        recordFunc.stopRecord();
        recordFunc.startRecord(UtilHelp.getTime());
    }

    private void stopScheduleRecord(boolean isFromBroadcast) {
        Log.d(LOG_TAG, "stopScheduleRecord()");
        if (mIsInScheduleRecording) {
            recordFunc.stopRecord();
        }
        if (!isFromBroadcast) {
            //取消到期定时器
            AlarmManager amEnd = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intentEnd = new Intent(ALARM_RECORD_STOP_ACTION);
            PendingIntent senderEnd = PendingIntent.getBroadcast(
                    this, 1, intentEnd, 0);
            amEnd.cancel(senderEnd);
        }

        setmSharedPreferencesAlarmEnable(false);

        mIsInScheduleRecording = false;
        mIsSetAlarm = false;

        sendStateUpdate();
        printState();
    }

    public long getSharedPreferenceAlarmStartTime() {
        return mSharedPreferences.getLong("alarmStartTime", 0);
    }

    public long getSharedPreferenceAlarmEndTime() {
        return mSharedPreferences.getLong("alarmEndTime", 0);
    }

    public boolean getSharedPreferenceAlarmEnable() {
        return mSharedPreferences.getBoolean("isEnable", false);
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

    public void setmSharedPreferencesAlarmEnable(boolean Enable) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("isEnable", Enable);
        mEditor.commit();
    }

    private void bootCheck() {
        Log.d(LOG_TAG, "bootCheck()");
        if (!getSharedPreferenceAlarmEnable()) {
            return;
        }

        //时间不晚于当前时间，退出
        Date startTime = new Date(getSharedPreferenceAlarmStartTime());
        Date endTime = new Date(getSharedPreferenceAlarmEndTime());
        Date now = new Date();
//        if((!startTime.after(now))||(!endTime.after(startTime))){
        if ((!startTime.after(now)) || (!endTime.after(startTime))) {
            setmSharedPreferencesAlarmEnable(false);
            Log.d(LOG_TAG, "bootCheck()--过期");
            return;
        }

        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_RECORD_START_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(
                this, 0, intent, 0);
        am.set(AlarmManager.RTC, getSharedPreferenceAlarmStartTime(), sender);

        AlarmManager amEnd = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentEnd = new Intent(ALARM_RECORD_STOP_ACTION);
        PendingIntent senderEnd = PendingIntent.getBroadcast(
                this, 1, intentEnd, 0);
        amEnd.set(AlarmManager.RTC, getSharedPreferenceAlarmStartTime(), senderEnd);
        mIsSetAlarm = true;     //设置闹钟标志

        setmSharedPreferencesAlarmEnable(true);
        sendStateUpdate();
        printState();
    }


    private void setAlarm() {
        Log.d(LOG_TAG, "entering setAlarm()");
        //如果已经设定了闹钟，先取消
        if (mIsSetAlarm) {
            cancelAlarm();
        }

        //时间不晚于当前时间，退出
        Date startTime = new Date(getSharedPreferenceAlarmStartTime());
        Date endTime = new Date(getSharedPreferenceAlarmEndTime());
        Date now = new Date();
        if ((!startTime.after(now)) || (!endTime.after(startTime))) {
            setmSharedPreferencesAlarmEnable(false);
            Log.d(LOG_TAG, "setAlarm() time error");
            return;
        }

        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_RECORD_START_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(
                this, 0, intent, 0);
        am.set(AlarmManager.RTC, getSharedPreferenceAlarmStartTime(), sender);

        AlarmManager amEnd = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentEnd = new Intent(ALARM_RECORD_STOP_ACTION);
        PendingIntent senderEnd = PendingIntent.getBroadcast(
                this, 1, intentEnd, 0);
        amEnd.set(AlarmManager.RTC, getSharedPreferenceAlarmEndTime(), senderEnd);
        mIsSetAlarm = true;     //设置闹钟标志

        setmSharedPreferencesAlarmEnable(true);
        sendStateUpdate();
        Log.d(LOG_TAG, "leaving setAlarm()");
        printState();
    }

    private void cancelAlarm() {
        Log.e(LOG_TAG, "cancelAlarm()");
        //如果没有设置闹钟，退出
        if (!mIsSetAlarm)
            return;

        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_RECORD_START_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(
                this, 0, intent, 0);
        am.cancel(sender);

        AlarmManager amEnd = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentEnd = new Intent(ALARM_RECORD_STOP_ACTION);
        PendingIntent senderEnd = PendingIntent.getBroadcast(
                this, 1, intentEnd, 0);
        amEnd.cancel(senderEnd);

        mIsSetAlarm = false;     //设置闹钟标志
        sendStateUpdate();
        printState();
    }

    Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OptMsg.MSG_REQ_RECORD_TRIGGER:
                    handleTriggerRecord((Messenger) msg.replyTo);
                    break;
                case OptMsg.MSG_REQ_CHECK_STATE:
                    handleCheckState((Messenger) msg.replyTo);
                    break;
                case OptMsg.MSG_REQ_SCHEDULE_RECORD_START:
                    handleScheduleRecordStart();
                    break;
                case OptMsg.MSG_REQ_SCHEDULE_RECORD_STOP:
                    handleScheduleRecordStop();
                    break;
                case OptMsg.MSG_REQ_SET_ALARM:
                    handleSetAlarm(msg);
                    break;
                case OptMsg.MSG_REQ_CANCLE_ALARM:
                    handleCancelAlarm();
                    break;
                case OptMsg.MSG_INTERVAL_UP:    //定时时间到
                    handleIntervalUp();
                    break;
                default:
                    ;
            }
            super.handleMessage(msg);
        }

        private void handleTriggerRecord(Messenger replyMessenger) {
            Log.d(LOG_TAG, "handleTriggerRecord()");
            int rstCode = -1;
            if (mIsRecording) {
                stopRecord();
                rstCode = OptMsg.MSG_STATE_NOT_RECORDING;
            } else {
                rstCode = startRecord();
            }
//            if (messenger != null) {
//                Message msg = new Message();
//                msg.what = rstCode;
//                Bundle b = new Bundle();
//                b.putBoolean("mIsRecording", mIsRecording);
//                b.putBoolean("mIsPlaying", mIsPlaying);
//                b.putBoolean("mIsInScheduleRecording", mIsInScheduleRecording);
//                b.putBoolean("mIsSetAlarm", mIsSetAlarm);
//                b.putLong("starttime", getSharedPreferenceAlarmStartTime());
//                b.putLong("endtime",getSharedPreferenceAlarmEndTime());
//                msg.setData(b);
//                try {
//                    Log.e(LOG_TAG,"handleTriggerRecord() watch this!");
//                    replyMessenger.send(msg);
//                } catch (RemoteException e) {
//                    Log.e(LOG_TAG,"handleTriggerRecord() error!");
//                    e.printStackTrace();
//                }
//            }
        }

        private void handleCheckState(Messenger replyMessenger) {
            Log.d(LOG_TAG, "handleCheckState()");
            replyTo = replyMessenger;
            if (messenger != null) {
                Message msg = new Message();
                msg.what = OptMsg.MSG_RST_CHECK_STATE;
                Bundle b = new Bundle();
                b.putBoolean("mIsRecording", mIsRecording);
                b.putBoolean("mIsPlaying", mIsPlaying);
                b.putBoolean("mIsInScheduleRecording", mIsInScheduleRecording);
                b.putBoolean("mIsSetAlarm", mIsSetAlarm);
                b.putLong("starttime", getSharedPreferenceAlarmStartTime());
                b.putLong("endtime", getSharedPreferenceAlarmEndTime());
                msg.setData(b);
                try {
                    Log.e(LOG_TAG, "handleCheckState() watch this!");
                    replyMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "handleCheckState() error!");
                    e.printStackTrace();
                }
            }
        }

        //broadcastReceiver发送过来，开始录音后设置一些状态，并发送给主界面
        private void handleScheduleRecordStart() {
            Log.d(LOG_TAG, "handleScheduleRecordStart()");
            stopRecord();
            startRecord();
            mIsInScheduleRecording = true;
        }

        //设置定时
        private void handleSetAlarm(Message msg) {
            Log.d(LOG_TAG, "handleSetAlarm()");
            Log.d(LOG_TAG + "starttime", (new Date(msg.getData().getLong("starttime", 0))).toLocaleString());
            Log.d(LOG_TAG + "endtime", (new Date(msg.getData().getLong("endtime", 0))).toLocaleString());
            setSharedPreferenceAlarmStartTime(msg.getData().getLong("starttime", 0));
            setSharedPreferenceAlarmEndTime(msg.getData().getLong("endtime", 0));
            setAlarm();
        }

        private void handleCancelAlarm() {
            Log.d(LOG_TAG, "handleCancelAlarm()");
            cancelAlarm();
        }

        private void handleScheduleRecordStop() {
            Log.d(LOG_TAG, "handleScheduleRecordStop()");
            stopScheduleRecord(false);
        }

        private void handleIntervalUp() {
            Log.d(LOG_TAG, "handleIntervalUp()");
            if (mIsInScheduleRecording) {
                alarmRecordTimerRestart();
                printState();
            }
        }
    };

    Messenger messenger = new Messenger(serviceHandler);

    public class TimerThread implements Runnable {
        @Override
        public void run() {
            Log.d(LOG_TAG, "启动定时器");
            while (mIsInScheduleRecording) {        //
                try {
                    // Thread.sleep(30*60*1000);      //线程暂停，但是是毫秒
                    Thread.sleep(15 * 1000);      //线程暂停，但是是毫秒
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



}
