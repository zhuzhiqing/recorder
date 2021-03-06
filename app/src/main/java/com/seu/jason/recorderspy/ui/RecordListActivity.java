package com.seu.jason.recorderspy.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ListView;

import com.seu.jason.recorderspy.R;
import com.seu.jason.recorderspy.adapter.RecordListAdapter;
import com.seu.jason.recorderspy.constant.Constants;
import com.seu.jason.recorderspy.ui.record.RecordListItem;
import com.seu.jason.recorderspy.util.UtilHelp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2015/4/12.
 */
public class RecordListActivity extends Activity {
    private List<RecordListItem> fileList = new ArrayList<RecordListItem>();
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list_activity);
        lv = (ListView) findViewById(R.id.record_list_activity_listVew);
        listFileInDirectory(new File(Constants.RecorderDirectoryWithoutSeparator));
    }

    private void listFileInDirectory(final File dir) {
        if (dir.isDirectory()) {
            getFilesDetails(dir.listFiles());
        }
    }

    private void getFilesDetails(File[] files) {
        fileList.clear();
        Drawable currentIcon;
        long duration = 0;
        for (File currentFile : files) {
            //判断是一个文件夹还是文件
            if (currentFile.isFile()) {
                //获取文件名
                String fileName = currentFile.getName();
                //根据文件名判断文件类型，设置不同的图标
                //   if()
                //获取时长
                try {
                    duration = UtilHelp.getAmrDuration(currentFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            //确保只显示文件名、不显示路径名
            fileList.add(new RecordListItem(currentFile.getName(), getDurationStr(duration), getResources().getDrawable(R.drawable.audio)));
        }
        //Collections.sort(fileList);
        RecordListAdapter ita = new RecordListAdapter(this);
        ita.setListItems(fileList);     //将列表设置到ListAdapter中
        lv.setAdapter(ita);
    }

    private String getDurationStr(long duration) {
        long second = 0;
        long miniute = 0;
        long hour = 0;
        long day = 0;

        duration /= 1000;
        second = (duration) % 60;   //秒
        duration /= 60;
        if ((duration) > 0) {
            miniute = duration % 60;
            duration /= 60;
            if (duration > 0) {
                hour = duration % 24;
                duration /= 24;
                if (duration > 0) {
                    day = duration;
                }
            }
        }
        String durationStr = "";
        if (day > 0) {
            durationStr += String.valueOf(day) + "天";
        }
        if (hour > 0) {
            durationStr += String.valueOf(hour) + "时";
        }

        if (miniute > 0) {
            durationStr += String.valueOf(miniute) + "分";
        }

        if (second > 0) {
            durationStr += String.valueOf(second) + "秒";
        }
        return durationStr;

    }
}
