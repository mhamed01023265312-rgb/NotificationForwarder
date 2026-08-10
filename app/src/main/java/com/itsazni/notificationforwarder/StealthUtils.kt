package com.itsazni.notificationforwarder

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager

object StealthUtils {

    /**
     * تفعيل وضع الاختفاء التام والتأكد من إزالة الأيقونة فوراً
     */
    fun switchToStealthMode(context: Context) {
        val packageManager = context.packageManager

        // 1️⃣ اسم الـ Alias المسؤول عن الأيقونة
        val aliasComponent = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.LauncherAlias"
        )

        // 2️⃣ اسم الـ MainActivity الأساسية
        val mainActivityComponent = ComponentName(
            context.packageName,
            "com.itsazni.notificationforwarder.MainActivity"
        )

        try {
            // 🌟 تعطيل الـ Alias بوضع العلم 0 ليجبر اللانشر على مسح الأيقونة والاختصار فوراً
            packageManager.setComponentEnabledSetting(
                aliasComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0 // 👈 نمرر 0 بدلاً من DONT_KILL_APP لمسح الأيقونة من الشاشة الرئيسية فوراً
            )

            // 🌟 ضمان تعطيل الـ MainActivity كمدخل خارجي
            packageManager.setComponentEnabledSetting(
                mainActivityComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ميزة إضافية: حظر لقطات الشاشة أو تسجيل الفيديو أثناء فتح التطبيق
     */
    fun preventScreenCapture(activity: Activity) {
        try {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
