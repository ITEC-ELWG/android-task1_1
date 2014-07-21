package com.kexife.market;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

public class AppDetailActivity extends Activity {

    private MyLogger logger = MyLogger.kLog();

    private NetworkThread networkThread;

    HashMap<String,Object> detail;
    HashMap<String,Object> moreDetail;

    ImageView imageViewIcon;
    TextView textViewTitle, textViewVersion, textViewSize, textViewTag, textViewMinSdkVersion, textViewCount, textViewDescription;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActionBar()!=null) getActionBar().hide();
        setContentView(R.layout.activity_app_detail);

        Handler handler = new MessageHandler();
        networkThread = new NetworkThread(handler,this);
        networkThread.start();

        imageViewIcon = (ImageView) findViewById(R.id.app_detail_icon);
        textViewTitle = (TextView) findViewById(R.id.app_detail_title);
        textViewVersion = (TextView) findViewById(R.id.app_detail_version);
        textViewSize = (TextView) findViewById(R.id.app_detail_size);
        textViewTag = (TextView) findViewById(R.id.app_detail_tag);
        textViewMinSdkVersion = (TextView) findViewById(R.id.app_detail_sdk);
        textViewCount = (TextView) findViewById(R.id.app_detail_count);
        textViewDescription = (TextView) findViewById(R.id.app_detail_description);
        progressBar = (ProgressBar) findViewById(R.id.app_detail_progressbar);


        detail = (HashMap<String, Object>)getIntent().getSerializableExtra("DETAIL");

        //Get bitmap from cache
        Bitmap bitmap = Utils.getBitmapFromCache( getCacheDir(), (String)detail.get("icon") );
        imageViewIcon.setImageBitmap(bitmap);
        textViewTitle.setText((String) detail.get("title"));
        textViewVersion.setText((String) detail.get("apkVersionName"));
        textViewSize.setText((String) detail.get("apkSize"));
        textViewTag.setText((String) detail.get("tag"));
        textViewMinSdkVersion.setText( Utils.sdkVersionName((Integer)detail.get("apkMinSdkVersion")) );
        textViewCount.setText((String) detail.get("installedCountStr"));

        getMoreDetail((Integer)detail.get("id"));
    }

    private void getMoreDetail(int id){
        Message message = new Message();
        message.what = 0x002;
        Bundle bundle = new Bundle();
        bundle.putInt("ID",id);
        message.setData(bundle);
        networkThread.getHandler().sendMessage(message);
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0x201:
                    moreDetail = (HashMap<String, Object>)msg.getData().getSerializable("MORE");
                    textViewDescription.setText((String) moreDetail.get("description"));
                    progressBar.setVisibility(View.GONE);
                    break;
                case 0x203:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AppDetailActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                default:
            }
        }
    }

    public void downloadOnClick(View view){
        try {
            if (!Utils.isDownloadManagerAvailable(this)) {
                Toast.makeText(this, "Not Support DownloadManager", Toast.LENGTH_SHORT).show();
            } else {
                DownloadFile((String) moreDetail.get("apkDownloadUrl"), (String)detail.get("title"), moreDetail.get("packageName") + ".apk", moreDetail.get("packageName") + ".apk");
            }
        }catch (Exception e){
            logger.w(e);
        }
    }


    public void DownloadFile(String urlPath, String title, String description, String fileName){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlPath));

        if(title!=null) request.setTitle(title);
        if(description!=null) request.setDescription(description);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
        request.setMimeType("application/vnd.android.package-archive");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

}