package com.seu.jason.recorderspy.ui;

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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.seu.jason.recorderspy.R;
import com.seu.jason.recorderspy.service.RecoredService;
import com.seu.jason.recorderspy.util.OptMsg;

public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";

    Messenger mainActivityMessenger = new Messenger(new ActivityMsgHandler());
    //界面控件
    private Button btnRecord;
    private Button btnScheduleRecord;
    private Button btnRecordList;
    private Button btnSettings;
    private Button btnHide;
    private ServiceConnection mSc;              //用于连接Service
    private IBinder serviceBinder;               //绑定后得到的service端的binder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initVar();
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        Intent serviceIntent = new Intent(this.getApplicationContext(), RecoredService.class);
        this.bindService(serviceIntent, mSc, Context.BIND_AUTO_CREATE);

//        th.start();
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

    //设置界面元素
    private void findView() {
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnScheduleRecord = (Button) findViewById(R.id.btnScheduleRecord);
        btnRecordList = (Button) findViewById(R.id.btnRecordList);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnHide = (Button) findViewById(R.id.btnHide);

        btnRecord.setOnClickListener(new BtnListener());
        btnScheduleRecord.setOnClickListener(new BtnListener());
        btnRecordList.setOnClickListener(new BtnListener());
        btnSettings.setOnClickListener(new BtnListener());
        btnHide.setOnClickListener(new BtnListener());
    }

    //初始化变量
    private void initVar() {
        Intent serviceIntent = new Intent(this.getApplicationContext(), RecoredService.class);
        startService(serviceIntent);
        mSc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceBinder = service;
                Log.d(LOG_TAG, "service connected");
                sendOptMsg(OptMsg.MSG_REQ_CHECK_STATE);       //checkState();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "service disconnected");
                serviceBinder = null;
            }
        };
    }

    private void sendOptMsg(int optCode) {
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
            e.printStackTrace();
        }
    }

    class BtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnRecord) {
                sendOptMsg(OptMsg.MSG_REQ_RECORD_TRIGGER);
            } else if (v == btnScheduleRecord) {

            } else if (v == btnRecordList) {
                Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
                startActivity(intent);
            } else if (v == btnSettings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (v == btnHide) {
                finish();
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
            Bundle b = msg.getData();
            if (b == null)
                return;

            if (b.getBoolean("isRecording")) {
                btnRecord.setText(R.string.btnStopRecordStr);
            }

            if (b.getBoolean("isPlaying")) {

            }
        }
    }


}
