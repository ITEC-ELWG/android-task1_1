package com.kexife.market;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FooterView extends LinearLayout {

    public static final int LOAD_MODE = 0;
    public static final int NETWORK_ERROR_MODE = 1;
    public static final int NO_MORE_APPS_MODE = 2;

    private ProgressBar progressBar;
    private TextView textView;
    private Context mContext;

    public FooterView(Context context, AttributeSet attrs, int mode){
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.app_list_footer,this);

        progressBar = (ProgressBar)findViewById(R.id.app_list_footer_progressbar);
        textView = (TextView)findViewById(R.id.app_list_footer_textview);
        mContext = getContext();

        setDisplayMode(mode);
    }

    public void setDisplayMode(int mode){
        switch(mode){
            case LOAD_MODE:
                progressBar.setVisibility(VISIBLE);
                textView.setText( mContext.getString(R.string.loading) );
                break;
            case NETWORK_ERROR_MODE:
                progressBar.setVisibility(GONE);
                textView.setText( mContext.getString(R.string.network_error) );
                break;
            case NO_MORE_APPS_MODE:
                progressBar.setVisibility(GONE);
                textView.setText( mContext.getString(R.string.no_more_apps) );
                break;
            default:
        }

    }
}
