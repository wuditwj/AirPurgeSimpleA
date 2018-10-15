package com.njwyt.airpurgesimple.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.njwyt.airpurgesimple.BR
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.adapter.UniversalAdapter
import com.njwyt.airpurgesimple.databinding.ItemRunningTimeBinding
import com.njwyt.airpurgesimple.entity.RunningTime
import com.njwyt.airpurgesimple.view.ItemDecoration
import kotlinx.android.synthetic.main.activity_running_time.*

/**
 * Created by jason_samuel on 2018/3/2.
 * 时段查看
 */
class RunningTimeActivity : BaseActivity() {

    private var universalAdapter: UniversalAdapter<RunningTime>? = null
    private var runningTimeList: ArrayList<RunningTime>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running_time)
        initData()
        initRecyclerView()
        initToolBar()
        initView()
        initListener()
    }

    private fun initToolBar() {
        // 去除Toolbar的默认标题
        tool_bar!!.title = ""
        // 使用自己的布局
        titleTextView.setText(R.string.timer)
        tv_back.setOnClickListener {
            finish()
        }
        tv_add.setOnClickListener {
            val intent = Intent(this, RunningTimeSetActivity::class.java)
            startActivity(intent)
        }
        setSupportActionBar(tool_bar)
    }

    private fun initData() {
        runningTimeList = ArrayList<RunningTime>()
        val r1 = RunningTime()
        r1.startTime = 0L
        r1.endTime = "10:20"
        r1.weeks[1] = true

        val r2 = RunningTime()
        r2.startTime = 0L
        r2.endTime = "10:20"
        r2.weeks[2] = true
        r2.weeks[3] = true
        r2.weeks[4] = true
        r2.weeks[5] = true


        val r3 = RunningTime()
        r3.startTime = 0L
        r3.endTime = "18:59"
        //r3.weeks[4] = true

        runningTimeList?.add(r1)
        runningTimeList?.add(r2)
        runningTimeList?.add(r3)
    }

    private fun initRecyclerView() {

        universalAdapter = UniversalAdapter<RunningTime>(runningTimeList, R.layout.item_running_time, BR.runningTime, UniversalAdapter.AdapterView { viewHolder, position ->

            val runningTime = runningTimeList?.get(position)
            val binding = viewHolder.binding as ItemRunningTimeBinding
            var weeks = ""

            for (i in runningTime?.weeks?.indices!!) {
                when (runningTime.weeks[i]) {
                    true -> {
                        when (weeks == "") {
                            true -> weeks = "${resources.getStringArray(R.array.weeks)[i]}"
                            false -> weeks = "${weeks}、${resources.getStringArray(R.array.weeks)[i]}"
                        }
                    }
                }
            }

            when (weeks == "") {
                true -> {
                    binding.tvWeeks.setText(R.string.everyday)
                }
                false -> {
                    binding.tvWeeks.text = weeks
                }
            }

            binding.rlRoot.setOnClickListener {

                val intent = Intent(this, RunningTimeSetActivity::class.java)
                intent.putExtra("runningTime", runningTime)
                startActivity(intent)
            }
        })
    }

    private fun initView() {
        recyclerview.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))

        // 下拉刷新转圈样式
        //binding.recyclerview.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        recyclerview.addItemDecoration(ItemDecoration(3))
        recyclerview.setAdapter(universalAdapter)
        recyclerview.setLoadingMoreEnabled(false)
        recyclerview.setPullRefreshEnabled(false)
    }

    private fun initListener() {
    }
}