package com.njwyt.airpurgesimple.entity

/**
 * 中国气象网实体类
 *
 * {"WeatherChinaInfo":{"city":"南京","cityid":"101190101","temp1":"4℃","temp2":"16℃","weather":"多云","img1":"n1.gif","img2":"d1.gif","ptime":"18:00"}}
 */
class WeatherChinaInfo {
    var city: String? = null
    var cityid: String? = null
    var temp1: String? = null
    var temp2: String? = null
    var weather: String? = null
    var img1: String? = null
    var img2: String? = null
    var ptime: String? = null
}