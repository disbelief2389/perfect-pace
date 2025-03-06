package com.example.perfectpace

import android.content.pm.PackageManager
import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.perfectpace.TimerBroadcastReceiver.TimerUpdateListener.formattedTime
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
        currentTime = formattedTime
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

//        val intent = Intent(this, TimerService::class.java)
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

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
                        modifier = Modifier.padding(innerPadding),
                        currentTime = currentTime,
                        isWorking = isWorking,
                        onStartStop = { timerService?.startStopwatch() },
                        onBreak = { timerService?.switchToBreakMode() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
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
    isWorking: Boolean,
    onStartStop: () -> Unit,
    onBreak: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentTime,
            modifier = Modifier.padding(24.dp)
        )

        androidx.compose.material3.Button( // Explicitly reference Compose Button
            onClick = onStartStop,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(if (isWorking) "Start Work" else "Pause")
        }

        androidx.compose.material3.Button(
            onClick = onBreak,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Take Break")
        }
    }
}

// Replace Greeting with this preview:
@Preview(showBackground = true)
@Composable
fun TimerPreview() {
    PerfectPaceTheme {
        TimerScreen(
            currentTime = "00:25:30",
            isWorking = true,
            onStartStop = {},
            onBreak = {}
        )
    }
}