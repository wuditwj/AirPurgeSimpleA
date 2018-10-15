package com.njwyt.airpurgesimple.entity

/**
 * 从服务器获取的实体类
 */
class SocketData {
    var dataLenght = 0          // 数据长度
    var maxModeCount = 0         // 最大模式数
    var maxRoomCount = 0         // 最大房间数
    var maxFanCount = 0          // 最大风机数
    var fanModeList = ArrayList<SocketFanMode>()  // 风机模式队列，根据最大模式数量设定。1.智能，234.强中弱，6.除甲醛
    var wifiIP = ""         // wifi ip
    var systemTime = ""     // 系统时间
    var roomCount = 0       // 房间数(暂未使用)
    var roomHeight = 0      // 房间高度cm
    var roomAreaList = ArrayList<Int>()           // 房间大小队列
    var leakOutRateList = ArrayList<Int>()       // 漏风率队列
    var haveSensorList = ArrayList<Boolean>()       // 房间是否有传感器队列
    var sensorCount = 0     // 传感器数
    var fanCount = 0        // 风机数
    var fanPowerList = ArrayList<Int>()            // 风机功率队列
    var fanSpeedList = ArrayList<Int>()            // 风机抽速队列
    var fanStaticList = ArrayList<Int>()           // 风机静电压队列
    var supplyVoltage = 0   // 供电电压
    var humidity = 0        // 湿度
    var humidityDeviation = 0  // 湿度偏离
    var temperature = 0        // 温度
    var temperatureDeviation = 0   // 温度偏离
    var pmConcentration = 0         // pm浓度
    var pmDeviation = 0         // pm偏离
    var vocLevel = 0         // voc等级
    var vocOudDoorDeviation = 0         // 室外voc偏移
    var minFreshAirRate = 0         // 最小新风率
    var maxFreshAirRateDay = 0         // 白天最大新风率
    var maxFreshAirRateNight = 0         // 夜晚最大新风率
    var systemTime2 = ""         // 系统时间（预留）
    var wifiStatu = 0         // wifi状态
    var netStatu = 0         // net状态
    var plcStatuList = ArrayList<Int>()         // plc状态队列
    var totalPower = 0         // 累计功耗
    var totalAirChange = 0         // 累计换风量
    var totalFilteringPM25 = 0         // 累计过滤PM2.5质量
    var changeFilterTime = 0         // 更换过滤器时间x
    var resetTotalPower = 0         // 复位后累计功耗
    var resetTotalAirChange = 0         // 复位后累计换风量
    var resetTotalFilteringPM25 = 0         // 复位后累计过滤PM2.5质量
    var fanControlMode = 0         // 风机控制模式
    var workMode = 0         // 工作模式
    var nextWorkMode = 0         // 下一工作模式
    var backupWorkMode = 0         // 备份工作模式
    var roomList = ArrayList<SocketRoom>()         // 房间队列
    var fanList = ArrayList<SocketFan>()         // 风机队列
    var dataBackUp = 0;             // 系统备份
    var dataRestore = 0;            // 系统恢复
    var timeIntervalList = ArrayList<ArrayList<String>>()         // 时段二维队列
    var modeSetList = ArrayList<ArrayList<Int>>()              // 模式二维队列
}

// 风机模式
class SocketFanMode {
    var fanStart = 0        // 风机开机时间
    var fanStop = 0         // 风机关机时间
    var maxPower = 0        // 风机最大功率
    var minPower = 0        // 风机最小功率
}

// 房间属性
class SocketRoom {
    var roomId = 0             // 房间序号
    var temperature = 0        // 温度
    var humidity = 0           // 湿度
    var voc = 0                // VOC
    var pm2_5 = 0              // 可吸入颗粒物
    var brightness = 0         // 房间亮度
}

// 风机属性
class SocketFan {
    var stopTime :Long = 0            // 当前状态结束时间
    var targetPower = 0         // 目标功率
    var currentPower = 0        // 当前功率
}
