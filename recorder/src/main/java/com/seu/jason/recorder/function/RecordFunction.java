package com.seu.jason.recorder.function;

import android.media.MediaRecorder;
import android.util.Log;

import com.seu.jason.recorder.util.Constants;
import com.seu.jason.recorder.util.OptMsg;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Jason on 2015/4/17.
 */
public class RecordFunction {
    private static final String LOG_TAG = "RecordFunction";

    //单例
    static private RecordFunction recordFunctionInstance = null;
    private Lock lock = new ReentrantLock();// 锁对象
    private boolean isInRecord = false;
    private boolean isInPlay = false;

    //语音操作对象
    private MediaRecorder mediaRecorder = null;

    //私有的默认构造子
    private RecordFunction() {
    }

    //静态工厂方法
    public static RecordFunction getInstance() {
        synchronized (RecordFunction.class) {
            if (recordFunctionInstance == null) {
                recordFunctionInstance = new RecordFunction();
            }
        }
        return recordFunctionInstance;
    }

    //析构
    public static void Destroy() {
        synchronized (RecordFunction.class) {
            recordFunctionInstance = null;
        }
    }

    //开始一次录音
    public int startRecord(String filename) {
        boolean result = false;
        String filePath = Constants.RecorderDirectory + filename + ".amr";
        File directory = new File(Constants.RecorderDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!isInRecord) {            //判断是否正在录音
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(filePath);
            try {                                                            //prepare失败是否要释放mediaRecorder
                mediaRecorder.prepare();
                mediaRecorder.start();
                isInRecord = true;
                return OptMsg.STATE_SUCCESS;
            } catch (IOException e) {
                Log.e(LOG_TAG, "startRecord().prepare() failed");
                return OptMsg.STATE_ERROR_UNKNOW;
            }

        }
        return OptMsg.STATE_ERROR_BUSY;
    }

    //停止录音
    public void stopRecord() {
        if (isInRecord) {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            isInRecord = false;
        }
    }
}
