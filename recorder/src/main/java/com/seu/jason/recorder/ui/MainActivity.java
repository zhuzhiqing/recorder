package com.seu.jason.recorder.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.seu.jason.recorder.R;
import com.seu.jason.recorder.service.RecordService;
import com.seu.jason.recorder.ui.list.RecordListActivity;
import com.seu.jason.recorder.util.OptMsg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {
    final private String LOG_TAG = "MainActivity";

    //界面控件
    private TextView textViewState;
    private TextView textViewAlarmState;
    private Button btnRecord;
    private Button btnScheduleRecord;
    private Button btnRecordList;
    private Spinner spinner;    //下拉控件
    private ArrayAdapter<String> adapter;
    private List<String> list = new ArrayList<String>();
    private long listMillSecond[] = {
            5 * 60 * 1000,          //list.add("5分钟");
            10 * 60 * 1000,         //list.add("10分钟");
            30 * 60 * 1000,         //list.add("30分钟");
            1 * 60 * 60 * 1000,       //list.add("1小时");
            2 * 60 * 60 * 1000,                    //list.add("2小时");
            5 * 60 * 60 * 1000,                    //list.add("5小时");
            12 * 60 * 60 * 1000,                    //list.add("12小时");
            24 * 60 * 60 * 1000,                   //list.add("24小时");
    };

    //Service通信
    private ServiceConnection mSc;              //用于连接Service
    private IBinder serviceBinder;               //绑定后得到的service端的binder
    Messenger mainActivityMessenger;
    ActivityMsgHandler activityMsgHandler;

    //状态变量
    private boolean mIsRecording;
    private boolean mIsBackgroundRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVar();
        findView();
        activityMsgHandler = new ActivityMsgHandler();
        mainActivityMessenger = new Messenger(activityMsgHandler);

    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        Intent serviceIntent = new Intent(this.getApplicationContext(), RecordService.class);
//        Bundle b = new Bundle();
//        b.putParcelable("messenger", mainActivityMessenger);
//        serviceIntent.putExtras(b);
        this.bindService(serviceIntent, mSc, Context.BIND_AUTO_CREATE);
        sendOptMsg(OptMsg.MSG_REQ_CHECK_STATE);

        Date date = new Date();
        Date date2 = new Date();
        date2.setDate(19);
        date2.setHours(22);
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

    public void sendOptMsg(int optCode) {
        Log.d(LOG_TAG, "sendOptMsg()");
        if (serviceBinder == null)
            return;

        Messenger messenger = new Messenger(serviceBinder);
        Message msg = new Message();
        msg.what = optCode;
        msg.replyTo = mainActivityMessenger;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "sendOptMsg() error!");
            e.printStackTrace();
        }
    }

    public void sendOptMsgTimeInterval(long interval) {
        Log.d(LOG_TAG, "sendOptMsgTimeInterval()");
        if (serviceBinder == null)
            return;

        Messenger messenger = new Messenger(serviceBinder);
        Message msg = new Message();
        msg.what = OptMsg.MSG_REQ_SET_TIME_INTERVAL;
        Bundle b = new Bundle();
        b.putLong("interval", interval);
        msg.setData(b);
        msg.replyTo = mainActivityMessenger;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "sendOptMsg() error!");
            e.printStackTrace();
        }
    }

    public void triggerBackgroundRecord() {
        // Log.d(LOG_TAG, "triggerBackgroundRecord()");
        if (!mIsBackgroundRecording) {
            Log.d(LOG_TAG, "startbackground()");
            sendOptMsg(OptMsg.MSG_REQ_BACKGROUND_START);
        } else {
            Log.d(LOG_TAG, "stopbackground()");
            sendOptMsg(OptMsg.MSG_REQ_BACKGROUND_STOP);
        }
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btnRecord) {
                sendOptMsg(OptMsg.MSG_REQ_RECORD_TRIGGER);
            } else if (v == btnScheduleRecord) {
                triggerBackgroundRecord();
            } else if (v == btnRecordList) {
                Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
                startActivity(intent);
            }
        }
    };

    private void findView() {
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnScheduleRecord = (Button) findViewById(R.id.btnScheduleRecord);
        btnRecordList = (Button) findViewById(R.id.btnRecordList);
        textViewState = (TextView) findViewById(R.id.textViewState);
        textViewAlarmState = (TextView) findViewById(R.id.textViewAlarmState);

        btnRecord.setOnClickListener(btnListener);
        btnScheduleRecord.setOnClickListener(btnListener);
        btnRecordList.setOnClickListener(btnListener);
        spinner = (Spinner) findViewById(R.id.spinnerInterval);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.setVisibility(View.VISIBLE);
                Log.d(LOG_TAG, String.valueOf(id));
                //    sendOptMsgTimeInterval(listMillSecond[(int)id]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    class ActivityMsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case OptMsg.MSG_RST_CHECK_STATE:
                    handleCheckState(msg);
                    break;
                default:
                    ;
            }
        }

        private void handleCheckState(Message msg) {
            Log.d(LOG_TAG, "handleCheckState()");
            Bundle b = msg.getData();
            mIsRecording = b.getBoolean("mIsRecording", false);
            mIsBackgroundRecording = b.getBoolean("mIsBackgroundRecord", false);
            long interval = b.getLong("interval", 0);

            if (mIsRecording) {
                textViewState.setText("正在录音");
                btnScheduleRecord.setEnabled(false);
                btnRecord.setText(R.string.btnStopRecordStr);
            } else {
                textViewState.setText("未启用");
                btnScheduleRecord.setEnabled(true);
                btnRecord.setText(R.string.btnStartRecordStr);
            }

            if (mIsBackgroundRecording) {
                textViewAlarmState.setText("后台录音中");
                btnRecord.setEnabled(false);
                btnScheduleRecord.setText(R.string.btnScheduledRecordCancelStr);
            } else {
                textViewAlarmState.setText("未启用");
                btnScheduleRecord.setText(R.string.btnScheduledRecordStr);
                btnRecord.setEnabled(true);
            }
        }
    }

    private void initVar() {
        mIsRecording = false;
        mIsBackgroundRecording = false;
        Intent serviceIntent = new Intent(this.getApplicationContext(), RecordService.class);
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

        list.add("5分钟");
        list.add("10分钟");
        list.add("30分钟");
        list.add("1小时");
        list.add("2小时");
        list.add("5小时");
        list.add("12小时");
        list.add("24小时");
    }
}
