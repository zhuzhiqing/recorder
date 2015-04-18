package com.seu.jason.recorder.ui.list;

import android.graphics.drawable.Drawable;

/**
 * Created by Jason on 2015/4/12.
 */
public class RecordListItem {
    private String mFileName;
    private String mRecordLong;
    private Drawable mIcon;
    private boolean mIsSelected;

    public RecordListItem(String mFileName, String mRecordLong, Drawable mIcon, boolean mIsSelected) {
        this.mFileName = mFileName;
        this.mRecordLong = mRecordLong;
        this.mIcon = mIcon;
        this.mIsSelected = mIsSelected;
    }

    public RecordListItem(String mFileName, String mRecordLong, Drawable mIcon) {
        this.mFileName = mFileName;
        this.mRecordLong = mRecordLong;
        this.mIcon = mIcon;
        this.mIsSelected = false;
    }

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getmRecordLong() {
        return mRecordLong;
    }

    public void setmRecordLong(String mRecordLong) {
        this.mRecordLong = mRecordLong;
    }

    public Drawable getmIcon() {
        return mIcon;
    }

    public void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public boolean ismIsSelected() {
        return mIsSelected;
    }

    public void setmIsSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }
}
