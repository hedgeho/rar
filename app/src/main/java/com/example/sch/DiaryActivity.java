package com.example.sch;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.sch.MainActivity.log;

public class DiaryActivity extends AppCompatActivity {

    static String COOKIE, ROUTE;
    static int USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        COOKIE = getIntent().getStringExtra("cookie");
        ROUTE = getIntent().getStringExtra("route");
        USER_ID = getIntent().getIntExtra("userId", -1);

        final ListView list = findViewById(R.id.list);

        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ArrayAdapter adapter = (ArrayAdapter) msg.obj;
                list.setAdapter(adapter);
            }
        };

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    URL url = new URL("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=97932");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    StringBuilder result = new StringBuilder();
                    log("cookie: " + con.getRequestProperty("Cookie"));
                    log("code " + con.getResponseCode());
                    Log.v("mylog", con.getResponseMessage());

                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();

                    Log.v("mylog","diary: " + result.toString());

                    ArrayList<String> arr = new ArrayList<>();
                    JSONObject obj = new JSONObject(result.toString());
                    log("ddd");
                    JSONArray array = obj.getJSONArray("result");
                    log("ddd");
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        if(!obj.has("overMark"))
                            continue;
                        if(obj.getDouble("overMark") != 0.0) {
                            if(obj.has("rating"))
                                arr.add(obj.getString("unitName") + ": рейтинг: " + obj.getString("rating")
                                    + ", средний балл: " + obj.getDouble("overMark"));
                            else
                                arr.add(obj.getString("unitName") + ": средний балл: " + obj.getDouble("overMark"));
                        }
                    }
                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.item, arr.toArray());
                    h.sendMessage(h.obtainMessage(211214, adapter));
            } catch (Exception e) {
                Log.e("mylog", e.toString());
            }
            }
        }.start();

        //ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.item, )
    }
}
