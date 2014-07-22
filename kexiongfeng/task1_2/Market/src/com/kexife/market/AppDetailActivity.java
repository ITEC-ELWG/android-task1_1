package com.kexife.market;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

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

        detail = (HashMap<String, Object>)getIntent().getSerializableExtra("DETAIL");

        initFindView();
        initSetView();

        Handler handler = new MessageHandler();
        networkThread = new NetworkThread(handler,this);
        networkThread.start();

        getDetail((Integer) detail.get("id"));
    }

    private void initFindView(){
        imageViewIcon = (ImageView) findViewById(R.id.app_detail_icon);
        textViewTitle = (TextView) findViewById(R.id.app_detail_title);
        textViewVersion = (TextView) findViewById(R.id.app_detail_version);
        textViewSize = (TextView) findViewById(R.id.app_detail_size);
        textViewTag = (TextView) findViewById(R.id.app_detail_tag);
        textViewMinSdkVersion = (TextView) findViewById(R.id.app_detail_sdk);
        textViewCount = (TextView) findViewById(R.id.app_detail_count);
        textViewDescription = (TextView) findViewById(R.id.app_detail_description);
        progressBar = (ProgressBar) findViewById(R.id.app_detail_progressbar);
    }

    private void initSetView(){

        //Using AsyncTask to get bitmap from path
        new ImageFromPathTask(imageViewIcon).execute((String)detail.get("icon"));

        textViewTitle.setText((String) detail.get("title"));
        textViewVersion.setText((String) detail.get("apkVersionName"));
        textViewSize.setText((String) detail.get("apkSize"));
        textViewTag.setText((String) detail.get("tag"));
        textViewMinSdkVersion.setText( Utils.sdkVersionName((Integer)detail.get("apkMinSdkVersion")) );
        textViewCount.setText((String) detail.get("installedCountStr"));
    }

    private void getDetail(int id){
        Message message = new Message();
        message.what = MessageType.GET_DETAIL;
        Bundle bundle = new Bundle();
        bundle.putInt("ID",id);
        message.setData(bundle);
        networkThread.getHandler().sendMessage(message);
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MessageType.RESPONSE:
                    moreDetail = (HashMap<String, Object>)msg.getData().getSerializable("MORE");
                    textViewDescription.setText((String) moreDetail.get("description"));
                    progressBar.setVisibility(View.GONE);
                    break;
                case MessageType.EXCEPTION:
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