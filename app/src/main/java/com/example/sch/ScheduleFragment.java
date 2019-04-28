package com.example.sch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.sch.LoginActivity.log;

public class ScheduleFragment extends Fragment implements View.OnClickListener {

    ArrayList<Lesson [][]> lessons = new ArrayList<>();
    int index = 0;
    int day = 6;
    TextView []tv = new TextView[7];
    Button btn1,btn2;

    String []days = {"пн","вт","ср","чт","пт","сб","вс"};
    String COOKIE, ROUTE;
    int USER_ID;
    TableLayout tableLayout, tableLayout1;
    Long d = 86400000L;
    Long d1, d2;
    String date;

    public ScheduleFragment () {
        lessons.add(new Lesson[6][6]);
    }

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void start() {
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd MM yyyy", Locale.ENGLISH);
        Date currentTime1 = Calendar.getInstance().getTime();
        date = dateFormat1.format(currentTime1);

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE", Locale.ENGLISH);
        Date currentTime2 = Calendar.getInstance().getTime();
        String dayOfTheWeek = dateFormat2.format(currentTime2);
        switch (dayOfTheWeek) {
            case "Mon":
                day = 1;
                break;
            case "Tue":
                day = 2;
                break;
            case "Wed":
                day = 3;
                break;
            case "Thu":
                day = 4;
                break;
            case "Fri":
                day = 5;
                break;
            case "Sat":
                day = 6;
                break;
            case "Sun":
                day = 7;
                break;
        }
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd MM yyyy", Locale.ENGLISH);
        try {
            d1 = new SimpleDateFormat("dd MM yyyy").parse(String.valueOf(formatForDateNow.format(dateNow))).getTime() - (day - 1) * d;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        d2 = d1 + d * 6;
        sasha(dayOfTheWeek);
        sasha(String.valueOf(dateNow));
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    Download(0);
                } catch (Exception e) {
                }
            }
        }.start();
    }

    @SuppressLint("ResourceType")
    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule,container,false);
        tableLayout = v.findViewById(R.id.table);
        tableLayout.setStretchAllColumns(true);
        tableLayout1 = v.findViewById(R.id.table1);
        tableLayout1.setColumnStretchable(1,true);
        tableLayout1.setColumnShrinkable(1,true);

        btn1 = v.findViewById(R.id.button1);
        btn2 = v.findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        btn1.setBackgroundColor(Color.TRANSPARENT);
        btn2.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout linearLayout = new LinearLayout(getActivity().getApplicationContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(1);

        TableRow tbrow1 = new TableRow(getActivity().getApplicationContext());
        tbrow1.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < 7; i++) {
            tv[i] = new TextView(getActivity().getApplicationContext());
            tv[i].setId(i);
            tv[i].setGravity(Gravity.CENTER);
            tv[i].setText(days[i]);
            tv[i].setTextColor(Color.WHITE);
            tv[i].setTextSize(20);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = (float) 1 / 7;
            tv[i].setLayoutParams(p);
            final int finalI = i;
            tv[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    day = finalI + 1;
                    okras(tv);
                    tv[finalI].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
                    tv[finalI].setTextColor(Color.BLACK);
                    tableLayout1.removeAllViews();
                    CreateTable();
                }
            });
            linearLayout.addView(tv[i]);
        }

        tv[day-1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
        tv[day - 1].setTextColor(Color.BLACK);
        tableLayout.addView(linearLayout);
        CreateTable();
        return v;
    }

    public void okras(TextView [] tv){
        for (int i = 0; i < 7; i++) {
            tv[i].setBackground(null);
            tv[i].setTextColor(Color.WHITE);
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.O)
    void Download(int index) {
        try {
            COOKIE = TheSingleton.getInstance().getCOOKIE();
            ROUTE = TheSingleton.getInstance().getROUTE();
            USER_ID = TheSingleton.getInstance().getUSER_ID();
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    lessons.get(index)[i][j] = new Lesson();
                }
            }
            URL url = new URL("https://app.eschool.center/ec-server/student/diary?" +
                    "userId=" + USER_ID + "&d1=" + d1 + "&d2=" + d2);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("Cookie", COOKIE + "; route=" + ROUTE + "; _pk_ses.1.81ed=*;" +
                    " site_ver=app; _pk_id.1.81ed=de563a6425e21a4f.1553009060." +
                    "16.1554146944.1554139340.");
            System.out.println(con.getResponseCode() + "");
            System.out.println(con.getResponseMessage());

            StringBuilder result = new StringBuilder();

            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            JSONObject obj1 = new JSONObject(result.toString());
            JSONArray array1 = obj1.getJSONArray("lesson");
            JSONArray array2 = obj1.getJSONArray("user");
            JSONObject object = array2.getJSONObject(0);
            JSONArray array3 = object.getJSONArray("mark");
            ArrayList<Mark> marks = new ArrayList<>();
            for (int i = 0; i < array3.length(); i++) {
                JSONObject object1 = array3.getJSONObject(i);
                //marks.add(new Mark(object1.getString("value"), 1, object1.getLong("lessonID")));
            }
            int numinday;
            Long day1;
            Long date1;
            int isODOD;
            String namesubject = "";
            String topic = "";
            String nameteacher = "";
            HomeWork homeWork = new HomeWork("");
            Long id1;

            for (int i = 0; i < array1.length(); i++) {
                obj1 = array1.getJSONObject(i);
                date1 = Long.valueOf(String.valueOf(obj1.getString("date")));
                id1 = obj1.getLong("id");
                isODOD = obj1.getInt("isODOD");
                if (isODOD != 1) {
                    JSONArray array11 = obj1.getJSONArray("part");
                    try {
                        JSONObject obj2 = array11.getJSONObject(0);
                        homeWork = new HomeWork(obj2.getJSONArray("variant").getJSONObject(0).getString("text"));
                    } catch (Exception e) {
                    }
                    day1 = (date1 - d1) / d;
                    numinday = obj1.getInt("numInDay");
                    JSONObject obj11 = obj1.getJSONObject("unit");
                    JSONObject obj12 = obj1.getJSONObject("tp");
                    JSONObject obj13 = obj1.getJSONObject("teacher");
                    if (obj11.has("name")) {
                        namesubject = obj11.getString("name");
                    }
                    if (obj12.has("topicName")) {
                        topic = obj12.getString("topicName");
                    }
                    if (obj13.has("factTeacherIN")) {
                        nameteacher = obj13.getString("factTeacherIN");
                    }
                    if (i == 17) {
                        numinday += 1;
                    }
                    lessons.get(index)[day1.intValue()][numinday - 1] = new Lesson(id1, numinday, (int) (day1 + 1), namesubject, nameteacher, topic, homeWork);
                    homeWork = new HomeWork(" ");
                }
            }
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Lesson lesson = lessons.get(index)[i][j];
                    Long id2 = lesson.id;
                    for (int k = 0; k < marks.size(); k++) {
                        Mark mark = marks.get(k);
                        if (id2 - mark.idlesson == 0) {
                            lessons.get(index)[i][j].marks.add(mark);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void CreateTable(){
        for (int i = 0; i < 6; i++) {
            TableRow tbrow = new TableRow(getActivity().getApplicationContext());
            tbrow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            tbrow.setBaselineAligned(false);

            TextView tv2 = new TextView(getActivity().getApplicationContext());
            tv2.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView tv1 = new TextView(getActivity().getApplicationContext());
            tv1.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            TextView tv3 = new TextView(getActivity().getApplicationContext());
            tv3.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT));

            tv2.setGravity(Gravity.CENTER_VERTICAL);
            tv1.setGravity(Gravity.CENTER);
            tv3.setGravity(Gravity.CENTER);

            tv1.setId(i);
            tv1.setTextColor(Color.WHITE);
            tv2.setId(i);
            tv3.setId(i);

            final int finalI = i;
            try {
                tbrow.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        DayFragment fragment = new DayFragment();
                        transaction.replace(R.id.frame, fragment);
                        try {
                            fragment.homework = lessons.get(index)[day - 1][finalI].homeWork.stringwork;
                            fragment.teachername = lessons.get(index)[day - 1][finalI].teachername;
                            fragment.topic = lessons.get(index)[day - 1][finalI].topic;
                            fragment.marks = lessons.get(index)[day - 1][finalI].marks;
                        } catch (Exception e) {
                        }
                        transaction.addToBackStack(null);
                        transaction.commit();
                        return false;
                    }
                });
            } catch (Exception e) {
            }
            tv1.setBackground(getResources().getDrawable(R.drawable.cell_phone));
            tv2.setBackground(getResources().getDrawable(R.drawable.cell_phone));
            tv3.setBackground(getResources().getDrawable(R.drawable.cell_phone));

            try {
                tv1.setText(String.valueOf(lessons.get(index)[day - 1][i].numInDay));
                String s = lessons.get(index)[day - 1][i].name + "\n" + lessons.get(index)[day - 1][i].homeWork.stringwork;
                Spannable spans = new SpannableString(s);
                spans.setSpan(new RelativeSizeSpan(1.5f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv2.setText(spans);
            }catch (Exception e){}
            tv1.setPadding(15, 0, 15, 0);
            tv2.setPadding(30,30,30,30);
            tv3.setPadding(30, 0, 30, 0);
            tv2.setMaxLines(2);
            tv2.setEllipsize(TextUtils.TruncateAt.END);
            try {
                StringBuilder s1 = new StringBuilder();
                for (int j = 0; j < lessons.get(index)[day - 1][i].marks.size(); j++) {
                    s1.append(lessons.get(index)[day - 1][i].marks.get(j).value);
                    if (lessons.get(index)[day - 1][i].marks.size() > 1 && j != lessons.get(index)[day - 1][i].marks.size() - 1) {
                        s1.append("/");
                    }
                }
                s1.append("\n");
                for (int j = 0; j < lessons.get(index)[day - 1][i].marks.size(); j++) {
                    s1.append(lessons.get(index)[day - 1][i].marks.get(j).coefficient);
                    if (lessons.get(index)[day - 1][i].marks.size() > 1 && j != lessons.get(index)[day - 1][i].marks.size() - 1) {
                        s1.append("/");
                    }
                }
                Spannable spans1 = new SpannableString(s1.toString());
                spans1.setSpan(new RelativeSizeSpan(1.4f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv3.setText(spans1);
            }catch (Exception ignored){}
            tbrow.addView(tv1);
            tbrow.addView(tv2);
            tbrow.addView(tv3);
            tableLayout1.addView(tbrow);
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

    //@TargetApi(Build.VERSION_CODES.O)
    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button1:
                day--;
                if(day<1){
                    index--;
                    if(index < 0){
                        index = 0;
                        d1 = d1-7*d;
                        d2 = d1+6*d;
                        lessons.add(0, new Lesson[6][6]);
                        new Thread() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                try {
                                    Download(index);
                                } catch(Exception e) {

                                }
                            }
                        }.start();

                    }
                    day = 7;
                }
                okras(tv);
                tv[day - 1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
                tv[day - 1].setTextColor(Color.BLACK);
                tableLayout1.removeAllViews();
                CreateTable();
                sasha(String.valueOf(index));
                break;
            case R.id.button2:
                day++;
                if(day>7){
                    index++;
                    if(index > lessons.size()-1){
                        d1 = d1-7*d;
                        d2 = d1+6*d;
                        lessons.add(new Lesson[6][6]);
                        new Thread() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                try {
                                    Download(index);
                                } catch(Exception e) {

                                }
                            }
                        }.start();
                    }
                    day = 1;
                }
                okras(tv);
                tv[day - 1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
                tv[day - 1].setTextColor(Color.BLACK);
                tableLayout1.removeAllViews();
                CreateTable();
                sasha(String.valueOf(index));
                break;
        }
    }

    static class Lesson {
        int numInDay, numDay;
        String name = "", teachername = "", topic = "", shortname = "";
        HomeWork homeWork;
        ArrayList<Mark> marks = new ArrayList<>();
        Long id;
        long unitId = 0;

        Lesson(Long id, int numInDay, int numDay, String name, String teachername, String topic, HomeWork homeWork) {
            this.numInDay = numInDay;
            this.numDay = numDay;
            this.name = name;
            this.teachername = teachername;
            this.topic = topic;
            this.homeWork = homeWork;
            this.id = id;
        }

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
}