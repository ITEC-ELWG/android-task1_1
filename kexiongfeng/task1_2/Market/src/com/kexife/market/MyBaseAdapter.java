package com.kexife.market;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class MyBaseAdapter extends BaseAdapter{
    private Context mContext;
    private List<HashMap<String,Object>> list;
    private int layoutId;
    private int[] itemId;


    public MyBaseAdapter(Context context, List<HashMap<String,Object>> list, int layoutId, int[] itemId){
        this.mContext = context;
        this.list = list;
        this.layoutId = layoutId;
        this.itemId = itemId;
    }

    @Override
    public int getCount(){
        return list.size();
    }

    @Override
    public Object getItem(int position){
        return list.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        if(convertView==null) {
            convertView = LayoutInflater.from(mContext).inflate(layoutId,null);

            holder =new ViewHolder();
            holder.iconImageView = (ImageView)convertView.findViewById(itemId[0]);
            holder.titleTextView = (TextView)convertView.findViewById(itemId[1]);
            holder.detailTextView = (TextView)convertView.findViewById(itemId[2]);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        HashMap<String,Object> map = list.get(position);

        //Using AsyncTask to get bitmap from path
        new ImageFromPathTask(holder.iconImageView).execute((String)map.get("icon"));

        holder.titleTextView.setText((String)map.get("title"));

        String text = map.get("apkVersionName") + " , "  + mContext.getString(R.string.this_week) +map.get("statWeeklyStr") + mContext.getString(R.string.download);
        holder.detailTextView.setText(text);

        return convertView;
    }

    public void updateList(List<HashMap<String,Object>> list){
        this.list = list;
        notifyDataSetChanged();
    }

    private static class ViewHolder{
        private ImageView iconImageView;
        private TextView titleTextView;
        private TextView detailTextView;
    }
}