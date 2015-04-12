package com.seu.jason.recorderspy.util;

import android.content.Context;
import android.content.res.Resources;

import com.seu.jason.recorderspy.R;

/**
 * Created by Jason on 2015/4/12.
 */
public class ErrorCode {
    public final static int SUCCESS = 1000;
    public final static int E_NO_SDCARD = 1001;
    public final static int E_STATE_RECODING =1002;
    public final static int E_UNKOWN = 1003;

    public static String getErrorInfo(Context vContext, int vType) throws Resources.NotFoundException{
        switch (vType){
            case SUCCESS:
                return "success";
            case E_NO_SDCARD:
                return vContext.getResources().getString(R.string.error_no_sdcard);
            case E_STATE_RECODING:
                return vContext.getResources().getString(R.string.error_state_record);
            case E_UNKOWN:
            default:
                return vContext.getResources().getString(R.string.error_unknown);
        }
    }
}
