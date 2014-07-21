package com.kexife.market;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class MyBaseAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private List<HashMap<String,Object>> list;
    private int layoutId;
    private int[] itemId;
    private Context mContext;

    public MyBaseAdapter(Context context, List<HashMap<String,Object>> list, int layoutId, int[] itemId){
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.layoutId =layoutId;
        this.itemId = itemId;
        this.mContext = context;
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
        if(convertView==null) convertView = mInflater.inflate(layoutId,null);

        for(int i=0;i<itemId.length;i++){
            switch(itemId[i]){
                case R.id.app_list_item_icon:
                    ImageView imgView = (ImageView)convertView.findViewById(itemId[i]);
                    //Get bitmap from cache
                    Bitmap bitmap = Utils.getBitmapFromCache( mContext.getCacheDir(), (String)list.get(position).get("icon"));
                    imgView.setImageBitmap(bitmap);
                    break;
                case R.id.app_list_item_title:
                    TextView textView = (TextView) convertView.findViewById(itemId[i]);
                    textView.setText((String)list.get(position).get("title"));
                    break;
                case R.id.app_list_item_detail:
                    TextView textView2 = (TextView) convertView.findViewById(itemId[i]);
                    String text = list.get(position).get("apkVersionName") + " , "  + mContext.getString(R.string.this_week) +list.get(position).get("statWeeklyStr") + mContext.getString(R.string.download);
                    textView2.setText(text);
                    break;
                default:
            }
        }
        return convertView;
    }

    public void updateList(List<HashMap<String,Object>> list){
        this.list = list;
        notifyDataSetChanged();
    }
}