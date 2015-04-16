package com.seu.jason.recorderspy.boardcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.seu.jason.recorderspy.service.RecoredService;

/**
 * Created by Jason on 2015/4/13.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {   //自启动
            Log.d(LOG_TAG, "onReceive(boot)");
            Intent intentService = new Intent(context, RecoredService.class);
            Bundle b = new Bundle();
            b.putInt(RecoredService.RECEIVER_CMD, RecoredService.RECEIVER_CMD_BOOT);
            intentService.putExtras(b);
//            intentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
            context.startService(intentService);
        } else if (intent.getAction().equals(RecoredService.ALARM_RECORD_START_ACTION)) {
            Log.d(LOG_TAG, "onReceive(start)");
            //定时时间到
            Intent intentService = new Intent(context, RecoredService.class);
            Bundle b = new Bundle();
            b.putInt(RecoredService.RECEIVER_CMD, RecoredService.RECEIVER_CMD_ALARM_START);
            intentService.putExtras(b);
//            intentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
            context.startService(intentService);

        } else if (intent.getAction().equals(RecoredService.ALARM_RECORD_STOP_ACTION)) {
            Log.d(LOG_TAG, "onReceive(end)");
            //结束定时时间到
            Intent intentService = new Intent(context, RecoredService.class);
            Bundle b = new Bundle();
            b.putInt(RecoredService.RECEIVER_CMD, RecoredService.RECEIVER_CMD_ALARM_END);
            intentService.putExtras(b);
//            intentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
            context.startService(intentService);
        }
    }
}
