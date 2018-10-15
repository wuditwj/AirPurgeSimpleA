package com.njwyt.airpurgesimple.db

import com.anupcowkur.reservoir.Reservoir
import com.google.gson.reflect.TypeToken
import com.njwyt.airpurgesimple.entity.*

object ReservoirHelper {

    // 从socket获得到的原始数据
    fun setSocketData(socketData: SocketData) {
        Reservoir.put("socketData", socketData)
    }

    fun getSocketData(): SocketData {
        if (Reservoir.contains("socketData"))
            return Reservoir.get("socketData", SocketData::class.java)
        else
            return SocketData()
    }

    // 预备发送到服务器的数据
    fun setSendData(sendData: List<String>?) {
        Reservoir.put("sendData", sendData)
    }

    fun getSendData(): List<String>? {
        if (Reservoir.contains("sendData"))
            try {
                return Reservoir.get("sendData", object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                return null
            }
        else
            return null
    }

    // 房间数据
    fun setRoomList(roomList: List<Room>) {
        Reservoir.put("roomList", roomList)
    }

    fun getRoomList(): List<Room> {
        if (Reservoir.contains("roomList"))
            return Reservoir.get("roomList", object : TypeToken<List<Room>>() {}.type)
        else
            return ArrayList()
    }

    // 风机数据
    fun setFanPowerList(fanPowerList: List<Int>) {
        Reservoir.put("fanPowerList", fanPowerList)
    }

    fun getFanPowerList(): List<Int> {
        if (Reservoir.contains("fanPowerList"))
            return Reservoir.get("fanPowerList", object : TypeToken<List<Int>>() {}.type)
        else
            return ArrayList()
    }

    // 风机数据
    fun setFanList(fanList: List<SocketFan>) {
        Reservoir.put("fanList", fanList)
    }

    fun getFanList(): List<SocketFan> {
        if (Reservoir.contains("fanList"))
            return Reservoir.get("fanList", object : TypeToken<List<SocketFan>>() {}.type)
        else
            return ArrayList()
    }

    // 模式数据
    fun setSystemMode(systemMode: SystemMode) {
        Reservoir.put("systemMode", systemMode)
    }

    fun getSystemMode(): SystemMode {
        if (Reservoir.contains("systemMode"))
            return Reservoir.get("systemMode", SystemMode::class.java)
        else
            return SystemMode()
    }

    // 系统设置
    fun setSystemSet(systemSet: SystemSet) {
        Reservoir.put("systemSet", systemSet)
    }

    fun getSystemSet(): SystemSet {
        if (Reservoir.contains("systemSet"))
            return Reservoir.get("systemSet", SystemSet::class.java)
        else
            return SystemSet()
    }

    // 服务器ip
    fun setIP(ip: String) {
        Reservoir.put("ip", ip)
    }

    fun getIP(): String {
        if (Reservoir.contains("ip"))
            return Reservoir.get("ip", String::class.java)
        else
            return "192.168.16.254"
    }

    // 是否已发送数据
    fun setIsSendData(isSendData: Boolean) {
        Reservoir.put("isSendData", isSendData)
    }

    fun isSendData(): Boolean {
        if (Reservoir.contains("isSendData"))
            return Reservoir.get("isSendData", Boolean::class.java)
        else
            return true
    }

    // 定时器数据是否已同步
    fun setIsSyncTime(isSendData: Boolean) {
        Reservoir.put("isSyncTime", isSendData)
    }

    fun isSyncTime(): Boolean {
        if (Reservoir.contains("isSyncTime"))
            return Reservoir.get("isSyncTime", Boolean::class.java)
        else
            return true
    }

    // 数据管理
    fun setDataManage(dataManage: DataManage) {
        Reservoir.put("dataManage", dataManage)
    }

    fun getDataManage(): DataManage {
        if (Reservoir.contains("dataManage"))
            return Reservoir.get("dataManage", DataManage::class.java)
        else
            return DataManage()
    }

    // 是否开启管理员模式
    fun setIsAdmin(isAdmin: Boolean) {
        Reservoir.put("isAdmin", isAdmin)
    }

    fun getIsAdmin(): Boolean {
        if (Reservoir.contains("isAdmin"))
            return Reservoir.get("isAdmin", Boolean::class.java)
        else
            return false
    }

    // 所在城市
    fun setCity(city: String) {
        Reservoir.put("city", city)
    }

    fun getCity(): String {
        if (Reservoir.contains("city"))
            return Reservoir.get("city", String::class.java)
        else
            return "北京市"
    }

    // 天气数据
    fun setWeatherFreeInfo(weatherFreeInfo: WeatherFreeInfo) {
        Reservoir.put("weatherFreeInfo", weatherFreeInfo)
    }

    fun getWeatherFreeInfo(): WeatherFreeInfo {
        if (Reservoir.contains("weatherFreeInfo"))
            return Reservoir.get("weatherFreeInfo", WeatherFreeInfo::class.java)
        else
            return WeatherFreeInfo()
    }
}