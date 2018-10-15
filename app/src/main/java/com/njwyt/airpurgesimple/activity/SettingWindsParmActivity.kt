package com.njwyt.airpurgesimple.activity

import android.os.Bundle
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.application.App
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.SystemMode
import com.njwyt.airpurgesimple.entity.WindsParm
import com.njwyt.airpurgesimple.view.ObservableScrollView
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_setting_winds_parm.*

/**
 * Created by jason_samuel on 2018/2/25.
 * 强中弱参数调整
 */

class SettingWindsParmActivity : BaseActivity() {

    var mCurrentWindsParm: WindsParm? = null            // 当前显示
    var mWindsParmList: ArrayList<WindsParm>? = null
    var mSelectMode: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_winds_parm)
        initData()
        initToolBar()
        initView()
        initListener()
    }

    private fun initData() {
        mWindsParmList = (intent.getSerializableExtra("systemMode") as SystemMode?)?.windsParmList
        mSelectMode = intent.getIntExtra("selectMode", 2)

        when (mSelectMode) {
            2 -> mSelectMode = 0     // 强
            3 -> mSelectMode = 1     // 中
            4 -> mSelectMode = 2     // 弱
            5 -> mSelectMode = 3     // 除甲醛
        }

        mCurrentWindsParm = mWindsParmList?.elementAtOrNull(mSelectMode)
    }

    private fun initToolBar() {
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText("${resources.getString(R.string.parm_set)} [${resources.getStringArray(R.array.winds).elementAtOrNull(mSelectMode + 1)}]")
        tv_back.setOnClickListener {
            finish()
        }
        setSupportActionBar(tool_bar)
    }

    private fun initView() {

        val newWindTime = mCurrentWindsParm?.newWindTime?.div(60)?.toFloat()
        tv_minute_new_wind.text = "${newWindTime?.toInt() ?: 0}"
        bs_new_wind_time.setProgress(newWindTime ?: 0f)

        val intervalTime = mCurrentWindsParm?.intervalTime?.div(60)?.toFloat()
        tv_minute_interval.text = "${intervalTime?.toInt() ?: 0}"
        bs_interval_time.setProgress(intervalTime ?: 0f)

        val maxPower = mCurrentWindsParm?.maxPower
        tv_watt_max.text = "${maxPower ?: 100}"
        bs_fan_max_power.setProgress(maxPower?.toFloat() ?: 100f)

        val minPower = mCurrentWindsParm?.minPower
        tv_watt_min.text = "${minPower ?: 1}"
        bs_fan_min_power.setProgress(minPower?.toFloat() ?: 1f)

        // todo 该版本的滑动条都禁止使用
        bs_new_wind_time.isEnabled = false
        bs_interval_time.isEnabled = false
        bs_fan_max_power.isEnabled = false
        bs_fan_min_power.isEnabled = false
        bs_pm2_5.isEnabled = false
        bs_voc.isEnabled = false
    }

    private fun initListener() {

        tv_back.setOnClickListener {
            finish()
        }

        btn_ok.setOnClickListener {

            // 保存当前数据
            val systemMode = ReservoirHelper.getSystemMode()
            systemMode.windsParmList.set(mSelectMode, mCurrentWindsParm!!)
            ReservoirHelper.setSystemMode(systemMode)
            // 通知系统提交
            App.instance?.saveSocketMessage()
            finish()
        }

        // 布局上下滑动时，刷新横向滑动条的显示
        scroll_view.setOnScrollChangedListener(object : ObservableScrollView.OnScrollChangedListener {
            override fun onScrollChanged(top: Int, oldTop: Int) {
                bs_new_wind_time.correctOffsetWhenContainerOnScrolling()
                bs_interval_time.correctOffsetWhenContainerOnScrolling()
                bs_fan_max_power.correctOffsetWhenContainerOnScrolling()
                bs_fan_min_power.correctOffsetWhenContainerOnScrolling()
                bs_pm2_5.correctOffsetWhenContainerOnScrolling()
                bs_voc.correctOffsetWhenContainerOnScrolling()
            }
        })

        bs_new_wind_time.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                tv_minute_new_wind.text = "${progress}"
                mCurrentWindsParm?.newWindTime = progress * 60
            }

        }

        bs_interval_time.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                tv_minute_interval.text = "${progress}"
                mCurrentWindsParm?.intervalTime = progress * 60
            }

        }

        bs_fan_max_power.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                tv_watt_max.text = "${progress}"
                mCurrentWindsParm?.maxPower = progress
            }

        }

        bs_fan_min_power.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                tv_watt_min.text = "${progress}"
                mCurrentWindsParm?.minPower = progress
            }

        }

        bs_pm2_5.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                tv_num_pm2_5.text = "${progress}"
                mCurrentWindsParm?.pm2_5 = progress
            }

        }

        bs_voc.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                tv_num_voc.text = "${progress}"
                mCurrentWindsParm?.voc = progress
            }

        }
    }
}
