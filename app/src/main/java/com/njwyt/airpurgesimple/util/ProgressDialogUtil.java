package com.njwyt.airpurgesimple.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

import com.njwyt.airpurgesimple.R;


/**
 * 进度条对话框(转圈圈)
 *
 * @author Jason-Samuel
 */
public class ProgressDialogUtil {

    private static ProgressDialog mProgressDialog;            // 等待对话框

    /**
     * 显示ProgressDialog
     *
     * @param activity 当前Activity
     * @param message  ProgressDialog所显示的消息
     */
    public static void show(final Activity activity, String message) {
        if (mProgressDialog != null) {
            dismiss();
        }
        mProgressDialog = ProgressDialog.show(activity, "", message);
        mProgressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    activity.finish();
                    activity.overridePendingTransition(0, R.anim.push_right_out);
                }
                return false;
            }
        });
    }

    /**
     * 显示ProgressDialog
     *
     * @param activity  当前Activity
     * @param messageId ProgressDialog所显示的消息
     */
    public static void show(final Activity activity, int messageId) {
        show(activity, activity.getString(messageId));
    }

    /**
     * 隐藏ProgressDialog
     */
    public static void dismiss() {
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {
        }
    }

    /**
     * 判断当前正在运行的Activity后隐藏ProgressDialog
     */
    public static boolean dismissByActivity(Context context) {

        if (!mProgressDialog.isShowing()) {
            return false;
        }
        Context dialogContext = ((ContextWrapper) mProgressDialog.getContext()).getBaseContext();

        if (dialogContext instanceof Activity) {
            if (!((Activity) dialogContext).isFinishing() && !((Activity) dialogContext).isDestroyed()) {

                if (context.getClass().getName().equals(ActivityUtil.getCurrentActivityName(context))) {
                    dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isShowing() {
        if (mProgressDialog == null) {
            return false;
        }
        return mProgressDialog.isShowing();
    }
}
