package com.njwyt.airpurgesimple.entity

import java.io.Serializable

/**
 * Created by jason_samuel on 2018/3/1.
 */
class SystemMode : Serializable {

    var selectedMode: Int = 0               // 已选中的模式（智能、强、中、弱、除甲醛）
    var nextMode: Int = 0                   // 下一模式
    var openTimer: Boolean = true          // 打开定时器
    var runningTime = RunningTime()            // 定时器数据
    var intellignetParm: IntellignetParm? = null    // 智能模式数据
    //var windsParm: WindsParm? = null
    var windsParmList = ArrayList<WindsParm>()  // 强中弱数据
}