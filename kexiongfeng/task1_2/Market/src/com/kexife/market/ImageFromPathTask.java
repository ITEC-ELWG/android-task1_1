package com.kexife.market;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ImageFromPathTask  extends AsyncTask<String,Void,Bitmap> {

    private final WeakReference imageViewReference;

    public ImageFromPathTask(ImageView imageView){
        imageViewReference = new WeakReference(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params){
        return Utils.getBitmapFromCache( params[0] );
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        if(isCancelled()){
            bitmap = null;
        }

        ImageView imageView= (ImageView)imageViewReference.get();
        if(imageView!=null){
            if(bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
