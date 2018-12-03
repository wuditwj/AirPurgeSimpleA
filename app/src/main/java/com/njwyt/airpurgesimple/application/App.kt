package com.njwyt.airpurgesimple.application

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.*
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.anupcowkur.reservoir.Reservoir
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.*
import com.njwyt.airpurgesimple.service.ClientService
import com.njwyt.airpurgesimple.util.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by jason_samuel on 2018/2/26.
 */

class App : Application() {

    private val TAG = "--==>>"

    // 静态
    companion object {
        var instance: App? = null
    }

    private var typeFace: Typeface? = null
    private var mClientBinder: ClientService.ClientBinder? = null
    private var intent: Intent? = null

    private var mSocketData = SocketData()

    private var errorTime = 0           // 重新发送的错误次数

    //--------------------------------------------------------------------------------------------//
//    private lateinit var wifiManager: WifiManager
//    private lateinit var wifiAutoConnectManager: WifiAutoConnectManager
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var wifiConnect: WifiConnect

    private var isopen: Boolean = true//判断用户是否是开着wifi的,默认是打开的

    private lateinit var activitys: Activity


    /**
     * 当前Acitity个数
     */
    private var activityAount = 0

    /**
     * App前后台状态
     */
    var isForeground = false

    override fun onCreate() {
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        super.onCreate()
        instance = this
        changeFont()
        startClientService()

        // 初始化Reservoir
        Reservoir.init(this, (2048 * 2048).toLong())

        //判断APP前后台
    }

    /**
     * 开启Socket服务
     */
    private fun startClientService() {
        intent = Intent(this, ClientService::class.java)
        startService(intent)

        // 从服务中接收回调消息
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (service !is ClientService.ClientBinder) {
                    return
                }
                mClientBinder = service
                mClientBinder!!.getClientService().setCallBackListener(object : ClientService.CallBack {
                    override fun onDataChange(byteStringList: ArrayList<String>) {

                        // 转换成安卓里的实体类，通过数据长度判断传输类型
                        var listSize = byteStringList.size
                        // 判断464的时候，忽略掉后面的6位
                        if (listSize == 464) {
                            // 忽略后六位
                            for (i in 0 until 6) {
                                byteStringList.removeAt(byteStringList.size - 1)
                            }
                        }
                        listSize = byteStringList.size
                        when {
                            listSize == 6 -> {
                                checkDataIdentical(mSocketData)
                                sendSocketMessage()
                            }

                            listSize == 458 -> {
                                // 读取解析程序设置数据
                                checkDataIdentical(mSocketData)
                                readProgramData(byteStringList)
                            }
                        }

                        // 清空本次缓存数据
                        byteStringList.removeAll(byteStringList)
                    }
                })
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }

        }, Context.BIND_AUTO_CREATE)
    }

    private fun stopClientService() {
        mClientBinder!!.stopService()
    }

    /**
     * 切换字体
     */
    private fun changeFont() {
        typeFace = Typeface.createFromAsset(assets, "dongqing_font.otf")
        val field = Typeface::class.java.getDeclaredField("SERIF")
        field.isAccessible = true
        field.set(null, typeFace)
    }

    /**
     * 读取服务器发来的程序配置
     */
    private fun readProgramData(byteStringList: ArrayList<String>) {

        mSocketData = ParseEntity.parseList(byteStringList)
        ReservoirHelper.setSocketData(mSocketData)

        // 发送到各个页面处理
        // 房间数据
        val room = ParseEntity.socketToRoom(mSocketData.roomList)
        EventBus.getDefault().post(MessageEvent<List<Room>>(Type.ROOM_DATA, room))

        // 房间是否有传感器
        EventBus.getDefault().post(MessageEvent<List<Boolean>>(Type.HAVE_SENSOR, mSocketData.haveSensorList))

        // 风机功率
        val fanPowerList = mSocketData.fanPowerList
        // EventBus.getDefault().post(MessageEvent<List<Int>>(Type.FAN_PWOER, fanPowerList))

        // 风机队列
        val fanList = mSocketData.fanList
        EventBus.getDefault().post(MessageEvent<List<SocketFan>>(Type.FAN_PWOER, fanList))

        // 模式设置
        val systemMode = ParseEntity.socketToSystemMode(mSocketData.fanModeList, mSocketData.workMode)
        systemMode.runningTime = ParseEntity.socketToRunningTime((mSocketData.timeIntervalList))
        systemMode.nextMode = mSocketData.nextWorkMode
        EventBus.getDefault().post(MessageEvent<SystemMode>(Type.SYSTEM_MODE, systemMode))

        // 系统设置
        val systemSet = ParseEntity.socketToSystemSet(mSocketData)
        EventBus.getDefault().post(MessageEvent<SystemSet>(Type.SYSTEM_SET, systemSet))

        val dataManage = DataManage()
        dataManage.dataBackUp = mSocketData.dataBackUp
        dataManage.dataRestore = mSocketData.dataRestore

        // 保存到本地数据
        ReservoirHelper.setRoomList(room)
        ReservoirHelper.setFanPowerList(fanPowerList)
        ReservoirHelper.setFanList(fanList)
        ReservoirHelper.setSystemMode(systemMode)
        ReservoirHelper.setSystemSet(systemSet)
        ReservoirHelper.setDataManage(dataManage)
        //获得到数据,关掉弹窗
        if (sharedPreferencesHelper.getSharedPreference("dataisnull",false)==false) {
            sharedPreferencesHelper.put("dataisnull", true)
            while (ProgressDialogUtil.isShowing()) {
                ProgressDialogUtil.dismiss()
            }

        }
    }

    /**
     * 发给服务器的List转换成String
     */
    fun sendDataListToString(byteStringList: List<String>): String {
        val sb = StringBuffer()
        for (str in byteStringList) {
            sb.append(str)
        }
        return sb.toString()
    }

    /**
     * 把当前配置数据保存下来等收到服务器通知后进行上传
     */
    fun saveSocketMessage() {
        if(ParseEntity.praseBean()!=null) {
//        if (isSysWifi()) {
            // 把Android Bean，转化成服务器String List
            val byteStringList = ParseEntity.praseBean()
            // 把StringList存储到本地缓存中，预备上传
            ReservoirHelper.setSendData(byteStringList)
        }else{
//            Log.i("--==>>","正在初始化")
            ToastUtil.showToast(this,R.string.hint_initial)
        }
//        }
    }

    /**
     * 向服务器发送已设置的数据
     */
    private fun sendSocketMessage() {
        val sendData = ReservoirHelper.getSendData()
        if (sendData != null) {
            mClientBinder?.sendSocketData(sendDataListToString(sendData))
            ReservoirHelper.setIsSendData(true)
        }
    }

    /**
     * 数据确认同步后，清空数据设置
     */
    private fun cleanDataManage() {
        val dataManage = DataManage()
        dataManage.dataBackUp = 0
        dataManage.dataRestore = 0
        ReservoirHelper.setDataManage(dataManage)
    }

    /**
     * 检测广播数据与本地已上传数据的一致性
     */
    private fun checkDataIdentical(socketData: SocketData) {
        val sendData = ReservoirHelper.getSendData()
        // 已发送过数据，并且发送缓存并未清空
        if (ReservoirHelper.isSendData() && sendData != null) {
            // 判断模式是否一致
            Log.d(TAG, "-->> 工作模式选择 = ${sendData[141]}")
            val isSameWorkMode = sendData[141] == "0${socketData.nextWorkMode}"

            var isSameTime = true
            // 打开定时器的时，并且数据未同步时，才会去检测定时器的一致性
            if (ReservoirHelper.getSystemMode().openTimer && !ReservoirHelper.isSyncTime()) {
                // 判断定时器是否上传成功
                isSameTime = socketData.timeIntervalList[0][0] == sendData[229] &&
                        socketData.timeIntervalList[0][1] == sendData[230] &&
                        socketData.timeIntervalList[0][2] == sendData[231] &&
                        socketData.timeIntervalList[0][3] == sendData[232]
            }

            // 判断传感器数是否一致
            val isSameSensorCount = sendData[86] == "0${socketData.sensorCount}"

            // 判断数据管理是否一致
            val isSameDataMange = sendData[237] == "0${socketData.dataBackUp}" && sendData[238] == "0${socketData.dataRestore}"

            // 数据一致
            if (isSameWorkMode && isSameTime && isSameSensorCount && isSameDataMange) {

                errorTime = 0
                // 清空预备发送的数据
                ReservoirHelper.setSendData(null)
                // 设置为不是已发送状态
                ReservoirHelper.setIsSendData(false)
                // 确认定时器时间已同步
                ReservoirHelper.setIsSyncTime(true)
                cleanDataManage()
                // 通知页面停止等待框
                EventBus.getDefault().post(MessageEvent<List<Room>>(Type.DISMISS_PROGRESS_DIALOG, null))
            } else {

                // 数据不一致，超时判断
                errorTime++
                if (errorTime >= 10) {

                    // 清空预备发送的数据
                    ReservoirHelper.setSendData(null)
                    errorTime = 0
                    // 通知页面停止等待框
                    EventBus.getDefault().post(MessageEvent<List<Room>>(Type.DISMISS_PROGRESS_DIALOG_TIMEOUT, null))
                }
            }
        }
    }

//    //判断是否是系统wifi
//    private fun isSysWifi(): Boolean {
//
//        //判断当前连接是否系统wifi
////获得当前连接的wifiIP
//        val n = wifiAutoConnectManager.getInformation(this)
//        Log.i(TAG, "当前连接:   $n")
////        Log.i("--==>>","取消保存")
//        sharedPreferencesHelper = SharedPreferencesHelper(this, "")
//        //判断是否输入过wifiIP密码
//        var wifiIP = sharedPreferencesHelper.getSharedPreference("wifiname", "") as String
//        var wifiPas = sharedPreferencesHelper.getSharedPreference("wifipassword", "") as String
//        if (n != null) {
////                判断当前连接是否系统wifi
//            if (!n.equals(wifiIP)) {
//                ToastUtil.showToast(this, R.string.hint_connect)
//                return false
//            }
//        } else {
//            ToastUtil.showToast(this, R.string.hint_connect)
//            return false
//        }
//        return true
//    }

    /**
     * Activity 生命周期监听，用于监控app前后台状态切换
     */
    private var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            if (activityAount == 0) {
                //app回到前台
                isForeground = true
                activitys = activity
                initIsFirst2()
                initBroadcastReceiver()
                Log.i("--==>>", "回到前台")
            }
            activityAount++
        }

        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            activityAount--
            if (activityAount == 0) {
                isForeground = false
                delWifi2()
                unregisterReceiver(receiver)
                Log.i("--==>>", "回到后台")
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

//    private fun initIsFirst() {
//        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        wifiAutoConnectManager = WifiAutoConnectManager(wifiManager)
//        //判断当前wifi状态
//        isopen = wifiAutoConnectManager.isWiFiEnable(this)
//        Log.i(TAG, "当前wifi状态:   $isopen")
//        //获得当前连接的wifiIP
//        val n = wifiAutoConnectManager.getInformation(this)
//        Log.i(TAG, "当前连接:   $n")
////        Log.i("--==>>","取消保存")
//        sharedPreferencesHelper = SharedPreferencesHelper(this, "")
//        //判断是否输入过wifiIP密码
//        var wifiIP = sharedPreferencesHelper.getSharedPreference("wifiname", "") as String
//        var wifiPas = sharedPreferencesHelper.getSharedPreference("wifipassword", "") as String
//        Log.i(TAG, "wifi ip:   $wifiIP\nwifi password:   $wifiPas")
//        if (wifiIP == "" && wifiPas == "") {
//            wifiAutoConnectManager.close()
//            showInputDialog()
//        } else {
//            if (n != null) {
////                判断当前连接是否系统wifi
//                if (!n.equals(wifiIP)) {
//                    wifiAutoConnectManager.connect(wifiIP, wifiPas, WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
//                }
//            } else {
//                wifiAutoConnectManager.connect(wifiIP, wifiPas, WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
//            }
//        }
//    }

    private fun initIsFirst2() {
        wifiConnect= WifiConnect(this)
        //判断当前wifi状态
        isopen=wifiConnect.isWifiEnable
        Log.i(TAG, "当前wifi状态:   $isopen")
        //获得当前连接的wifiIP
        val n = wifiConnect.getInformation(this)
        Log.i(TAG, "当前连接:   $n")
//        Log.i("--==>>","取消保存")
        sharedPreferencesHelper = SharedPreferencesHelper(this, "")
        //判断是否输入过wifiIP密码
        val wifiIP = sharedPreferencesHelper.getSharedPreference("wifiname", "") as String
        val wifiPas = sharedPreferencesHelper.getSharedPreference("wifipassword", "") as String
        Log.i(TAG, "wifi ip:   $wifiIP\nwifi password:   $wifiPas")
        if (wifiIP == "" && wifiPas == "") {
            wifiConnect.closeWifi()
            showInputDialog()
        } else {
            if (n != null) {
//                判断当前连接是否系统wifi
                if (!n.equals(wifiIP)) {
                    wifiConnect.connectWifi(wifiIP,wifiPas,WifiConnect.SecurityMode.WPA2)
                }
            } else {
                wifiConnect.connectWifi(wifiIP,wifiPas,WifiConnect.SecurityMode.WPA2)
            }
        }
    }

    private fun showInputDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activitys)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_wifi, null)
        builder.setView(view)
        var dialog: AlertDialog = builder.show()
        var wifiOK: Button = view.findViewById(R.id.wifiOK)
        var wifiName: EditText = view.findViewById(R.id.wifiName)
        var wifiPassWord: EditText = view.findViewById(R.id.wifiPassWord)
        wifiOK.setOnClickListener {
            //将IP密码存储
            sharedPreferencesHelper.put("wifiname", wifiName.text)
            sharedPreferencesHelper.put("wifipassword", wifiPassWord.text)
            //判断当前本地数据是否为空
            sharedPreferencesHelper.put("dataisnull",false)
            //存完之后直接连接
            wifiConnect.connectWifi(sharedPreferencesHelper.getSharedPreference("wifiname", "") as String,
                    sharedPreferencesHelper.getSharedPreference("wifipassword", "") as String,
                    WifiConnect.SecurityMode.WPA2)
//            wifiAutoConnectManager.connect(sharedPreferencesHelper.getSharedPreference("wifiname", "") as String,
//                    sharedPreferencesHelper.getSharedPreference("wifipassword", "") as String,
//                    WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
            dialog.dismiss()
        }
        //点击屏幕不消失
        dialog.setCanceledOnTouchOutside(false)

    }

//    //退出当前连接并主动连接用户家自己的网络
//    private fun delWifi() {
//
//        //断开当前连接
//        wifiAutoConnectManager.dis()
//        Log.i("--=>>", "断开连接")
//        //取消保存网络
//        wifiAutoConnectManager.forget("\"" + sharedPreferencesHelper.getSharedPreference("wifiname", "") as String + "\"")
//        Log.i("--==>>", "忘记网络:   " + sharedPreferencesHelper.getSharedPreference("wifiname", "") as String)
//        //如果之前用户wifi是关闭着的,退出的时候就关闭,否则就打开
//        Log.i(TAG, "isopen=:$isopen")
//        if (!isopen) {
//            wifiAutoConnectManager.close()
//            Log.i(TAG, "关闭wifi")
//        }
//    }

    //退出当前连接并主动连接用户家自己的网络
    private fun delWifi2() {

        //断开当前连接
        wifiConnect.dis()
        Log.i("--=>>", "断开连接")
        //取消保存网络
        wifiConnect.forget("\"" + sharedPreferencesHelper.getSharedPreference("wifiname", "") as String + "\"")
        Log.i("--==>>", "忘记网络:   " + sharedPreferencesHelper.getSharedPreference("wifiname", "") as String)
        //如果之前用户wifi是关闭着的,退出的时候就关闭,否则就打开
        Log.i(TAG, "isopen=:$isopen")
        if (!isopen) {
            wifiConnect.closeWifi()
            Log.i(TAG, "关闭wifi")
        }
    }

    private fun initBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
//        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
//        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)

        registerReceiver(receiver, intentFilter)
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                when {
                    info.state == NetworkInfo.State.DISCONNECTED -> {
                        Log.i(TAG, "连接已断开")
                        ToastUtil.showToast(applicationContext, R.string.hint_connect_dis)
//                        titleTv.text = "连接已断开"
                    }
                    info.state == NetworkInfo.State.CONNECTED -> {
                        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo = wifiManager.connectionInfo
                        Log.i(TAG, "已连接到网络:" + wifiInfo.ssid)
                        while (ProgressDialogUtil.isShowing()) {
                            ProgressDialogUtil.dismiss()
                        }
                        ToastUtil.showToast(applicationContext, context.getString(R.string.hint_connect_suc) + wifiInfo.ssid)
//                        titleTv.text = "已连接到网络:" + wifiInfo.ssid
                        //判断是否是在系统网络
//                        if(wifiAutoConnectManager.getInformation(context).equals(sharedPreferencesHelper.getSharedPreference("wifiname", ""))){
                        //判断是否有数据,弹出初始化弹窗
                        if (sharedPreferencesHelper.getSharedPreference("dataisnull", false) == false) {
                            if (!ProgressDialogUtil.isShowing()) {
                                ProgressDialogUtil.show(activitys, R.string.hint_initial)
                            }
                        }

                    }
                    else -> {
                        val state = info.detailedState
                        when (state) {
                            NetworkInfo.DetailedState.CONNECTING -> {
                                // 弹出等待框不给用户操作
                                ToastUtil.showToast(applicationContext, R.string.hint_connecting)
                                if (!ProgressDialogUtil.isShowing()) {
                                    ProgressDialogUtil.show(activitys, R.string.hint_connecting)
                                }
                            }
                            NetworkInfo.DetailedState.AUTHENTICATING -> {
                                ToastUtil.showToast(applicationContext, R.string.hint_identity)
                            }
                            NetworkInfo.DetailedState.OBTAINING_IPADDR -> {
                                ToastUtil.showToast(applicationContext, R.string.hint_get_ip)
                            }
                            NetworkInfo.DetailedState.FAILED -> {
                                ToastUtil.showToast(applicationContext, R.string.hint_connect_fail)
                                Log.i(TAG, "连接失败")
                            }

                        }
                    }
                }

            }
        }
    }

}
