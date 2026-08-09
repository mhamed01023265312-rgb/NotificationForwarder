package com.itsazni.notificationforwarder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthUtils {

    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // تعطيل MainActivityAlias الظاهر على الشاشة الرئيسية
        val aliasComponent = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.MainActivityAlias"
        )

        try {
            packageManager.setComponentEnabledSetting(
                aliasComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0 // لإجبار لانشر الموبايل على حذف الأيقونة فوراً
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
