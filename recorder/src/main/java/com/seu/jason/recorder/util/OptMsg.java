package com.seu.jason.recorder.util;

/**
 * Created by Jason on 2015/4/13.
 */
public class OptMsg {
    static final public int MSG_REQ_RECORD_START = 3100;
    static final public int MSG_REQ_RECORD_STOP = 3101;
    static final public int MSG_REQ_RECORD_TRIGGER = 3102;
    static final public int MSG_REQ_PLAY_START = 3103;
    static final public int MSG_REQ_PLAY_STOP = 3104;
    static final public int MSG_REQ_CHECK_STATE = 3105;
    static final public int MSG_REQ_SCHEDULE_RECORD_START = 3106;
    static final public int MSG_REQ_SCHEDULE_RECORD_STOP = 3107;
    static final public int MSG_REQ_SET_ALARM = 3108;
    static final public int MSG_REQ_CANCLE_ALARM = 3109;
    static final public int MSG_REQ_ALARM_TIMING = 3110;
    static final public int MSG_REQ_BACKGROUND_START = 3111;
    static final public int MSG_REQ_BACKGROUND_STOP = 3112;//定时时间到
    static final public int MSG_REQ_SET_TIME_INTERVAL = 3113;

    static final public int MSG_RST_RECORD_START_SUCCESS = 3201;
    static final public int MSG_REQ_RECORD_START_FAILED = 3202;
    static final public int MSG_RST_RECORD_STOP_SUCCESS = 3203;
    static final public int MSG_REQ_RECORD_STOP_FAILED = 3204;
    static final public int MSG_RST_PLAY_START_SUCCESS = 3205;
    static final public int MSG_RST_PLAY_START_FAILED = 3206;
    static final public int MSG_RST_PLAY_STOP_SUCCESS = 3207;
    static final public int MSG_RST_PLAY_STOP_FAILED = 3208;
    static final public int MSG_RST_CHECK_STATE = 3209;

    static final public int MSG_STATE_RECORDING = 3300;
    static final public int MSG_STATE_NOT_RECORDING = 3301;
    static final public int MSG_STATE_PLAYING = 3302;
    static final public int MSG_STATE_NOT_PLAYING = 3303;
    static final public int MSG_STATE_UNKNOW = 3304;
    static final public int MSG_STATE_SCHEDULE_RECORDING = 3305;

    static final public int MSG_INTERVAL_UP = 3400; //录音间隔

    static final public int STATE_SUCCESS = 3501;
    static final public int STATE_ERROR_BUSY = 3502;
    static final public int STATE_ERROR_UNKNOW = 3503;

    static final public int MSG_REQ_SEND_HEART = 3401; //请求发送心跳

}
