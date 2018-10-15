package com.njwyt.airpurgesimple.activity

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.application.App
import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.MessageEvent
import com.njwyt.airpurgesimple.entity.SystemMode
import com.njwyt.airpurgesimple.util.*
import kotlinx.android.synthetic.main.activity_running_time_simple.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by jason_samuel on 2018/3/2.
 * 时段查看
 */
class RunningTimeSimpleActivity : BaseActivity() {

    val TAG = "--==>>"

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiAutoConnectManager: WifiAutoConnectManager
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var reflashHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_running_time_simple)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiAutoConnectManager = WifiAutoConnectManager(wifiManager)
        initView()
        initData()
        initToolBar()
        initListener()
    }


    /**
     * 从Application实时获取数据
     */
    @Subscribe
    fun getSystemModeByEventBus(msg: MessageEvent<SystemMode>) {

        when (msg.message) {
            Type.DISMISS_PROGRESS_DIALOG -> {

                if (ProgressDialogUtil.dismissByActivity(this@RunningTimeSimpleActivity)) {
                    // 新开启线程弹通知
                    reflashHandler.post({
                        ToastUtil.showToast(this@RunningTimeSimpleActivity, R.string.hint_edit_success)
                    })
                    finish()
                }
            }

        // 发送超时
            Type.DISMISS_PROGRESS_DIALOG_TIMEOUT -> {

                // 新开启线程弹通知
                reflashHandler.post({
                    ToastUtil.showToast(this@RunningTimeSimpleActivity, R.string.hint_timeout)
                })
                ProgressDialogUtil.dismissByActivity(this@RunningTimeSimpleActivity)
            }
        }
    }

    private fun initToolBar() {
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText(R.string.timer)
        tv_back.setOnClickListener {
            finish()
        }
        setSupportActionBar(tool_bar)
    }

    private fun initData() {
        np_timer.value = ReservoirHelper.getSystemMode().runningTime.time.toInt() / 60 / 60

        when (ReservoirHelper.getSystemMode().nextMode) {
            0 -> np_mode.value = 6  // 关机
            1 -> np_mode.value = 0  // 智能
            2 -> np_mode.value = 1  // 强
            3 -> np_mode.value = 2  // 中
            4 -> np_mode.value = 3  // 弱
            5 -> np_mode.value = 5  // 直排
            6 -> np_mode.value = 4  // 除甲醛
            else -> np_mode.value = 0     // 智能
        }
    }

    private fun initView() {
        np_timer.displayedValues = Type.TIMER_TYPE
        np_mode.displayedValues = Type.MODE_TYPE
    }

    //判断是否是系统wifi
    private fun isSysWifi() : Boolean{

        //判断当前连接是否系统wifi
//获得当前连接的wifiIP
        val n=wifiAutoConnectManager.getInformation(this)
        Log.i(TAG,"当前连接:   $n")
//        Log.i("--==>>","取消保存")
        sharedPreferencesHelper = SharedPreferencesHelper(this, "")
        //判断是否输入过wifiIP密码
        var wifiIP = sharedPreferencesHelper.getSharedPreference("wifiname", "") as String
        var wifiPas = sharedPreferencesHelper.getSharedPreference("wifipassword", "") as String
        if (n!=null) {
//                判断当前连接是否系统wifi
            if (!n .equals(wifiIP) ) {
                ToastUtil.showToast(this,R.string.hint_connect)
                return false
            }
        }else{
            ToastUtil.showToast(this,R.string.hint_connect)
            return false
        }
        return true
    }

    private fun initListener() {
        btn_ok.setOnClickListener({
            if(isSysWifi()) {
                // 设置定时时长
                val systemMode = ReservoirHelper.getSystemMode()
                systemMode.runningTime.time = (np_timer.value * 60 * 60).toLong()    // 把小时转换成秒
                // 如果用户设置了0小时，则为定时器立即取消
                systemMode.openTimer = systemMode.runningTime.time != 0L
                when (np_mode.value) {
                    0 -> systemMode.nextMode = 1
                    1 -> systemMode.nextMode = 2
                    2 -> systemMode.nextMode = 3
                    3 -> systemMode.nextMode = 4
                    4 -> systemMode.nextMode = 6
                    5 -> systemMode.nextMode = 5
                    6 -> systemMode.nextMode = 0
                }
                ReservoirHelper.setSystemMode(systemMode)
                ReservoirHelper.setIsSyncTime(false)    // 定时器设为未同步状态
                // 通知系统提交
                App.instance?.saveSocketMessage()

                // 弹出等待框不给用户操作
                ProgressDialogUtil.show(this, R.string.waiting)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}