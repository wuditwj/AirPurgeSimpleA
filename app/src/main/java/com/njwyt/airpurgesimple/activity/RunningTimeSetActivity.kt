package com.njwyt.airpurgesimple.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.entity.RunningTime
import kotlinx.android.synthetic.main.activity_running_time_set.*

/**
 * Created by jason_samuel on 2018/3/2.
 * 时段设置
 */
class RunningTimeSetActivity : BaseActivity() {

    var runningTime: RunningTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running_time_set)
        initData()
        initToolBar()
        initView()
        initListener()
    }

    private fun initToolBar() {
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText(R.string.edit_timer)
        tv_cancel.setOnClickListener {
            finish()
        }

        tv_confirm.setOnClickListener {
            finish()
        }
        setSupportActionBar(tool_bar)
    }

    private fun initData() {

        runningTime = intent.getSerializableExtra("runningTime") as RunningTime?
    }

    private fun initView() {

        val hourList = ArrayList<String>()
        val minuteList = ArrayList<String>()
        // 生成以0开头的数字字符
        for (i in 0..23) {
            when (i < 10) {
                true -> hourList.add("0$i")
                false -> hourList.add("$i")
            }
        }

        for (i in 0..59) {
            when (i < 10) {
                true -> minuteList.add("0$i")
                false -> minuteList.add("$i")
            }
        }

        np_star_hour.minValue = 1
        np_star_hour.maxValue = hourList.size
        np_star_hour.displayedValues = hourList.toArray(arrayOfNulls<String>(0))

        np_star_minute.minValue = 1
        np_star_minute.maxValue = hourList.size
        np_star_minute.displayedValues = minuteList.toArray(arrayOfNulls<String>(0))

        np_stop_hour.minValue = 1
        np_stop_hour.maxValue = hourList.size
        np_stop_hour.displayedValues = hourList.toArray(arrayOfNulls<String>(0))

        np_stop_minute.minValue = 1
        np_stop_minute.maxValue = hourList.size
        np_stop_minute.displayedValues = hourList.toArray(arrayOfNulls<String>(0))

        // 初始化日期
        var weeks = ""
        var isEveryday = true
        for (i in runningTime?.weeks?.indices!!) {
            if (runningTime!!.weeks[i]) {
                when (weeks == "") {
                    true -> weeks = "${resources.getStringArray(R.array.weeks)[i]}"
                    false -> weeks = "${weeks}、${resources.getStringArray(R.array.weeks)[i]}"
                }
            } else {
                isEveryday = false
            }
        }

        if (weeks == "" || isEveryday) {
            weeks = resources.getString(R.string.everyday)
        }

        tv_weeks.setText(weeks)
    }

    private fun initListener() {

        ll_repeat.setOnClickListener {
            intent = Intent(this, RunningTimeRepeatActivity::class.java)
            startActivity(intent)
        }
    }
}