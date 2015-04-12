package com.seu.jason.recorderspy;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.seu.jason.recorderspy.settings.SettingsActivity;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";

    //界面控件
    private Button btnStartRecord;
    private Button btnStopRecord;
    private Button btnStartPlay;
    private Button btnStopPlay;

    //语音操作对象
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;

    private String mFileName = "test";
    @Override
         protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        // 设置sdcard的路径
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName = mFileName + File.separator + "audiorecordtest.arm";
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
            if(v==btnStartRecord){
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                mediaRecorder.setOutputFile(mFileName);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                try{
                    mediaRecorder.prepare();
                }catch(IOException e){
                    Log.e(LOG_TAG,"prepare() failed");
                }
                mediaRecorder.start();
            }else if(v==btnStopRecord){
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }else if(v==btnStartPlay){
                mediaPlayer = new MediaPlayer();
                try{
                    mediaPlayer.setDataSource(mFileName);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }catch(IOException e){
                    Log.e(LOG_TAG,"play record failed!");
                }
            }else if(v==btnStopPlay){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
    private void findView(){
        btnStartRecord = (Button)findViewById(R.id.btnStartRecord);
        btnStopRecord = (Button)findViewById(R.id.btnStopRecord);
        btnStartPlay = (Button)findViewById(R.id.btnStartPlay);
        btnStopPlay = (Button)findViewById(R.id.btnStopPlay);

        btnStartRecord.setOnClickListener(new BtnListener());
        btnStopRecord.setOnClickListener(new BtnListener());
        btnStartPlay.setOnClickListener(new BtnListener());
        btnStopPlay.setOnClickListener(new BtnListener());
    }
}
