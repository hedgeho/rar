package com.example.sch;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private String COOKIE, ROUTE;
    private int USER_ID;
    int threadId = 0;
    String threadName = "";
    private String[] msg;
    private String[] time;
    private Handler h;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        USER_ID = TheSingleton.getInstance().getUSER_ID();

        View view = inflater.inflate(R.layout.chat, container, false);

        android.support.v7.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(threadName);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                log("aaaa");
                if(menuItem.getItemId() == android.R.id.home || menuItem.getItemId() == R.id.homeAsUp) {
                    log("aaaaaaaaaaaaAA");
                    getActivity().getFragmentManager().popBackStack();
                }
                return false;
            }
        });
        if(toolbar.findViewById(R.id.homeAsUp) != null)
            log("ekfbj");
        if(toolbar.findViewById(android.R.id.home) != null)
            log("dddddddd");
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
                LinearLayout container = view.findViewById(R.id.container);
                LayoutInflater inflater = getLayoutInflater();
                View item;
                LinearLayout.LayoutParams params;
                TextView tv;
                for (int i = msg.length-1; i >= 0; i--) {
                    item = inflater.inflate(R.layout.chat_item, container, false);
                    tv = item.findViewById(R.id.tv_text);
                    tv.setText(msg[i]);
                    tv.setTextColor(Color.WHITE);
                    tv.setMaxWidth(view.getMeasuredWidth()-200);
                    tv = item.findViewById(R.id.tv_time);
                    tv.setText(time[i]);
                    item.setPadding(0, 16, 4, 0);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.END;
                    container.addView(item, params);
                }
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
                    JSONObject tmp;
                    Date date;
                    msg = new String[array.length()];
                    time = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        tmp = array.getJSONObject(i);
                        if(!tmp.has("msg")) {
                            loge(tmp.toString());
                            msg[i] = "";
                            time[i] = "";
                            continue;
                        }
                        msg[i] = tmp.getString("msg");
                        date = new Date(tmp.getLong("sendDate"));
                        time[i] = date.getHours() + ":" + date.getMinutes();
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
}
