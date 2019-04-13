package com.example.sch;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class PeriodFragment extends Fragment {

    private String COOKIE, ROUTE;
    private int USER_ID;
    TableLayout table;
    Subject[] subjects;
    boolean ready = false;
    public PeriodFragment () {}



    public void start(final Context context) {
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        USER_ID = TheSingleton.getInstance().getUSER_ID();
        table = new TableLayout(context);

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

                    JSONObject obj = new JSONObject(result.toString());
                    JSONArray array = obj.getJSONArray("result");
                    int[] unitId_array = new int[array.length()];
                    subjects = new Subject[array.length()];
                    for (int i = 0; i < array.length(); i++) {
//                        tmp = new ArrayList<>();
                        obj = array.getJSONObject(i);
                        unitId_array[i] = obj.getInt("unitId");
                        /*if(!obj.has("overMark"))
                            continue;*/
                        if(obj.has("overMark")) {
                            if (obj.getDouble("overMark") != 0.0) {
                                if (obj.has("rating")) {
                                    subjects[i] = new Subject(obj.getString("unitName"), obj.getDouble("overMark"), obj.getString("rating"));
                                } else {
                                    subjects[i] = new Subject(obj.getString("unitName"), obj.getDouble("overMark"), "");
                                }
                            } else {
                                subjects[i] = new Subject(obj.getString("unitName"), -1,  "");
                            }
                        }
                    }

                    url = new URL("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=97932");
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");

                    log("getting marks...");
                    log(con.getResponseCode() + "");

                    result = new StringBuilder();
                    rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();
                    log(result.toString());


                    ArrayList<ArrayList<String>> arr = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        arr.add(new ArrayList<String>());
                    }
                    ArrayList<String> tmp;
                    obj = new JSONObject(result.toString());
                    array = obj.getJSONArray("result");
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        if (obj.has("markVal")) {
                            int unitId = obj.getInt("unitId");
                            int k = -1;
                            for (int j = 0; j < unitId_array.length; j++) {
                                if (unitId == unitId_array[j]) {
                                    k = j;
                                    break;
                                }
                            }
                            tmp = arr.get(k);
                            tmp.add(obj.getString("markVal"));
                        }
                    }

                    TableRow row;
                    TextView tv;
                    for (int i = 0; i < arr.size(); i++) {
                        row = new TableRow(context);
                        tmp = arr.get(i);
                        if(tmp.size() == 0) {
                            tv = new TextView(context);
                            tv.setLayoutParams(new TableRow.LayoutParams(0));
                            tv.setText("");
                            row.addView(tv);
                            tv.setHeight(86);
                        }
                        for (int j = 0; j < tmp.size(); j++) {
                            tv = new TextView(context);
                            tv.setTextSize(16);
                            tv.setLayoutParams(new TableRow.LayoutParams(j));
                                tv.setPadding(0, 0, 8, 8);
                            tv.setText(tmp.get(j));
                            tv.setTextColor(getResources().getColor(R.color.three));
                            tv.setHeight(86);
                            row.addView(tv);
                        }
                        row.setPadding(0, 0, 0, 8);
                        table.addView(row);
                    }
                    ready = true;
                } catch (Exception e) {
                    loge(e.toString());
                }
            }
        }.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.diary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        while(true) {
            try {
                Thread.sleep(10);
                if(ready)
                    break;
            } catch (InterruptedException e) {
                //
            }
        }
        LinearLayout l_subjects = view.findViewById(R.id.linear);
        LinearLayout tmp;
        TextView tv_tmp;
        for (Subject s: subjects) {
            if(s == null) {
                log("null");
                continue;
            }
            log(s.toString());
            tmp = new LinearLayout(getContext());
            tmp.setOrientation(LinearLayout.HORIZONTAL);
            tv_tmp = new TextView(getContext());
            tv_tmp.setText(s.name);
            tv_tmp.setTextSize(16);
            tv_tmp.setTextColor(getResources().getColor(R.color.three));
            tv_tmp.setPadding(0, 0, 8, 0);
            tmp.addView(tv_tmp);

            if(!(s.avg == -1)) {
                tv_tmp = new TextView(getContext());
                tv_tmp.setText(String.format(Locale.UK, "%.2f", s.avg));
                tv_tmp.setTextSize(16);
                tv_tmp.setTextColor(getResources().getColor(R.color.two));
                tv_tmp.setPadding(0, 0, 8, 0);
                tmp.addView(tv_tmp);
            }

            if(!s.rating.equals("")) {
                tv_tmp = new TextView(getContext());
                tv_tmp.setText(s.rating);
                tv_tmp.setTextSize(16);
                tv_tmp.setTextColor(getResources().getColor(R.color.one));
                tv_tmp.setPadding(0, 0, 8, 0);
                tmp.addView(tv_tmp);
            }
            tmp.setPadding(0, 0, 0, 8);
            l_subjects.addView(tmp);
        }

        HorizontalScrollView scroll = view.findViewById(R.id.tv_users);
        LinearLayout layout = new LinearLayout(getContext());
        if(table.getParent() != null) {
            ((ViewGroup)table.getParent()).removeView(table);
        }
        layout.addView(table, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scroll.addView(layout, HorizontalScrollView.LayoutParams.MATCH_PARENT, HorizontalScrollView.LayoutParams.WRAP_CONTENT);
    }

    class Subject {
        String name, rating;
        double avg;

        Subject(String name, double avg, String rating) {
            this.name = name;
            this.avg = avg;
            this.rating = rating;
        }

        @Override
        public String toString() {
            return name + " " + rating + " " + avg;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        log("onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        log("onDetach");
    }
}
