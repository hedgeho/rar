package com.example.sch;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
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
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewUtils;
import android.text.Html;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private String COOKIE, ROUTE;
    private int PERSON_ID;
    int threadId = 0;
    String threadName = "";
    private String[] msg, time;
    private int[] user_ids;
    private Attach[][] files;
    private Handler h;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        PERSON_ID = TheSingleton.getInstance().getPERSON_ID();

        View view = inflater.inflate(R.layout.chat, container, false);

        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(threadName);
        // todo toolbar subtitle
        // toolbar.setSubtitle("subtitle");

        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        // Inflate the layout for this fragment``
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        return view;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        h = new Handler() {
            @Override
            public void handleMessage(Message yoy) {
                final LinearLayout container = view.findViewById(R.id.container);
                final ScrollView scroll = view.findViewById(R.id.scroll);
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
                    tv.setMaxWidth(view.getMeasuredWidth()-200);
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
                    params.topMargin = 40;
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
                            tv_attach.setMaxWidth(view.getMeasuredWidth()-200);
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
                        tv.setMaxWidth(view.getMeasuredWidth()-200);
                        tv = item.findViewById(R.id.tv_time);
                        tv.setText(String.format(Locale.UK, "%02d:%02d", new Date().getHours(), new Date().getMinutes()));
                        item.setPadding(0, 16, 4, 0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.END;
                        container.addView(item, params);
                        scroll.post(new Runnable() {
                            @Override
                            public void run() {
                                scroll.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                };
                view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String text = et.getText().toString();
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    hand.sendMessage(hand.obtainMessage(0, text));
                                    HttpURLConnection con = (HttpURLConnection) new URL("https://app.eschool.center/ec-server/chat/sendNew").openConnection();
                                    con.setRequestMethod("POST");
                                    con.setRequestProperty("Cookie", COOKIE + "; site_ver=app; route=" + ROUTE + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");// "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.13.1554062260.1554051192.");
                                    con.setDoOutput(true);
                                    con.getOutputStream().write(("threadId=" + threadId + "&msgText=" + text +
                                            "&msgUID=" + System.currentTimeMillis()).getBytes());
                                    log("threadId=" + threadId + "&msgText=" + text +
                                            "&msgUID=" + System.currentTimeMillis());
                                    con.connect();
                                    log(con.getResponseMessage());
                                } catch (Exception e) {
                                    loge(e.toString());
                                }
                            }
                        }.start();
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

                    log(result.toString());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        log("kdd");
        if(item.getItemId() == android.R.id.home || item.getItemId() == R.id.homeAsUp) {
            log("smth");
            getActivity().getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
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
