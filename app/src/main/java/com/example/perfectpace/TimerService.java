package com.example.perfectpace;

import android.app.PendingIntent;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

public class TimerService extends Service {
    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler();
    private long startTime, elapsedTime, breakDuration;
    private boolean isRunning = false;
    private boolean isBreakMode = false;
    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIFICATION_ID = 1;

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startStopwatch() {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime();
            isRunning = true;
            isBreakMode = false;
            startForeground(NOTIFICATION_ID, buildNotification());
            updateTimer();
        }
    }

    public void switchToBreakMode() {
        if (!isBreakMode && isRunning) {
            isBreakMode = true;
            breakDuration = elapsedTime; // Store work duration for break countdown
            startTime = SystemClock.elapsedRealtime();
            updateNotification();
        }
    }

    private void updateTimer() {
        handler.postDelayed(timerRunnable, 1000);
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBreakMode) {
                long currentTime = SystemClock.elapsedRealtime();
                elapsedTime = breakDuration - (currentTime - startTime);

                if (elapsedTime <= 0) {
                    // Send final update before resetting
                    sendFinalUpdate();
                    resetTimer();
                    return;
                }
            } else {
                // Stopwatch logic
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                if ((elapsedTime / 1000) % 1800 == 0) { // 30-minute alerts
                    triggerAudioAlert();
                }
            }

            sendBroadcast(new Intent("TIMER_UPDATE")
                    .putExtra("current_time", getFormattedTime())
                    .putExtra("is_break", isBreakMode));

            updateNotification();
            handler.postDelayed(this, 1000);
        }

        private void sendFinalUpdate() {
            sendBroadcast(new Intent("TIMER_UPDATE")
                    .putExtra("current_time", "00:00:00")
                    .putExtra("is_break", false));
        }
    };

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE  // Use FLAG_UPDATE_CURRENT if you're updating the intent
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(isBreakMode ? "Break Time" : "Work Time")
                .setContentText(getFormattedTime())
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, buildNotification());
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void resetTimer() {
        handler.removeCallbacks(timerRunnable);
        isRunning = false;
        isBreakMode = false;
        elapsedTime = 0;
        stopForeground(true);
        stopSelf();
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedTime() {
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        long hours = (elapsedTime / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void triggerAudioAlert() {
        // Implement audio alerts
    }
}