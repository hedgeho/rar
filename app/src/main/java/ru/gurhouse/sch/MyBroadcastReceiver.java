package ru.gurhouse.sch;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.LoginActivity.login;

public class MyBroadcastReceiver extends BroadcastReceiver {

    final static int ACTION_READ = 0;
    final static int ACTION_DOWNLOAD = 1;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        switch(intent.getIntExtra("action", -1)) {
            case ACTION_READ:
                log("action read, extra: " + intent.getIntExtra("threadId", -1));

                ArrayList<TheSingleton.Notification> notifications = new ArrayList<>(TheSingleton.getInstance().getNotifications());
                NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                int count = 0;
                TheSingleton.Notification n;
                StringBuilder s = new StringBuilder("notifications: [");
                for (int i = 0; i < notifications.size(); i++) {
                    n = notifications.get(i);
                    if(n == null)
                        continue;
                    s.append("(").append(n.threadId).append("; ").append(n.notificationId).append("), ");
                    if (n.threadId == intent.getIntExtra("threadId", -1)) {
                        manager.cancel(notifications.get(i).notificationId);
                        TheSingleton.getInstance().getNotifications().remove(i - count);
                        count++;
                    }
                }
                if (TheSingleton.getInstance().getNotifications().size() == 0) {
                    manager.cancel(0);
                    TheSingleton.getInstance().summary = null;
                }
                s.delete(s.length() - 2, s.length());
                s.append("]");
                log("not: " + s.toString());
                log("deleted " + count + " notification" + (count != 1 ? "s" : ""));

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            connect("https://app.eschool.center/ec-server/chat/readAll?threadId=" + intent.getIntExtra("threadId", -1),
                                    null, context);
                        } catch (LoginActivity.NoInternetException e) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> Toast.makeText(context,
                                            "Нет доступа к интернету", Toast.LENGTH_SHORT).show());
                        } catch (IOException e) {
                            loge(e);
                        }
                    }
                }.start();
                break;
            case ACTION_DOWNLOAD:
                try {
                    JSONArray array = new JSONArray(intent.getStringExtra("data"));
                    log("download: " + intent.getStringExtra("data"));
                    JSONObject obj;
                    String name, url;
                    int permissionCheck = ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE");
                    log("perm check: " + permissionCheck);
                    if (permissionCheck >= 0) {
                        SharedPreferences pref = context.getSharedPreferences("pref", 0);
                        if(System.currentTimeMillis() - pref.getLong("cookie_time", 0) > 60*60*1000) {
                            login(context);
                        }
                        for (int i = 0; i < array.length(); i++) {
                            obj = array.getJSONObject(i);
                            name = obj.getString("fileName");
                            url = "https://app.eschool.center/ec-server/files/" + obj.getInt("fileId");
                            log("saving " + name);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            try {
                                request.setDescription("Downloading file from " + new URL(url).getHost());
                            } catch (MalformedURLException e) {
                                loge(e);
                                request.setDescription("Some Description");
                            }
                            request.setTitle(name);
                            request.addRequestHeader("Cookie", pref.getString("cookie", "") + "; site_ver=app;" +
                                    " route=" + pref.getString("route", "") + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);

                            // get download service and enqueue file
                            DownloadManager dmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                            dmanager.enqueue(request);
                        }
                    }

                } catch (Exception e) {
                    loge(e);
                }
        }
    }
}
