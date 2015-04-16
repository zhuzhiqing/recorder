package com.seu.jason.recorderspy.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.seu.jason.recorderspy.R;
import com.seu.jason.recorderspy.service.RecoredService;
import com.seu.jason.recorderspy.ui.alarm.DateTimePickDialogUtil;
import com.seu.jason.recorderspy.util.OptMsg;
import com.seu.jason.recorderspy.util.UtilHelp;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";

    Messenger mainActivityMessenger;// = new Messenger(new ActivityMsgHandler());
    SharedPreferences mSharedPreferences;
    boolean mIsRecording;
    boolean mIsPlaying;
    boolean mIsInScheduleRecording;
    boolean mIsSetAlarm;
    //界面控件
    private Button btnRecord;
    private Button btnScheduleRecord;
    private Button btnRecordList;
    private Button btnSettings;
    private Button btnStartTime;
    private Button btnEndTime;
    private TextView textViewStartTime;
    private TextView textViewEndTime;
    private TextView textViewState;
    private TextView textViewAlarmState;
    private ServiceConnection mSc;              //用于连接Service
    private IBinder serviceBinder;               //绑定后得到的service端的binder
    ActivityMsgHandler activityMsgHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initVar();
        activityMsgHandler = new ActivityMsgHandler();
        mainActivityMessenger = new Messenger(activityMsgHandler);
    }


    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        Intent serviceIntent = new Intent(this.getApplicationContext(), RecoredService.class);
        Bundle b = new Bundle();
        b.putParcelable("messenger", mainActivityMessenger);
        serviceIntent.putExtras(b);
        this.bindService(serviceIntent, mSc, Context.BIND_AUTO_CREATE);

        Date date = new Date();
        Date date2 = new Date();
        date2.setDate(18);
        date2.setHours(0);
        date2.setSeconds(0);
        if (date.after(date2)) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();
        //此处必需要解绑，否则造成内存泄露
        this.unbindService(mSc);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    BtnListener btnListener = new BtnListener();
    //设置界面元素
    private void findView() {
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnScheduleRecord = (Button) findViewById(R.id.btnScheduleRecord);
        btnRecordList = (Button) findViewById(R.id.btnRecordList);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnStartTime = (Button) findViewById(R.id.btnStartTime);
        btnEndTime = (Button) findViewById(R.id.btnEndTime);
        textViewEndTime = (TextView) findViewById(R.id.textViewEndTime);
        textViewStartTime = (TextView) findViewById(R.id.textViewStartTime);
        textViewState = (TextView) findViewById(R.id.textViewState);
        textViewAlarmState = (TextView) findViewById(R.id.textViewAlarmState);

        btnRecord.setOnClickListener(btnListener);
        btnScheduleRecord.setOnClickListener(btnListener);
        btnRecordList.setOnClickListener(btnListener);
        btnSettings.setOnClickListener(btnListener);
        btnStartTime.setOnClickListener(btnListener);
        btnEndTime.setOnClickListener(btnListener);
    }

    public void setTextViewState(String str) {
        textViewState.setText(str);
    }

    public void setTextViewAlarmState(String str) {
        textViewAlarmState.setText(str);
    }

    //初始化变量
    private void initVar() {
        Intent serviceIntent = new Intent(this.getApplicationContext(), RecoredService.class);
        startService(serviceIntent);
        mSc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(LOG_TAG, "service connected");
                serviceBinder = service;
                sendOptMsg(OptMsg.MSG_REQ_CHECK_STATE);       //checkState();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "service disconnected");
                serviceBinder = null;
            }
        };

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mIsRecording = false;
        mIsPlaying = false;
        mIsInScheduleRecording = false;
        mIsSetAlarm = false;
    }

    public void sendOptMsg(int optCode) {
        Log.d(LOG_TAG, "sendOptMsg()");
        if (serviceBinder == null)
            return;

        Messenger messenger = new Messenger(serviceBinder);
        Message msg = new Message();
        msg.what = optCode;
        msg.replyTo = mainActivityMessenger;
        //如果设定闹钟，则需要传递两个时间
        if (optCode == OptMsg.MSG_REQ_SET_ALARM) {
            Bundle b = new Bundle();
            b.putLong("starttime", getSharedPreferenceAlarmStartTime());
            b.putLong("endtime", getSharedPreferenceAlarmEndTime());
            msg.setData(b);
        }
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "sendOptMsg() error!");
            e.printStackTrace();
        }
    }

    private void setAlarmRecordTime() {
        Log.d(LOG_TAG, "setAlarmRecordTime()");

        if (mIsInScheduleRecording) {     //正在定时录音中，此时按钮显示停止定时录音，按下表示停止定时录音
            sendOptMsg(OptMsg.MSG_REQ_SCHEDULE_RECORD_STOP);
        } else if (mIsSetAlarm) {          //已经设定定时，此时按钮显示删除定时，按下表示删除定时
            //发送停止定时
            sendOptMsg(OptMsg.MSG_REQ_CANCLE_ALARM);

        } else {
            Date now = new Date();
            Calendar calendarStartTime = DateTimePickDialogUtil.getCalendarByInintData(textViewStartTime.getText().toString());
//            calendarStartTime.set();
            long startTime = calendarStartTime.getTimeInMillis();
            Calendar calendarEndTime = DateTimePickDialogUtil.getCalendarByInintData(textViewEndTime.getText().toString());
            long endTime = calendarEndTime.getTimeInMillis();

            if (startTime < now.getTime()) {
                textViewAlarmState.setText("开始时间设置错误");
                return;
            }
            if (endTime < startTime) {
                textViewAlarmState.setText("结束时间设置错误");
                return;
            }

            setSharedPreferenceAlarmStartTime(startTime);
            setSharedPreferenceAlarmEndTime(endTime);
            sendOptMsg(OptMsg.MSG_REQ_SET_ALARM);
        }
    }

    private void setAlarmStartTime() {
        DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
                MainActivity.this, UtilHelp.getTimeInChinese());
        dateTimePicKDialog.dateTimePicKDialog(textViewStartTime);
    }

    private void setAlarmEndTime() {
        DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
                MainActivity.this, UtilHelp.getTimeInChinese());
        dateTimePicKDialog.dateTimePicKDialog(textViewEndTime);
    }

    public long getSharedPreferenceAlarmStartTime() {
        return mSharedPreferences.getLong("alarmStartTime", 0);
    }

    public long getSharedPreferenceAlarmEndTime() {
        return mSharedPreferences.getLong("alarmEndTime", 0);
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


    class BtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnRecord) {
                sendOptMsg(OptMsg.MSG_REQ_RECORD_TRIGGER);
            } else if (v == btnScheduleRecord) {
                setAlarmRecordTime();
            } else if (v == btnRecordList) {
                Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
                startActivity(intent);
            } else if (v == btnSettings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (v == btnStartTime) {
                setAlarmStartTime();
            } else if (v == btnEndTime) {
                setAlarmEndTime();
            }
        }
    }

    class ActivityMsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            handleSpecificMsg(msg);
        }

        private void handleSpecificMsg(Message msg) {
            switch (msg.what) {
                case OptMsg.MSG_STATE_RECORDING:
                    btnRecord.setText(R.string.btnStopRecordStr);
                    break;
                case OptMsg.MSG_STATE_NOT_RECORDING:
                    btnRecord.setText(R.string.btnStartRecordStr);
                    break;
                case OptMsg.MSG_RST_CHECK_STATE:            //checkState返回结果
                    handleCheckState(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private void handleCheckState(Message msg) {
            Log.d(LOG_TAG, "handleCheckState()--");
            Bundle b = msg.getData();
            if (b == null) {
                Log.d(LOG_TAG, "handleCheckState() no extra data!");
                return;
            }

            mIsRecording = b.getBoolean("mIsRecording");
            mIsInScheduleRecording = b.getBoolean("mIsInScheduleRecording");
            mIsSetAlarm = b.getBoolean("mIsSetAlarm");

            setSharedPreferenceAlarmStartTime(b.getLong("starttime"));
            setSharedPreferenceAlarmEndTime(b.getLong("endtime"));

            textViewStartTime.setText(UtilHelp.getTimeInChinese(getSharedPreferenceAlarmStartTime()));
            textViewEndTime.setText(UtilHelp.getTimeInChinese(getSharedPreferenceAlarmEndTime()));

            if (mIsRecording) {
                btnScheduleRecord.setEnabled(false);
                btnEndTime.setEnabled(false);
                btnStartTime.setEnabled(false);
                btnRecord.setText(R.string.btnStopRecordStr);
                textViewState.setText("录音中");
            } else {
                btnScheduleRecord.setEnabled(true);
                btnEndTime.setEnabled(true);
                btnStartTime.setEnabled(true);
                btnRecord.setText(R.string.btnStartRecordStr);
                textViewState.setText("未录音");
            }

            if (mIsInScheduleRecording) {
                btnRecord.setEnabled(false);                //禁用录音键
                btnEndTime.setEnabled(false);
                btnStartTime.setEnabled(false);
                btnScheduleRecord.setText(R.string.btnStopRecordStr);   //按钮显示结束定时录音
                textViewAlarmState.setText(R.string.btnScheduledRecordInRecordingStr);
            } else {
                btnRecord.setEnabled(true);                //启用录音键
                if (mIsSetAlarm) {        //已设定定时录音
                    btnScheduleRecord.setText(R.string.btnScheduledRecordCancelStr);//删除定时
                    btnStartTime.setEnabled(false);
                    btnEndTime.setEnabled(false);

                    textViewAlarmState.setText("已设定定时");
                } else {
                    btnScheduleRecord.setText(R.string.btnScheduledRecordStr);      //设置定时
                    btnEndTime.setEnabled(true);
                    btnStartTime.setEnabled(true);
                    textViewAlarmState.setText("未设置定时");
                }
            }

            if (mIsInScheduleRecording || mIsRecording) {
                btnRecordList.setEnabled(false);
                btnSettings.setEnabled(false);
            } else {
                btnRecordList.setEnabled(true);
                btnSettings.setEnabled(true);
            }
        }
    }


}
