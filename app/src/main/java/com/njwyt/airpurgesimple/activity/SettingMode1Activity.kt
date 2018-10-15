package com.njwyt.airpurgesimple.activity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.application.App
import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.MessageEvent
import com.njwyt.airpurgesimple.entity.SystemMode
import com.njwyt.airpurgesimple.util.ProgressDialogUtil
import com.njwyt.airpurgesimple.util.SharedPreferencesHelper
import com.njwyt.airpurgesimple.util.ToastUtil
import com.njwyt.airpurgesimple.util.WifiAutoConnectManager
import kotlinx.android.synthetic.main.activity_setting_mode1.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
Date:    2018/7/18
Time:    10:43
author:  Twj
 */
class SettingMode1Activity : BaseActivity() {

    val TAG = "--==>>"

    var mSystemMode: SystemMode? = null
    var selectedMode = 1                    // 默认选中模式
    var reflashHandler = Handler()
    //--------------------------------------------------------------------------------------------//
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiAutoConnectManager: WifiAutoConnectManager
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper


    private lateinit var modeList: kotlin.Array<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_setting_mode1)

        init()
    }

    override fun onResume() {
        super.onResume()
        initIsFirst()
        initData()
        initView()
    }


    private fun initIsFirst() {
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiAutoConnectManager = WifiAutoConnectManager(wifiManager)
    }

    private fun init() {
        modeList = arrayOf(ly_shutdown, ly_intelligent, ly_strong, ly_medium, ly_weak,
                ly_force, ly_formalde_removal)
        ly_timer.setOnClickListener {
            val intent = Intent(this, RunningTimeSimpleActivity::class.java)
            startActivity(intent)
        }
        //防止打开APP的时候会自动监听
        val swTouchListener = View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    sw_time.setOnCheckedChangeListener(timeCheckLintener)
                    Log.i(TAG, "down")
                }
                MotionEvent.ACTION_MOVE -> {
                    sw_time.setOnCheckedChangeListener(timeCheckLintener)
                    Log.i(TAG, "move")
                }
                MotionEvent.ACTION_UP -> {
                    sw_time.setOnCheckedChangeListener(timeCheckLintener)
                    Log.i(TAG, "up")
                }
            }
            false
        }
        sw_time.setOnTouchListener(swTouchListener)

    }

    var timeCheckLintener: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
        setTimer()
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


    private fun setTimer() {
        if (isSysWifi()) {
            // 保存当前数据
            val systemMode = ReservoirHelper.getSystemMode()
            systemMode.selectedMode = selectedMode
            if (systemMode.openTimer != sw_time.isChecked) {
                ReservoirHelper.setIsSyncTime(false)    // 定时器设为未同步状态
            }
            systemMode.openTimer = sw_time.isChecked
            if (systemMode.openTimer && systemMode.runningTime.time == 0L) {
                // 定时器打开后，并且时间为0，时间默认为1小时(3600秒)，并且固定为关机
                systemMode.runningTime.time = 3600
                systemMode.nextMode = 0
                ReservoirHelper.setIsSyncTime(false)    // 定时器设为未同步状态
            }
            ReservoirHelper.setSystemMode(systemMode)
            // 通知系统提交
            App.instance?.saveSocketMessage()

            // 弹出等待框不给用户操作
            ProgressDialogUtil.show(this, R.string.waiting)
        }
    }


    private fun setSelect(selectedMode: Int) {

        if (isSysWifi()) {
            sw_time.isChecked=false
            // 保存当前数据
            val systemMode = ReservoirHelper.getSystemMode()
            systemMode.selectedMode = selectedMode
            if (systemMode.openTimer != sw_time.isChecked) {
                ReservoirHelper.setIsSyncTime(false)    // 定时器设为未同步状态
            }
            systemMode.openTimer = sw_time.isChecked
            if (systemMode.openTimer && systemMode.runningTime.time == 0L) {
                // 定时器打开后，并且时间为0，时间默认为1小时(3600秒)，并且固定为关机
                systemMode.runningTime.time = 3600
                systemMode.nextMode = 0
                ReservoirHelper.setIsSyncTime(false)    // 定时器设为未同步状态
            }
            ReservoirHelper.setSystemMode(systemMode)
            // 通知系统提交
            App.instance?.saveSocketMessage()

            // 弹出等待框不给用户操作
            ProgressDialogUtil.show(this, R.string.waiting)
        }
    }

    /**
     * 从Application实时获取数据
     */
    @Subscribe
    fun getSystemModeByEventBus(msg: MessageEvent<SystemMode>) {

        when (msg.message) {
        // 系统模式切换
            Type.SYSTEM_MODE -> {
                reflashHandler.post {
                    mSystemMode = msg.body
                    //实时刷新模式
                    initView()
                }
            }

        // 发送成功
            Type.DISMISS_PROGRESS_DIALOG -> {

                if (ProgressDialogUtil.dismissByActivity(this@SettingMode1Activity)) {
                    // 新开启线程弹通知
                    reflashHandler.post {
                        ToastUtil.showToast(this@SettingMode1Activity, R.string.hint_edit_success)
                        sw_time.setOnCheckedChangeListener(null)
                        initData()
                        initView()
                    }
                    // finish()
                }
            }

        // 发送超时
            Type.DISMISS_PROGRESS_DIALOG_TIMEOUT -> {

                // 新开启线程弹通知
                reflashHandler.post {
                    ToastUtil.showToast(this@SettingMode1Activity, R.string.hint_timeout)
                    sw_time.setOnCheckedChangeListener(null)
                }
                if (ProgressDialogUtil.isShowing()) {
                    ProgressDialogUtil.dismissByActivity(this@SettingMode1Activity)
                }
            }
        }
    }

    /**
     * 从本地数据库中读取上一次数据
     */
    private fun initData() {

        mSystemMode = ReservoirHelper.getSystemMode()
        selectedMode = mSystemMode!!.selectedMode
    }

    private fun initView() {

        setBackground(mSystemMode?.selectedMode)

        when (mSystemMode?.openTimer) {
            true -> sw_time.isChecked = true
            false -> sw_time.isChecked = false
        }

        sw_time.isChecked = ReservoirHelper.getSystemMode().runningTime.time > 0
    }

    //设置当前模式的背景颜色
    private fun setBackground(selectedMode: Int?) {
        for (i in modeList.indices) {
            modeList[i].setBackgroundResource(R.drawable.set_mode1_shape)
            modeList[i].setOnClickListener {
                setSelect(i)
            }
            if (i == selectedMode) {
                modeList[i].setBackgroundResource(R.color.colorLine)
                modeList[i].setOnClickListener { null }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


}

