package com.njwyt.airpurgesimple.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.util.StringUtil
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException


/**
 * Created by jason_samuel on 2018/4/4.
 */

class ClientService : Service() {

    private val TAG = "ClientService"
    private var ip = ""
    private val port = 8080
    private var socket: Socket? = null
    private var socketIsClose = true
    private var socketLenght = 3

    private var mCallBack: CallBack? = null

    private var data = "服务器正在执行"


    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "-->> 服务器已绑定")
        return ClientBinder()
    }

    inner class ClientBinder : android.os.Binder() {

        fun getClientService(): ClientService {
            return this@ClientService
        }

        fun setData(data: String) {
            this@ClientService.data = data
        }

        /**
         * 使用socket向服务器发送数据包
         */
        fun sendSocketData(byteString: String) {

            if (socketIsClose) return
            Thread {
                val outputStream = socket?.getOutputStream()
                // 转成十六进制byte发送
                //val buff = StringUtil.toBytes(byteString)
                //outputStream?.write(buff)
                //outputStream?.flush()

                // 按25个字节一段来切割发送
                Log.d(TAG, "-->> sendString = ${byteString}")
                var cutString = byteString
                var sendString = ""
                while (cutString.length > 50) {
                    sendString = cutString.substring(0, 50)
                    cutString = cutString.substring(50)
                    //Log.d(TAG, "-->> sendString = ${sendString}")
                    val buff = StringUtil.toBytes(sendString)
                    outputStream?.write(buff)
                    outputStream?.flush()
                    //Thread.sleep(50)
                }
                if (cutString.length != 0) {
                    val buff = StringUtil.toBytes(cutString)
                    //Log.d(TAG, "-->> sendString = ${cutString}")
                    outputStream?.write(buff)
                    outputStream?.flush()
                }
                Log.d(TAG, "-->> send message")
                /*val writer = DataOutputStream(socket?.getOutputStream())
                writer.writeUTF("\r\n") // 写一个UTF-8的信息
                Log.d(TAG, "-->> sendSocketData")*/
            }.start()
        }

        fun stopService() {
            stopSocket()
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "-->>onCreate: ")
        startSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "-->>onDestroy: ")
        stopSocket()
    }

    var count = 0
    private val socketThread = Thread(Runnable {

        while (true) {
            try {
                if (socket == null) {
                    // 创建Socket对象 & 指定服务端的IP及端口号
                    ip = ReservoirHelper.getIP()
                    socket = Socket(ip, port)

                } else {
                    if (!socketIsClose) {
                        val inputStream = socket?.getInputStream()
                        val count = inputStream?.available()!!

                        if (count > 0) {
                            val buff = ByteArray(count)
                            inputStream.read(buff)
                            val byteString = StringUtil.bytes2hex03(buff)
                            val byteStringList = ArrayList<String>()
                            for (i in 0 until byteString.length step 2) {
                                byteStringList.add("${byteString[i]}${byteString[i + 1]}")
                            }
                            Log.d(TAG, "-->> 数据一共有${byteStringList.size}个")
                            //Log.d(TAG, "-->> 数据内容是${byteString}")
                            // 效验数据头尾
                            if (byteStringList[0] == "ff" &&
                                    byteStringList[byteStringList.size - 1] == "0a" &&
                                    byteStringList[byteStringList.size - 2] == "0d") {
                                mCallBack?.onDataChange(byteStringList)
                                //Log.d(TAG, "-->> 开始解析...")
                            }
                        }


                        //val buff = ByteArray(count)
                        //inputStream?.read(buff)
                        /*var count = 0
                        val byteStringList = ArrayList<String>()
                        while (!socketIsClose) {
                            val byteStr = StringUtil.bytes2hex03(buff)
                            byteStringList.add(byteStr)
                            if (count == 2) {
                                socketLenght = Integer.parseInt(byteStringList[1] + byteStringList[2], 16)
                                if (socketLenght == 0) break
                            }
                            count++
                            if (socketLenght <= count) {
                                // 如果当前计数器大于等于服务器发送字节的总长度就退出
                                break
                            }
                            inputStream?.read(buff)
                        }
                        // 数据为6的时候是服务器通知客户端可以发送消息
                        Log.d(TAG, "-->> 数据一共有${byteStringList.size}个")
                        // 发送数据给Application
                        mCallBack?.onDataChange(byteStringList)
                        //parseList(byteStringList)
                        //Log.d(TAG, "-->> 数据一共有${byteStringList.size}个")
                        //byteStringList.removeAll(byteStringList)
                        //mSocketData = SocketData()*/
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 判断客户端和服务器是否连接成功，睡眠1秒
                try {
                    socket!!.sendUrgentData(0)
                    // 连接正常
                    socketIsClose = false
                } catch (e: Exception) {
                    // 断开连接
                    socketIsClose = true
                } finally {
                    if (socketIsClose) {
                        resetSocket()
                    }
                    Thread.sleep(1000)
                }
            }
        }

    })

    private fun startSocket() {
        socketThread.start()
        socketIsClose = false
    }

    private fun stopSocket() {
        socketIsClose = true
        socket?.close()
        socketThread.interrupt()
        socketThread.join()
    }

    /**
     * 重新连接
     */
    private fun resetSocket() {
        try {
            ip = ReservoirHelper.getIP()
            socket = Socket(ip, port)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setCallBackListener(callBack: CallBack) {
        mCallBack = callBack
    }

    interface CallBack {
        fun onDataChange(byteStringList: ArrayList<String>)
    }
}
