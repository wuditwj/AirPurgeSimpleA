package com.njwyt.airpurgesimple.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.njwyt.airpurgesimple.util.NetUtil;

public class NetBroadcastReceiver extends BroadcastReceiver {
 
    public OnNetEventListener evevt;
 
    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = NetUtil.getNetWorkState(context);
            // 接口回调传过去状态的类型
            evevt.onNetChange(netWorkState);
        }
    }

    public void setNetEventChangeListener(OnNetEventListener evevt) {
        this.evevt = evevt;
    }
 
    // 自定义接口
    public interface OnNetEventListener {
        public void onNetChange(int netMobile);
    }
}