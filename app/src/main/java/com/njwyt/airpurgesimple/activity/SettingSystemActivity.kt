package com.njwyt.airpurgesimple.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.application.App
import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.databinding.ActivitySettingSystemBinding
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.MessageEvent
import com.njwyt.airpurgesimple.entity.SystemSet
import com.njwyt.airpurgesimple.util.ActivityUtil
import com.njwyt.airpurgesimple.util.ProgressDialogUtil
import com.njwyt.airpurgesimple.util.ToastUtil
import kotlinx.android.synthetic.main.activity_setting_system.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SettingSystemActivity : BaseActivity() {

    private var binding: ActivitySettingSystemBinding? = null
    private var reflashHandler = Handler()
    private var openAdminClickHandler = Handler()        // 打开管理员模式点击倒计时
    private var openAdminClickTime = 0                  // 打开管理员点击次数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_system)
        binding!!.wifiIP.setText(ReservoirHelper.getIP())
        initToolBar()
        initView()
        initListener()
    }

    //设置标题栏
    private fun initToolBar() {
        /*binding!!.toolBar.title = ""
        binding!!.titleTextView.text = getString(R.string.set_system)
        setSupportActionBar(binding!!.toolBar)
        //设置返回键
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding!!.toolBar.setNavigationOnClickListener {
            ReservoirHelper.setIP(binding!!.wifiIP.text.toString())
            finish()
        }*/
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText(R.string.set_system)
        tv_back.setOnClickListener {
            finish()
        }
        // 通过点击10次，在开发者模式与普通模式间切换
        titleTextView.setOnClickListener {

            openAdminClickTime++

            // 点击10次以后打开开发者模式
            if (openAdminClickTime == 10) {
                if (ReservoirHelper.getIsAdmin()) {
                    ToastUtil.showToast(SettingSystemActivity@ this, R.string.hint_lock_admin)
                    ReservoirHelper.setIsAdmin(false)
                    np_sensor_count.isEnabled = false
                } else {
                    ToastUtil.showToast(SettingSystemActivity@ this, R.string.hint_unlock_admin)
                    ReservoirHelper.setIsAdmin(true)
                    np_sensor_count.isEnabled = true
                }
                openAdminClickTime = 0
            }

            if (openAdminClickTime in 5..9) {
                if (ReservoirHelper.getIsAdmin()) {
                    ToastUtil.showToast(SettingSystemActivity@ this,  String.format(resources.getString(R.string.hint_lock_click_time), 10 - openAdminClickTime))
                } else {
                    ToastUtil.showToast(SettingSystemActivity@ this,  String.format(resources.getString(R.string.hint_unlock_click_time), 10 - openAdminClickTime))
                }
            }

            // 重新开始计时
            openAdminClickHandler.removeCallbacks(openAdminClickRunnable)
            openAdminClickHandler.postDelayed(openAdminClickRunnable, 2000)

        }
        setSupportActionBar(tool_bar)
    }

    private val openAdminClickRunnable = Runnable {
        openAdminClickTime = 0
    }

    @Subscribe
    fun getSystemSetDataByEventbus(msg: MessageEvent<SystemSet>) {

        when (msg.message) {
            Type.DISMISS_PROGRESS_DIALOG -> {
                if (ProgressDialogUtil.dismissByActivity(this@SettingSystemActivity)) {
                    // 新开启线程弹通知
                    reflashHandler.post {
                        ToastUtil.showToast(this@SettingSystemActivity, R.string.hint_edit_success)
                    }
                    finish()
                }
            }

        // 发送超时
            Type.DISMISS_PROGRESS_DIALOG_TIMEOUT -> {

                // 新开启线程弹通知
                reflashHandler.post {
                    ToastUtil.showToast(this@SettingSystemActivity, R.string.hint_timeout)
                }
                ProgressDialogUtil.dismissByActivity(this@SettingSystemActivity)
            }
        }
    }

    private fun initView() {

        val systemSet = ReservoirHelper.getSystemSet()
        np_sensor_count.value = systemSet.sensorCount
        roomHight.setText(systemSet.roomHight.toString())//层高
        peopleNum.setText(systemSet.peopleNum.toString())//居住人数
        useTime.setText(systemSet.useTime.toString())//过滤器使用时间
        motorPower.setText(systemSet.motorPower.toString())//电机功率

        if (!ReservoirHelper.getIsAdmin()) {
            np_sensor_count.isEnabled = false
        }
    }

    private fun initListener() {
        binding!!.btnOk.setOnClickListener {
            // 保存当前数据
            val systemSet = ReservoirHelper.getSystemSet()
            systemSet.sensorCount = np_sensor_count.value
            ReservoirHelper.setSystemSet(systemSet)
            // 通知系统提交
            App.instance?.saveSocketMessage()

            // 弹出等待框不给用户操作
            ProgressDialogUtil.show(this, R.string.waiting)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}


