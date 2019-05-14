package com.example.sch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import static java.lang.Thread.sleep;

public class PeriodFragment extends Fragment {

    private String COOKIE, ROUTE;
    private int USER_ID;
    ArrayList<Subject> subjects;
    ArrayList<Day> days;
    boolean ready = false;
    boolean red = false;
    ArrayList<Cell> cells;
    ArrayList<TextView> txts;
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

    LinearLayout layout1, layout2, layout3, layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void start() {
        cells = new ArrayList<>();
        days = new ArrayList<>();
        subjects = new ArrayList<>();
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        USER_ID = TheSingleton.getInstance().getUSER_ID();
        txts = new ArrayList<>();
        Download();
    }

    ArrayList<Day> getDays() {
        return days;
    }

    ArrayList<Subject> getSubjects() {
        return subjects;
    }

    boolean isReady() {
        return red;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary, container, false);
        while (true) {
            try {
                sleep(10);
                if (ready)
                    break;
            } catch (InterruptedException e) {
            }

        }
        StringBuilder y = new StringBuilder();
        layout = view.findViewById(R.id.linear);
        layout1 = view.findViewById(R.id.linear1);
        layout2 = view.findViewById(R.id.linear2);
        layout3 = view.findViewById(R.id.linear3);
        for (int i = 0; i < subjects.size() - 1; i++) {
            TextView txt1 = new TextView(getActivity().getApplicationContext());
            TextView txt2 = new TextView(getActivity().getApplicationContext());
            LinearLayout linearLayout = new LinearLayout(getActivity().getApplicationContext());
            txt1.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 40, 10);
            txt1.setLayoutParams(lp);
            txt1.setTextSize(20);
            txt2.setTextSize(20);
            txt2.setLayoutParams(lp);
            txt2.setTextColor(getResources().getColor(R.color.two));
            txt1.setText(subjects.get(i).shortname);
            txt2.setText(String.valueOf(subjects.get(i).avg));
            try {
                txt2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        MarkFragment fragment = new MarkFragment();
                        transaction.replace(R.id.frame, fragment);
                        try {

                        } catch (Exception e) {
                        }
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
            } catch (Exception e) {
            }
            try {
                txt1.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        MarkFragment fragment = new MarkFragment();
                        transaction.replace(R.id.frame, fragment);
                        try {

                        } catch (Exception e) {
                        }
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
            } catch (Exception e) {
            }
            layout2.addView(txt2);
            layout1.addView(txt1);
            try {
                for (int j = 0; j < subjects.get(i).calls.size(); j++) {
                    if (subjects.get(i).calls.get(j).mktWt != 0) {
                        Double d = subjects.get(i).calls.get(j).mktWt;
                        txts.add(new TextView(getActivity().getApplicationContext()));
                        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp1.setMargins(0, 0, 10, 10);
                        txts.get(txts.size() - 1).setLayoutParams(lp1);
                        try {
                            final int finalI = i;
                            final int finalJ = j;
                            txts.get(txts.size() - 1).setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                    MarkFragment fragment = new MarkFragment();
                                    transaction.replace(R.id.frame, fragment);
                                    try {
                                        fragment.coff = subjects.get(finalI).calls.get(finalJ).mktWt;
                                        fragment.data = subjects.get(finalI).calls.get(finalJ).date;
                                        fragment.teachname = subjects.get(finalI).calls.get(finalJ).teachFio;
                                        fragment.topic = subjects.get(finalI).calls.get(finalJ).lptname;
                                        fragment.value = subjects.get(finalI).calls.get(finalJ).markvalue;
                                        fragment.subject = subjects.get(finalI).name;
                                    } catch (Exception e) {
                                    }
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                }
                            });
                        } catch (Exception e) {
                        }
                        txts.get(txts.size() - 1).setTextSize(20);
                        txts.get(txts.size() - 1).setTextColor(Color.WHITE);
                        txts.get(txts.size() - 1).setBackground(getResources().getDrawable(R.drawable.gradient_list));
                        txts.get(txts.size() - 1).setPadding(15, 0, 15, 0);
//
                        if (d <= 0.5)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff1));
                        else if (d <= 1)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff2));
                        else if (d <= 1.25)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff3));
                        else if (d <= 1.35)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff4));
                        else if (d <= 1.5)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff5));
                        else if (d <= 1.75)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff6));
                        else if (d <= 2)
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff7));
                        else
                            txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff8));

                        if (subjects.get(i).calls.get(j).markvalue != null)
                            txts.get(txts.size() - 1).setText(subjects.get(i).calls.get(j).markvalue);
                        else {
                            txts.get(txts.size() - 1).setText("7");
                            txts.get(txts.size() - 1).setTextColor(Color.TRANSPARENT);
                        }
                        linearLayout.addView(txts.get(txts.size() - 1));
                    }
                }
            } catch (Exception e) {
            }
            layout3.addView(linearLayout);
        }
        return view;
    }

    void Download() {

        new Thread() {
            @SuppressLint("SimpleDateFormat")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    //------------------------------------------------------------------------------------------------
                    URL url = new URL("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=97932");
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

                    JSONObject object = new JSONObject(result.toString());
                    JSONArray array = object.getJSONArray("result");
                    for (int i = 0; i < array.length(); i++) {
                        subjects.add(new PeriodFragment.Subject());
                        JSONObject obj = array.getJSONObject(i);
                        if (obj.has("overMark")) {
                            double d = obj.getDouble("overMark");
                            String s = String.valueOf(d);
                            if (s.length() > 4) {
                                s = String.format(Locale.UK, "%.2f", d);
                            }
                            subjects.get(i).avg = Double.valueOf(s);
                        }
                        if (obj.has("totalMark"))
                            subjects.get(i).totalmark = obj.getString("totalMark");
                        if (obj.has("unitName"))
                            subjects.get(i).name = obj.getString("unitName");
                        if (obj.has("rating"))
                            subjects.get(i).rating = obj.getString("rating");
                        if (obj.has("unitId"))
                            subjects.get(i).unitid = obj.getInt("unitId");
                        subjects.get(i).calls = new ArrayList<>();
                    }
                    URL url1 = new URL("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=97932");
                    HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                    con1.setRequestMethod("GET");
                    con1.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*; site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    StringBuilder result1 = new StringBuilder();
                    BufferedReader rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
                    while ((line = rd1.readLine()) != null) {
                        result1.append(line);
                    }
                    rd1.close();
                    JSONObject object1 = new JSONObject(result1.toString());
                    JSONArray arraydaylessons = object1.getJSONArray("result");
                    for (int i = 0; i < arraydaylessons.length(); i++) {
                        object1 = arraydaylessons.getJSONObject(i);
                        PeriodFragment.Cell call = new PeriodFragment.Cell();
                        if (object1.has("lptName"))
                            call.lptname = object1.getString("lptName");
                        if (object1.has("markDate"))
                            call.markdate = object1.getString("markDate");
                        if (object1.has("lessonId"))
                            call.lessonid = object1.getLong("lessonId");
                        if (object1.has("markVal"))
                            call.markvalue = object1.getString("markVal");
                        if (object1.has("mktWt"))
                            call.mktWt = object1.getDouble("mktWt");
                        if (object1.has("teachFio"))
                            call.teachFio = object1.getString("teachFio");
                        if (object1.has("startDt"))
                            call.date = object1.getString("startDt");
                        if (object1.has("unitId"))
                            call.unitid = object1.getInt("unitId");
                        cells.add(call);
                    }
                    COOKIE = TheSingleton.getInstance().getCOOKIE();
                    ROUTE = TheSingleton.getInstance().getROUTE();
                    USER_ID = TheSingleton.getInstance().getUSER_ID();

                    String s1 = cells.get(0).date;
                    String s2 = cells.get(cells.size() - 1).date;
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    Long d1 = format.parse(s1).getTime();
                    Long d2 = format.parse(s2).getTime();

                    URL url2 = new URL("https://app.eschool.center/ec-server/student/diary?" +
                            "userId=" + USER_ID + "&d1=" + d1 + "&d2=" + d2);
                    HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
                    con2.setRequestMethod("GET");

                    con2.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*;" +
                            " site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060." +
                            "16.1554146944.1554139340.");

                    StringBuilder result2 = new StringBuilder();

                    BufferedReader rd2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));

                    while ((line = rd2.readLine()) != null) {
                        result2.append(line);
                    }
                    rd2.close();
                    JSONObject object2 = new JSONObject(result2.toString());
                    JSONArray array2 = object2.getJSONArray("lesson");

                    Long day1 = 0l;
                    Long date1;
                    int isODOD;
                    int index = -1;
                    for (int i = 0; i < array2.length(); i++) {
                        object2 = array2.getJSONObject(i);
                        date1 = Long.valueOf(String.valueOf(object2.getString("date")));
                        isODOD = object2.getInt("isODOD");
                        if (isODOD != 1) {
                            if (!date1.equals(day1) || date1 - day1 != 0) {
                                index++;
                                Date date = new Date(date1);
                                PeriodFragment.Day thisday = new PeriodFragment.Day();
                                thisday.day = String.valueOf(date);
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
                                thisday.lessons = new ArrayList<>();
                                days.add(thisday);
                            }
                            Lesson lesson = new Lesson();
                            lesson.id = object2.getLong("id");
                            lesson.numInDay = object2.getInt("numInDay");
                            if (object2.getJSONObject("unit").has("id"))
                                lesson.unitId = object2.getJSONObject("unit").getLong("id");
                            if (object2.getJSONObject("unit").has("name"))
                                lesson.name = object2.getJSONObject("unit").getString("name");
                            if (object2.getJSONObject("unit").has("short"))
                                lesson.shortname = object2.getJSONObject("unit").getString("short");
                            if (object2.getJSONObject("tp").has("topicName"))
                                lesson.topic = object2.getJSONObject("tp").getString("topicName");
                            if (object2.getJSONObject("teacher").has("factTeacherIN"))
                                lesson.teachername = object2.getJSONObject("teacher").getString("factTeacherIN");
                            JSONArray ar = object2.getJSONArray("part");
                            lesson.homeWork = new HomeWork();
                            lesson.homeWork.stringwork = "";
                            for (int j = 0; j < ar.length(); j++) {
                                if (ar.getJSONObject(j).getString("cat").equals("DZ")) {
                                    if (ar.getJSONObject(j).has("variant")) {
                                        JSONArray ar1 = ar.getJSONObject(j).getJSONArray("variant");
                                        for (int k = 0; k < ar1.length(); k++) {
                                            if (ar1.getJSONObject(k).has("text")) {
                                                lesson.homeWork.stringwork += ar1.getJSONObject(k).getString("text") + "\n";
                                            }
                                            if (ar1.getJSONObject(k).has("file")) {
                                                JSONArray ar2 = ar1.getJSONObject(k).getJSONArray("file");
                                                for (int l = 0; l < ar2.length(); l++) {
                                                    //
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            days.get(index).lessons.add(lesson);
                        }
                        day1 = date1;
                    }

                    for (int i = 0; i < days.size(); i++) {
                        for (int j = 0; j < cells.size(); j++) {
                            s1 = cells.get(j).date;
                            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                            d1 = format.parse(s1).getTime();
                            if (cells.get(j).mktWt != 0) {
                                if (days.get(i).daymsec - d1 == 0 || days.get(i).daymsec.equals(d1)) {
                                    for (int k = 0; k < days.get(i).lessons.size(); k++) {
                                        if (days.get(i).lessons.get(k).id.equals(cells.get(j).lessonid)) {
                                            Mark mark = new Mark();
                                            mark.idlesson = cells.get(j).lessonid;
                                            mark.coefficient = cells.get(j).mktWt;
                                            if (cells.get(j).markvalue != null)
                                                mark.value = cells.get(j).markvalue;
                                            else
                                                mark.value = "";
                                            mark.teachFio = cells.get(j).teachFio;
                                            mark.date = cells.get(j).markdate;

                                            mark.topic = cells.get(j).lptname;
                                            mark.unitid = cells.get(j).unitid;
                                            for (int l = 0; l < subjects.size() - 1; l++) {
                                                if (subjects.get(l).unitid == mark.unitid) {
                                                    subjects.get(l).calls.add(cells.get(j));
                                                    if (subjects.get(l).shortname != null)
                                                        subjects.get(l).shortname = days.get(i).lessons.get(k).shortname;
                                                    if (days.get(i).lessons.get(k).shortname.equals("Обществозн."))
                                                        subjects.get(l).shortname = "Общест.";
                                                    if (days.get(i).lessons.get(k).shortname.equals("Физ. культ."))
                                                        subjects.get(l).shortname = "Физ-ра";
                                                    if (days.get(i).lessons.get(k).shortname.equals("Инф. и ИКТ"))
                                                        subjects.get(l).shortname = "Информ.";
                                                }
                                            }
                                            days.get(i).lessons.get(k).marks.add(mark);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ready = true;
                    red = true;
                    //---------------------------------------------------------------------------------------------------------------------------------
                } catch (Exception e) {
                    Download();
                }
            }
        }.start();
    }

    static class Cell {
        String lptname, markvalue, date;
        double mktWt = 0;
        Long lessonid;
        String markdate, teachFio;
        int unitid;

        Cell() {
        }
    }

    static class Day {
        Long daymsec;
        String day;
        int numday;
        ArrayList<Lesson> lessons;

        Day() {
        }
    }

    static class Lesson {
        int numInDay, numDay;
        String name = "", teachername = "", topic = "", shortname = "";
        HomeWork homeWork;
        ArrayList<Mark> marks = new ArrayList<>();
        Long id;
        long unitId = 0;

        Lesson() {
        }

    }

    static class HomeWork {
        ArrayList<Integer> idfils;
        String stringwork = "";

        HomeWork(String a) {
            stringwork = a;
        }

        HomeWork() {
        }
    }

    static class Mark {
        public int unitid;
        String value, teachFio, date, topic;
        double coefficient;
        Long idlesson;

        Mark() {
        }
    }

    public class Subject {
        String name, rating = "", shortname = "", totalmark;
        double avg;
        int unitid;
        ArrayList<Cell> calls;

        Subject() {
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
