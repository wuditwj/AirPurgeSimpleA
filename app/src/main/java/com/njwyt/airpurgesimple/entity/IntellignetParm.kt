package com.njwyt.airpurgesimple.entity

import java.io.Serializable

/**
 * Created by jason_samuel on 2018/2/25.
 * 智能模式参数
 */

class IntellignetParm : Serializable {

    var sensitivity: Int = 1    // 灵敏度
    var temperature: Int = 18   // 温度
    var humidity: Int? = 0      // 湿度

    var dayMaxPower: Int? = 0     // 白天最大功率
    var dayMinPower: Int? = 0     // 白天最小功率
    var dayTime: Int? = 0         // 白天开始时间

    var nightMaxPower: Int? = 0   // 夜晚最大功率
    var nightMinPower: Int? = 0   // 夜晚最大功率
    var nightTime: Int? = 0       // 夜晚开始时间

}