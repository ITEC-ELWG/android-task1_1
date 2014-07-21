package com.kexife.market;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    public static String getJsonStr(String urlPath) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        InputStream inStream = conn.getInputStream();
        while( (len=inStream.read(data)) != -1){
            outStream.write(data,0,len);
        }
        inStream.close();
        return new String( outStream.toByteArray() );
    }

    public static Bitmap downloadImage(String imgPath) throws Exception{
        URL url = new URL(imgPath);
        Bitmap bitmap = null;
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode()==200){
            InputStream inStream = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(inStream);
        }
        return bitmap;
    }
}
