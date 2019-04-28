package com.example.sch;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewUtils;
import android.text.Html;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.HARDWARE_PROPERTIES_SERVICE;
import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    View view;
    private String COOKIE, ROUTE;
    private int PERSON_ID;
    int threadId = 0;
    String threadName = "";
    private String[] msg, time;
    private int[] user_ids;
    private Attach[][] files;
    private Handler h;
    private boolean uploading = false;
    private int last_msg;
    ScrollView scroll;
    private ArrayList<Uri> attach;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        PERSON_ID = TheSingleton.getInstance().getPERSON_ID();

        View view = inflater.inflate(R.layout.chat, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(threadName);
        setHasOptionsMenu(true);
        // todo toolbar subtitle
        // toolbar.setSubtitle("subtitle");

        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        // Inflate the layout for this fragment``
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        this.view = view;
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        menu.add(0, 1, 0, "quit");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                ((MainActivity) getActivity()).quit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        log("onAttach");
        super.onAttach(context);
    }

    void newMessage(String text, long time, int sender_id, int thread_id, String sender_fio) {
        log("new message in ChatFragment");
        if(thread_id != this.threadId) {
            log("wrong thread, sorry");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "1");
            builder.setContentTitle(sender_fio)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.attach);
            Notification notif = builder.build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //  todo early versions
                getActivity().getSystemService(NotificationManager.class).notify(1, notif);
            }
        }
        final LinearLayout container = view.findViewById(R.id.container);
        LayoutInflater inflater = getLayoutInflater();
        View item;
        LinearLayout.LayoutParams params;
        TextView tv, tv_attach;
            item = inflater.inflate(R.layout.chat_item, container, false);
            tv = item.findViewById(R.id.tv_text);
            if(Html.fromHtml(text).toString().equals("")) {
                ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params1.setMargins(0,0,0,0);
                tv.setLayoutParams(params1);
            }
            tv.setText(Html.fromHtml(text));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setTextColor(Color.WHITE);
            tv.setMaxWidth(view.getMeasuredWidth()-300);
            loge("long:" + time + ", int: " + sender_id);
            Date date = new Date(time);

            tv = item.findViewById(R.id.tv_time);
            tv.setText(date.getHours() + ":" + date.getMinutes());
            //tv.setText(time);
            item.setPadding(0, 16, 4, 0);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            if(PERSON_ID != sender_id) {
                ConstraintLayout l = item.findViewById(R.id.item_main);
                l.setBackground(getResources().getDrawable(R.drawable.chat_border_left));
                params.gravity = Gravity.START;
            }
            params.topMargin = 20;
            params.bottomMargin = 20;
            container.addView(item);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        scroll = view.findViewById(R.id.scroll);
        ViewTreeObserver.OnScrollChangedListener listener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            if (scroll != null) {
                if (scroll.getScrollY() == 0 && !uploading) {
                    log("top!!");
                    uploading = true;
                    final Handler h = new Handler() {
                        @Override
                        public void handleMessage(Message yoyyoyoy) {
                            final LinearLayout container = view.findViewById(R.id.container);
                            LayoutInflater inflater = getLayoutInflater();
                            View item;
                            LinearLayout.LayoutParams params;
                            TextView tv, tv_attach;
                            for (int i = 0; i < /*(first_time ? msg : )*/msg.length; i++) {
                                item = inflater.inflate(R.layout.chat_item, container, false);
                                tv = item.findViewById(R.id.tv_text);
                                if(Html.fromHtml(msg[i]).toString().equals("")) {
                                    ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                                    params1.setMargins(0,0,0,0);
                                    tv.setLayoutParams(params1);
                                }
                                tv.setText(Html.fromHtml(msg[i]));
                                tv.setMovementMethod(LinkMovementMethod.getInstance());
                                tv.setTextColor(Color.WHITE);
                                tv.setMaxWidth(view.getMeasuredWidth()-300);
                                tv = item.findViewById(R.id.tv_time);
                                tv.setText(time[i]);
                                item.setPadding(0, 16, 4, 0);
                                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.gravity = Gravity.END;
                                if(PERSON_ID != user_ids[i]) {
                                    ConstraintLayout l = item.findViewById(R.id.item_main);
                                    l.setBackground(getResources().getDrawable(R.drawable.chat_border_left));
                                    params.gravity = Gravity.START;
                                }
                                params.topMargin = 20;
                                params.bottomMargin = 20;
                                if(files[i] != null) {
                                    for (final Attach a: files[i]) {
                                        tv_attach = new TextView(getContext());
                                        tv_attach.setText(a.name + " (" + a.size + " B)");
                                        tv_attach.setTextColor(getResources().getColor(R.color.two));
                                        tv_attach.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String url = "https://app.eschool.center/ec-server/files/" + a.fileId;
                                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                                request.setDescription("Some description");
                                                request.setTitle(a.name);
                                                request.addRequestHeader("Cookie", COOKIE + "; site_ver=app; route=" + ROUTE + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                                                request.allowScanningByMediaScanner();
                                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, a.name);

                                                // get download service and enqueue file
                                                DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                                manager.enqueue(request);
                                            }
                                        });
                                        tv_attach.setMaxWidth(view.getMeasuredWidth()-300);
                                        ((LinearLayout)item.findViewById(R.id.attach)).addView(tv_attach);
                                    }
                                }
                                container.addView(item, 0, params);
                            }
                            scroll.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(container.getChildCount() >= 25)
                                        scroll.scrollTo(0, container.getChildAt(25).getTop());
                                }
                            });
                            uploading = false;
                        }
                    };
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("https://app.eschool.center/ec-server/chat/messages?getNew=false&" +
                                        "isSearch=false&rowStart=1&rowsCount=25&threadId=" + threadId + "&msgStart=" + last_msg);
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                con.setRequestProperty("Cookie", COOKIE + "; site_ver=app; route=" + ROUTE + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");// "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.13.1554062260.1554051192.");
                                StringBuilder result = new StringBuilder();

                                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

                                String line;
                                while ((line = rd.readLine()) != null) {
                                    result.append(line);
                                }
                                rd.close();

                                JSONArray array = new JSONArray(result.toString());

                                msg = new String[array.length()];
                                time = new String[array.length()];
                                user_ids = new int[array.length()];
                                files = new Attach[array.length()][];
                                JSONObject tmp, tmp1;
                                Date date;
                                for (int i = 0; i < array.length(); i++) {
                                    tmp = array.getJSONObject(i);
                                    date = new Date(tmp.getLong("sendDate"));
                                    time[i] = String.format(Locale.UK, "%02d:%02d", date.getHours(), date.getMinutes());
                                    if(tmp.getInt("attachCount") <= 0) {
                                        files[i] = null;
                                    } else {
                                        files[i] = new Attach[tmp.getInt("attachCount")];
                                        for (int j = 0; j < files[i].length; j++) {
                                            tmp1 = tmp.getJSONArray("attachInfo").getJSONObject(j);
                                            files[i][j] = new Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                                    tmp1.getString("fileName"), tmp1.getString("fileType"));
                                        }
                                    }
                                    user_ids[i] = tmp.getInt("senderId");
                                    if(i == array.length() - 1) {
                                        last_msg = tmp.getInt("msgNum");
                                    }
                                    if(!tmp.has("msg")) {
                                        // todo
                                        loge(tmp.toString());
                                        msg[i] = "";
                                        continue;
                                    }
                                    msg[i] = tmp.getString("msg");
                                }
                                h.sendEmptyMessage(0);
                            } catch (Exception e) {
                                loge(e.toString());
                            }
                        }
                    }.start();
                }
            }
        }
    };
        scroll.getViewTreeObserver().addOnScrollChangedListener(listener);
        h = new Handler() {
            @Override
            public void handleMessage(Message yoy) {
                final LinearLayout container = view.findViewById(R.id.container);
                LayoutInflater inflater = getLayoutInflater();
                View item;
                LinearLayout.LayoutParams params;
                TextView tv, tv_attach;
                for (int i = msg.length-1; i >= 0; i--) {
                    item = inflater.inflate(R.layout.chat_item, container, false);
                    tv = item.findViewById(R.id.tv_text);
                    if(Html.fromHtml(msg[i]).toString().equals("")) {
                        ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                        params1.setMargins(0,0,0,0);
                        tv.setLayoutParams(params1);
                    }
                    tv.setText(Html.fromHtml(msg[i]));
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setTextColor(Color.WHITE);
                    tv.setMaxWidth(view.getMeasuredWidth()-300);
                    tv = item.findViewById(R.id.tv_time);
                    tv.setText(time[i]);
                    item.setPadding(0, 16, 4, 0);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.END;
                    if(PERSON_ID != user_ids[i]) {
                        ConstraintLayout l = item.findViewById(R.id.item_main);
                        l.setBackground(getResources().getDrawable(R.drawable.chat_border_left));
                        params.gravity = Gravity.START;
                    }
                    params.topMargin = 20;
                    params.bottomMargin = 20;
                    if(files[i] != null) {
                        for (final Attach a: files[i]) {
                            tv_attach = new TextView(getContext());
                            tv_attach.setText(a.name + " (" + a.size + " B)");
                            tv_attach.setTextColor(getResources().getColor(R.color.two));
                            tv_attach.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = "https://app.eschool.center/ec-server/files/" + a.fileId;
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                    request.setDescription("Some description");
                                    request.setTitle(a.name);
                                    request.addRequestHeader("Cookie", COOKIE + "; site_ver=app; route=" + ROUTE + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, a.name);

                                    // get download service and enqueue file
                                    DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                }
                            });
                            tv_attach.setMaxWidth(view.getMeasuredWidth()-300);
                            ((LinearLayout)item.findViewById(R.id.attach)).addView(tv_attach);
                        }
                    }
                    container.addView(item, params);
                }
                view.findViewById(R.id.scroll_container).setBackgroundColor(getResources().getColor(R.color.six));

                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                final EditText et = view.findViewById(R.id.et);
                final Handler hand = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        String text = (String) msg.obj;
                        View item = getLayoutInflater().inflate(R.layout.chat_item, container, false);
                        TextView tv = item.findViewById(R.id.tv_text);
                        if(Html.fromHtml(text).toString().equals("")) {
                            ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                            params1.setMargins(0,0,0,0);
                            tv.setLayoutParams(params1);
                        }
                        tv.setText(Html.fromHtml(text));
                        tv.setTextColor(Color.WHITE);
                        tv.setMaxWidth(view.getMeasuredWidth()-300);
                        tv = item.findViewById(R.id.tv_time);
                        tv.setText(String.format(Locale.UK, "%02d:%02d", new Date().getHours(), new Date().getMinutes()));
                        item.setPadding(0, 16, 4, 0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.END;
                        params.topMargin = 20;
                        params.bottomMargin = 20;
                        container.addView(item, params);
                        scroll.post(new Runnable() {
                            @Override
                            public void run() {
                                scroll.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                };
                scroll = view.findViewById(R.id.scroll);

                view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String text = et.getText().toString();
                        final ArrayList<Uri> files = attach;
                        attach = null;
                        et.setText("");
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    hand.sendMessage(hand.obtainMessage(0, text));
                                    HttpURLConnection con = (HttpURLConnection) new URL("https://app.eschool.center/ec-server/chat/sendNew").openConnection();
                                    con.setRequestMethod("POST");
                                    con.setRequestProperty("Cookie", COOKIE + "; site_ver=app; route=" + ROUTE + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");// "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.13.1554062260.1554051192.");
                                    con.setDoOutput(true);
                                    String msg = "threadId=" + threadId + "&msgText=" + text +
                                            "&msgUID=" + System.currentTimeMillis();
                                    if(files != null) {
                                        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary1dwAnfncTuhzihRd");
                                        InputStream is = getActivity().getContentResolver().openInputStream(files.get(0));
                                        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                                        int bufferSize = 1024;
                                        byte[] buffer = new byte[bufferSize];

                                        int len;
                                        while ((len = is.read(buffer)) != -1) {
                                            byteBuffer.write(buffer, 0, len);
                                        }
                                        msg += "&file=";

                                        DataOutputStream request = new DataOutputStream(con.getOutputStream());

                                        /*request.writeBytes("--*****\r\n");
                                        request.writeBytes("Content-Disposition: form-data; name=\"" +
                                                "file" + "\";filename=\"" +
                                                "file.png" + "\"\r\n");
                                        request.writeBytes("\r\n");*/
                                        request.writeBytes(msg);
                                        request.write(byteBuffer.toByteArray());
                                        /*request.writeBytes("\r\n");
                                        request.writeBytes("--*****\r\n");*/
                                        request.flush();
                                        request.close();
                                        /*OutputStream os = con.getOutputStream();
                                        os.write(msg.getBytes());
                                        os.write(byteBuffer.toByteArray());*/
                                    } else {
                                        con.getOutputStream().write(msg.getBytes());
                                    }
                                    log(msg);
                                    //con.connect();
                                    log(con.getResponseMessage());
                                } catch (Exception e) {
                                    loge(e.toString());
                                }
                            }
                        }.start();
                    }
                });
                view.findViewById(R.id.btn_file).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                        // Filter to only show results that can be "opened", such as a
                        // file (as opposed to a list of contacts or timezones)
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        // Filter to show only images, using the image MIME data type.
                        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                        // To search for all documents available via installed storage providers,
                        // it would be "*/*".
                        intent.setType("*/*");

                        startActivityForResult(intent, 42);
                    }
                });
            }
        };
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&rowStart=1&rowsCount=25&threadId=" + threadId);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    StringBuilder result = new StringBuilder();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();

                    JSONArray array = new JSONArray(result.toString());
                    JSONObject tmp, tmp1;
                    Date date;
                    msg = new String[array.length()];
                    time = new String[array.length()];
                    user_ids = new int[array.length()];
                    files = new Attach[array.length()][];
                    for (int i = 0; i < array.length(); i++) {
                        tmp = array.getJSONObject(i);
                        date = new Date(tmp.getLong("sendDate"));
                        time[i] = String.format(Locale.UK, "%02d:%02d", date.getHours(), date.getMinutes());
                        if(tmp.getInt("attachCount") <= 0) {
                            files[i] = null;
                        } else {
                            files[i] = new Attach[tmp.getInt("attachCount")];
                            for (int j = 0; j < files[i].length; j++) {
                                tmp1 = tmp.getJSONArray("attachInfo").getJSONObject(j);
                                files[i][j] = new Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                        tmp1.getString("fileName"), tmp1.getString("fileType"));
                            }
                        }
                        user_ids[i] = tmp.getInt("senderId");
                        if(i == array.length() - 1) {
                            last_msg = tmp.getInt("msgNum");
                        }
                        if(!tmp.has("msg")) {
                            // todo
                            loge(tmp.toString());
                            msg[i] = "";
                            continue;
                        }
                        msg[i] = tmp.getString("msg");
                    }
                    h.sendEmptyMessage(1);
                } catch (Exception e) {loge(e.toString());}
            }
        }.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("result");

        if(attach == null)
            attach = new ArrayList<>();
        attach.add(data.getData());
    }

    @Override
    public void onDetach() {
        scroll = null;
        super.onDetach();
    }

    private class Attach {
        int fileId, size;
        String name, type;

        Attach(int fileId, int size, String name, String type) {
            this.fileId = fileId;
            this.size = size;
            this.name = name;
            this.type = type;
        }
    }
}
