package com.kexife.market;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class Utils {

    public static Bitmap getBitmapFromCache(File dir, String filename){
        File file = new File(dir,filename);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(in);
    }

    public static boolean isDownloadManagerAvailable(Context context){
        try{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD){
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size()>0;
        }catch(Exception e){
            return false;
        }
    }

    public static String sdkVersionName(int version){
        switch(version){
            case 1:
                return "Base";
            case 2:
                return "1.1";
            case 3:
                return "1.5";
            case 4:
                return "1.6";
            case 5:
                return "2.0";
            case 6:
                return "2.0.1";
            case 7:
                return "2.1";
            case 8:
                return "2.2";
            case 9:
                return "2.3";
            case 10:
                return "2.3.3";
            case 11:
                return "3.0";
            case 12:
                return "3.1";
            case 13:
                return "3.2";
            case 14:
                return "4.0";
            case 15:
                return "4.0.3";
            case 16:
                return "4.1";
            case 17:
                return "4.2";
            case 18:
                return "4.3";
            case 19:
                return "4.4";
            default: return "4.4";
        }
    }
}
