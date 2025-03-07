package com.example.perfectpace

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perfectpace.ui.theme.PerfectPaceTheme

class MainActivity : ComponentActivity(), TimerBroadcastReceiver.TimerUpdateListener {
    private var timerService: TimerService? = null
    private var isBound = false

    // Compose state
    private var currentTime by mutableStateOf("00:00:00")
    private var isWorking by mutableStateOf(true)

    private lateinit var timerBroadcastReceiver: TimerBroadcastReceiver

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.service
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            timerService = null
        }
    }

    override fun onTimeUpdate(intent: Intent?) {
        currentTime = intent?.getStringExtra("current_time") ?: "00:00:00"
        println("Time updated: $currentTime")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // In your onCreate() method:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

        timerBroadcastReceiver = TimerBroadcastReceiver()
        TimerBroadcastReceiver.setTimerUpdateListener(this)
        val filter = IntentFilter("TIMER_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerBroadcastReceiver, filter)
        }

        setContent {
            PerfectPaceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .clickable {
                                println("Screen tapped. isWorking: $isWorking")
                                if (isWorking) {
                                    timerService?.switchToBreakMode()
                                } else {
                                    timerService?.startStopwatch()
                                }
                                isWorking = !isWorking
                            },
                        currentTime = currentTime,
                        isWorking = isWorking
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        startService(intent) // Start the service initially
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        println("Service started and bound")
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        unregisterReceiver(timerBroadcastReceiver)
    }
}

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    currentTime: String,
    isWorking: Boolean
) {
    val backgroundColor by animateColorAsState(
        if (isWorking) {
            Color.White
        } else {
            Color.Black
        }, animationSpec = tween(durationMillis = 150)
    )
    val textColor by animateColorAsState(
        if (isWorking) {
            Color.Black
        } else {
            Color.White
        }, animationSpec = tween(durationMillis = 150)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable { /* Click handling is done in MainActivity */ },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentTime,
            color = textColor,
            fontSize = 48.sp,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimerPreview() {
    PerfectPaceTheme {
        TimerScreen(
            currentTime = "00:25:30",
            isWorking = true
        )
    }
}