package com.HandleStudio.lolmusic.lolmusic;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

/**
 * Created by 2bab on 14-7-10.
 * Some operation of music file about search , add , delete
 */
public class FileSearchHelper {

    private Context context;
    private LayoutInflater listContainer ;
    private int count;
    public static int[] ids;         //存放音乐文件的id数组
    public static String[] titles;   //存放音乐文件的标题数组
    public static int[][] duration; //存放音乐文件的时长数组
    public static String[] artist;   //存放音乐文件的作者数组
    public static String[] path;     //存放音乐文件的路径数组
    public static String[] fileName; //存音乐文件的文件名数组，做title备用
    private String[] queryItem =
        {
            MediaStore.Audio.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
        };
    Cursor c;

    public FileSearchHelper(Context con){
        context = con;
        listContainer = LayoutInflater.from(context);  //创建视图容器
        fileSearch();
    }

    public void fileSearch(){
        c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,queryItem,null,null,null);
        c.moveToFirst();
        count = c.getCount();
        ids = new int[count];
        titles = new String[count];
        duration = new int[count][2];
        artist = new String[count];
        path = new String[count];
        fileName = new String[count];
        for(int i=0;i<count;i++){
            ids[i] = c.getInt(0);
            titles[i] = c.getString(1);/*Log.i("cursor",titles[i]);*/
            //时长转换  todo:确认音乐时长都是整秒
            duration[i][0] = c.getInt(2)/1000/60;
            duration[i][1] = c.getInt(2)/1000%60;
            artist[i] = c.getString(3);
            path[i] = c.getString(4);
            fileName[i] = c.getString(5);
            c.moveToNext();
        }
        c.close();
    }

    public String getFilePath(int position){
        return path[position];
    }
    public String getFileTitle(int position){return titles[position];}
    public String getFileArtist(int position){return artist[position];}
    public Bundle getFileDuration(int position){
        Bundle bundle = new Bundle();
        bundle.putInt("minute",duration[position][0]);
        bundle.putInt("second",duration[position][1]);
        return bundle;
    }
    public int getFileCount(){return count;}


    public class MusicListAdapter extends BaseAdapter{

        @Override
        public int getCount(){
            return count;
        }

        @Override
        public Object getItem(int position){
            return null;
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parents){
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = listContainer.inflate(R.layout.listview_music_file,null);
                viewHolder = new ViewHolder();
                viewHolder.listTitle = (TextView) convertView.findViewById(R.id.music_list_title);
                viewHolder.listArtist = (TextView) convertView.findViewById(R.id.music_list_artist);
                viewHolder.listDuration = (TextView) convertView.findViewById(R.id.music_list_duration);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.listTitle.setText(titles[position]);
            viewHolder.listArtist.setText(artist[position]);
            viewHolder.listDuration.setText(duration[position][0]+":"+duration[position][1]);
            return convertView;
        }


    }

    public final class ViewHolder{
        public TextView listTitle;
        public TextView listArtist;
        public TextView listDuration;
    }

    public MusicListAdapter getMusicListAdapter(){
        MusicListAdapter adapter = new MusicListAdapter();
        return adapter;
    }


}
