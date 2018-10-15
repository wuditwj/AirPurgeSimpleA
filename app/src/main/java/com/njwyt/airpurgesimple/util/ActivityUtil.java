package com.njwyt.airpurgesimple.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ActivityUtil {

    /**
     * 获取当前正在运行的Activity名称
     * @param context
     * @return
     */
    public static String getCurrentActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        List<ActivityManager.RunningTaskInfo> forGroundActivity = activityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo currentActivity;
        currentActivity = forGroundActivity.get(0);
        return currentActivity.topActivity.getClassName();
    }
}
