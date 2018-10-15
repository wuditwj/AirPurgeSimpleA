package com.njwyt.airpurgesimple.biz

import android.util.Log
import com.google.gson.Gson
import com.njwyt.airpurgesimple.content.Address
import com.njwyt.airpurgesimple.entity.WeatherFreeInfo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Created by jason_samuel on 2018/2/27.
 * 获取天气服务
 */
class WeatherService {

    var retrofit: Retrofit? = null
    var gson = Gson()

    init {
        retrofit = Retrofit.Builder()
                .baseUrl(Address.FREE_WEATHER)
                //.addConverterFactory(GsonConverterFactory.create())       // 添加json自动解析
                .build()
    }

    interface WeatherInterface {
        /*@HTTP(method = "GET", path = "json.shtml?city={cityId}", hasBody = false)
        fun getWeather(@Path("cityId") id: String): Call<ResponseBody>*/
        @GET("json.shtml")
        fun getWeather(@Query("city") cityId: String): Call<ResponseBody>
    }

    interface OnGetWeather {
        fun getWeather(weatherFreeInfo: WeatherFreeInfo)
    }

    /**
     * 获取天气
     * @param cityId 城市id
     */
    fun getWeather(cityId: String, onGetWeather: OnGetWeather) {

        val weatherInterface = retrofit?.create(WeatherInterface::class.java)
        val call: Call<ResponseBody>? = weatherInterface?.getWeather(cityId);

        call?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()!!.bytes())
                val weatherFreeInfo = gson.fromJson<WeatherFreeInfo>(json, WeatherFreeInfo::class.java);
                if (weatherFreeInfo.status == 200) {
                    onGetWeather.getWeather(weatherFreeInfo)
                }
                //Log.d("WeatherService", "-->> getWeather = " + weatherFreeInfo.data!!.wendu)
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d("WeatherService", "-->> error " + t?.message)
            }
        })
    }
}