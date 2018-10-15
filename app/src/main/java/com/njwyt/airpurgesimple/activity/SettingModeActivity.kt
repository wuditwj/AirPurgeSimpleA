package com.njwyt.airpurgesimple.activity

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.application.App
import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.MessageEvent
import com.njwyt.airpurgesimple.entity.SystemMode
import com.njwyt.airpurgesimple.util.*
import kotlinx.android.synthetic.main.activity_setting_mode.*
import kotlinx.android.synthetic.main.dialog_wifi.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 *
 * Created by jason_samuel on 2018/2/25.
 * 模式选择
 */

class SettingModeActivity : BaseActivity() {

    val TAG = "SettingModeActivity"

    var mSystemMode: SystemMode? = null
    var selectedMode = 1                    // 默认选中模式
    var reflashHandler = Handler()

    //--------------------------------------------------------------------------------------------//
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiAutoConnectManager: WifiAutoConnectManager
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_setting_mode)
        //initData()
        //initView()
        initToolBar()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        initIsFirst()
        initData()
        initView()

        initWifi()
    }

    private fun initIsFirst() {
        wifiManager=applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiAutoConnectManager= WifiAutoConnectManager(wifiManager)
        //获得当前连接的wifiIP
        wifiAutoConnectManager.getInformation(this)
//        //断开当前连接
//        wifiAutoConnectManager.dis()
//
//        wifiAutoConnectManager.forget("\""+"njwyt"+"\"")
        Log.i("--==>>","取消保存")
        sharedPreferencesHelper= SharedPreferencesHelper(this,"")
//        var isFirst= sharedPreferencesHelper.getSharedPreference("isfirst",true)
//        //判断是否首次打开APP
//        if (isFirst as Boolean){
//            isFirst=sharedPreferencesHelper.put("isfirst",false)
//            showInputDialog()
//        }else{
//            wifiAutoConnectManager.connect(sharedPreferencesHelper.getSharedPreference("wifiname","")as String,
//                    sharedPreferencesHelper.getSharedPreference("wifipassword","")as String ,
//                    WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
//            Log.i("--==>>","wifi ip:   "+sharedPreferencesHelper.getSharedPreference("wifiname","")as String+"\n"+
//                    "wifi password:   "+sharedPreferencesHelper.getSharedPreference("wifipassword","")as String )
//
//        }
        //判断是否输入过wifiIP密码
        var wifiIP=sharedPreferencesHelper.getSharedPreference("wifiname","")as String
        var wifiPas=sharedPreferencesHelper.getSharedPreference("wifipassword","")as String
        if (wifiIP == "" && wifiPas == ""){
//            isFirst=sharedPreferencesHelper.put("isfirst",false)
            showInputDialog()
        }else{
            //判断当前连接是否系统wifi
            if(wifiAutoConnectManager.getInformation(this)!=wifiIP) {
                wifiAutoConnectManager.connect(wifiIP, wifiPas, WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
                Log.i("--==>>", "wifi ip:   $wifiIP\nwifi password:   $wifiPas")
            }
        }

    }

    private fun showInputDialog() {
        val builder:AlertDialog.Builder=AlertDialog.Builder(this)
        val view:View=LayoutInflater.from(this).inflate(R.layout.dialog_wifi,null)
        builder.setView(view)
        var dialog:AlertDialog=builder.show()
        var wifiOK:Button=view.findViewById(R.id.wifiOK)
        var wifiName:EditText=view.findViewById(R.id.wifiName)
        var wifiPassWord:EditText=view.findViewById(R.id.wifiPassWord)
        wifiOK.setOnClickListener{
            //将IP密码存储
            sharedPreferencesHelper.put("wifiname",wifiName.text)
            sharedPreferencesHelper.put("wifipassword",wifiPassWord.text)
            //存完之后直接连接
            wifiAutoConnectManager.connect(sharedPreferencesHelper.getSharedPreference("wifiname","")as String,
                    sharedPreferencesHelper.getSharedPreference("wifinpassword","")as String ,
                    WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
        dialog.dismiss()
        }
        //点击屏幕不消失
        dialog.setCanceledOnTouchOutside(false)

    }

    private fun initWifi() {


    }

    /**
     * 从本地数据库中读取上一次数据
     */
    private fun initData() {

        mSystemMode = ReservoirHelper.getSystemMode()
        selectedMode = mSystemMode!!.selectedMode
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
                    //initView()
                }
            }

        // 发送成功
            Type.DISMISS_PROGRESS_DIALOG -> {

                if (ProgressDialogUtil.dismissByActivity(this@SettingModeActivity)) {
                    // 新开启线程弹通知
                    reflashHandler.post {
                        ToastUtil.showToast(this@SettingModeActivity, R.string.hint_edit_success)
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
                    ToastUtil.showToast(this@SettingModeActivity, R.string.hint_timeout)
                }
                if (ProgressDialogUtil.isShowing()) {
                    ProgressDialogUtil.dismissByActivity(this@SettingModeActivity)
                }
            }
        }
    }

    private fun initToolBar() {
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText(R.string.set_mode)
        tv_back.setOnClickListener {
            finish()
        }
        setSupportActionBar(tool_bar)
    }

    private fun initView() {

        when (mSystemMode?.selectedMode) {
            0 -> rb_shutdown.isChecked = true
            1 -> rb_intelligent.isChecked = true
            2 -> rb_strong.isChecked = true
            3 -> rb_medium.isChecked = true
            4 -> rb_weak.isChecked = true
            5 -> rb_force.isChecked = true
            6 -> rb_formalde_removal.isChecked = true
            else -> rb_intelligent.isChecked = true
        }

        when (mSystemMode?.openTimer) {
            true -> sw_timer.isChecked = true
        }

        sw_timer.isChecked = ReservoirHelper.getSystemMode().runningTime.time > 0
        tv_use_timer.setText(if (sw_timer.isChecked) R.string.enabled else R.string.disable)
    }

    private fun initListener() {

        // 单选按钮被选中
        rg_mode.setOnCheckedChangeListener { xRadioGroup, checkId ->

            when (checkId) {
                rb_shutdown.id -> selectedMode = 0
                rb_intelligent.id -> selectedMode = 1
                rb_strong.id -> selectedMode = 2
                rb_medium.id -> selectedMode = 3
                rb_weak.id -> selectedMode = 4
                rb_force.id -> selectedMode = 5
                rb_formalde_removal.id -> selectedMode = 6
            }
            sw_timer.isChecked = false
        }

        // 多选按钮被选中
        sw_timer.setOnCheckedChangeListener { compoundButton, b ->
            tv_use_timer.setText(if (b) R.string.enabled else R.string.disable)
        }

        // TODO 智能模式暂时不能进入
        /*ll_intelligent.setOnClickListener {
            val intent = Intent(this, SettingIntelligentParmActivity::class.java)
            startActivity(intent)
        }*/

//        ll_strong.setOnClickListener {
//            val intent = Intent(this, SettingWindsParmActivity::class.java)
//            intent.putExtra("systemMode", mSystemMode)
//            intent.putExtra("selectMode", 2)
//            startActivity(intent)
//        }
//
//        ll_medium.setOnClickListener {
//            val intent = Intent(this, SettingWindsParmActivity::class.java)
//            intent.putExtra("systemMode", mSystemMode)
//            intent.putExtra("selectMode", 3)
//            startActivity(intent)
//        }
//
//        ll_weak.setOnClickListener {
//            val intent = Intent(this, SettingWindsParmActivity::class.java)
//            intent.putExtra("systemMode", mSystemMode)
//            intent.putExtra("selectMode", 4)
//            startActivity(intent)
//        }
//
//        // 除甲醛
//        ll_formalde_removal.setOnClickListener {
//            val intent = Intent(this, SettingWindsParmActivity::class.java)
//            intent.putExtra("systemMode", mSystemMode)
//            intent.putExtra("selectMode", 5)
//            startActivity(intent)
//        }
//
//        // 定时
//        ll_timer.setOnClickListener {
//            val intent = Intent(this, RunningTimeSimpleActivity::class.java)
//            startActivity(intent)
//        }

        btn_ok.setOnClickListener {

            //判断当前连接是否系统wifi
            var wifiIP=sharedPreferencesHelper.getSharedPreference("wifiname","")as String
            if(wifiAutoConnectManager.getInformation(this)!=wifiIP) {
                Log.i("--==>>","当前状态不是系统wifi")
                wifiAutoConnectManager.connect(wifiIP,
                        sharedPreferencesHelper.getSharedPreference("wifipassword","")as String,
                        WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA)
            }
            //todo

            // 保存当前数据
            val systemMode = ReservoirHelper.getSystemMode()
            systemMode.selectedMode = selectedMode
            if (systemMode.openTimer != sw_timer.isChecked) {
                ReservoirHelper.setIsSyncTime(false)    // 定时器设为未同步状态
            }
            systemMode.openTimer = sw_timer.isChecked
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

    override fun onStop() {
        //断开当前连接
        wifiAutoConnectManager.dis()
        Log.i("--=>>","断开连接")
        //取消保存网络
        wifiAutoConnectManager.forget("\""+sharedPreferencesHelper.getSharedPreference("wifiname","") as String+"\"")
        Log.i("--==>>","忘记网络:   "+sharedPreferencesHelper.getSharedPreference("wifiname","")as String )
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }



}
