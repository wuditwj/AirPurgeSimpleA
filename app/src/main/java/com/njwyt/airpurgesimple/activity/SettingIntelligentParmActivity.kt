package com.njwyt.airpurgesimple.activity

import android.os.Bundle
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.entity.IntellignetParm
import com.njwyt.airpurgesimple.entity.SystemMode
import kotlinx.android.synthetic.main.activity_setting_intelligent_parm.*

/**
 * Created by jason_samuel on 2018/2/25.
 * 智能模式的参数调整
 */

class SettingIntelligentParmActivity : BaseActivity() {

    var mIntellignetParm: IntellignetParm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_intelligent_parm)
        initData()
        initToolBar()
        initView()
        initListener()
    }

    private fun initData() {
        mIntellignetParm = (intent.getSerializableExtra("systemMode") as SystemMode?)?.intellignetParm
    }

    private fun initToolBar() {
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText(R.string.intelligent_mode)
        tv_back.setOnClickListener {
            finish()
        }
        setSupportActionBar(tool_bar)
    }

    private fun initView() {

    }

    private fun initListener() {

        tv_back.setOnClickListener {
            finish()
        }

        btn_ok.setOnClickListener {
            finish()
        }
    }
}
