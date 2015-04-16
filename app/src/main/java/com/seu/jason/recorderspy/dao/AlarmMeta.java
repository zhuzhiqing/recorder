package com.seu.jason.recorderspy.dao;

/**
 * Created by Jason on 2015/4/14.
 */
public class AlarmMeta {
    final static public int STATE_SUCCESS = 1001;
    final static public int STATE_FAILED = 1002;
    final static public int STATE_EXPIRED = 1001;

    public int _id;
    public String date;
    public int state;

    public AlarmMeta() {

    }

    public AlarmMeta(String date, int state) {
        this.date = date;
        this.state = state;
    }
}
