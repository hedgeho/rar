package ru.gurhouse.sch;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static android.view.View.GONE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;
import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;

public class ChatFragment extends Fragment {

    private View view;
    private int PERSON_ID;
    private LayoutInflater inflater;
    private LinearLayout container;

    private Msg[] messages;
    private Handler h;
    private boolean uploading = false;
    private int last_msg = -1;
    private List<Integer> first_msgs;
    private ScrollView scroll;
    private List<File> attach = new ArrayList<>();
    private boolean scrolled = false, first_time = true;
    private MenuItem itemToEnable = null;

    Activity context;
    int threadId = 0;
    int type = 0;
    String threadName = "";
    int searchMsgId = -1;
    boolean group = false;
    String topic = "";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null) {
            context = getActivity();

            getActivity().findViewById(R.id.btn_file).setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 43);
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        PERSON_ID = TheSingleton.getInstance().getPERSON_ID();

        this.inflater = inflater;

        first_msgs = new ArrayList<>();
        View view = inflater.inflate(R.layout.chat, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(threadName);
        if(!topic.equals("") && group && !topic.equals(threadName))
            toolbar.setSubtitle(topic);
        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        // Inflate the layout for this fragment``
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        if(type == 0) {
            view.findViewById(R.id.sender).setVisibility(GONE);
        }
        this.container = view.findViewById(R.id.main_container);
        this.view = view;
        return view;
    }

    File pinned = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String path = ImageFilePath.getPath(context,data.getData());
            if(data.getData() != null && path != null) {
                pinned = new File(path);
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 124);
            }
        } else {
            String path = ImageFilePath.getPath(context,data.getData());
            if(data.getData() != null && path != null) attach(new File(path));
        }
    }

    public static String getMimeType(String url) {
        String type = "";
        if (url.lastIndexOf(".") != -1) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(url.substring(url.lastIndexOf(".")+1));
        }
        return type;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 124) {
            attach(pinned);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getContext() != null) {
            menu.clear();
            MenuItem ref = menu.add(0, 3, 0, "Обновить");
            ref.setIcon(getResources().getDrawable(R.drawable.refresh));
            ref.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(0, 1, 0,
                    getContext().getSharedPreferences("pref", 0).getString("muted", "[]")
                            .contains("" + threadId)?"Включить уведомления":"Отключить уведомления")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            if(group)
                menu.add(0, 2, 1, "Покинуть беседу");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1 && getContext() != null) {
            SharedPreferences pref = getContext().getSharedPreferences("pref", 0);
            if(item.getTitle().equals("Отключить уведомления")) {
                try {
                    JSONArray array = new JSONArray(pref.getString("muted", "[]"));
                    array.put(threadId);
                    pref.edit().putString("muted", array.toString()).apply();
                } catch (Exception e) {e.printStackTrace();}
                item.setTitle("Включить уведомления");
            } else {
                try {
                    JSONArray array = new JSONArray(pref.getString("muted", "[]")), a = new JSONArray();
                    for (int i = 0; i < array.length(); i++) {
                        if(!(array.getInt(i) == threadId)) {
                            a.put(array.getInt(i));
                        }
                    }
                    pref.edit().putString("muted", a.toString()).apply();
                    item.setTitle("Отключить уведомления");
                } catch (Exception e) {e.printStackTrace();}
            }
        } else if(item.getItemId() == 2) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        connect("https://app.eschool.center/ec-server/chat/leave?threadId=" + threadId, null, getContext());
                    } catch (LoginActivity.NoInternetException e) {
                        getContext().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Нет интернета", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {loge(e);}
                }
            }.start();
            getContext().onBackPressed();
        } else if (item.getItemId() == 3) {
            log("refreshing chat");
            item.setEnabled(false);
            itemToEnable = item;
            new Thread() {
                @Override
                public void run() {
                    try {
                        download(h);
                    } catch (LoginActivity.NoInternetException e) {
                        getContext().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Нет доступа к интернету", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        return super.onOptionsItemSelected(item);
    }

    void newMessage(String text, Date time, int sender_id, int thread_id, String sender_fio, List<Attach> attach, boolean toBottom) {
        log("new message in ChatFragment");
        log("notif thread: " + thread_id + ", this thread id: " + this.threadId);
        if(thread_id != this.threadId && sender_id != thread_id) {
            log("wrong thread, sorry");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1");
            builder.setContentTitle(sender_fio)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.alternative);
            Notification notif = builder.build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify(TheSingleton.getInstance().notification_id++, notif);
            return;
        }
        final LinearLayout container = view.findViewById(R.id.main_container);
        View item;
        TextView tv, tv_attach;
        if(PERSON_ID == sender_id) {
            item = inflater.inflate(R.layout.chat_item, container, false);
        } else {
            item = inflater.inflate(R.layout.chat_item_left, container, false);
        }
        tv = item.findViewById(R.id.chat_tv_sender);
        if(!group && tv != null) {
            tv.setVisibility(GONE);
        } else if (tv != null){
            tv.setText(sender_fio);
            tv.setVisibility(View.VISIBLE);
        }

        if(attach != null && attach.size() == 1 && attach.get(0).type.contains("image") && text.isEmpty()){
            LinearLayout layout = item.findViewById(R.id.layout);
            layout.setBackground(null);
            layout.setPadding(0,0,0,0);
        }
        tv = item.findViewById(R.id.tv_text);
        if(text.isEmpty() && (attach == null || attach.isEmpty())) {
            tv.setText(" ");
        }else if(attach != null && !attach.isEmpty() && text.isEmpty()){
            tv.setVisibility(GONE);
        }else {
            tv.setText(Html.fromHtml(text.replace("\n","<br>")));
        }
        for (int i = 0; attach != null && i < attach.size(); i++) {
            if(attach.get(i) == null) continue;
            if(attach.get(i).type.contains("image")){
                ImageView image = new ImageView(context);
                image.setPadding(5,5,5,5);

                final int i2 = i;
                (new Thread(() -> {
                    try {
                        URL obj = new URL("https://app.eschool.center/ec-server/files/" + attach.get(i2).fileId);
                        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                        connection.setRequestProperty("Cookie", TheSingleton.getInstance().getCOOKIE() + "; site_ver=app; route=" + TheSingleton.getInstance().getROUTE() + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                        connection.setRequestMethod("GET");

                        Bitmap bitmap2 = BitmapFactory.decodeStream(connection.getInputStream());
                        Bitmap bitmap;
                        if(bitmap2.getWidth() > bitmap2.getHeight()) {
                            bitmap = Bitmap.createScaledBitmap(bitmap2, 720, 720 * bitmap2.getHeight() / bitmap2.getWidth(), false);
                        }else {
                            bitmap = Bitmap.createScaledBitmap(bitmap2, 720 * bitmap2.getWidth() / bitmap2.getHeight(), 720, false);
                        }
                        RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                        roundedBitmap.setCornerRadius(30);
                        getActivity().runOnUiThread(() -> {
                            image.setImageDrawable(roundedBitmap);
                        });
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }
                })).start();

                image.setOnClickListener(v -> {
                    String url = "https://app.eschool.center/ec-server/files/" + attach.get(i2).fileId;
                    ((MainActivity) getActivity()).saveFile(url, attach.get(i2).name, true);
                    Toast toast = Toast.makeText(context,
                            attach.get(i2).name+" загружается...",
                            Toast.LENGTH_LONG);
                    toast.show();
                });
                ((LinearLayout) item.findViewById(R.id.attach)).addView(image,0);
            }else {
                final int i2 = i;
                tv_attach = new TextView(getContext());
                float size = attach.get(i).size;
                String s = "B";
                if (size > 900) {
                    s = "KB";
                    size /= 1024;
                }
                if (size > 900) {
                    s = "MB";
                    size /= 1024;
                }
                tv_attach.setText(String.format(Locale.getDefault(), attach.get(i2).name + " (%.2f " + s + ")", size));
                tv_attach.setTextColor(getResources().getColor(R.color.two));
                tv_attach.setMaxWidth(tv.getMaxWidth());
                tv_attach.setOnClickListener(v -> {
                    String url = "https://app.eschool.center/ec-server/files/" + attach.get(i2).fileId;
                    ((MainActivity) getActivity()).saveFile(url, attach.get(i2).name, true);
                    Toast toast = Toast.makeText(context,
                            attach.get(i2).name+" загружается...",
                            Toast.LENGTH_LONG);
                    toast.show();
                });

                tv_attach.setPadding(15,15,15,15);
                ((LinearLayout) item.findViewById(R.id.attach)).addView(tv_attach);
            }
            //tv_attach.setText(String.format(Locale.getDefault(), a.getString("fileName") + " (%.2f " + s + ")", size));
            //tv_attach.setTextColor(getResources().getColor(R.color.two));
            //tv_attach.setOnClickListener(v -> {
            //    try {
            //        String url = "https://app.eschool.center/ec-server/files/" + a.getInt("fileId");
            //        ((MainActivity) getContext()).saveFile(url, a.getString("fileName"), true);
            //    } catch (JSONException e) {e.printStackTrace();}
            //});
            //((LinearLayout) item.findViewById(R.id.attach)).addView(tv_attach);
        }

        tv = item.findViewById(R.id.tv_time);
        tv.setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(time));
        log("person_id: " + PERSON_ID + ", sender: " + sender_id);

        if(toBottom) container.addView(item);
        else container.addView(item,0);
        if(scroll != null)
            scroll.post(() -> scroll.scrollTo(0, scroll.getChildAt(0).getBottom()));
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        if(first_time) {
            scroll = view.findViewById(R.id.scroll);
            view.findViewById(R.id.btn_refresh).setOnClickListener((v)->{
                view.findViewById(R.id.tv_error).setVisibility(View.INVISIBLE);
                v.setVisibility(View.INVISIBLE);
                new Thread(() -> {
                    try {
                        download(h);
                    } catch (LoginActivity.NoInternetException e) {
                        getContext().runOnUiThread(() -> {
                            v.setVisibility(View.VISIBLE);
                            view.findViewById(R.id.tv_error).setVisibility(View.VISIBLE);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }}).start();
            });

            ViewTreeObserver.OnScrollChangedListener listener = () -> {
                if (scroll != null) {
                    if (!scrolled)
                        scrolled = true;
                    else {
                        LinearLayout l = scroll.findViewById(R.id.main_container);
                        if (l.findViewWithTag("result") != null && scrolled)
                            if (l.findViewWithTag("result").getTag(R.id.TAG_POSITION).equals("left"))
                                l.findViewWithTag("result").setBackground(getResources().getDrawable(R.drawable.chat_border_left));
                            else
                                l.findViewWithTag("result").setBackground(getResources().getDrawable(R.drawable.chat_border));
                    }
                    if (scroll.getScrollY() == 0 && !uploading && last_msg != 0) {
                        log("top!!");
                        uploading = true;
                        final Handler h = new Handler() {
                            @Override
                            public void handleMessage(Message yoyyoyoy) {
                                final LinearLayout container = view.findViewById(R.id.main_container);
//                                View item;
//                                TextView tv, tv_attach;
//                                Calendar cal = getInstance(), cal1 = getInstance();
//                                int i = 0;
                                for (Msg msg : messages) {
                                    newMessage(msg.text,msg.time,msg.user_id,msg.user_id,msg.sender, msg.files,false);
//                                    if(PERSON_ID == msg.user_id) {
////                                        item = inflater.inflate(R.layout.chat_item, container, false);
////                                    } else {
////                                        item = inflater.inflate(R.layout.chat_item_left, container, false);
////                                    }
////                                    tv = item.findViewById(R.id.chat_tv_sender);
////                                    if(!group && tv != null) {
////                                        tv.setVisibility(GONE);
////                                    } else if(tv != null) {
////                                        tv.setText(msg.sender);
////                                        tv.setVisibility(View.VISIBLE);
////                                    }
////                                    tv = item.findViewById(R.id.tv_text);
////                                    if (msg.text.isEmpty() && (msg.files == null || msg.files.isEmpty())) {
////                                        tv.setText("          ");
////                                    }else if(msg.files != null && !msg.files.isEmpty() && msg.text.isEmpty()){
////                                        tv.setVisibility(GONE);
////                                    }else
////                                        tv.setText(Html.fromHtml(msg.text));
////                                    tv = item.findViewById(R.id.tv_time);
////
////                                    tv.setText(String.format(Locale.UK, "%02d:%02d", msg.time.getHours(), msg.time.getMinutes()));
////                                    if (msg.files != null) {
////                                        for (final Attach a : msg.files) {
////                                            if (a.type.toLowerCase().contains("image")) {
////                                                ImageView image = new ImageView(context);
////                                                image.setPadding(15, 15, 15, 15);
////
////                                                (new Thread(() -> {
////                                                    try {
////                                                        URL obj = new URL("https://app.eschool.center/ec-server/files/" + a.fileId);
////                                                        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
////                                                        connection.setRequestProperty("Cookie", TheSingleton.getInstance().getCOOKIE() + "; site_ver=app; route=" + TheSingleton.getInstance().getROUTE() + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
////                                                        connection.setRequestMethod("GET");
////
////                                                        Bitmap bitmap2 = BitmapFactory.decodeStream(connection.getInputStream());
////                                                        Bitmap bitmap;
////                                                        if (bitmap2.getWidth() > bitmap2.getHeight())
////                                                            bitmap = Bitmap.createScaledBitmap(bitmap2, 720, 720 * bitmap2.getHeight() / bitmap2.getWidth(), false);
////                                                        else
////                                                            bitmap = Bitmap.createScaledBitmap(bitmap2, 720 * bitmap2.getWidth() / bitmap2.getHeight(), 720, false);
////                                                        getActivity().runOnUiThread(() -> {
////                                                            image.setImageBitmap(bitmap);
////                                                        });
////                                                    } catch (IOException | NullPointerException e) {
////                                                        e.printStackTrace();
////                                                    }
////                                                })).start();
////
////                                                image.setOnClickListener(v -> {
////                                                    String url = "https://app.eschool.center/ec-server/files/" + a.fileId;
////                                                    ((MainActivity) getActivity()).saveFile(url, a.name, true);
////                                                    Toast toast = Toast.makeText(context,
////                                                    a.name+" загружается...",
////                                                    Toast.LENGTH_LONG);
////                                                    toast.show();
////                                                });
////                                                ((LinearLayout) item.findViewById(R.id.attach)).addView(image, 0);
////                                            } else {
////                                                tv_attach = new TextView(getContext());
////                                                float size = a.size;
////                                                String s = "B";
////                                                if (size > 900) {
////                                                    s = "KB";
////                                                    size /= 1024;
////                                                }
////                                                if (size > 900) {
////                                                    s = "MB";
////                                                    size /= 1024;
////                                                }
////                                                tv_attach.setText(String.format(Locale.getDefault(), a.name + " (%.2f " + s + ")", size));
////                                                tv_attach.setTextColor(getResources().getColor(R.color.two));
////                                                tv_attach.setOnClickListener(v -> {
////                                                    String url = "https://app.eschool.center/ec-server/files/" + a.fileId;
////                                                    ((MainActivity) getActivity()).saveFile(url, a.name, true);
////                                                    Toast toast = Toast.makeText(context,
////                                                    a.name+" загружается...",
////                                                    Toast.LENGTH_LONG);
////                                                    toast.show();
////                                                });
////                                                tv_attach.setPadding(15, 15, 15, 15);
////                                                ((LinearLayout) item.findViewById(R.id.attach)).addView(tv_attach);
////                                            }
////
////                                            //tv_attach.setText(String.format(Locale.getDefault(), a.name + " (%.2f " + s + ")", size));
////                                            //tv_attach.setTextColor(getResources().getColor(R.color.two));
////                                            //tv_attach.setOnClickListener(v -> {
////                                            //    String url = "https://app.eschool.center/ec-server/files/" + a.fileId;
////                                            //    ((MainActivity) getContext()).saveFile(url, a.name, true);
////                                            //});
////                                            //((LinearLayout) item.findViewById(R.id.attach)).addView(tv_attach);
////                                        }
////                                    } else
////                                        item.findViewById(R.id.attach).setVisibility(GONE);
////                                    container.addView(item, 0);
////                                    i++;
                                }
                                if(scroll != null)
                                    scroll.post(() -> {
                                        if (container.getChildCount() >= 25 && messages.length > 0)
                                            scroll.scrollTo(0, container.getChildAt(messages.length - 1).getBottom());
                                    });
                                uploading = false;
                            }
                        };
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    JSONArray array;
                                    try {
                                        array = new JSONArray(connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&" +
                                                "isSearch=false&rowStart=1&rowsCount=25&threadId=" + threadId + "&msgStart=" + last_msg, null, getContext()));
                                    } catch (LoginActivity.NoInternetException e) {
                                        getContext().runOnUiThread(() -> Toast.makeText(getContext(), "Нет интернета", Toast.LENGTH_SHORT).show());
                                        return;
                                    }
                                    if(array.length() == 0) {
                                        last_msg = 0;
                                    }
                                    messages = new Msg[array.length()];
                                    for (int i = 0; i < messages.length; i++) {
                                        messages[i] = new Msg();
                                    }
                                    JSONObject tmp, tmp1;
                                    Msg msg;
                                    for (int i = 0; i < array.length(); i++) {
                                        msg = messages[i];
                                        tmp = array.getJSONObject(i);
                                        msg.time = new Date(tmp.getLong("sendDate"));
                                        if (tmp.getInt("attachCount") <= 0) {
                                            msg.files = null;
                                        } else {
                                            msg.files = new ArrayList<>();
                                            log(tmp.getString("attachInfo"));
                                            for (int j = 0; j < tmp.getInt("attachCount"); j++) {
                                                tmp1 = tmp.getJSONArray("attachInfo").getJSONObject(j);
                                                msg.files.add(new Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                                        tmp1.getString("fileName"), tmp1.getString("fileType")));
                                            }
                                        }
                                        msg.user_id = tmp.getInt("senderId");
                                        msg.msg_id = tmp.getInt("msgNum");
                                        msg.sender = tmp.getString("senderFio");
                                        if (i == array.length() - 1) {
                                            last_msg = tmp.getInt("msgNum");
                                        }
                                        if (!tmp.has("msg")) {
                                            loge("no msg tag: " + tmp.toString());
                                            msg.text = "";
                                            continue;
                                        }
                                        msg.text = tmp.getString("msg");
                                    }
                                    h.sendEmptyMessage(0);
                                } catch (Exception e) {
                                    loge("on scroll top: " + e.toString());
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                    else if (scroll.getChildAt(0).getBottom()
                            <= (scroll.getHeight() + scroll.getScrollY()) && !uploading) {
                        if (first_msgs.size() == 0) return;
                        log("bottom");
                        uploading = true;
                        final Handler h = new Handler() {
                            @Override
                            public void handleMessage(Message yoyoy) {
                                final LinearLayout container = view.findViewById(R.id.main_container);
                                View item;
                                TextView tv;
                                Calendar cal = getInstance(), cal1 = getInstance();
                                Msg msg;
                                for (int i = messages.length - 1; i >= 0; i--) {
                                    msg = messages[i];
                                    if(i != messages.length-1) {
                                        cal.setTime(msg.time);
                                        cal1.setTime(messages[i+1].time);
                                        if(cal1.get(Calendar.DAY_OF_MONTH) != cal.get(Calendar.DAY_OF_MONTH)) {
                                            item = inflater.inflate(R.layout.date_divider, container, false);
                                            tv = item.findViewById(R.id.tv_date);
                                            tv.setText(getDate(cal));
                                            container.addView(item);
                                        }
                                    }
                                    newMessage(msg.text,msg.time,msg.user_id,msg.user_id,msg.sender,msg.files, true);
                                }
                                uploading = false;
                                first_msgs.remove(first_msgs.size() - 1);
                            }
                        };
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    JSONArray array = new JSONArray(connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&" +
                                            "isSearch=false&rowStart=0&rowsCount=25&threadId=" + threadId + "&msgStart=" + (first_msgs.get(first_msgs.size() - 1) + 1),
                                            null, getContext()));

                                    messages = new Msg[array.length()];
                                    for (int i = 0; i < messages.length; i++) {
                                        messages[i] = new Msg();
                                    }
                                    JSONObject tmp, tmp1;
                                    Msg msg;
                                    for (int i = 0; i < array.length(); i++) {
                                        msg = messages[i];
                                        tmp = array.getJSONObject(i);
                                        msg.time = new Date(tmp.getLong("sendDate"));
                                        if (tmp.getInt("attachCount") <= 0) {
                                            msg.files = null;
                                        } else {
                                            msg.files = new ArrayList<>();
                                            for (int j = 0; j < tmp.getInt("attachCount"); j++) {
                                                tmp1 = tmp.getJSONArray("attachInfo").getJSONObject(j);
                                                msg.files.add(new Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                                        tmp1.getString("fileName"), tmp1.getString("fileType")));
                                            }
                                        }
                                        msg.user_id = tmp.getInt("senderId");
                                        msg.msg_id = tmp.getInt("msgNum");
                                        msg.sender = tmp.getString("senderFio");
                                        if (!tmp.has("msg")) {
                                            loge("no msg tag: " + tmp.toString());
                                            msg.text = "";
                                            continue;
                                        }
                                        msg.text = tmp.getString("msg");
                                    }
                                    h.sendEmptyMessage(0);
                                } catch (LoginActivity.NoInternetException e) {
                                    getContext().runOnUiThread(() -> Toast.makeText(getContext(), "Нет интернета", Toast.LENGTH_SHORT).show());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    loge("on scroll bottom: " + e.toString());
                                }
                            }
                        }.start();
                    }
                }
            };
            scroll.getViewTreeObserver().addOnScrollChangedListener(listener);
            h = new Handler() {
                @Override
                public void handleMessage(Message yoy) {
                    //final LinearLayout container = view.findViewById(R.id.main_container);
                    if(container == null)
                        container = view.findViewById(R.id.main_container);
                    container.removeAllViews();
                    View item;
                    TextView tv;
                    Calendar cal = Calendar.getInstance(), cal1 = Calendar.getInstance();
                    log(messages.length + "");
                    Msg msg;
                    for (int i = messages.length-1; i >= 0; i--) {
                        if(i >= messages.length)
                            continue;
                        msg = messages[i];
//                        if (msg.text.equals(""))
//                            continue;
                        if(i != messages.length-1) {
                            cal.setTime(msg.time);//tg
                            cal1.setTime(messages[i+1].time);
                            if(cal1.get(Calendar.DAY_OF_MONTH) != cal.get(Calendar.DAY_OF_MONTH)) {
                                item = inflater.inflate(R.layout.date_divider, container, false);
                                tv = item.findViewById(R.id.tv_date);
                                tv.setText(getDate(cal));
                                container.addView(item);
                            }
                        }
                        newMessage(msg.text,msg.time,msg.user_id,msg.user_id,msg.sender,msg.files, true);
                    }
                    view.findViewById(R.id.scroll_container).setBackgroundColor(getResources().getColor(R.color.six));

                    if(scroll == null)
                        scroll = view.findViewById(R.id.scroll);
                    if(scroll == null)
                        scroll = ChatFragment.this.view.findViewById(R.id.scroll);

                    scroll.post(() -> {
                        if(searchMsgId == -1)
                            scroll.fullScroll(ScrollView.FOCUS_DOWN);
                        else {
                            scroll.scrollTo(0, container.findViewWithTag("result").getTop());
                            container.findViewWithTag("result").setBackground(getResources().getDrawable(R.drawable.chat_border_highlited));
                            //scrolled = true;
                        }
                    });

                    final EditText et = view.findViewById(R.id.et);
                    scroll = view.findViewById(R.id.scroll);

                    view.findViewById(R.id.btn_send).setOnClickListener(v -> {
                        final String text = et.getText().toString();
                        //attach = null;
                        et.setText("");
                        et.requestFocus();
                        et.requestFocusFromTouch();
                        if(type == 1){
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Сообщение увидит только создатель диалога").setTitle("Вы уверены, что хотите отправить сообщение?").setPositiveButton("Отправить", (dialog, which)->ChatFragment.this.sendMessage(threadId, text, System.currentTimeMillis()))
                                    .setNegativeButton("Отмена", null).show();
                        } else
                            ChatFragment.this.sendMessage(threadId, text, System.currentTimeMillis());
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                try {
//                                    getActivity().runOnUiThread(() -> {
//                                        View item1 = inflater.inflate(R.layout.chat_item, container, false);
//                                        TextView tv1 = item1.findViewById(R.id.tv_text);
//                                        if (text.isEmpty() && attach.isEmpty()) {
//                                            tv1.setText("          ");
//                                        }else if(!attach.isEmpty()){
//                                            tv1.setVisibility(GONE);
//                                        }else
//                                            tv1.setText(Html.fromHtml(text));
//                                        tv1 = item1.findViewById(R.id.tv_time);
//                                        tv1.setText(String.format(Locale.UK, "%02d:%02d", new Date().getHours(), new Date().getMinutes()));
//                                        container.addView(item1);
//                                        scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
//                                    });
////                                    if(!files.isEmpty()) {
//                                        //uploadFile(new File(files.get(0).getPath()));
//
////                                    } else {
////                                        connect("https://app.eschool.center/ec-server/chat/sendNew",  "threadId=" + threadId + "&msgText=" + text +
////                                                "&msgUID=" + System.currentTimeMillis());
////                                    }
//                                } catch (Exception e) {
//                                    loge("rar: " + e.toString());
//                                    e.printStackTrace();
//                                }
//                            }
//                        }.start();
                    });
                    first_time = false;
                    if(itemToEnable != null)
                        itemToEnable.setEnabled(true);
                }
            };
            new Thread() {
                @Override
                public void run() {
                    try {
                        download(h);
                    } catch (LoginActivity.NoInternetException e) {
                        getContext().runOnUiThread(()->{
                            TextView tv = view.findViewById(R.id.tv_error);
                            tv.setText("Нет доступа к интернету");
                            tv.setVisibility(View.VISIBLE);
                            view.findViewById(R.id.btn_refresh).setVisibility(View.VISIBLE);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        loge("onViewCreated() run: " + e.toString());
                    }
                }
            }.start();
        }
    }

    @Override
    public void onDetach() {
        scroll = null;
        super.onDetach();
    }

    private void download(Handler h) throws IOException, JSONException, LoginActivity.NoInternetException {
        new Thread() {
            @Override
            public void run() {
                try {
                    connect("https://app.eschool.center/ec-server/chat/readAll?threadId=" + threadId, null, getContext());
                } catch (LoginActivity.NoInternetException ignore) {
                } catch (Exception e) {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        }.start();

        boolean found = false;
        if(searchMsgId == -1)
            found = true;
        JSONObject tmp, tmp1;
        last_msg = -1;
        do {
            JSONArray array = new JSONArray(connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&" +
                    "rowStart=1&rowsCount=25&threadId=" + threadId + (last_msg == -1?"":"&msgStart="+last_msg), null, getContext()));
            messages = new Msg[array.length()];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = new Msg();
            }
            Msg msg;
            for (int i = 0; i < array.length(); i++) {
                msg = messages[i];
                tmp = array.getJSONObject(i);
                msg.time = new Date(tmp.getLong("sendDate"));
                if (tmp.getInt("attachCount") <= 0) {
                    msg.files = null;
                } else {
                    msg.files = new ArrayList<>();
                    for (int j = 0; j < tmp.getInt("attachCount"); j++) {
                        tmp1 = tmp.getJSONArray("attachInfo").getJSONObject(j);
                        msg.files.add(new Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                tmp1.getString("fileName"), tmp1.getString("fileType")));
                    }
                }
                msg.user_id = tmp.getInt("senderId");
                msg.msg_id = tmp.getInt("msgNum");
                msg.sender = tmp.getString("senderFio");
                if (i == array.length() - 1)
                    last_msg = tmp.getInt("msgNum");
                else if (i == 0)
                    first_msgs.add(tmp.getInt("msgNum"));
                if(tmp.getInt("msgNum") == searchMsgId)
                    found = true;
                if (!tmp.has("msg")) {
                    log("no msg tag: " + tmp.toString());
                    msg.text = "";
                    continue;
                }
                msg.text = tmp.getString("msg");
            }
        } while (!found);
        if(first_msgs.size() > 0)
            first_msgs.remove(first_msgs.size()-1);
        if(h != null)
            h.sendEmptyMessage(1);
    }

    LinearLayout attachedLayout;
    HorizontalScrollView attachedScroll;

    private void attach(File f){
        try {
            String path = ImageFilePath.getPath(context, Uri.fromFile(f));
            if (path == null) return;
            if (attachedLayout == null || attachedScroll == null) {
                attachedLayout = getActivity().findViewById(R.id.attached);
                attachedScroll = getActivity().findViewById(R.id.attachedScroll);
            }
            attach.add(f);
            attachedScroll.setVisibility(View.VISIBLE);
            View view;
            Drawable delete = getResources().getDrawable(R.drawable.delete);
            if (getMimeType(f.getPath()) != null && getMimeType(f.getPath()).contains("image")) {


                Bitmap bitmap = Bitmap.createBitmap(250,250, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.parseColor("#797979"));
                canvas.drawBitmap(
                        Bitmap.createScaledBitmap(
                                BitmapFactory.decodeStream(new FileInputStream(f)), 250, 250, false
                        ), 0,0,null);

                RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                roundedBitmap.setCornerRadius(30);



                ImageView view2 = new ImageView(context);
                view2.setImageResource(R.drawable.delete);

                view2.setBackground(roundedBitmap);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 250);
                layoutParams.setMargins(10, 10, 10, 10);
                view2.setLayoutParams(layoutParams);
                //view2.setBackgroundResource(R.drawable.image_bubble);
                view2.setPadding(10, 10, 10, 10);

                view = view2;

            } else {
                TextView view2 = new TextView(context);
                view2.setText(f.getName());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 250);
                layoutParams.setMargins(10, 10, 10, 10);
                view2.setLayoutParams(layoutParams);
                view2.setPadding(10, 10, 10, 10);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view2.setForeground(delete);
                    view2.setBackgroundResource(R.drawable.image_bubble);
                }else{
                    Bitmap bitmap = Bitmap.createBitmap(250,250, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.parseColor("#797979"));
                    canvas.drawBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.delete
                                ), 250, 250, false
                            ), 0,0,null);

                    RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                    roundedBitmap.setCornerRadius(30);

                    view2.setBackground(roundedBitmap);
                }
                view = view2;
            }
            view.setOnClickListener((v) -> {
                detach(f, v);
            });
            attachedLayout.addView(view);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clearAttached(){
        if(attachedLayout == null || attachedScroll == null) {
            attachedLayout = getActivity().findViewById(R.id.attached);
            attachedScroll = getActivity().findViewById(R.id.attachedScroll);
        }
        attach.clear();
        attachedLayout.removeAllViews();
        attachedScroll.setVisibility(GONE);
    }

    private void detach(File f, View v){
        if(attachedLayout == null || attachedScroll == null) {
            attachedLayout = getActivity().findViewById(R.id.attached);
            attachedScroll = getActivity().findViewById(R.id.attachedScroll);
        }
        attachedLayout.removeView(v);
        attach.remove(f);
        if(attachedLayout.getChildCount() == 0){
            attachedScroll.setVisibility(GONE);
        }

    }

    private void sendMessage(int threadId, String text, long time) {
        new Thread(() -> {
            try {
                HttpPost post = new HttpPost("https://app.eschool.center/ec-server/chat/sendNew");
                HttpClient httpAsyncClient = AndroidHttpClient.newInstance("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393", getContext());
                MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
                reqEntity.setBoundary("----WebKitFormBoundaryfgXAnWy3pntveyQZ");
                for (File f : attach) {
                    reqEntity.addBinaryBody("file", f, ContentType.create(getMimeType(f.getPath())), transliterate(f.getName()));

                }
                getActivity().runOnUiThread(this::clearAttached);
                reqEntity.addTextBody("threadId", "" + threadId);
                reqEntity.addTextBody("msgUID", "" + time);
                reqEntity.addTextBody("msgText", text, ContentType.parse("text/plain; charset=utf-8"));
                post.setHeader("Cookie", TheSingleton.getInstance().getCOOKIE() + "; site_ver=app; route=" + TheSingleton.getInstance().getROUTE() + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                post.setHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryfgXAnWy3pntveyQZ ");
                post.setEntity(reqEntity.build());
                JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpAsyncClient.execute(post).getEntity())).getJSONObject("message");
                getActivity().runOnUiThread(() -> {
                    try {
                        ArrayList<Attach> files = new ArrayList<>();
                        for (int j = 0; j < jsonObject.getInt("attachCount"); j++) {
                            JSONObject tmp1 = jsonObject.getJSONArray("attachInfo").getJSONObject(j);
                            files.add(new Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                    tmp1.getString("fileName"), tmp1.getString("fileType")));
                        }
                        newMessage(jsonObject.has("msg") ? jsonObject.getString("msg") : "", new Date(jsonObject.getLong("createDate")), jsonObject.getInt("senderId"), jsonObject.getInt("threadId"), jsonObject.getString("senderFio"), jsonObject.has("attachInfo") ? files : new ArrayList<>(),true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                httpAsyncClient.getConnectionManager().closeExpiredConnections();
//                log("sending file code " + code);
            } catch (IllegalStateException ignored){
            } catch (UnknownHostException e) {
                Toast.makeText(getContext(), "Нет интернета", Toast.LENGTH_SHORT).show();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static final String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
            "октября", "ноября", "декабря"};

    private static String getDate(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH),
                month = calendar.get(MONTH),
                year = calendar.get(YEAR);
        Calendar current = Calendar.getInstance();
        if(current.get(YEAR) != year)
            return String.format(Locale.getDefault(), "%02d.%02d.%02d", day, month+1, year);
        if(current.get(Calendar.DAY_OF_MONTH) == day)
            return "Сегодня";
        else if(current.get(Calendar.DAY_OF_MONTH) + 1 == day)
            return "Вчера";
        return String.format(Locale.getDefault(), "%d " + months[month], day);
    }

    private class Msg {
        Date time;
        List<Attach> files = new ArrayList<>();
        int user_id, msg_id;
        String text, sender;
    }

    public static class Attach {
        public int fileId = 0, size = 0;
        public String name = "", type = "";

        Attach(int fileId, int size, String name, String type) {
            this.fileId = fileId;
            this.size = size;
            if(name != null) this.name = name;
            if(type != null) this.type = type;
        }
    }

    public Activity getContext() {return (context==null?getActivity():context);}

    public static String transliterate(String srcstring) {
        List<String> copyTo = new ArrayList<>();

        String cyrcodes = "";
        for (int i='А';i<='Я';i++) {
            cyrcodes = cyrcodes + (char)i;
        }
        cyrcodes+='Ё';
        for (int j='а';j<='я';j++) {
            cyrcodes = cyrcodes + (char)j;
        }
        cyrcodes+='ё';
        // Uppercase
        copyTo.add("A");
        copyTo.add("B");
        copyTo.add("V");
        copyTo.add("G");
        copyTo.add("D");
        copyTo.add("E");
        copyTo.add("Zh");
        copyTo.add("Z");
        copyTo.add("I");
        copyTo.add("I");
        copyTo.add("K");
        copyTo.add("L");
        copyTo.add("M");
        copyTo.add("N");
        copyTo.add("O");
        copyTo.add("P");
        copyTo.add("R");
        copyTo.add("S");
        copyTo.add("T");
        copyTo.add("U");
        copyTo.add("F");
        copyTo.add("Kh");
        copyTo.add("TS");
        copyTo.add("Ch");
        copyTo.add("Sh");
        copyTo.add("Shch");
        copyTo.add("");
        copyTo.add("Y");
        copyTo.add("");
        copyTo.add("E");
        copyTo.add("YU");
        copyTo.add("YA");
        copyTo.add("YO");

        // lowercase
        copyTo.add("a");
        copyTo.add("b");
        copyTo.add("v");
        copyTo.add("g");
        copyTo.add("d");
        copyTo.add("e");
        copyTo.add("zh");
        copyTo.add("z");
        copyTo.add("i");
        copyTo.add("i");
        copyTo.add("k");
        copyTo.add("l");
        copyTo.add("m");
        copyTo.add("n");
        copyTo.add("o");
        copyTo.add("p");
        copyTo.add("r");
        copyTo.add("s");
        copyTo.add("t");
        copyTo.add("u");
        copyTo.add("f");
        copyTo.add("kh");
        copyTo.add("ts");
        copyTo.add("ch");
        copyTo.add("sh");
        copyTo.add("shch");
        copyTo.add("");
        copyTo.add("y");
        copyTo.add("");
        copyTo.add("e");
        copyTo.add("yu");
        copyTo.add("ya");
        copyTo.add("yo");


        String newstring = "";
        char onechar;
        int replacewith;
        for (int j=0; j<srcstring.length();j++) {
            onechar = srcstring.charAt(j);
            replacewith = cyrcodes.indexOf((int)onechar);
            if (replacewith > -1) {
                newstring = newstring + copyTo.get(replacewith);
            } else {
                // keep the original character, not in replace list
                newstring = newstring + onechar;
            }
        }

        return newstring;
    }
}
