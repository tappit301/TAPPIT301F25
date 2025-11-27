package com.example.eventapp.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.eventapp.App;
import com.example.eventapp.R;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central place for:
 *  - Saving notifications in Firestore
 *  - Optionally showing a local system notification
 */
public class NotificationHelper {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public static void logNotificationToFirestore(
            String userId,
            String type,
            String message
    ) {
        if (userId == null || userId.isEmpty()) return;

        FirebaseFirestore db = FirebaseHelper.getFirestore();

        Map<String, Object> doc = new HashMap<>();
        doc.put("userId", userId);
        doc.put("type", type);
        doc.put("message", message);
        doc.put("createdAt", Timestamp.now());
        doc.put("read", false);

        db.collection("notifications").add(doc);
    }

    public static void showLocalNotification(
            Context context,
            String title,
            String message
    ) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications_24)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        Notification notification = builder.build();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(COUNTER.getAndIncrement(), notification);
        }
    }

    public static void notifyUser(
            Context context,
            String userId,
            String type,
            String title,
            String message
    ) {
        logNotificationToFirestore(userId, type, message);

        if (context != null) {
            showLocalNotification(context, title, message);
        }
    }
}
