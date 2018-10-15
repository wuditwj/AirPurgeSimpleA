package com.njwyt.airpurgesimple.util

/**
 * 空气质量指数计算
 */
object AQICalculate {

    private val aqiLimit = arrayOf(arrayOf(0f, 15.4f, 0f, 50f),
            arrayOf(15.5f, 40.4f, 51f, 100f),
            arrayOf(40.5f, 65.4f, 101f, 150f),
            arrayOf(65.5f, 150.4f, 151f, 200f),
            arrayOf(150.5f, 250.4f, 201f, 300f),
            arrayOf(250.5f, 350.4f, 301f, 400f),
            arrayOf(350.5f, 500.4f, 401f, 500f))

    fun calcualate(pm2_5: Double): Double {
        /*var aqi = 0.0

        for (i in 0 until 7) {
            if (pm2_5 in aqiLimit[i][0]..aqiLimit[i][1]) {
                aqi = (aqiLimit[i][3] - aqiLimit[i][2]) / (aqiLimit[i][1] - aqiLimit[i][0]) * (pm2_5 - aqiLimit[i][0]) + aqiLimit[i][2]
            }
        }*/
        // 根据实测浓度×50/0.035
        return pm2_5 * 50 / 0.035
    }
}