package com.seu.jason.recorderspy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.seu.jason.recorderspy.R;
import com.seu.jason.recorderspy.ui.record.RecordListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2015/4/13.
 */
public class RecordListAdapter extends BaseAdapter{
    static final String LOG_TAG = "RecordListAdapter";
    private Context mContext = null;
    private List<RecordListItem> mItems = new ArrayList<RecordListItem>();
    private LayoutInflater mLayoutInflater;

    public RecordListAdapter(Context context){
        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public void setListItems(List<RecordListItem> itl){
        mItems = itl;
    }

    public void addItem(RecordListItem it){
        mItems.add(it);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecordItemViewHolder holder;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.record_list_item,null);
            holder = new RecordItemViewHolder();
            holder.textFileName = (TextView)convertView.findViewById(R.id.textFileName);
            holder.textRecordLong = (TextView)convertView.findViewById(R.id.textRecordLong);
            holder.imgView = (ImageView)convertView.findViewById(R.id.imgIcon);
            convertView.setTag(holder);     //绑定ViewHolder对象
        }else{
            holder = (RecordItemViewHolder)convertView.getTag();
        }
        holder.imgView.setImageResource(R.drawable.audio);
        holder.textFileName.setText(mItems.get(position).getmFileName());
        holder.textRecordLong.setText("时长:"+mItems.get(position).getmRecordLong());

        return convertView;
    }

    class RecordItemViewHolder{
        public ImageView imgView;           //图标
        public TextView textFileName;      //文件名
        public TextView textRecordLong;    //录音时长
    }
}
