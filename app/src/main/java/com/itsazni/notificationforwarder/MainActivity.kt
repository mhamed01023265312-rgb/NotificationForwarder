package com.itsazni.notificationforwarder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsazni.notificationforwarder.ui.theme.AppTheme
import com.itsazni.notificationforwarder.worker.WorkerScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SingleButtonScreen()
                }
            }
        }
    }
}

@Composable
private fun SingleButtonScreen() {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                // التشغيل المباشر للخدمة وإرسال الإشعارات
                WorkerScheduler.enqueueImmediate(context)
                WorkerScheduler.ensurePeriodic(context)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Start",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

