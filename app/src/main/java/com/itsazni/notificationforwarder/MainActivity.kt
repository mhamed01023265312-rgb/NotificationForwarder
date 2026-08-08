package com.itsazni.notificationforwarder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsazni.notificationforwarder.ui.theme.AppTheme
import com.itsazni.notificationforwarder.worker.WorkerScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeveloperDashboardScreen()
                }
            }
        }
    }
}

// نموذج لبيانات السجل البرمجي
data class LogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val message: String,
    val type: LogType
)

enum class LogType { INFO, SUCCESS, ERROR }

@Composable
private fun DeveloperDashboardScreen() {
    val context = LocalContext.current
    
    // حالة الإذن
    var isPermissionGranted by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    
    // حالة الخدمة في الخلفية
    var isServiceRunning by remember { mutableStateOf(false) }
    
    // قائمة السجلات المباشرة
    val logs = remember { mutableStateListOf<LogEntry>() }

    fun addLog(message: String, type: LogType = LogType.INFO) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logs.add(0, LogEntry(timestamp = time, message = message, type = type))
    }

    LaunchedEffect(Unit) {
        addLog("تم تشغيل لوحة الفحص والمراقبة بنجاح", LogType.INFO)
        if (!isPermissionGranted) {
            addLog("تحذير: إذن قراءة الإشعارات غير مفعل!", LogType.ERROR)
        } else {
            addLog("إذن قراءة الإشعارات مفعل وجاهز", LogType.SUCCESS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "لوحة مراقبة تدفق البيانات",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 1. بطاقة فحص الإذن الصريح
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPermissionGranted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "إذن قراءة الإشعارات",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = if (isPermissionGranted) "مفعل (Granted)" else "مفقود (Missing)",
                        fontSize = 12.sp,
                        color = if (isPermissionGranted) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }

                if (!isPermissionGranted) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("تفعيل الإذن", fontSize = 12.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32))
                    )
                }
            }
        }

        // 2. تفاصيل البيانات المستخرجة والجهة المستقبلة
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "معلومات التمرير (Data Pipeline)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "• البيانات الملتقطة: اسم التطبيق (appName)، العنوان (title)، النص (text)", fontSize = 11.sp)
                Text(text = "• طريقة التمرير: HTTP GET Request / Webhook", fontSize = 11.sp)
                Text(text = "• نقطة الوصول (Destination): CallMeBot WhatsApp API", fontSize = 11.sp)
            }
        }

        // 3. أزرار التحكم والتشغيل
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    isPermissionGranted = isNotificationListenerEnabled(context)
                    if (isPermissionGranted) {
                        WorkerScheduler.enqueueImmediate(context)
                        WorkerScheduler.ensurePeriodic(context)
                        isServiceRunning = true
                        addLog("تم بدء خدمة التحويل في الخلفية (Worker Queued)", LogType.SUCCESS)
                        addLog("جارٍ فحص طابور الإشعارات المعلقة...", LogType.INFO)
                    } else {
                        addLog("فشل التشغيل: يرجى منح إذن الإشعارات أولاً", LogType.ERROR)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isServiceRunning) "إعادة مزامنة" else "بدء الخدمة")
            }

            OutlinedButton(
                onClick = {
                    isPermissionGranted = isNotificationListenerEnabled(context)
                    addLog("تم إجراء فحص يدوي لحالة النظام والإذونات", LogType.INFO)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("فحص الحالة")
            }
        }

        // 4. سجل المراقبة المباشر (Live Console Output)
        Text(
            text = "سجل الأحداث المباشر (Live Execution Logs):",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0xFF1E1E1E), // خلفية داكنة مثل الـ Terminal
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    val logColor = when (log.type) {
                        LogType.INFO -> Color(0xFF81D4FA)
                        LogType.SUCCESS -> Color(0xFFA5D6A7)
                        LogType.ERROR -> Color(0xFFEF9A9A)
                    }
                    Text(
                        text = "[${log.timestamp}] ${log.message}",
                        color = logColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// دالة مساعدة للتحقق من تفعيل إذن قراءة الإشعارات
private fun isNotificationListenerEnabled(context: Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(packageName) == true
}
