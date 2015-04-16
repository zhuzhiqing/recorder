package com.seu.jason.recorderspy.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2015/4/14.
 */
public class AlarmDBManager {
    final static private String LOG_TAG = "AlarmDBManager";
    private AlarmDBHelper helper;
    private SQLiteDatabase db;

    public AlarmDBManager(Context context) {
        helper = new AlarmDBHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * add a alarm
     */
    public void add(AlarmMeta alarm) {
        Log.d(LOG_TAG, "add()");
        db.beginTransaction();//开始事务
        try {
            db.execSQL("INSERT INTO alarm VALUES(null,?,?)", new Object[]{alarm.date, alarm.state,});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * add a alarm
     */
    public void addList(List<AlarmMeta> alarms) {
        Log.d(LOG_TAG, "addList()");
        db.beginTransaction();//开始事务
        try {
            for (AlarmMeta alarm : alarms) {
                db.execSQL("INSERT INTO alarm VALUES(null,?,?)", new Object[]{alarm.date, alarm.state,});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void updateState(AlarmMeta alarm) {
        Log.d(LOG_TAG, "updateState()");
        ContentValues cv = new ContentValues();
        cv.put("state", alarm.state);
        db.update("alarm", cv, "_id = ?", new String[]{String.valueOf(alarm._id)});
    }

    public void updateDate(AlarmMeta alarm) {
        Log.d(LOG_TAG, "updateDate()");
        ContentValues cv = new ContentValues();
        cv.put("date", alarm.date);
        db.update("alarm", cv, "_id = ?", new String[]{String.valueOf(alarm._id)});
    }

    public void delete(AlarmMeta alarm) {
        Log.d(LOG_TAG, "delete()");
        db.delete("alarm", "_id=?", new String[]{String.valueOf(alarm._id)});
    }

    public List<AlarmMeta> query() {
        Log.d(LOG_TAG, "query()");
        ArrayList<AlarmMeta> alarms = new ArrayList<AlarmMeta>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            AlarmMeta alarm = new AlarmMeta();
            alarm._id = c.getInt(c.getColumnIndex("_id"));
            alarm.date = c.getString(c.getColumnIndex("date"));
            alarm.state = c.getInt(c.getColumnIndex("state"));
            alarms.add(alarm);
        }
        c.close();
        return alarms;
    }

    public Cursor queryTheCursor() {
        return db.rawQuery("SELECT * FROM alarm", null);
    }

    public void closeDB() {
        Log.d(LOG_TAG, "closeDB()");
        db.close();
    }

}
