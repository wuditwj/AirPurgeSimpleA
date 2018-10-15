package com.njwyt.airpurgesimple.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.njwyt.airpurgesimple.R;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by jason_samuel on 2018/2/25.
 */

public class BaseActivity extends AppCompatActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().register(this);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        //取消状态栏
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        setFontSize();
        setStatusBar();
    }

    /**
     * 获取字体大小
     */
    private void setFontSize() {
        setTheme(R.style.Default_TextSize_Middle);
    }


    /**
     * 设置状态栏为透明色
     */
    private void setStatusBar() {

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_right_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //EventBus.getDefault().unregister(this);
    }
}

