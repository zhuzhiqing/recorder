package com.seu.jason.recorderspy.function;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.seu.jason.recorderspy.constant.Constants;
import com.seu.jason.recorderspy.util.OptMsg;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jason on 2015/4/12.
 */
public class RecordFunc {
    private static final String LOG_TAG="RecordFunc";

    //单例
    static private RecordFunc recordFuncInstance= null;
    private boolean isInRecord = false;
    private boolean isInPlay = false;

    //语音操作对象
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;

    //私有的默认构造子
    private RecordFunc() {}

    //静态工厂方法
    public static RecordFunc getInstance() {
        if (recordFuncInstance == null) {
            recordFuncInstance = new RecordFunc();
        }
        return recordFuncInstance;
    }

    //开始一次录音
    public int startRecord(String filename){
        boolean result = false;
        String filePath = Constants.RecorderDirectory+filename+".amr";
        File directory= new File( Constants.RecorderDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if(!isInRecord){            //判断是否正在录音
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(filePath);
            try{                                                            //prepare失败是否要释放mediaRecorder
                mediaRecorder.prepare();
                mediaRecorder.start();
                isInRecord = true;
                return OptMsg.MSG_STATE_RECORDING;
            }catch(IOException e){
                Log.e(LOG_TAG,"startRecord().prepare() failed");
                return OptMsg.MSG_STATE_NOT_RECORDING;
            }

        }
        return OptMsg.MSG_STATE_NOT_RECORDING;
    }

    //停止录音
    public void stopRecord(){
        if(isInRecord){
            if(mediaRecorder != null){
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            isInRecord = false;
        }
    }

    //开始播放
    public void startPlay(String fileName){
        //if(isInPlay)
        String filePath = Constants.RecorderDirectory+fileName+".arm";
        mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            Log.e(LOG_TAG,"play record failed!");
        }
    }

    //停止播放
    public void stopPlay(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
