package com.seu.jason.recorderspy.constant;

import android.os.Environment;

import java.io.File;

/**
 * Created by Jason on 2015/4/12.
 */
public class Constants {
    //录音文件保存路径
    public static String RecorderDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator;
}
