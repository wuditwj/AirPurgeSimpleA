package com.njwyt.airpurgesimple.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Date:    2018/7/4
 * Time:    9:23
 * author:  Twj
 */
public class WifiAutoConnectManager {

    private static final String TAG = "--==>>";

    WifiManager wifiManager;


    // 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    // 构造函数
    public WifiAutoConnectManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    /**
     * 提供一个外部接口，传入要连接的无线网
     *
     * @param ssid
     * @param password
     * @param type
     */
    public void connect(String ssid, String password, WifiCipherType type) {
        Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
        thread.start();
        Log.i(TAG, "开启连接线程");
    }

    /**
     * 判断当前wifi状态是否是打开
     *
     * @param context
     * @return
     */
    public boolean isWiFiEnable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return !((info == null) || (!info.isAvailable())) && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 获取当前连接的WiFiIP
     */
    public String getInformation(Context context) {
        if (isWiFiEnable(context)) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            //去掉双引号
            return wifiInfo.getSSID().replace("\"", "");
        }else {
            return null;
        }
    }

    /**
     * 断开开当前连接
     */
    public void dis() {
        wifiManager.disconnect();
    }

    /**
     * 关闭wifi
     */
    public void close() {
        wifiManager.setWifiEnabled(false);
    }

    /**
     * 取消保存网络
     *
     * @param targetssid
     */
    public void forget(String targetssid) {
        List<WifiConfiguration> wifiConfigs = wifiManager.getConfiguredNetworks();

        if(wifiConfigs!=null) {
            for (WifiConfiguration wifiConfig : wifiConfigs) {
                String ssid = wifiConfig.SSID;
//                Log.i(TAG, "removeWifiBySsid ssid=" + ssid);
                if (ssid.equals(targetssid)) {
                    wifiManager.removeNetwork(wifiConfig.networkId);
                    wifiManager.saveConfiguration();
                    Log.i(TAG, "removieWifiBySsid success, SSID = " + wifiConfig.SSID + " netId = " + String.valueOf(wifiConfig.networkId));
//                    Log.i(TAG, "removeSSID");
                }
            }
        }
    }

    // 查看以前是否也配置过这个网络
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs!=null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
// nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
// wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
// wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
// 此处需要修改否则不能自动重联
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    // 打开wifi功能
    private boolean openWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
            Log.i(TAG, "打开wifi");
        }
        return bRet;
    }

    class ConnectRunnable implements Runnable {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public ConnectRunnable(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }

        @Override
        public void run() {
// 打开wifi
            openWifi();
// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
            while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                try {
// 为了避免程序一直while循环，让它睡个100毫秒检测……
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }
            }

            WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
//
            if (wifiConfig == null) {
                Log.i(TAG, "wifiConfig is null!");
                return;
            }

            WifiConfiguration tempConfig = isExsits(ssid);

            if (tempConfig != null) {
                wifiManager.removeNetwork(tempConfig.networkId);
            }

            int netID = wifiManager.addNetwork(wifiConfig);
            boolean enabled = wifiManager.enableNetwork(netID, true);
            boolean connected = wifiManager.reconnect();
        }
    }

    private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
                return false;
            }
        }

        return true;
    }


//    public BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
////                        text_state.setText("连接已断开");
//                    Log.i(TAG, "连接已断开");
//                    tv.setText("连接已断开");
//                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
//                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
////                        text_state.setText("已连接到网络:" + wifiInfo.getSSID());
//                    Log.i(TAG, "已连接到网络:" + wifiInfo.getSSID());
//                    tv.setText("已连接到网络:" + wifiInfo.getSSID());
//                } else {
//                    NetworkInfo.DetailedState state = info.getDetailedState();
//                    if (state == NetworkInfo.DetailedState.CONNECTING) {
////                        text_state.setText("连接中...");
//                        Log.i(TAG, "连接中...");
//                        tv.setText("连接中...");
//                    } else if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
////                        text_state.setText("正在验证身份信息...");
//                        Log.i(TAG, "正在验证身份信息...");
//                        tv.setText("正在验证身份信息...");
//                    } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
////                        text_state.setText("正在获取IP地址...");
//                        Log.i(TAG, "正在获取IP地址...");
//                        tv.setText("正在获取IP地址...");
//                    } else if (state == NetworkInfo.DetailedState.FAILED) {
////                        text_state.setText("连接失败");
//                        Log.i(TAG, "连接失败");
//                        tv.setText("连接失败");
//                    }
//                }
//
//            }
//        }
//    };

    }
