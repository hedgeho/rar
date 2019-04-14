package com.example.sch;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    Long d = Long.valueOf(86400000);
    Long d1 = 1554670800000L, d2 = d1 + d * 6;

    public ScheduleFragment () {
        lessons.add(new Lesson[6][6]);
    }

    public void start() {
        SimpleDateFormat sdf = new SimpleDateFormat("");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    Download(0);
                } catch(Exception e) {

                }
            }
        }.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedue,container,false);
        v.setBackgroundColor(Color.WHITE);
        tableLayout = v.findViewById(R.id.table);
        tableLayout.setStretchAllColumns(true);
        tableLayout1 = v.findViewById(R.id.table1);
        tableLayout1.setColumnStretchable(1,true);
        tableLayout1.setColumnShrinkable(1,true);

        btn1 = v.findViewById(R.id.button1);
        btn2 = v.findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        TableRow tbrow1 = new TableRow(getActivity().getApplicationContext());
        tbrow1.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < 7; i++) {
            tv[i] = new TextView(getActivity().getApplicationContext());
            tv[i].setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            tv[i].setId(i);
            tv[i].setGravity(Gravity.CENTER);
            tv[i].setText(days[i]);
            tv[i].setTextSize(15);
            tbrow1.addView(tv[i]);
        }

        tv[day-1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
        tv[day-1].setBackgroundColor(getResources().getColor(R.color.five));
        tableLayout.addView(tbrow1);
        CreateTable();
        return v;
    }
    public void okras(TextView [] tv){
        for (int i = 0; i < 7; i++) {
            tv[i].setBackgroundColor(Color.WHITE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void CreateTable(){
        for (int i = 0; i < 6; i++) {
            TableRow tbrow = new TableRow(getActivity().getApplicationContext());
            tbrow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            TextView tv1 = new TextView(getActivity().getApplicationContext());
            tv1.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            TextView tv2 = new TextView(getActivity().getApplicationContext());
            tv2.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT));

            TextView tv3 = new TextView(getActivity().getApplicationContext());
            tv2.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT));

            tv2.setGravity(Gravity.CENTER_VERTICAL);
            tv1.setGravity(Gravity.CENTER);
            tv3.setGravity(Gravity.CENTER_HORIZONTAL);

            tv1.setId(i);
            tv1.setTextColor(getResources().getColor(R.color.five));
            tv2.setId(i);
            tv3.setId(i);

            tbrow.setOnClickListener(this);

            tv1.setBackground(getResources().getDrawable(R.drawable.cell_phone));
            tv2.setBackground(getResources().getDrawable(R.drawable.cell_phone));
            tv3.setBackground(getResources().getDrawable(R.drawable.cell_phone));

            try {
                tv1.setText(String.valueOf(lessons.get(index)[day - 1][i].numInDay));
                String s = lessons.get(index)[day - 1][i].name + "\n" + lessons.get(index)[day - 1][i].homeWork.stringwork;
                Spannable spans = new SpannableString(s);
                spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv2.setText(spans);
            }catch (Exception e){}
            tv1.setHeight(180);
            tv1.setWidth(50);
            tv3.setHeight(180);
            tv3.setWidth(170);
            tv2.setHeight(180);
            tv2.setPadding(30,30,30,30);
            tv3.setPadding(0,70,0,0);
            try {
                StringBuilder s1 = new StringBuilder();
                for (int j = 0; j < lessons.get(index)[day - 1][i].marks.size(); j++) {
                    s1.append(String.valueOf(lessons.get(index)[day - 1][i].marks.get(j).value));
                    if (lessons.get(index)[day - 1][i].marks.size() > 1 && j != lessons.get(index)[day - 1][i].marks.size() - 1) {
                        s1.append("/");
                    }
                }
                s1.append("\n");
                for (int j = 0; j < lessons.get(index)[day - 1][i].marks.size(); j++) {
                    s1.append(String.valueOf(lessons.get(index)[day - 1][i].marks.get(j).coefficient));
                    if (lessons.get(index)[day - 1][i].marks.size() > 1 && j != lessons.get(index)[day - 1][i].marks.size() - 1) {
                        s1.append("/");
                    }
                }
                Spannable spans1 = new SpannableString(s1.toString());
                spans1.setSpan(new RelativeSizeSpan(1.2f), 0,s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans1.setSpan(new RelativeSizeSpan(0.9f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv3.setText(spans1);
            }catch (Exception e){}

            tbrow.addView(tv1);
            tbrow.addView(tv2);
            tbrow.addView(tv3);

            tableLayout1.addView(tbrow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    void Download(int index){
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
                marks.add(new Mark(Integer.valueOf(object1.getString("value")), 1, object1.getLong("lessonID")));
            }
            int numinday;
            Long day;
            Long date;
            int isODOD;
            String namesubject = "";
            String topic = "";
            String nameteacher = "";
            HomeWork homeWork = new HomeWork("");
            Long id;

            for (int i = 0; i < array1.length(); i++) {
                obj1 = array1.getJSONObject(i);
                date = Long.valueOf(String.valueOf(obj1.getString("date")));
                id = obj1.getLong("id");
                isODOD = obj1.getInt("isODOD");
                if (isODOD != 1) {
                    JSONArray array11 = obj1.getJSONArray("part");
                    try {
                        JSONObject obj2 = array11.getJSONObject(0);
                        homeWork = new HomeWork(obj2.getJSONArray("variant").getJSONObject(0).getString("text"));
                    } catch (Exception e) {
                    }
                    day = (date - d1) / d;
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
                    lessons.get(index)[Math.toIntExact(day)][numinday - 1] = new Lesson(id, numinday, (int) (day + 1), namesubject, nameteacher, topic, homeWork);
                    homeWork = new HomeWork(" ");
                }
            }
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    Lesson lesson = lessons.get(index)[i][j];
                    Long id1 = lesson.id;
                    for (int k = 0; k < marks.size(); k++) {
                        Mark mark = marks.get(k);
                        System.out.println(id1 - mark.idlesson);
                        if (id1 - mark.idlesson == 0) {
                            lessons.get(index)[i][j].marks.add(mark);
                        }
                    }
                }
            }
        } catch (Exception e){}
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
                        System.out.println(lessons.size());
                    }
                    day = 7;
                }
                okras(tv);
                tv[day-1].setBackgroundColor(getResources().getColor(R.color.four));
                tableLayout1.removeAllViews();
                CreateTable();
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
                        System.out.println(lessons.size());
                    }
                    day = 1;
                }
                okras(tv);
                tv[day-1].setBackgroundColor(getResources().getColor(R.color.four));
                tableLayout1.removeAllViews();
                CreateTable();
                break;
            default:
                break;
        }
    }

    class Lesson {
        int numInDay,numDay;
        String name,teachername,topic;
        HomeWork homeWork;
        ArrayList<Mark> marks = new ArrayList<>();
        Long id;

        Lesson(Long id, int numInDay, int numDay, String name, String teachername, String topic,HomeWork homeWork){
            this.numInDay = numInDay;
            this.numDay = numDay;
            this.name = name;
            this.teachername = teachername;
            this.topic = topic;
            this.homeWork = homeWork;
            this.id = id;
        }
        Lesson(){}

    }
    class HomeWork {
        int []idfils;
        String stringwork;

        HomeWork(String a){
            stringwork = a;
        }
        HomeWork(){ }
    }
    class Mark{
        int value;
        double coefficient;
        Long idlesson;
        Mark(int value, double coefficient, Long idlesson){
            this.value = value;
            this.coefficient = coefficient;
            this.idlesson = idlesson;
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
    static void sashs(String s){
        Log.v("sasha", s);
    }

}
