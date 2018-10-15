package com.njwyt.airpurgesimple.entity

import java.io.Serializable

/**
 * Created by jason_samuel on 2018/2/25.
 * 强中弱模式参数
 */

class WindsParm : Serializable {

    // 风机设置
    var newWindTime: Int? = 0       // 新风时间
    var intervalTime: Int? = 0      // 间隔时间
    var maxPower: Int? = 0          // 风机最大功率
    var minPower: Int? = 0          // 最小功率

    // 强制排风设置
    var pm2_5: Int? = 0             // pm2.5
    var voc: Int? = 0               // VOC
}