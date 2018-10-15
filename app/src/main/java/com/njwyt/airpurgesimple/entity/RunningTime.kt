package com.njwyt.airpurgesimple.entity

import java.io.Serializable
import java.util.*

/**
 * Created by jason_samuel on 2018/2/25.
 * 风机运行时间(定时器)
 */

class RunningTime : Serializable {

    var startTime: Long? = null
    var endTime: String? = null
    var weeks = booleanArrayOf(false, false, false, false, false, false, false)

    // 简易定时器，单位：秒
    var time: Long = 0
}