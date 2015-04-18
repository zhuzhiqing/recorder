package com.seu.jason.recorder.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by Jason on 2015/4/12.
 */
public class Constants {
    public final static String AppFolder = "RecorderSpy";
    //录音文件保存路径
    public final static String RecorderDirectoryWithoutSeparator = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + AppFolder;
    public final static String RecorderDirectory = RecorderDirectoryWithoutSeparator + File.separator;

}
