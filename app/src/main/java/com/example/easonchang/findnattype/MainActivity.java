package com.example.easonchang.findnattype;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button testBtn;
    private ScrollView mInfoScroll;
    private ProgressBar testLoading;
    private LinearLayout mInfoLayout;
    private NatTypeHandleUtil mNatTypeHandle;
    private Handler checkNATTypeHandler;
    private Runnable checkNATTypeRunnable;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private int NATType = -1;
    private String publicIP = null;
    private String localIP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNATInfoSharePreference();

        testBtn = findViewById(R.id.test);
        testLoading = findViewById(R.id.progressBar);
        mInfoScroll = findViewById(R.id.infoScroll);
        mInfoLayout = findViewById(R.id.infoLayout);

        mNatTypeHandle = NatTypeHandleUtil.getInstance();
        mNatTypeHandle.setSharePreferences(settings);

        checkNATTypeHandler = new Handler();

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initNATInfoSharePreference();
                if (isNetworkOnline()){
                    testLoading.setVisibility(View.VISIBLE);
                    testBtn.setText(R.string.btn_testing);
                    testBtn.setEnabled(false);
                    mNatTypeHandle.ExecuteStunNAT();
                    checkNATTypeHandler.postDelayed(checkNATTypeRunnable,500);
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
                    NATType = settings.getInt("NatType",-1);
                    publicIP = settings.getString("PublicIP","");
                    localIP = settings.getString("LocalIP","");
                    addRecord(getTime(),4);
                    addRecord("NAT Type: "+String.valueOf(NATType)+" "+getNATTypeMean(NATType),1);
                    addRecord("PublicIP: "+publicIP,2);
                    addRecord("LocalIP: "+localIP+"\n",3);
                    testLoading.setVisibility(View.INVISIBLE);
                    checkNATTypeHandler.removeCallbacks(checkNATTypeRunnable);
                    testBtn.setText(R.string.btn_start);
                    testBtn.setEnabled(true);
                }
                else{
                    checkNATTypeHandler.postDelayed(checkNATTypeRunnable,500);
                }

            }

        };
    }
    private void addRecord(String message, int type){
        TextView textView = new TextView(MainActivity.this);
        textView.setText(message);

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
        else if (type == 5){
            lp2.gravity = Gravity.CENTER_HORIZONTAL;
            textView.setTextColor(getResources().getColor(R.color.red_forbidden));
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setTextColor(getResources().getColor(R.color.yale_lock_color));
        }
        textView.setTextSize(18);
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

    private String getNATTypeMean(int NatType){
        String result = "";
        switch (NatType){
            case 1:
                result = "Full Cone";
                break;
            case 2:
                result = "Restricted Cone";
                break;
            case 3:
                result = "Port Restricted Cone";

                break;
            case 4:
                result = "Symmetric";
                break;
            default:
                result = "Unknow";
                break;
        }
        return result;
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

    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }
}
