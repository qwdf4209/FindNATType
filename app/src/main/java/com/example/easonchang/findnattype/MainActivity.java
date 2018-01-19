package com.example.easonchang.findnattype;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button testBtn;
    private ScrollView mInfoScroll;
    private ProgressBar testLoading;
    private LinearLayout mInfoLayout;
    private RelativeLayout mInfoScrollMask;
    private NatTypeHandleUtil mNatTypeHandle;
    private Handler checkNATTypeHandler;
    private Runnable checkNATTypeRunnable;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private String NATType = null;
    private String publicIP = null;
    private String localIP = null;
    private NetworkUtils mNetworkUtils;
    private int analysisCounts = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNetworkUtils = new NetworkUtils(this);
        initNATInfoSharePreference();

        testBtn = findViewById(R.id.test);
        testBtn.setTextSize(adjustButtonFontSize());
        testLoading = findViewById(R.id.progressBar);
        mInfoScroll = findViewById(R.id.infoScroll);
        mInfoLayout = findViewById(R.id.infoLayout);
        mInfoScrollMask = findViewById(R.id.infoScroll_mask);

        mNatTypeHandle = NatTypeHandleUtil.getInstance();
        mNatTypeHandle.setSharePreferences(settings);

        checkNATTypeHandler = new Handler();

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initNATInfoSharePreference();
                analysisCounts++;
                if (mNetworkUtils.isNetworkOnline()){
                    testLoading.setVisibility(View.VISIBLE);
                    mInfoScrollMask.setVisibility(View.VISIBLE);
                    testBtn.setText(R.string.btn_testing);
                    testBtn.setEnabled(false);
                    mNatTypeHandle.ExecuteStunNAT();
                    checkNATTypeHandler.postDelayed(checkNATTypeRunnable,1000);
                }
                else{
                    addRecord(getResources().getString(R.string.network_not_connected),5);
                }

            }
        });

        checkNATTypeRunnable = new Runnable() {
            @Override
            public void run() {
                if (settings.getBoolean("testFinish",false)){
                    NATType = settings.getString("NatType","");
                    publicIP = settings.getString("PublicIP","");
                    localIP = settings.getString("LocalIP","");
                    addRecord("("+String.valueOf(analysisCounts)+") Time: "+getTime(),4);
                    addRecord("Network Type: "+mNetworkUtils.ConnectionType(),5);
                    addRecord("NAT Type: "+NATType,1);
                    addRecord("Public IP: "+publicIP,2);
                    addRecord("Local IP: "+localIP+"\n",3);
                    testLoading.setVisibility(View.INVISIBLE);
                    checkNATTypeHandler.removeCallbacks(checkNATTypeRunnable);
                    testBtn.setText(R.string.btn_start);
                    testBtn.setEnabled(true);
                    mInfoScrollMask.setVisibility(View.INVISIBLE);
                }
                else if (!mNatTypeHandle.errorMsg.equals("")){
                    addRecord("("+String.valueOf(analysisCounts)+") "+mNatTypeHandle.errorMsg+"\n",1);
                    testLoading.setVisibility(View.INVISIBLE);
                    checkNATTypeHandler.removeCallbacks(checkNATTypeRunnable);
                    testBtn.setText(R.string.btn_start);
                    testBtn.setEnabled(true);
                    mInfoScrollMask.setVisibility(View.INVISIBLE);
                    mNatTypeHandle.errorMsg = "";
                }
                else{
                    checkNATTypeHandler.postDelayed(checkNATTypeRunnable,1000);
                }

            }

        };
    }
    private void addRecord(String message, int type){
        TextView textView = new TextView(MainActivity.this);
        textView.setText(message);
        textView.setTextSize(adjustFontSize());

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setTextColor(getResources().getColor(R.color.red_forbidden));
        }
        else if (type == 2){
            lp2.gravity = Gravity.LEFT;
            textView.setTextColor(getResources().getColor(R.color.deviceon));
        }
        else if (type == 3){
            lp2.gravity = Gravity.LEFT;
            textView.setTextColor(getResources().getColor(R.color.yale_lock_color));
        }
        else if (type ==4){
            lp2.gravity = Gravity.CENTER;
            textView.setTextColor(getResources().getColor(R.color.bell_off));
        }
        else if (type == 5){
            lp2.gravity = Gravity.LEFT;
            textView.setTextColor(getResources().getColor(R.color.bell_off));
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setTextColor(getResources().getColor(R.color.bell_off));
        }
        textView.setBackgroundColor(getResources().getColor(R.color.black));

        textView.setLayoutParams(lp2);
        mInfoLayout.addView(textView);
        mInfoScroll.post(new Runnable() {
            @Override
            public void run() {
                mInfoScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void setFullScreen(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void initNATInfoSharePreference(){
        settings = getSharedPreferences("NATTYPE_PARAMETER", 0);
        editor = settings.edit();
        editor.putBoolean("testFinish",false);
        editor.putInt("NatType",-1);
        editor.commit();
    }

    private String getTime(){
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);

        int month = c.get(Calendar.MONTH);
        String tempMonth;
        if ((month + 1) < 10) {
            tempMonth = "0" + Integer.toString(month+1);
        } else {
            tempMonth = Integer.toString(month+1);
        }

        int date = c.get(Calendar.DAY_OF_MONTH);
        String tempDate;
        if (date < 10) {
            tempDate = "0" + Integer.toString(date);
        } else {
            tempDate = Integer.toString(date);
        }

        int hour = c.get(Calendar.HOUR_OF_DAY);
        String tempHour;
        if (hour < 10) {
            tempHour = "0" + Integer.toString(hour);
        } else {
            tempHour = Integer.toString(hour);
        }
        int minute = c.get(Calendar.MINUTE);
        String tempMinute;
        if (minute < 10) {
            tempMinute = "0" + Integer.toString(minute);
        } else {
            tempMinute = Integer.toString(minute);
        }

        String ts = Integer.toString(year) + "-" + tempMonth + "-"
                + tempDate + " " + tempHour + ":" + tempMinute;
        return ts;
    }

    private int getScreenInfo(String parameter){
        DisplayMetrics dm = new DisplayMetrics();
        Display realSize = this.getWindowManager().getDefaultDisplay();
        realSize.getRealMetrics(dm);
        int value = 0;
        switch (parameter){
            case "Width":
                value = dm.widthPixels;
                break;
            case "Height":
                value = dm.heightPixels;
                break;
            default:
                value = 0;

        }
        return value;
    }

    //获取字体大小
    public int adjustFontSize() {
        int screenWidth;
        screenWidth=getScreenInfo("Width")>getScreenInfo("Height")?getScreenInfo("Width"):getScreenInfo("Height");
        int rate = (int)(5.5*(float) screenWidth/320); //我自己测试这个倍数比较适合，当然你可以测试后再修改
        return rate<15?15:rate; //字体太小也不好看的
    }

    //获取字体大小
    public int adjustButtonFontSize() {
        int screenWidth;
        screenWidth=getScreenInfo("Width")>getScreenInfo("Height")?getScreenInfo("Width"):getScreenInfo("Height");
        int rate = (int)(8*(float) screenWidth/320); //我自己测试这个倍数比较适合，当然你可以测试后再修改
        return rate<15?15:rate; //字体太小也不好看的
    }

    //將全螢幕畫面轉換成Bitmap
    private Bitmap getScreenShot()
    {
        //藉由View來Cache全螢幕畫面後放入Bitmap
        View mView = getWindow().getDecorView();
        mView.setDrawingCacheEnabled(true);
        mView.buildDrawingCache();
        Bitmap mFullBitmap = mView.getDrawingCache();

        //取得系統狀態列高度
        Rect mRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(mRect);
        int mStatusBarHeight = mRect.top;

        //取得手機螢幕長寬尺寸
        int mPhoneWidth = getWindowManager().getDefaultDisplay().getWidth();
        int mPhoneHeight = getWindowManager().getDefaultDisplay().getHeight();

        //將狀態列的部分移除並建立新的Bitmap
        Bitmap mBitmap = Bitmap.createBitmap(mFullBitmap, 0, mStatusBarHeight, mPhoneWidth, mPhoneHeight - mStatusBarHeight);
        //將Cache的畫面清除
        mView.destroyDrawingCache();

        return mBitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }

}
