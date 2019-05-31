package com.example.sch;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

import static com.example.sch.LoginActivity.log;


public class MyFirebaseService extends FirebaseMessagingService {

    public MyFirebaseService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log("message received");
//        log("from: " + remoteMessage.getFrom());
//        log("msg id: " + remoteMessage.getMessageId());
        Intent data = remoteMessage.toIntent();
        long time = 15551882447791L;
        switch (data.getStringExtra("type")) {
            case "mark":
                log("new mark");
                int val = Integer.parseInt(data.getStringExtra("val")),
                        unitId = Integer.parseInt(data.getStringExtra("unitId"));
                double coef = Double.parseDouble(data.getStringExtra("coef"));
                NotificationManager notificationManager;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationManager = getSystemService(NotificationManager.class);
                    if (notificationManager.getNotificationChannel("1") == null) {
                        CharSequence ch_name = "New messages";
                        String description = "CHANNEL_DESCRIPTION";

                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel channel = new NotificationChannel("1", ch_name, importance);
                        channel.setDescription(description);
                        // Register the channel with the system; you can't change the importance
                        // or other notification behaviors after this

                        notificationManager.createNotificationChannel(channel);
                    }
                }
                ArrayList<PeriodFragment.Subject> subjects = TheSingleton.getInstance().getSubjects();
                if(subjects == null) {
                    return;
                }
                PeriodFragment.Subject s;
                for (int i = 0; i < subjects.size(); i++) {
                    s = subjects.get(i);
                    if (s.unitid == unitId) {
                        NotificationManagerCompat compat = NotificationManagerCompat.from(this);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
                        builder.setContentText(s.name + ": " + val + " с коэф. " + coef)
                                .setContentTitle("Новая оценка")
                                .setSmallIcon(R.drawable.attach);
                        Notification notif = builder.build();
                        compat.notify(TheSingleton.getInstance().notification_id++, notif);
                        TheSingleton.getInstance().setHasNotifications(true);
                        break;
                    }
                }
                break;
            case "msg":
                log("new message");
                log("sender: " + remoteMessage.toIntent().getStringExtra("senderFio"));
                log("text: " + remoteMessage.toIntent().getStringExtra("text"));
                String text = remoteMessage.toIntent().getStringExtra("text");
                String sender_fio = remoteMessage.toIntent().getStringExtra("senderFio");
                int sender = Integer.parseInt(remoteMessage.toIntent().getStringExtra("senderId"));
                int thread_id = Integer.parseInt(remoteMessage.toIntent().getStringExtra("threadId"));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationManager = getSystemService(NotificationManager.class);
                    if (notificationManager.getNotificationChannel("1") == null) {
                        CharSequence ch_name = "New messages & marks";
                        String description = "CHANNEL_DESCRIPTION";

                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel channel = new NotificationChannel("1", ch_name, importance);
                        channel.setDescription(description);
                        // Register the channel with the system

                        notificationManager.createNotificationChannel(channel);
                    }
                }
                if (isBackground()) {
                    // background/not running
                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(this, LoginActivity.class);
                    resultIntent.putExtra("notif", true);
                    resultIntent.putExtra("type", "msg");
                    resultIntent.putExtra("threadId", thread_id);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    stackBuilder.editIntentAt(0).putExtra("notif", true);
                    stackBuilder.editIntentAt(0).putExtra("type", "msg");
                    stackBuilder.editIntentAt(0).putExtra("threadId", thread_id);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
                    builder.setContentTitle(remoteMessage.toIntent().getStringExtra("senderFio"))
                            .setContentText(remoteMessage.toIntent().getStringExtra("text"))
                            .setSmallIcon(R.drawable.attach)
                            .setContentIntent(resultPendingIntent);
                    Notification notif = builder.build();
                    NotificationManagerCompat.from(this).notify(TheSingleton.getInstance().notification_id++, notif);
                } else {
                    // application is in foreground
                    log("sending to broadcast");
                    sendBroadcast(new Intent("com.example.sch.action").putExtra("text", text).putExtra("sender_fio", sender_fio)
                            .putExtra("time", time).putExtra("sender_id", sender).putExtra("thread_id", thread_id));
                }
                if (remoteMessage.getNotification() != null) {
                    log(remoteMessage.getNotification().getTitle() + "");
                    log(remoteMessage.getNotification().getBody() + "");
                }
                break;
            case "lesson":
                log("new lesson");
                String subject = data.getStringExtra("subject"),
                    event = data.getStringExtra("event");
                coef = Integer.parseInt(data.getStringExtra("coef"));
                unitId = Integer.parseInt(data.getStringExtra("unitId"));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationManager = getSystemService(NotificationManager.class);
                    if (notificationManager.getNotificationChannel("1") == null) {
                        CharSequence ch_name = "New messages & marks";
                        String description = "CHANNEL_DESCRIPTION";

                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel channel = new NotificationChannel("1", ch_name, importance);
                        channel.setDescription(description);
                        // Register the channel with the system

                        notificationManager.createNotificationChannel(channel);
                    }
                }
                subjects = TheSingleton.getInstance().getSubjects();
                for (int i = 0; i < subjects.size(); i++) {
                    s = subjects.get(i);
                    if (s.unitid == unitId) {
                        NotificationManagerCompat compat = NotificationManagerCompat.from(this);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
                        builder.setContentTitle("Новая клетка")
                                .setContentText(subject + "/" + s.name + ": коэф " + coef)
                                .setSmallIcon(R.drawable.attach);
                        Notification notif = builder.build();
                        compat.notify(TheSingleton.getInstance().notification_id++, notif);
                        break;
                    }
                }
                break;
        }


        //long time = Integer.valueOf(remoteMessage.toIntent().getStringExtra("date"));


        //Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getTitle() + ":\n" + remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show();
    }

    boolean isBackground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return !componentInfo.getPackageName().equals("com.example.sch");
    }

    @Override
    public void onDeletedMessages() {
        log("onDeletedMessages");
    }
}
