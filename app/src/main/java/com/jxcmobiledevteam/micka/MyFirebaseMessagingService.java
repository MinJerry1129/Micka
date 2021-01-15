package com.jxcmobiledevteam.micka;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.installations.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import okhttp3.internal.Util;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public MyFirebaseMessagingService() {
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCMtoken:", "Refreshed token: " + token);
        Common.getInstance().setPhone_token(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("Remote Message::", "From: " + remoteMessage.getFrom());
        if(remoteMessage.getData().size() > 0){
            Log.d("Message data payload::", "From: " + remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null) {
            Log.d("Notification body::", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle(remoteMessage.getNotification().getTitle())
//                .setContentText(remoteMessage.getNotification().getBody())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .build();
//        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
//        manager.notify(123, notification);

//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.notification);
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel("Sesame", "Sesame", importance);
            mChannel.setDescription(remoteMessage.getNotification().getBody());
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            mChannel.setSound(defaultSoundUri, audioAttributes);
//
            mNotifyManager.createNotificationChannel(mChannel);
            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
            bigTextStyle.setBigContentTitle(remoteMessage.getNotification().getTitle());
            bigTextStyle.bigText(remoteMessage.getNotification().getBody());
            Notification notification = new Notification.Builder(this, "Sesame")
                    .setStyle(bigTextStyle)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .build();
            final int random = new Random().nextInt(61) + 20;
            mNotifyManager.notify(random, notification);
        }
//For Android Version lower than oreo.
        else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setColor(Color.parseColor("#FFD600"))
                    .setChannelId("Sesame")
                    .setPriority(NotificationCompat.PRIORITY_LOW);
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle(remoteMessage.getNotification().getTitle());
            bigTextStyle.bigText(remoteMessage.getNotification().getBody());
            mBuilder.setStyle(bigTextStyle);
            final int random = new Random().nextInt(61) + 20;
            mNotifyManager.notify(random, mBuilder.build());
        }
    }
}
