package com.njwyt.airpurgesimple.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.view.animation.AlphaAnimation;

import com.baidu.location.BDLocation;
import com.njwyt.airpurgesimple.R;
import com.njwyt.airpurgesimple.databinding.ActivitySplashBinding;
import com.njwyt.airpurgesimple.db.ReservoirHelper;
import com.njwyt.airpurgesimple.util.LocationUtil;

/**
 * Created by jason_samuel on 2018/2/25.
 * 启动页
 */

public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private final int APPLY_PERMISSIONS_CODE = 2;     // 申请权限request code

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        setLogoAaimation();
        initData();
        //startMainActivity();
        applyPermissions();
    }

    private void setLogoAaimation() {
        // 设置虚化层显示渐变动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        //设置动画持续时长
        alphaAnimation.setDuration(1000);

        binding.ivLogo.clearAnimation();
        binding.ivLogo.startAnimation(alphaAnimation);
    }

    private void startMainActivity() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }

    private void initData() {
        // 判断是否首次进入
        SharedPreferences shared = getSharedPreferences("airpurge", MODE_PRIVATE);
        boolean firstOpen = shared.getBoolean("firstOpen", true);
        if (firstOpen) {
            ReservoirHelper.INSTANCE.setIsAdmin(false);
        }
    }

    /**
     * 申请权限
     */
    private void applyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            // 申请定位权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    APPLY_PERMISSIONS_CODE);
        } else {
            getLocation();
            startMainActivity();
        }
    }

    /**
     * 申请权限回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case APPLY_PERMISSIONS_CODE: {
                // 判断用户选择允许还是拒绝
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 允许
                    getLocation();
                } else {
                    // 拒绝
                }
                startMainActivity();
            }
            break;
        }
    }

    private void getLocation() {

        LocationUtil.getLocation(getApplicationContext(), new LocationUtil.OnLocationGetListener() {
            @Override
            public void onLocationGet(BDLocation location) {
                String city = location.getCity();    //获取城市
                ReservoirHelper.INSTANCE.setCity(city);
            }
        });
    }
}
