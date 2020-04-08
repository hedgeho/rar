package ru.gurhouse.sch;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;

public class TotalMarks extends Fragment {

    TableLayout tableLayout;
    static int yearid;
    static ArrayList<Subject> subjectsList;
    static ArrayList<Period> periodsList;

    public TotalMarks() {

    }

    static int USER_ID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_total_marks, container, false);
        tableLayout = v.findViewById(R.id.table);
        draw();
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle(ScheduleFragment.yearname + "");
        return v;
    }

    void start() {
        USER_ID = TheSingleton.getInstance().getUSER_ID();
    }

    void draw() {
        if(subjectsList == null || periodsList == null)
            getActivity().onBackPressed();
        TableRow row = new TableRow(getContext());
        TextView tv = new TextView(getContext());
        row.addView(tv);
        for (int i = 0; i < periodsList.size(); i++) {
            tv = new VerticalTextView(getContext());
            tv.setText(periodsList.get(i).name);
            tv.setPadding(8, 8, 8, 8);
            row.addView(tv);
        }
        row.setPadding(0, 8, 0, 8);
        tableLayout.addView(row);
        for (int i = 0; i < subjectsList.size(); i++) {
            row = new TableRow(getContext());
            tv = new TextView(getContext());
            tv.setText(subjectsList.get(i).name);
            row.addView(tv);
            for (int j = 0; j < periodsList.size(); j++) {
                tv = new TextView(getContext());
                if(subjectsList.get(i).marks[j] == 0)
                    tv.setText("-");
                else
                    tv.setText(subjectsList.get(i).marks[j] + "");
                tv.setPadding(32, 8, 32, 8);
                row.addView(tv);
            }
            tableLayout.addView(row);
        }
    }

    static void Download3(Context context) {
        log("Download3()");
        new Thread(() -> {
            try {
                String str = connect("https://app.eschool.center/ec-server/yearplan/academyears",
                        null, context);
//                    if(str.charAt(0) == '/') {
//                        Download3();
//                        return;
                // todo handle connect errors
//                    }
                JSONArray array1 = new JSONArray(str);
                JSONObject object1;
                for (int i = 0; i < array1.length(); i++) {
                    object1 = array1.getJSONObject(i);
                    if (object1.getInt("y1") == ScheduleFragment.yearname) {
                        yearid = object1.getInt("yearId");
                        break;
                    }
                }
                JSONObject object = new JSONObject(
                        connect("https://app.eschool.center/ec-server/student/getTotalMarks?yearid=" + yearid + "&userid=" + USER_ID + "&json=true",
                                null, context));
                object = object.getJSONObject("RegisterOfClass");
                object = object.getJSONObject("User");
                JSONArray array = object.getJSONArray("Result");
                subjectsList = new ArrayList<>();
                periodsList = new ArrayList<>();
                Subject subject;
                Period period;
                for (int i = 0; i < array.length(); i++) {
                    object = array.getJSONObject(i);
                    subject = new Subject();
                    subject.unitId = object.getInt("UnitID");
                    subject.name = object.getString("UnitName");
                    boolean contains = false;
                    for (Subject s : subjectsList) {
                        if(s.unitId == subject.unitId) {
                            contains = true;
                            break;
                        }
                    }
                    if(!contains) {
                        subjectsList.add(subject);
                    }

                    period = new Period();
                    period.num = object.getInt("PeriodNum");
                    period.name = object.getString("PeriodName");
                    contains = false;
                    for (Period p : periodsList) {
                        if(p.num == period.num) {
                            contains = true;
                            break;
                        }
                    }
                    if(!contains) {
                        periodsList.add(period);
                    }
                }
                Collections.sort(periodsList, (p1, p2) -> Integer.compare(p1.num, p2.num));
                Collections.sort(subjectsList, (s1, s2) -> Integer.compare(s1.unitId, s2.unitId));
                for (Subject s : subjectsList) {
                    s.marks = new int[periodsList.size()];
                }
                for (int i = 0; i < array.length(); i++) {
                    object = array.getJSONObject(i);
                    for (Subject s : subjectsList) {
                        if (s.unitId == object.getInt("UnitID")) {
                            for (int j = 0; j < periodsList.size(); j++) {
                                if(periodsList.get(j).num == object.getInt("PeriodNum")) {
                                    s.marks[j] = object.getInt("Value5");
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

//                getActivity().runOnUiThread(this::draw);
            } catch (Exception e) {
                loge(e);
            }
        }).start();
    }

    static class Subject {
        int unitId;
        String name;
        int[] marks;
    }
    static class Period {
        int num;
        String name;
    }
    public class VerticalTextView extends android.support.v7.widget.AppCompatTextView {

        public VerticalTextView(Context context) {
            super(context);
        }

        public VerticalTextView(Context context, AttributeSet attrs){
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
            super.onMeasure(heightMeasureSpec, widthMeasureSpec);
            setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        }

        @Override
        protected void onDraw(Canvas canvas){
            TextPaint textPaint = getPaint();
            textPaint.setColor(getCurrentTextColor());
            textPaint.drawableState = getDrawableState();

            canvas.save();


            canvas.translate(0, getHeight());
            canvas.rotate(-90);


            canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

            getLayout().draw(canvas);
            canvas.restore();
        }
    }
}
