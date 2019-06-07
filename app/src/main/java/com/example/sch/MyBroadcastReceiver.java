package com.example.sch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.sch.LoginActivity.connect;
import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        log("action read, extra: " + intent.getIntExtra("threadId", -1));
        ArrayList<TheSingleton.Notification> notifications = new ArrayList<>(TheSingleton.getInstance().getNotifications());
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        int count = 0;
        TheSingleton.Notification n;
        StringBuilder s = new StringBuilder("notifications: [");
        for (int i = 0; i < notifications.size(); i++) {
            n = notifications.get(i);
            s.append("(").append(n.threadId).append("; ").append(n.notificationId).append("), ");
            if(n.threadId == intent.getIntExtra("threadId", -1)) {
                manager.cancel(notifications.get(i).notificationId);
                TheSingleton.getInstance().getNotifications().remove(i - count);
                count++;
            }
        }
        if(TheSingleton.getInstance().getNotifications().size() == 0) {
            manager.cancel(0);
            TheSingleton.getInstance().summary = null;
        }
        s.delete(s.length()-2, s.length());
        s.append("]");
        log(s.toString());
        log("deleted " + count + " notification" + (count != 1?"s":""));
        new Thread() {
            @Override
            public void run() {
                try {
                    connect("https://app.eschool.center/ec-server/chat/readAll?threadId=" + intent.getIntExtra("threadId", -1),
                            null, context);
                } catch (IOException e) {
                    loge(e.toString());
                }
            }
        }.start();
    }
}
