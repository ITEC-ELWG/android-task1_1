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
import android.widget.TextView;

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
    public static int[] albumId;
    public static int[] size;

    private String[] queryItem =
        {
            MediaStore.Audio.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM_ID,
        };
    Cursor cursor;

    public FileSearchHelper(Context con){
        context = con;
        listContainer = LayoutInflater.from(context);  //创建视图容器
        fileSearch();
    }

    public void fileSearch(){
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,queryItem,MediaStore.Audio.Media.SIZE+">500000",null,null);
        cursor.moveToFirst();
        count = cursor.getCount();
        ids = new int[count];
        titles = new String[count];
        duration = new int[count][2];
        artist = new String[count];
        path = new String[count];
        fileName = new String[count];
        albumId = new int[count];
        size = new int[count];
        for(int i=0;i<count;i++){
            ids[i] = cursor.getInt(0);
            titles[i] = cursor.getString(1);/*Log.i("cursor",titles[i]);*/
            //时长转换  todo:确认音乐时长都是整秒
            duration[i][0] = cursor.getInt(2)/1000/60;
            duration[i][1] = cursor.getInt(2)/1000%60;
            artist[i] = cursor.getString(3);
            path[i] = cursor.getString(4);
            fileName[i] = cursor.getString(5);
            albumId[i] = cursor.getInt(6);
            cursor.moveToNext();
        }
        cursor.close();
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
    public int getFileSongId(int position){return ids[position];}
    public int getFileAlbumId(int position){return albumId[position];}


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
            viewHolder.listArtist.setText("   -   "+artist[position]);
            viewHolder.listDuration.setText(timeTransform(duration[position][0],duration[position][1]));
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

    public String timeTransform(int minute, int second){
        String mm,ss;
        if (minute<10) mm = "0" + minute;
        else mm = String.valueOf(minute);
        if (second<10) ss = "0" + second;
        else ss = String.valueOf(second);
        return mm+":"+ss;
    }

}
