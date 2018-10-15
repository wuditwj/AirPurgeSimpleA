package com.njwyt.airpurgesimple.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.njwyt.airpurgesimple.R
import com.njwyt.airpurgesimple.application.App
import com.njwyt.airpurgesimple.content.Type
import com.njwyt.airpurgesimple.db.ReservoirHelper
import com.njwyt.airpurgesimple.entity.DataManage
import com.njwyt.airpurgesimple.entity.MessageEvent
import com.njwyt.airpurgesimple.entity.SystemMode
import com.njwyt.airpurgesimple.util.ProgressDialogUtil
import com.njwyt.airpurgesimple.util.ToastUtil
import kotlinx.android.synthetic.main.activity_data_manage.*
import kotlinx.android.synthetic.main.dialog_simple.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by Administrator on 2018/2/28.
 */
class DataManageActivity : BaseActivity(), View.OnClickListener {

    private var reflashHandler = Handler()
    private var dataBackUp = 0;
    private var dataRestore = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_data_manage)
        initToolBar()
        initListener()
    }

    private fun initListener() {
        data_backup.setOnClickListener(this)
        data_upload.setOnClickListener(this)
        data_download.setOnClickListener(this)
        data_analysis.setOnClickListener(this)
        data_restore.setOnClickListener(this)
        data_restore_factory.setOnClickListener(this)
    }


    private fun initToolBar() {
        //设置标题栏
        tool_bar.setTitle("")
        titleTextView.setText(R.string.set_data)
        tool_bar.setOnClickListener {
            finish()
        }
        setSupportActionBar(tool_bar)
    }

    /**
     * 从Application实时获取数据
     */
    @Subscribe
    fun getSystemModeByEventBus(msg: MessageEvent<SystemMode>) {

        when (msg.message) {
        // 发送成功
            Type.DISMISS_PROGRESS_DIALOG -> {

                if (ProgressDialogUtil.dismissByActivity(this@DataManageActivity)) {
                    // 新开启线程弹通知
                    reflashHandler.post {
                        ToastUtil.showToast(this@DataManageActivity, R.string.hint_edit_success)
                    }
                }
            }

        // 发送超时
            Type.DISMISS_PROGRESS_DIALOG_TIMEOUT -> {

                // 新开启线程弹通知
                reflashHandler.post {
                    ToastUtil.showToast(this@DataManageActivity, R.string.hint_timeout)
                }
                if (ProgressDialogUtil.isShowing()) {
                    ProgressDialogUtil.dismissByActivity(this@DataManageActivity)
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
        //备份
            R.id.data_backup -> {
                dataBackUp = 1
                showDialog(R.string.hint_data_backup, R.id.data_backup)
            }
        //上传
            R.id.data_upload ->
                Toast.makeText(this, "===", Toast.LENGTH_SHORT).show()
        //下载
            R.id.data_download ->
                Toast.makeText(this, "===", Toast.LENGTH_SHORT).show()
        //分析
            R.id.data_analysis ->
                Toast.makeText(this, "===", Toast.LENGTH_SHORT).show()
        //恢复
            R.id.data_restore -> {
                dataRestore = 1
                showDialog(R.string.hint_data_restore, R.id.data_restore)
            }
        // 恢复出厂设置
            R.id.data_restore_factory -> {
                showDialog(R.string.hint_data_restore_factory, R.id.data_restore_factory)
            }
        }

    }

    /**
     * 弹出提示框
     *
     * @param
     */
    private fun showDialog(hintRes: Int, clickRes: Int) {
        val deleteDialog = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_simple, null)
        deleteDialog.setView(dialogView)
        deleteDialog.setCancelable(false)
        val dialog = deleteDialog.show()

        dialogView.tv_hint.setText(hintRes)
        // 是
        dialogView.btn_ok.setOnClickListener {
            when (clickRes) {
                R.id.data_backup -> notifyDataChange()
                R.id.data_restore -> notifyDataChange()
                R.id.data_restore_factory -> restoreFactory()
            }
            dialog.dismiss()
        }

        // 否
        dialogView.btn_no.setOnClickListener { dialog.dismiss() }
    }

    /**
     * 通知提交数据
     */
    private fun notifyDataChange() {
        val dataManage = DataManage()
        dataManage.dataBackUp = dataBackUp
        dataManage.dataRestore = dataRestore
        ReservoirHelper.setDataManage(dataManage)
        // 通知系统提交
        App.instance?.saveSocketMessage()

        // 弹出等待框不给用户操作
        ProgressDialogUtil.show(this, R.string.waiting)
    }

    /**
     * 恢复出厂设置
     */
    private fun restoreFactory() {

        val byteStringList = ArrayList<String>()
        val byteString = Type.FACTORY_SETTING
        //byteString = byteString.replace("(.{2})".toRegex(), "$1 ")

        for (i in 0 until byteString.length step 2) {
            byteStringList.add("${byteString[i]}${byteString[i + 1]}")
        }
        // 直接修改等待上传的十六进制
        ReservoirHelper.setSendData(byteStringList)

        // 弹出等待框不给用户操作
        ProgressDialogUtil.show(this, R.string.waiting)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}