package com.njwyt.airpurgesimple.activity

import android.os.Bundle
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.entity.Room
import kotlinx.android.synthetic.main.activity_room_detail.*

/**
 * Created by jason_samuel on 2018/2/25.
 * 房间参数详情
 */

class RoomDetailActivity : BaseActivity() {

    var room: Room? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_room_detail)
        initData()
        //initToolBar()
        initView()
        initListener()

    }

    /*private fun initToolBar() {
        // 去除Toolbar的默认标题
        toolbar.setTitle("")
        // 使用自己的布局
        titleTextView.setText(room?.roomName)
        setSupportActionBar(toolbar)
    }*/

    /**
     * 获取房间数据
     */
    private fun initData() {
        room = intent.getSerializableExtra("room") as Room?
    }

    private fun initView() {

        tv_room_name.text = "${room?.roomName}"
        tv_temperature.text = "${room?.temperature} ℃"
        tv_humidity.text = "${room?.humidity} HR"
        tv_pm2_5.text = "${room?.pm2_5} μg/m3"
        var vocStr = ""
        when (room?.voc) {
            0 -> vocStr = "优"
            1 -> vocStr = "良"
            2 -> vocStr = "中"
            3 -> vocStr = "差"
        }
        tv_voc.text = "$vocStr"
        tv_air_quality.text = "${room?.airQuality}"
    }

    private fun initListener() {

        rl_root.setOnClickListener {
            finish()
        }

        tv_cancel.setOnClickListener {
            finish()
        }

        tv_setting.setOnClickListener {
            // todo 编辑房间信息
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
