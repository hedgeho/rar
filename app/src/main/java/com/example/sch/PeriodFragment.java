package com.example.sch;

import android.annotation.SuppressLint;
import android.content.Context;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.sch.LoginActivity.log;

public class PeriodFragment extends Fragment {

    private String COOKIE, ROUTE;
    private int USER_ID;
    TableLayout table;
    Subject[] subjects;
    ArrayList<Subject> subjects1;
    boolean ready = false;
    Long d = 86400000L;
    ArrayList<Call> calls;
    ArrayList<Day> days1;
    public PeriodFragment () {}

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    static void sasha(Boolean s) {
        Log.v("sasha", String.valueOf(s));
    }

    static void sasha(Long s) {
        Log.v("sasha", String.valueOf(s));
    }


    void start(final Context context) {
        calls = new ArrayList<>();
        days1 = new ArrayList<>();
        subjects1 = new ArrayList<>();
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        USER_ID = TheSingleton.getInstance().getUSER_ID();
        table = new TableLayout(context);

        new Thread() {
            @SuppressLint("SimpleDateFormat")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    URL url1 = new URL("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=97932");
                    HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                    con1.setRequestMethod("GET");
                    con1.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    StringBuilder result1 = new StringBuilder();

                    BufferedReader rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));

                    String line;
                    while ((line = rd1.readLine()) != null) {
                        result1.append(line);
                    }
                    rd1.close();

                    JSONObject obj = new JSONObject(result1.toString());
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

                    URL url2 = new URL("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=97932");
                    HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
                    con2.setRequestMethod("GET");
                    con2.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");

                    StringBuilder result2 = new StringBuilder();
                    BufferedReader rd2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
                    while ((line = rd2.readLine()) != null) {
                        result2.append(line);
                    }
                    rd2.close();


                    ArrayList<ArrayList<String>> arr = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        arr.add(new ArrayList<String>());
                    }
                    ArrayList<String> tmp;
                    obj = new JSONObject(result2.toString());
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
                        row.setPadding(0, 0, 0, 0);
                        table.addView(row);
                    }
                    ready = true;

                    //------------------------------------------------------------------------------------------------
                    URL url5 = new URL("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=97932");
                    HttpURLConnection con5 = (HttpURLConnection) url5.openConnection();
                    con5.setRequestMethod("GET");
                    con5.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    StringBuilder result5 = new StringBuilder();

                    BufferedReader rd5 = new BufferedReader(new InputStreamReader(con5.getInputStream()));

                    while ((line = rd5.readLine()) != null) {
                        result5.append(line);
                    }
                    rd5.close();

                    JSONObject object1 = new JSONObject(result5.toString());
                    JSONArray array1 = object1.getJSONArray("result");
                    for (int i = 0; i < array1.length(); i++) {
                        subjects1.add(new Subject());
                        obj = array1.getJSONObject(i);
                        if (obj.has("overMark")) {
                            double d = obj.getDouble("overMark");
                            String s = String.valueOf(d);
                            if (s.length() > 4) {
                                s = String.format(Locale.UK, "%.2f", d);
                            }
                            subjects1.get(i).avg = Double.valueOf(s);
                        }
                        sasha(String.valueOf(subjects1.get(i).avg));
                        if (obj.has("unitName"))
                            subjects1.get(i).name = obj.getString("unitName");
                        if (obj.has("rating"))
                            subjects1.get(i).rating = obj.getString("rating");
                        if (obj.has("unitId"))
                            subjects1.get(i).unitid = obj.getInt("unitId");
                    }
                    URL url3 = new URL("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=97932");
                    HttpURLConnection con3 = (HttpURLConnection) url3.openConnection();
                    con3.setRequestMethod("GET");
                    con3.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    StringBuilder result3 = new StringBuilder();
                    sasha("1");
                    BufferedReader rd3 = new BufferedReader(new InputStreamReader(con3.getInputStream()));
                    while ((line = rd3.readLine()) != null) {
                        result3.append(line);
                    }
                    sasha(String.valueOf(result3));
                    rd3.close();
                    JSONObject object = new JSONObject(result3.toString());
                    JSONArray arraydaylessons = object.getJSONArray("result");
                    for (int i = 0; i < arraydaylessons.length(); i++) {
                        object = arraydaylessons.getJSONObject(i);
                        Call call = new Call();
                        if (object.has("lptName"))
                            call.lptname = object.getString("lptName");
                        if (object.has("markDate"))
                            call.markdate = object.getString("markDate");
                        if (object.has("lessonId"))
                            call.lessonid = object.getLong("lessonId");
                        if (object.has("markVal"))
                            call.markvalue = object.getString("markVal");
                        if (object.has("mktWt"))
                            call.mktWt = object.getDouble("mktWt");
                        if (object.has("teachFio"))
                            call.teachFio = object.getString("teachFio");
                        if (object.has("startDt"))
                            call.date = object.getString("startDt");
                        sasha(call.teachFio + " " + call.lptname + " " + call.markvalue + " " + call.mktWt);
                        calls.add(call);
                    }
                    sasha(String.valueOf((calls.size() + " " + arraydaylessons.length())) + " hhh ");

                    COOKIE = TheSingleton.getInstance().getCOOKIE();
                    ROUTE = TheSingleton.getInstance().getROUTE();
                    USER_ID = TheSingleton.getInstance().getUSER_ID();

                    String s1 = calls.get(0).date;
                    String s2 = calls.get(calls.size() - 1).date;
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    Long d1 = format.parse(s1).getTime();
                    Long d2 = format.parse(s2).getTime();

                    sasha("2");

                    URL url4 = new URL("https://app.eschool.center/ec-server/student/diary?" +
                            "userId=" + USER_ID + "&d1=" + d1 + "&d2=" + d2);
                    HttpURLConnection con4 = (HttpURLConnection) url4.openConnection();
                    con4.setRequestMethod("GET");
                    sasha("3");

                    con4.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*;" +
                            " site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060." +
                            "16.1554146944.1554139340.");

                    StringBuilder result4 = new StringBuilder();

                    BufferedReader rd4 = new BufferedReader(new InputStreamReader(con4.getInputStream()));

                    while ((line = rd4.readLine()) != null) {
                        result4.append(line);
                    }
                    rd4.close();
                    JSONObject obj1 = new JSONObject(result4.toString());
                    JSONArray array2 = obj1.getJSONArray("lesson");

                    Long day1 = 0l;
                    Long date1;
                    int isODOD;
                    int index = -1;
                    for (int i = 0; i < array2.length(); i++) {
                        obj1 = array2.getJSONObject(i);
                        date1 = Long.valueOf(String.valueOf(obj1.getString("date")));
                        if (date1.equals(day1) || date1 - day1 == 0) {
                            isODOD = obj1.getInt("isODOD");
                            if (isODOD != 1) {
                                ScheduleFragment.Lesson lesson = new ScheduleFragment.Lesson();
                                lesson.id = obj1.getLong("id");
                                lesson.numInDay = obj1.getInt("numInDay");
                                if (obj1.getJSONObject("unit").has("id"))
                                    lesson.unitId = obj1.getJSONObject("unit").getLong("id");
                                if (obj1.getJSONObject("unit").has("name"))
                                    lesson.name = obj1.getJSONObject("unit").getString("name");
                                if (obj1.getJSONObject("unit").has("short"))
                                    lesson.shortname = obj1.getJSONObject("unit").getString("short");
                                if (obj1.getJSONObject("tp").has("topicName"))
                                    lesson.topic = obj1.getJSONObject("tp").getString("topicName");
                                if (obj1.getJSONObject("teacher").has("factTeacherIN"))
                                    lesson.teachername = obj1.getJSONObject("teacher").getString("factTeacherIN");
                                JSONArray ar = obj1.getJSONArray("part");
                                lesson.homeWork = new ScheduleFragment.HomeWork();
                                lesson.homeWork.stringwork = "";
                                for (int j = 0; j < ar.length(); j++) {
                                    if (ar.getJSONObject(j).getString("cat") == "DZ") {
                                        if (ar.getJSONObject(j).has("variant")) {
                                            JSONArray ar1 = ar.getJSONObject(j).getJSONArray("variant");
                                            for (int k = 0; k < ar1.length(); k++) {
                                                if (ar1.getJSONObject(k).has("text")) {
                                                    lesson.homeWork.stringwork += ar1.getJSONObject(k).getString("text") + " ";
                                                }
                                                JSONArray ar2 = ar1.getJSONObject(j).getJSONArray("file");
                                                for (int l = 0; l < ar2.length(); l++) {
                                                    //
                                                }
                                            }
                                        }
                                    }
                                }
                                days1.get(index).lessons.add(lesson);
                            }
                        } else {
                            isODOD = obj1.getInt("isODOD");
                            if (isODOD != 1) {
                                days1.add(new Day());
                                index++;
                                Day thisday = new Day();
                                thisday.daymsec = date1;
                                Date datathis = new Date();
                                datathis.setTime(date1);
                                SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE", Locale.ENGLISH);
                                String dayOfTheWeek = dateFormat2.format(datathis);
                                switch (dayOfTheWeek) {
                                    case "Mon":
                                        thisday.numday = 1;
                                        break;
                                    case "Tue":
                                        thisday.numday = 2;
                                        break;
                                    case "Wed":
                                        thisday.numday = 3;
                                        break;
                                    case "Thu":
                                        thisday.numday = 4;
                                        break;
                                    case "Fri":
                                        thisday.numday = 5;
                                        break;
                                    case "Sat":
                                        thisday.numday = 6;
                                        break;
                                    case "Sun":
                                        thisday.numday = 7;
                                        break;
                                }
                                ScheduleFragment.Lesson lesson = new ScheduleFragment.Lesson();
                                lesson.id = obj1.getLong("id");
                                lesson.numInDay = obj1.getInt("numInDay");
                                if (obj1.getJSONObject("unit").has("id"))
                                    lesson.unitId = obj1.getJSONObject("unit").getLong("id");
                                if (obj1.getJSONObject("unit").has("name"))
                                    lesson.name = obj1.getJSONObject("unit").getString("name");
                                if (obj1.getJSONObject("unit").has("short"))
                                    lesson.shortname = obj1.getJSONObject("unit").getString("short");
                                if (obj1.getJSONObject("tp").has("topicName"))
                                    lesson.topic = obj1.getJSONObject("tp").getString("topicName");
                                if (obj1.getJSONObject("teacher").has("factTeacherIN"))
                                    lesson.teachername = obj1.getJSONObject("teacher").getString("factTeacherIN");
                                JSONArray ar = obj1.getJSONArray("part");
                                lesson.homeWork = new ScheduleFragment.HomeWork();
                                lesson.homeWork.stringwork = "";
                                for (int j = 0; j < ar.length(); j++) {
                                    if (ar.getJSONObject(j).getString("cat") == "DZ") {
                                        if (ar.getJSONObject(j).has("variant")) {
                                            JSONArray ar1 = ar.getJSONObject(j).getJSONArray("variant");
                                            for (int k = 0; k < ar1.length(); k++) {
                                                if (ar1.getJSONObject(k).has("text")) {
                                                    lesson.homeWork.stringwork += ar1.getJSONObject(k).getString("text") + " ";
                                                }
                                                JSONArray ar2 = ar1.getJSONObject(j).getJSONArray("file");
                                                for (int l = 0; l < ar2.length(); l++) {
                                                    //
                                                }
                                            }
                                        }
                                    }
                                }
                                thisday.lessons = new ArrayList<>();
                                thisday.lessons.add(lesson);
                                days1.set(index, thisday);
                            }
                        }
                        day1 = date1;
                    }
                    for (int i = 0; i < days1.size(); i++) {
                        for (int j = 0; j < calls.size(); j++) {
                            s1 = calls.get(j).date;
                            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                            d1 = format.parse(s1).getTime();
                            if (days1.get(i).daymsec - d1 == 0 || days1.get(i).daymsec == d1) {
                                sasha(days1.get(i).day + " ");
                                for (int k = 0; k < days1.get(i).lessons.size(); k++) {
                                    if (days1.get(i).lessons.get(k).id - calls.get(j).lessonid == 0) {
                                        ScheduleFragment.Mark mark = new ScheduleFragment.Mark();
                                        mark.idlesson = calls.get(j).lessonid;
                                        mark.coefficient = calls.get(j).mktWt;
                                        mark.value = calls.get(j).markvalue;
                                        mark.teachFio = calls.get(j).teachFio;
                                        mark.date = calls.get(j).markdate;
                                        mark.topic = calls.get(j).lptname;
                                        days1.get(i).lessons.get(k).marks.add(mark);
                                    }
                                }
                            }
                        }
                    }
                    //---------------------------------------------------------------------------------------------------------------------------------
                } catch (Exception e) {
                    sasha(e.toString());
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
            tmp.setPadding(0, 0, 0, 0);
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
        int unitid;

        Subject(String name, double avg, String rating) {
            this.name = name;
            this.avg = avg;
            this.rating = rating;
        }

        Subject() {
        }

        @Override
        public String toString() {
            return name + " " + rating + " " + avg;
        }
    }

    class Call {
        String lptname, markvalue, date;
        double mktWt;
        Long lessonid;
        String markdate, teachFio;

        Call() {
        }
    }

    class Day {
        Long daymsec;
        String day;
        int numday, unitid;
        ArrayList<ScheduleFragment.Lesson> lessons;

        Day(String day, ArrayList<ScheduleFragment.Lesson> lessons) {
        }

        Day() {
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
