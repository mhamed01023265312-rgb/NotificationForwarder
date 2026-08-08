package com.itsazni.notificationforwarder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsazni.notificationforwarder.ui.theme.AppTheme
import com.itsazni.notificationforwarder.worker.WorkerScheduler
import kotlinx.coroutines.launch
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
                    color = Color(0xFF0F0E17) // خلفية آركيد داكنة
                ) {
                    PixelAssistantScreen()
                }
            }
        }
    }
}

// نموذج رسائل المساعد الذكي
data class PixelMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String, // "BOT" or "USER"
    val text: String,
    val time: String
)

@Composable
fun PixelAssistantScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isPermissionGranted by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var userInput by remember { mutableStateOf("") }

    // قائمة رسائل المساعد وقاعدة بيانات المواعيد المتسجلة
    val messages = remember {
        mutableStateListOf(
            PixelMessage(
                sender = "BOT",
                text = "أهلاً بك يا بطل! 🤖 أنا مساعدك البكسلي الذكي.. خدمة الإشعارات أصبحت شغالـة حالياً في الخلفية تلقائياً! ⚡\n\nتقدر تسألني عن مواعيدك أو تقول لي يسجل لك نوت جديدة.",
                time = getCurrentTime()
            )
        )
    }

    val scheduleNotes = remember {
        mutableStateListOf(
            "مراجعة إعدادات السيرفر 🎯",
            "اختبار إشعارات التليجرام 📱",
            "إنهاء مهام اليوم 🚀"
        )
    }

    // التشغيل التلقائي للخدمة بمجرد فتح التطبيق
    LaunchedEffect(Unit) {
        if (isPermissionGranted) {
            WorkerScheduler.enqueueImmediate(context)
            WorkerScheduler.ensurePeriodic(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. هيدر الآركيد والشخصية المباشرة ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F1B24), shape = RoundedCornerShape(4.dp))
                .border(2.dp, Color(0xFF00FF66), shape = RoundedCornerShape(4.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // روبوت بكسلي متحرك
            PixelRobotMascot(
                modifier = Modifier
                    .size(54.dp)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PIXEL-BOT v1.0",
                    color = Color(0xFF00FF66),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isPermissionGranted) "● النظام متصل والشغال بالخلفية" else "▲ إذن الإشعارات مفقود!",
                    color = if (isPermissionGranted) Color(0xFF00E5FF) else Color(0xFFFF0055),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. تنبيه الإذن لو غير مفعل ---
        if (!isPermissionGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(2.dp, Color(0xFFFF0055), RoundedCornerShape(4.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0813))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "نحتاج إذن وصول الإشعارات لتشغيل المحول!",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("تفعيل الآن", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // --- 3. شاشة المحادثة واستعراض المواعيد ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp)),
            color = Color(0xFF05050A)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    PixelMessageBubble(msg)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 4. حقل إدخال الآركيد وإرسال الأسئلة ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = {
                    Text("اسأل المساعد (مثلاً: علينا إيه النهاردة؟)", fontSize = 11.sp, color = Color.Gray)
                },
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1F1B24)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FF66),
                    unfocusedBorderColor = Color(0xFF00E5FF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    processUserInput(userInput, messages, scheduleNotes) { userInput = "" }
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                })
            )

            Button(
                onClick = {
                    processUserInput(userInput, messages, scheduleNotes) { userInput = "" }
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                },
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66))
            ) {
                Text("إرسال", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// معالجة أسئلة المستخدم والرد الآلي للمساعد الذكي
private fun processUserInput(
    input: String,
    messages: MutableList<PixelMessage>,
    scheduleNotes: MutableList<String>,
    onClear: () -> Unit
) {
    if (input.isBlank()) return

    val userText = input.trim()
    messages.add(PixelMessage(sender = "USER", text = userText, time = getCurrentTime()))
    onClear()

    // تحليل رد البوت على الأسئلة الكلاسيكية
    val botReply = when {
        userText.contains("علينا ايه") || userText.contains("مواعيد") || userText.contains("جدول") || userText.contains("النهاردة") -> {
            val notesFormatted = scheduleNotes.mapIndexed { i, note -> "${i + 1}. $note" }.joinToString("\n")
            "📅 مواعيدك المسجلة عندك اليوم:\n$notesFormatted"
        }
        userText.contains("اضف") || userText.contains("سجل") || userText.contains("ميعاد") -> {
            val cleanTask = userText.replace("اضف", "").replace("سجل", "").replace("ميعاد", "").trim()
            if (cleanTask.isNotEmpty()) {
                scheduleNotes.add(cleanTask)
                "✅ تم تسجيل النوت بنجاح: \"$cleanTask\""
            } else {
                "💡 اذكر اسم الميعاد أو المهمة بعد كلمة 'سجل'."
            }
        }
        else -> {
            "👾 فهمتك! أحياناً بكون مشغول بمراقبة وتمرير الإشعارات للتليجرام.. تقدر تسألني: 'علينا إيه النهاردة؟' أو تقول لي 'سجل ميعاد [المهمة]'."
        }
    }

    messages.add(PixelMessage(sender = "BOT", text = botReply, time = getCurrentTime()))
}

// رسم روبوت مبكسل كلاسيكي باستخدام الـ Canvas
@Composable
fun PixelRobotMascot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot_anim")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_float"
    )

    Canvas(modifier = modifier.offset(y = floatY.dp)) {
        val pixelSize = size.width / 10f
        
        // مصفوفة البكسل للروبوت (10x10)
        val grid = listOf(
            "0001111000",
            "0000110000",
            "0111111110",
            "1101111011",
            "1100110011",
            "1111111111",
            "0110000110",
            "0011111100",
            "0110110110",
            "1100000011"
        )

        grid.forEachIndexed { r, row ->
            row.forEachIndexed { c, char ->
                if (char == '1') {
                    drawRect(
                        color = Color(0xFF00FF66),
                        topLeft = Offset(c * pixelSize, r * pixelSize),
                        size = Size(pixelSize - 1f, pixelSize - 1f)
                    )
                }
            }
        }
    }
}

// تصميم حبابات المحادثة بلغة الآركيد
@Composable
fun PixelMessageBubble(msg: PixelMessage) {
    val isBot = msg.sender == "BOT"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isBot) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isBot) Color(0xFF121A24) else Color(0xFF1E2B1A),
                    shape = RoundedCornerShape(2.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isBot) Color(0xFF00E5FF) else Color(0xFF00FF66),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = if (isBot) "🤖 PIXEL-BOT" else "👤 أنت",
                color = if (isBot) Color(0xFF00E5FF) else Color(0xFF00FF66),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg.text,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = msg.time,
                color = Color.Gray,
                fontSize = 8.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(packageName) == true
}
