package com.njwyt.airpurgesimple.util

import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.*

/**
 * 解析出的实体类相互转换
 */
object ParseEntity {

    private val TAG = "PareseEntity"

    /**
     * 房间数据格式转化
     *
     * @param socketRoomList
     * @return
     */
    fun socketToRoom(socketRoomList: List<SocketRoom>): List<Room> {

        val roomList = ArrayList<Room>()
        for (i in 0 until socketRoomList.size) {
            val r = Room()
            r.roomName = Type.ROOM_NAME[i]
            r.roomId = socketRoomList[i].roomId
            r.temperature = socketRoomList[i].temperature
            r.humidity = socketRoomList[i].humidity
            r.voc = socketRoomList[i].voc
            r.pm2_5 = socketRoomList[i].pm2_5
            r.airQuality = AQICalculate.calcualate(r.pm2_5.toDouble()).toInt();
            r.brightness = socketRoomList[i].brightness
            roomList.add(r)
        }
        return roomList
    }

    fun socketToSystemMode(socketFanModeList: List<SocketFanMode>, workMode: Int): SystemMode {

        // 关机，智能，强，中，弱，直排，除甲醛
        val systemMode = SystemMode()

        // 设置强中弱和除甲醛模式
        val windsParmList = ArrayList<WindsParm>()
        for (i in socketFanModeList.indices) {
            if (i >= 2 && i <= 5) {
                val windsParm = WindsParm()
                windsParm.newWindTime = socketFanModeList[i].fanStart
                windsParm.intervalTime = socketFanModeList[i].fanStop
                windsParm.maxPower = socketFanModeList[i].maxPower
                windsParm.minPower = socketFanModeList[i].minPower
                windsParmList.add(windsParm)
            }
        }
        systemMode.selectedMode = workMode
        systemMode.windsParmList = windsParmList
        return systemMode
    }

    fun socketToRunningTime(timeIntervalList: ArrayList<ArrayList<String>>): RunningTime {

        val runningTime = RunningTime()            // 定时器数据
        val longTime = java.lang.Long.parseLong(
                timeIntervalList[0][0] +
                        timeIntervalList[0][1] +
                        timeIntervalList[0][2] +
                        timeIntervalList[0][3], 16)
        val startTime = java.lang.Long.parseLong(
                timeIntervalList[0][4] +
                        timeIntervalList[0][5] +
                        timeIntervalList[0][6] +
                        timeIntervalList[0][7], 16)
        runningTime.time = longTime
        //runningTime.startTime = System.currentTimeMillis() / 1000 - 1320
        runningTime.startTime = startTime - 28800;
        return runningTime
    }

    fun socketToSystemSet(socketData: SocketData): SystemSet {
        val systemSet = SystemSet()
        systemSet.wifiIP = socketData.wifiIP//服务器IP
        systemSet.sensorCount = socketData.sensorCount//传感器数量
        systemSet.roomHight = socketData.roomHeight//层高
        systemSet.peopleNum = socketData.roomCount//层高
        systemSet.useTime = socketData.changeFilterTime//过滤器使用时间
        systemSet.motorPower = socketData.fanPowerList[0]//电机功率

        return systemSet
    }

    /**
     * 风机模式转socket
     * @param windsParmList
     * @return
     */
    fun windsParmListToSocket(windsParmList: List<WindsParm>): ArrayList<SocketFanMode> {
        val socketFanModeList = ArrayList<SocketFanMode>()
        for (wp in windsParmList) {
            val sfm = SocketFanMode()
            sfm.fanStart = wp.newWindTime!!
            sfm.fanStop = wp.intervalTime!!
            sfm.maxPower = wp.maxPower!!
            sfm.minPower = wp.minPower!!
            socketFanModeList.add(sfm)
        }
        return socketFanModeList
    }

    /**
     * 把房间转socket
     * @param roomList
     * @return
     */
    fun roomListToSocket(roomList: List<Room>): ArrayList<SocketRoom> {

        val socketRoomList = ArrayList<SocketRoom>()
        for (room in roomList) {
            val sr = SocketRoom()
            sr.roomId = room.roomId
            sr.temperature = room.temperature
            sr.humidity = room.humidity
            sr.voc = room.voc
            sr.pm2_5 = room.pm2_5
            sr.brightness = room.brightness
            socketRoomList.add(sr)
        }
        return socketRoomList
    }

    /**
     * 把服务器List数据转换成Android Bean
     */
    fun parseList(byteStringList: ArrayList<String>): SocketData {

        val mSocketDataTemp = SocketData()
        val fanModeListSize: Int // 风机模式队列长度
        val roomAreaListSize: Int // 房间大小队列长度
        val leakOutRateListSize: Int // 漏风率队列
        val haveSensorListSize: Int // 房间是否有传感器队列
        val fanPowerListSize: Int // 风机功率队列
        val fanSpeedListSize: Int // 风机抽速队列
        val fanStaticListSize: Int // 风机静电压队列
        val plcStatuListSize: Int // plc状态队列
        val roomListSize: Int // 房间队列
        val fanListSize: Int // 风机队列

        // 最大模式数
        mSocketDataTemp.maxModeCount = Integer.parseInt(byteStringList[3], 16)
        fanModeListSize = mSocketDataTemp.maxModeCount * 6      // 6代表里面是6位

        // 最大房间数
        mSocketDataTemp.maxRoomCount = Integer.parseInt(byteStringList[4], 16)
        roomListSize = mSocketDataTemp.maxRoomCount * 8
        roomAreaListSize = mSocketDataTemp.maxRoomCount
        leakOutRateListSize = mSocketDataTemp.maxRoomCount
        haveSensorListSize = mSocketDataTemp.maxRoomCount
        plcStatuListSize = mSocketDataTemp.maxRoomCount

        // 最大风机数
        mSocketDataTemp.maxFanCount = Integer.parseInt(byteStringList[5], 16)
        fanPowerListSize = mSocketDataTemp.maxFanCount
        fanSpeedListSize = mSocketDataTemp.maxFanCount
        fanStaticListSize = mSocketDataTemp.maxFanCount
        fanListSize = mSocketDataTemp.maxFanCount * 7

        var position = 6

        // 风机模式队列
        for (i in position until fanModeListSize + position step 6) {
            val sfm = SocketFanMode()
            for (j in 0 until 6) {
                when (j) {
                    0 -> sfm.fanStart = Integer.parseInt(byteStringList[i + j] + byteStringList[i + j + 1], 16)
                    2 -> sfm.fanStop = Integer.parseInt(byteStringList[i + j] + byteStringList[i + j + 1], 16)
                    4 -> sfm.maxPower = Integer.parseInt(byteStringList[i + j], 16)
                    5 -> sfm.minPower = Integer.parseInt(byteStringList[i + j], 16)
                }
            }
            mSocketDataTemp.fanModeList.add(sfm)
            position += 6  // 这个对象占用了6位
        }

        // wifi ip
        mSocketDataTemp.wifiIP =
                "${Integer.parseInt(byteStringList[position++], 16)}." +
                "${Integer.parseInt(byteStringList[position++], 16)}." +
                "${Integer.parseInt(byteStringList[position++], 16)}." +
                "${Integer.parseInt(byteStringList[position++], 16)}"

        // 系统时间
        mSocketDataTemp.systemTime =
                byteStringList[position++] +
                byteStringList[position++] +
                byteStringList[position++] +
                byteStringList[position++]
        mSocketDataTemp.systemTime = "${java.lang.Long.parseLong(mSocketDataTemp.systemTime, 16)}"

        // 房间数
        mSocketDataTemp.roomCount = Integer.parseInt(byteStringList[position++], 16)

        // 房间高度cm
        mSocketDataTemp.roomHeight = Integer.parseInt(byteStringList[position++] + byteStringList[position++], 16)

        // 房间面积
        for (i in 0 until roomAreaListSize) {
            mSocketDataTemp.roomAreaList.add(Integer.parseInt(byteStringList[position++], 16))
        }

        // 漏风率
        for (i in 0 until leakOutRateListSize) {
            mSocketDataTemp.leakOutRateList.add(Integer.parseInt(byteStringList[position++], 16))
        }

        // 房间是否有传感器
        for (i in 0 until haveSensorListSize) {
            mSocketDataTemp.haveSensorList.add(if (Integer.parseInt(byteStringList[position++], 16) == 1) true else false)
        }

        // 传感器数
        mSocketDataTemp.sensorCount = Integer.parseInt(byteStringList[position++], 16)

        // 风机数
        mSocketDataTemp.fanCount = Integer.parseInt(byteStringList[position++], 16)

        // 风机功率队列
        for (i in 0 until fanPowerListSize) {
            mSocketDataTemp.fanPowerList.add(Integer.parseInt(byteStringList[position++], 16))
        }

        // 风机抽速队列
        for (i in 0 until fanSpeedListSize) {
            mSocketDataTemp.fanSpeedList.add(Integer.parseInt(byteStringList[position++], 16))
        }

        // 风机静电压队列
        for (i in 0 until fanStaticListSize) {
            mSocketDataTemp.fanStaticList.add(Integer.parseInt(byteStringList[position++], 16))
        }

        // 供电电压
        mSocketDataTemp.supplyVoltage = Integer.parseInt(byteStringList[position++], 16)

        // 湿度
        mSocketDataTemp.humidity = Integer.parseInt(byteStringList[position++], 16)

        // 湿度偏离
        mSocketDataTemp.humidityDeviation = Integer.parseInt(byteStringList[position++], 16)

        // 温度
        mSocketDataTemp.temperature = Integer.parseInt(byteStringList[position++], 16)

        // 温度偏离
        mSocketDataTemp.temperatureDeviation = Integer.parseInt(byteStringList[position++], 16)

        // pm浓度
        mSocketDataTemp.pmConcentration = Integer.parseInt(byteStringList[position++], 16)

        // pm偏离
        mSocketDataTemp.pmDeviation = Integer.parseInt(byteStringList[position++], 16)

        // voc等级
        mSocketDataTemp.vocLevel = Integer.parseInt(byteStringList[position++], 16)

        // 室外voc偏移
        mSocketDataTemp.vocOudDoorDeviation = Integer.parseInt(byteStringList[position++], 16)

        // 最小新风率
        mSocketDataTemp.minFreshAirRate = Integer.parseInt(byteStringList[position++], 16)

        // 白天最大新风率
        mSocketDataTemp.maxFreshAirRateDay = Integer.parseInt(byteStringList[position++], 16)

        // 夜晚最大新风率
        mSocketDataTemp.maxFreshAirRateNight = Integer.parseInt(byteStringList[position++], 16)

        // 系统时间（预留）
        mSocketDataTemp.systemTime2 =
                byteStringList[position++] +
                byteStringList[position++] +
                byteStringList[position++] +
                byteStringList[position++]
        mSocketDataTemp.systemTime2 = "${java.lang.Long.parseLong(mSocketDataTemp.systemTime2, 16)}"

        // wifi状态
        mSocketDataTemp.wifiStatu = Integer.parseInt(byteStringList[position++], 16)

        // net状态
        mSocketDataTemp.netStatu = Integer.parseInt(byteStringList[position++], 16)

        // plc状态队列
        for (i in 0 until plcStatuListSize) {
            mSocketDataTemp.plcStatuList.add(Integer.parseInt(byteStringList[position++], 16))
        }

        // 累计功耗
        mSocketDataTemp.totalPower =
                Integer.parseInt(byteStringList[position++]
                        + byteStringList[position++], 16)

        // 累计换风量
        mSocketDataTemp.totalAirChange =
                Integer.parseInt(byteStringList[position++]
                        + byteStringList[position++]
                        + byteStringList[position++]
                        + byteStringList[position++], 16)

        // 累计过滤PM2.5质量
        mSocketDataTemp.totalFilteringPM25 =
                Integer.parseInt(byteStringList[position++]
                        + byteStringList[position++]
                        + byteStringList[position++]
                        + byteStringList[position++], 16)

        // 更换过滤器时间
        mSocketDataTemp.changeFilterTime =
                Integer.parseInt(byteStringList[position++]
                        + byteStringList[position++]
                        + byteStringList[position++]
                        + byteStringList[position++], 16)

        // 复位后累计功耗
        mSocketDataTemp.resetTotalPower = Integer.parseInt(byteStringList[position++], 16)

        // 复位后累计换风量
        mSocketDataTemp.resetTotalAirChange =
                Integer.parseInt(byteStringList[position++]
                        + byteStringList[position++], 16)

        // 复位后累计过滤PM2.5质量
        mSocketDataTemp.resetTotalFilteringPM25 =
                Integer.parseInt(byteStringList[position++]
                        + byteStringList[position++], 16)

        // 风机控制模式
        mSocketDataTemp.fanControlMode = Integer.parseInt(byteStringList[position++], 16)

        // 工作模式
        mSocketDataTemp.workMode = Integer.parseInt(byteStringList[position++], 16)

        // 下一工作模式
        mSocketDataTemp.nextWorkMode = Integer.parseInt(byteStringList[position++], 16)

        // 备份工作模式
        mSocketDataTemp.backupWorkMode = Integer.parseInt(byteStringList[position++], 16)


        // 房间队列(需要再加一个房间)
        for (i in position until roomListSize + position + 8 step 8) {
            val sr = SocketRoom()
            for (j in 0 until 8) {
                when (j) {
                    0 -> sr.roomId = Integer.parseInt(byteStringList[i + j], 16)
                    1 -> sr.humidity = Integer.parseInt(byteStringList[i + j], 16)
                    2 -> sr.temperature = Integer.parseInt(byteStringList[i + j], 16)
                    3 -> sr.pm2_5 = Integer.parseInt(byteStringList[i + j] + byteStringList[i + j + 1], 16)
                    5 -> sr.voc = Integer.parseInt(byteStringList[i + j], 16)
                    6 -> sr.brightness = Integer.parseInt(byteStringList[i + j], 16)
                }
            }
            mSocketDataTemp.roomList.add(sr)
            position += 8  // 这个对象占用了8位
        }

        // 风机队列
        for (i in position until fanListSize + position step 7) {
            val sf = SocketFan()
            for (j in 0 until 7) {
                when (j) {
                    0 -> sf.stopTime =
                            java.lang.Long.parseLong(byteStringList[i + j]
                                    + byteStringList[i + j + 1]
                                    + byteStringList[i + j + 2]
                                    + byteStringList[i + j + 3], 16)
                    4 -> sf.targetPower = Integer.parseInt(byteStringList[i + j], 16)
                    5 -> sf.currentPower = Integer.parseInt(byteStringList[i + j] + byteStringList[i + j + 1], 16)
                }
            }
            mSocketDataTemp.fanList.add(sf)
            position += 7  // 这个对象占用了7位
        }

        // 时段二维队列总长度为7*16=112个
        for (i in 0 until 7) {
            val timeIntervalList = ArrayList<String>()
            for (j in 0 until 16) {
                //timeIntervalList.add(Integer.parseInt(byteStringList[position++], 16))
                // 不进行进制转换
                timeIntervalList.add(byteStringList[position++])
            }
            mSocketDataTemp.timeIntervalList.add(timeIntervalList)
        }

        mSocketDataTemp.dataBackUp = Integer.parseInt(mSocketDataTemp.timeIntervalList[0][8], 16)// 系统备份
        mSocketDataTemp.dataRestore = Integer.parseInt(mSocketDataTemp.timeIntervalList[0][9], 16)// 系统恢复

        // 模式二维队列总长度为7*16=112个
        for (i in 0 until 7) {
            val modeSetList = ArrayList<Int>()
            for (j in 0 until 16) {
                modeSetList.add(Integer.parseInt(byteStringList[position++], 16))
            }
            mSocketDataTemp.modeSetList.add(modeSetList)
        }
        //Log.d("PareseEntity", "-->> position = $position")
        return mSocketDataTemp
    }


    /**
     * 把Android Bean，转化成服务器List
     */
    fun praseBean(): ArrayList<String>? {

        // 最初接收到的数据源
        val socketData = ReservoirHelper.getSocketData()

        // 组装socket发送的实体类
        val socketSend = SocketSend()
        val systemMode = ReservoirHelper.getSystemMode()
        val systemSet = ReservoirHelper.getSystemSet()
        val dataManage = ReservoirHelper.getDataManage()

        val fanModelist = ParseEntity.windsParmListToSocket(systemMode.windsParmList)
        if (fanModelist.size==0){
            return null
        }
        socketData.fanModeList.set(2, fanModelist[0])
        socketData.fanModeList.set(3, fanModelist[1])
        socketData.fanModeList.set(4, fanModelist[2])
        socketData.fanModeList.set(5, fanModelist[3])
        socketSend.fanModeList = socketData.fanModeList
        socketSend.wifiIP = systemSet.wifiIP
        socketSend.systemTime = "${System.currentTimeMillis() / 1000 + 28800}"  // 服务端说要加8个小时
        socketSend.roomCount = systemSet.peopleNum
        socketSend.roomHeight = systemSet.roomHight
        socketSend.roomAreaList = socketData.roomAreaList                                  // 未修改
        socketSend.leakOutRateList = socketData.leakOutRateList                            // 未修改
        socketSend.haveSensorList = socketData.haveSensorList                              // 未修改
        socketSend.sensorCount = systemSet.sensorCount
        socketSend.fanCount = socketData.fanCount                                          // 未修改
        socketData.fanPowerList.set(0, systemSet.motorPower)
        socketSend.fanPowerList = socketData.fanPowerList
        socketSend.fanSpeedList = socketData.fanSpeedList                                  // 未修改
        socketSend.fanStaticList = socketData.fanStaticList                                // 未修改
        socketSend.supplyVoltage = socketData.supplyVoltage                                // 未修改
        socketSend.humidity = socketData.humidity                                          // 未修改
        socketSend.humidityDeviation = socketData.humidityDeviation                        // 未修改
        socketSend.temperature = socketData.temperature                                    // 未修改
        socketSend.temperatureDeviation = socketData.temperatureDeviation                  // 未修改
        socketSend.pmConcentration = socketData.pmConcentration                            // 未修改
        socketSend.pmDeviation = socketData.pmDeviation                                    // 未修改
        socketSend.vocLevel = socketData.vocLevel                                          // 未修改
        socketSend.vocOudDoorDeviation = socketData.vocOudDoorDeviation                    // 未修改
        socketSend.minFreshAirRate = socketData.minFreshAirRate                            // 未修改
        socketSend.maxFreshAirRateDay = socketData.maxFreshAirRateDay                      // 未修改
        socketSend.maxFreshAirRateNight = socketData.maxFreshAirRateNight                  // 未修改
        socketSend.systemTime2 = socketData.systemTime
        socketSend.wifiStatu = socketData.wifiStatu                                        // 未修改
        socketSend.netStatu = socketData.netStatu                                          // 未修改
        socketSend.plcStatuList = socketData.plcStatuList                                  // 未修改
        socketSend.totalPower = socketData.totalPower                                      // 未修改
        socketSend.totalAirChange = socketData.totalAirChange                              // 未修改
        socketSend.totalFilteringPM25 = socketData.totalFilteringPM25                      // 未修改
        socketSend.changeFilterTime = systemSet.useTime
        socketSend.resetTotalPower = socketData.resetTotalPower                            // 未修改
        socketSend.resetTotalAirChange = socketData.resetTotalAirChange                    // 未修改
        socketSend.resetTotalFilteringPM25 = socketData.resetTotalFilteringPM25            // 未修改
        socketSend.fanControlMode = socketData.fanControlMode                              // 未修改
        socketSend.workMode = socketData.workMode                                          // 未修改
        // 如果开启了定时器，那么发给服务器的就是nextMode，否则发送当前模式直接修改
        if (ReservoirHelper.getSystemMode().openTimer) {
            socketSend.nextWorkMode = systemMode.nextMode
        } else {
            socketSend.nextWorkMode = systemMode.selectedMode
        }
        socketSend.backupWorkMode = socketData.backupWorkMode                              // 未修改
        socketSend.roomList = ParseEntity.roomListToSocket(ReservoirHelper.getRoomList())
        socketSend.fanList = socketData.fanList                                            // 未修改
        socketSend.timeIntervalList = socketData.timeIntervalList
        // 这里修改成了直接从systemMode中获取
        var timeHex = StringUtil.toHex(systemMode.runningTime.time, 8)
        // 如果定时器关闭，就上传全0
        if (!ReservoirHelper.getSystemMode().openTimer) timeHex = "00000000"
        // 如果定时器已经同步，那么就把本地数据改为全F上传
        if (ReservoirHelper.isSyncTime()) timeHex = "ffffffff"
        socketSend.timeIntervalList[0][0] = "${timeHex[0]}${timeHex[1]}"
        socketSend.timeIntervalList[0][1] = "${timeHex[2]}${timeHex[3]}"
        socketSend.timeIntervalList[0][2] = "${timeHex[4]}${timeHex[5]}"
        socketSend.timeIntervalList[0][3] = "${timeHex[6]}${timeHex[7]}"

        // 数据备份
        val dataBackUp = StringUtil.toHex(dataManage.dataBackUp, 2)
        socketSend.timeIntervalList[0][8] = "${dataBackUp[0]}${dataBackUp[1]}"

        // 恢复数据
        val dataRestore = StringUtil.toHex(dataManage.dataRestore, 2)
        socketSend.timeIntervalList[0][9] = "${dataRestore[0]}${dataRestore[1]}"

        socketSend.modeSetList = socketData.modeSetList                                    // 未修改

        // 实体类转String List
        val byteStringList = ArrayList<String>()

        // 设备识别号
        byteStringList.add(StringUtil.toHex(socketSend.deviceType, 2))

        // 数据长度
        byteStringList.add("00")
        byteStringList.add("00")

        // 风机模式队列
        for (item in socketSend.fanModeList) {
            val fanStartHex = StringUtil.toHex(item.fanStart, 4)
            byteStringList.add("${fanStartHex[0]}${fanStartHex[1]}")
            byteStringList.add("${fanStartHex[2]}${fanStartHex[3]}")
            val fanStopHex = StringUtil.toHex(item.fanStop, 4)
            byteStringList.add("${fanStopHex[0]}${fanStopHex[1]}")
            byteStringList.add("${fanStopHex[2]}${fanStopHex[3]}")
            byteStringList.add(StringUtil.toHex(item.maxPower, 2))
            byteStringList.add(StringUtil.toHex(item.minPower, 2))
        }

        // wifi ip
        val wifiIPArray = socketSend.wifiIP.split(".")
        byteStringList.add(StringUtil.toHex(Integer.parseInt(wifiIPArray[0]), 2))
        byteStringList.add(StringUtil.toHex(Integer.parseInt(wifiIPArray[1]), 2))
        byteStringList.add(StringUtil.toHex(Integer.parseInt(wifiIPArray[2]), 2))
        byteStringList.add(StringUtil.toHex(Integer.parseInt(wifiIPArray[3]), 2))

        // 系统时间
        val systemTimeHex = StringUtil.toHex(socketSend.systemTime.toLong(), 8)
        byteStringList.add("${systemTimeHex[0]}${systemTimeHex[1]}")
        byteStringList.add("${systemTimeHex[2]}${systemTimeHex[3]}")
        byteStringList.add("${systemTimeHex[4]}${systemTimeHex[5]}")
        byteStringList.add("${systemTimeHex[6]}${systemTimeHex[7]}")

        // 房间数
        byteStringList.add(StringUtil.toHex(socketSend.roomCount, 2))

        // 房间高度
        val roomHeightHex = StringUtil.toHex(socketSend.roomHeight, 4)
        byteStringList.add("${roomHeightHex[0]}${roomHeightHex[1]}")
        byteStringList.add("${roomHeightHex[2]}${roomHeightHex[3]}")

        // 房间大小队列
        for (item in socketSend.roomAreaList) {
            byteStringList.add(StringUtil.toHex(item, 2))
        }

        // 漏风率队列
        for (item in socketSend.leakOutRateList) {
            byteStringList.add(StringUtil.toHex(item, 2))
        }

        // 房间是否有传感器队列
        for (item in socketSend.haveSensorList) {
            byteStringList.add(if (item) "01" else "00")
        }

        // 传感器数
        byteStringList.add(StringUtil.toHex(socketSend.sensorCount, 2))

        // 风机数
        byteStringList.add(StringUtil.toHex(socketSend.fanCount, 2))

        // 风机功率队列
        for (item in socketSend.fanPowerList) {
            byteStringList.add(StringUtil.toHex(item, 2))
        }

        // 风机抽速队列
        for (item in socketSend.fanSpeedList) {
            byteStringList.add(StringUtil.toHex(item, 2))
        }

        // 风机静电压队列
        for (item in socketSend.fanStaticList) {
            byteStringList.add(StringUtil.toHex(item, 2))
        }

        // 供电电压
        byteStringList.add(StringUtil.toHex(socketSend.supplyVoltage, 2))

        // 湿度
        byteStringList.add(StringUtil.toHex(socketSend.humidity, 2))

        // 湿度偏离
        byteStringList.add(StringUtil.toHex(socketSend.humidityDeviation, 2))

        // 温度
        byteStringList.add(StringUtil.toHex(socketSend.temperature, 2))

        // 温度偏离
        byteStringList.add(StringUtil.toHex(socketSend.temperatureDeviation, 2))

        // pm浓度
        byteStringList.add(StringUtil.toHex(socketSend.pmConcentration, 2))

        // pm偏离
        byteStringList.add(StringUtil.toHex(socketSend.pmDeviation, 2))

        // voc等级
        byteStringList.add(StringUtil.toHex(socketSend.vocLevel, 2))

        // 室外voc偏移
        byteStringList.add(StringUtil.toHex(socketSend.vocOudDoorDeviation, 2))

        // 最小新风率
        byteStringList.add(StringUtil.toHex(socketSend.minFreshAirRate, 2))

        // 白天最大新风率
        byteStringList.add(StringUtil.toHex(socketSend.maxFreshAirRateDay, 2))

        // 夜晚最大新风率
        byteStringList.add(StringUtil.toHex(socketSend.maxFreshAirRateNight, 2))

        // 系统时间
        val systemTime2Hex = StringUtil.toHex(socketSend.systemTime2.toLong(), 8)
        byteStringList.add("${systemTime2Hex[0]}${systemTime2Hex[1]}")
        byteStringList.add("${systemTime2Hex[2]}${systemTime2Hex[3]}")
        byteStringList.add("${systemTime2Hex[4]}${systemTime2Hex[5]}")
        byteStringList.add("${systemTime2Hex[6]}${systemTime2Hex[7]}")

        // wifi状态
        byteStringList.add(StringUtil.toHex(socketSend.wifiStatu, 2))

        // net状态
        byteStringList.add(StringUtil.toHex(socketSend.netStatu, 2))

        // plc状态队列
        for (item in socketSend.plcStatuList) {
            byteStringList.add(StringUtil.toHex(item, 2))
        }

        // 累计功耗
        val totalPowerHex = StringUtil.toHex(socketSend.totalPower, 4)
        byteStringList.add("${totalPowerHex[0]}${totalPowerHex[1]}")
        byteStringList.add("${totalPowerHex[2]}${totalPowerHex[3]}")

        // 累计换风量
        val totalAirChangeHex = StringUtil.toHex(socketSend.totalAirChange.toLong(), 8)
        byteStringList.add("${totalAirChangeHex[0]}${totalAirChangeHex[1]}")
        byteStringList.add("${totalAirChangeHex[2]}${totalAirChangeHex[3]}")
        byteStringList.add("${totalAirChangeHex[4]}${totalAirChangeHex[5]}")
        byteStringList.add("${totalAirChangeHex[6]}${totalAirChangeHex[7]}")

        // 累计过滤PM2.5质量
        val totalFilteringPM25Hex = StringUtil.toHex(socketSend.totalFilteringPM25.toLong(), 8)
        byteStringList.add("${totalFilteringPM25Hex[0]}${totalFilteringPM25Hex[1]}")
        byteStringList.add("${totalFilteringPM25Hex[2]}${totalFilteringPM25Hex[3]}")
        byteStringList.add("${totalFilteringPM25Hex[4]}${totalFilteringPM25Hex[5]}")
        byteStringList.add("${totalFilteringPM25Hex[6]}${totalFilteringPM25Hex[7]}")

        // 更换过滤器时间
        val changeFilterTimeHex = StringUtil.toHex(socketSend.changeFilterTime.toLong(), 8)
        byteStringList.add("${changeFilterTimeHex[0]}${changeFilterTimeHex[1]}")
        byteStringList.add("${changeFilterTimeHex[2]}${changeFilterTimeHex[3]}")
        byteStringList.add("${changeFilterTimeHex[4]}${changeFilterTimeHex[5]}")
        byteStringList.add("${changeFilterTimeHex[6]}${changeFilterTimeHex[7]}")

        // 复位后累计功耗
        byteStringList.add(StringUtil.toHex(socketSend.resetTotalPower, 2))

        // 复位后累计换风量
        val resetTotalAirChangeHex = StringUtil.toHex(socketSend.resetTotalAirChange, 4)
        byteStringList.add("${resetTotalAirChangeHex[0]}${resetTotalAirChangeHex[1]}")
        byteStringList.add("${resetTotalAirChangeHex[2]}${resetTotalAirChangeHex[3]}")

        // 复位后累计过滤PM2.5质量
        val resetTotalFilteringPM25Hex = StringUtil.toHex(socketSend.resetTotalFilteringPM25, 4)
        byteStringList.add("${resetTotalFilteringPM25Hex[0]}${resetTotalFilteringPM25Hex[1]}")
        byteStringList.add("${resetTotalFilteringPM25Hex[2]}${resetTotalFilteringPM25Hex[3]}")

        // 风机控制模式
        byteStringList.add(StringUtil.toHex(socketSend.fanControlMode, 2))

        // 工作模式
        byteStringList.add(StringUtil.toHex(socketSend.workMode, 2))

        // 下一工作模式
        byteStringList.add(StringUtil.toHex(socketSend.nextWorkMode, 2))

        // 备份工作模式
        byteStringList.add(StringUtil.toHex(socketSend.backupWorkMode, 2))

        // 房间队列
        for (item in socketSend.roomList) {
            byteStringList.add(StringUtil.toHex(item.roomId, 2))
            byteStringList.add(StringUtil.toHex(item.temperature, 2))
            byteStringList.add(StringUtil.toHex(item.humidity, 2))
            byteStringList.add(StringUtil.toHex(item.voc, 2))
            val pm2_5Hex = StringUtil.toHex(item.pm2_5.toLong(), 4)
            byteStringList.add("${pm2_5Hex[0]}${pm2_5Hex[1]}")
            byteStringList.add("${pm2_5Hex[2]}${pm2_5Hex[3]}")
            byteStringList.add(StringUtil.toHex(item.brightness, 2))
            byteStringList.add("ff")
        }

        // 风机队列
        for (item in socketSend.fanList) {
            val stopTimeHex = StringUtil.toHex(item.stopTime, 8)
            byteStringList.add("${stopTimeHex[0]}${stopTimeHex[1]}")
            byteStringList.add("${stopTimeHex[2]}${stopTimeHex[3]}")
            byteStringList.add("${stopTimeHex[4]}${stopTimeHex[5]}")
            byteStringList.add("${stopTimeHex[6]}${stopTimeHex[7]}")
            byteStringList.add(StringUtil.toHex(item.targetPower, 2))
            val currentPowerHex = StringUtil.toHex(item.currentPower.toLong(), 4)
            byteStringList.add("${currentPowerHex[0]}${currentPowerHex[1]}")
            byteStringList.add("${currentPowerHex[2]}${currentPowerHex[3]}")
        }

        // 时段二维队列总长度为7*16=112个
        //Log.d(TAG, "-->> size = ${byteStringList.size}")
        for (item in socketSend.timeIntervalList) {
            for (item2 in item) {
                // 不进行进制转换
                //byteStringList.add(StringUtil.toHex(item2, 2))
                byteStringList.add(item2)
            }
        }

        // 模式二维队列总长度为7*16=112个
        for (item in socketSend.modeSetList) {
            for (item2 in item) {
                byteStringList.add(StringUtil.toHex(item2, 2))
            }
        }

        socketSend.dataLenght = byteStringList.size
        val dataLenghtHex = StringUtil.toHex(socketSend.dataLenght, 4)
        byteStringList.set(1, "${dataLenghtHex[0]}${dataLenghtHex[1]}")
        byteStringList.set(2, "${dataLenghtHex[2]}${dataLenghtHex[3]}")

        byteStringList.add("0d")
        byteStringList.add("0a")

        return byteStringList
    }
}
