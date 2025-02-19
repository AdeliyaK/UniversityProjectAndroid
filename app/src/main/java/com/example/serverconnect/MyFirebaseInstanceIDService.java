package com.example.serverconnect;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    public static final String TAG = "Push_Android";
    private static final String CHANNEL_ID = "my_channel_id";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        MainActivity.sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "From: " + message.getFrom());

        // Проверка за data payload
        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
        }

        // Проверка за notification payload
        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            showNotification(message.getNotification().getTitle(), message.getNotification().getBody());
        }
    }

    private void showNotification(String title, String message) {
        // Създаване на Notification Channel (за Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Main Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Създаване на известие
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Смени с реална икона
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        // Проверка за разрешение (за Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Missing notification permission!");
            return;
        }

        manager.notify(1, builder.build());
    }
}
