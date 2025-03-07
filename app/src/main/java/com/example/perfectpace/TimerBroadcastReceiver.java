package com.example.perfectpace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class TimerBroadcastReceiver extends BroadcastReceiver {
    // Interface to pass updates to MainActivity
    public interface TimerUpdateListener {

        void onTimeUpdate(Intent intent); // Pass full Intent
    }

    private static TimerUpdateListener listener;

    // Set the listener from MainActivity
    public static void setTimerUpdateListener(TimerUpdateListener activityListener) {
        listener = activityListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "TIMER_UPDATE")) {
            // Get time from service
            String currentTime = intent.getStringExtra("current_time");

            // Safely update UI if listener is registered
            if (listener != null) {
                listener.onTimeUpdate(intent);
                System.out.println("Broadcast received: " + currentTime);
            }
        }
    }
}
