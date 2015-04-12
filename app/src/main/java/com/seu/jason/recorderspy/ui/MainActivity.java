package com.seu.jason.recorderspy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.seu.jason.recorderspy.R;
import com.seu.jason.recorderspy.function.RecordFunc;
import com.seu.jason.recorderspy.util.ErrorCode;
import com.seu.jason.recorderspy.util.UtilHelp;



public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";

    //界面控件
    private Button btnRecord;
    private Button btnScheduleRecord;
    private Button btnRecordList;
    private Button btnSettings;
    private Button btnHide;

    private boolean mIsInRecord=false;
    @Override
         protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initVar();
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
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v==btnRecord){
                record();
            }else if(v==btnScheduleRecord){

            }else if(v==btnRecordList){
                Intent intent = new Intent(MainActivity.this,RecordListActivity.class);
                startActivity(intent);
            }else if(v==btnSettings){
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }else if(v==btnHide){

            }
        }
    }

    //设置界面元素
    private void findView(){
        btnRecord = (Button)findViewById(R.id.btnRecord);
        btnScheduleRecord = (Button)findViewById(R.id.btnScheduleRecord);
        btnRecordList = (Button)findViewById(R.id.btnRecordList);
        btnSettings = (Button)findViewById(R.id.btnSettings);
        btnHide =  (Button)findViewById(R.id.btnHide);

        btnRecord.setOnClickListener(new BtnListener());
        btnScheduleRecord.setOnClickListener(new BtnListener());
        btnRecordList.setOnClickListener(new BtnListener());
        btnSettings.setOnClickListener(new BtnListener());
        btnHide.setOnClickListener(new BtnListener());
    }

    //初始化变量
    private void initVar(){

    }

    private void record(){
        if(!mIsInRecord){       //不在录音
            RecordFunc recordFunc = RecordFunc.getInstance();
            int result = recordFunc.startRecord(UtilHelp.getTime());
            if(result!=ErrorCode.SUCCESS){
                mIsInRecord = false;
            }else {
                mIsInRecord = true;
                btnRecord.setText(R.string.btnStopRecordStr);
            }
        }else{
            RecordFunc recordFunc=RecordFunc.getInstance();
            recordFunc.stopRecord();
            mIsInRecord = false;
            btnRecord.setText(R.string.btnStartRecordStr);
        }
    }
}
