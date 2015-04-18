package com.seu.jason.recorder.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.seu.jason.recorder.service.RecordService;

/**
 * Created by Jason on 2015/4/17.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {   //自启动
            Log.d(LOG_TAG, "onReceive(boot)");
            Intent intentService = new Intent(context, RecordService.class);
            Bundle b = new Bundle();
            b.putInt(RecordService.RECEIVER_CMD, RecordService.RECEIVER_CMD_BOOT);
            intentService.putExtras(b);
//            intentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
            context.startService(intentService);
        }
    }
}
