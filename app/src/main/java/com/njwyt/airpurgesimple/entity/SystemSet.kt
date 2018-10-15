package com.njwyt.airpurgesimple.entity

import java.io.Serializable

/**
 * Created by jason_samuel on 2018/3/1.
 */
class SystemSet : Serializable {

    var wifiIP: String = ""//服务器IP
    var sensorCount: Int = 0//传感器数量
    var roomArea: Int = 0//使用面积
    var roomHight: Int = 0//层高
    var peopleNum: Int = 0//居住人数
    var useTime: Int = 0//过滤器使用时间
    var motorPower: Int = 0//电机功率

}