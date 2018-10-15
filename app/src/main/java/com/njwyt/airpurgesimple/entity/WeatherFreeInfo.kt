package com.njwyt.airpurgesimple.entity

class WeatherFreeInfo {
    var date: String? = null

    var message: String? = null

    var status: Int = 0

    var city: String? = null

    var count: Int = 0

    var data: Data? = null
}

class Yesterday {
    var date: String? = null

    var sunrise: String? = null

    var high: String? = null

    var low: String? = null

    var sunset: String? = null

    var aqi: Int = 0

    var fx: String? = null

    var fl: String? = null

    var type: String? = null

    var notice: String? = null
}

class Forecast {
    var date: String? = null

    var sunrise: String? = null

    var high: String? = null

    var low: String? = null

    var sunset: String? = null

    var aqi: Int = 0

    var fx: String? = null

    var fl: String? = null

    var type: String? = null

    var notice: String? = null
}

class Data {
    var shidu: String? = null

    var pm25: Int = 0

    var pm10: Int = 0

    var quality: String? = null

    var wendu: String? = null

    var ganmao: String? = null

    var yesterday: Yesterday? = null

    var forecast: List<Forecast>? = null
}
