package com.njwyt.airpurgesimple.entity

import java.io.Serializable

/**
 * Created by jason_samuel on 2018/2/25.
 * 房间参数
 */

class Room : Serializable {

    var roomId: Int = 0
    var roomName: String? = null    // 房间名称
    var roomType: Int = 0           // 房间类别
    var temperature: Int = 0        // 温度
    var humidity: Int = 0           // 湿度
    var voc: Int = 0                // VOC
    var pm2_5: Int = 0              // 可吸入颗粒物
    var airQuality: Int = 0         // 空气质量
    var brightness: Int = 0         // 空气质量
    var haveSensor = false         // 是否有传感器
}
