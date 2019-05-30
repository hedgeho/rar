package com.example.sch;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.sch.LoginActivity.connect;
import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class ScheduleFragment extends Fragment implements DatePickerDialog.OnDateSetListener {


    boolean READY = false;
    boolean shown = false;
    static int pageCount = 10001;
    boolean ready = false;
    String[] period;
    boolean first = true;
    private String COOKIE, ROUTE;
    private int USER_ID;

    TextView []tv = new TextView[7];
    int day;
    Date datenow;
    int pernum;
    String[] days1 = {"пн", "вт", "ср", "чт", "пт", "сб", "вс"};
    ViewPager pager;
    PagerAdapter pagerAdapter;
    LinearLayout linear1;
    int lastposition;
    ArrayList<PageFragment> pageFragments;
    View v;
    DatePickerDialog datePickerDialog;
    int[] week;
    int yearname = 2018;
    Period[] periods = new Period[7];
    private int startYear = 2019;
    private int starthMonth = 1;
    private int startDay = 1;

    public ScheduleFragment () {
        datenow = new Date();
        period = new String[7];
        Calendar c = Calendar.getInstance();
        c.setTime(datenow);
        c.add(Calendar.DAY_OF_WEEK, -1);
        day = c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DAY_OF_WEEK, 1);
        week = new int[7];

        for (int i = 0; i < 7; i++) {
            periods[i] = new Period();
        }

        sasha("Day of week:" + day + "  " + datenow);
        pageFragments = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            pageFragments.add(new PageFragment());
            Calendar[] calendar = {Calendar.getInstance()};
            calendar[0] = Calendar.getInstance();
            calendar[0].setTime(datenow);
            calendar[0].add(Calendar.DAY_OF_MONTH, (i - (pageCount / 2) + 1));
            calendar[0].set(Calendar.HOUR_OF_DAY, 0);
            calendar[0].set(Calendar.MINUTE, 0);
            calendar[0].set(Calendar.SECOND, 0);
            calendar[0].set(Calendar.MILLISECOND, 0);
            pageFragments.get(i).c = calendar[0];
            calendar[0].add(Calendar.DAY_OF_WEEK, -1);
            pageFragments.get(i).dayofweek = calendar[0].get(Calendar.DAY_OF_WEEK);
        }
    }

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    void start() {
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        USER_ID = TheSingleton.getInstance().getUSER_ID();
        Download1();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("oncreateview");
        if(v == null)
            v = inflater.inflate(R.layout.fragment_schedule, container, false);
        if(!READY) {
            v = inflater.inflate(R.layout.fragment_schedule, container, false);
            return v;
        }
        if(!shown)
        log("oncreateview2");
        return v;
    }

    void show() {
        shown = true;
        log("show");
        try {
            for (int i = 0; i < pageCount; i++) {
                pageFragments.get(i).subjects = periods[pernum].subjects;
            }
            int y = 0;
            try {
                Long daymsec = periods[pernum].days.get(y).daymsec;
                for (int i = 0; i < pageCount; i++) {
                    if (pageFragments.get(i).c.getTimeInMillis() - daymsec == 0) {
                        pageFragments.get(i).day = periods[pernum].days.get(y);
                        if (y + 1 - periods[pernum].days.size() == 0) {
                            break;
                        } else {
                            y++;
                            daymsec = periods[pernum].days.get(y).daymsec;
                        }
                    }
                }
            } catch (Exception e) {
                sasha(String.valueOf(e));
            }
            pager = v.findViewById(R.id.pager);
            pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
            pager.setAdapter(pagerAdapter);
            pager.setCurrentItem(pageCount / 2 + 1);
            lastposition = pager.getCurrentItem();
            int w;
            if (pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                w = 7;
            } else
                w = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_WEEK) - 1;
            sasha("hshs " + pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_MONTH) + " " + w);

            for (int i = w - 1; i < 7; i++) {
                week[i] = pager.getCurrentItem() - 1 + i - w + 1 + 1;
            }
            for (int i = w - 2; i > -1; i--) {
                week[i] = pager.getCurrentItem() - 1 + i - w + 1 + 1;
            }
            for (int i = 0; i < 7; i++) {
                sasha(week[i] + "SSS");
            }
            sasha(lastposition + " last " + pageFragments.get(lastposition - 1).c.get(Calendar.DAY_OF_WEEK) + " " + pageFragments.get(lastposition - 1).c.get(Calendar.DAY_OF_MONTH));
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    okras(tv);
                    int w;
                    if (pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                        w = 7;
                    } else
                        w = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_WEEK) - 1;
                    sasha("hshs " + pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_MONTH) + " " + w);

                    for (int i = w - 1; i < 7; i++) {
                        week[i] = pager.getCurrentItem() - 1 + i - w + 1 + 1;
                    }
                    for (int i = w - 2; i > -1; i--) {
                        week[i] = pager.getCurrentItem() - 1 + i - w + 1 + 1;
                    }
                    for (int i = 0; i < 7; i++) {
                        sasha(week[i] + "SSS");
                    }
                    for (int i = 0; i < 7; i++) {
                        String s = days1[i] + "\n" + pageFragments.get(week[i] - 1).c.get(Calendar.DAY_OF_MONTH);
                        Spannable spans = new SpannableString(s);
                        spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans.setSpan(new ForegroundColorSpan(Color.WHITE), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv[i].setText(spans);
                    }
                    tv[w - 1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
                    String s = days1[w - 1] + "\n" + pageFragments.get(week[w - 1] - 1).c.get(Calendar.DAY_OF_MONTH);
                    Spannable spans = new SpannableString(s);
                    spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    spans.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    spans.setSpan(new ForegroundColorSpan(Color.BLACK), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv[w - 1].setText(spans);
                    lastposition = pager.getCurrentItem();
                    startDay = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_MONTH);
                    starthMonth = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.MONTH);
                    startYear = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.YEAR);
                    datePickerDialog = new DatePickerDialog(getContext(), ScheduleFragment.this, startYear, starthMonth, startDay);
                    Calendar c = Calendar.getInstance();
                    c.setTime(datenow);
                    c.set(Calendar.MONTH, 9);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    c.add(Calendar.YEAR, -1);
                    long minDate = c.getTime().getTime();
                    c.setTime(datenow);
                    c.set(Calendar.MONTH, 9);
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    c.add(Calendar.MONTH, 1);
                    long maxDate = c.getTime().getTime();
                    datePickerDialog.getDatePicker().setMaxDate(maxDate);
                    datePickerDialog.getDatePicker().setMinDate(minDate);
                    datePickerDialog.getDatePicker().setSpinnersShown(true);
                    day = w;
                    sasha(String.valueOf(position));
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            startDay = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_MONTH);
            starthMonth = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.MONTH);
            startYear = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.YEAR);
            datePickerDialog = new DatePickerDialog(getContext(), ScheduleFragment.this, startYear, starthMonth, startDay);
            Calendar c = Calendar.getInstance();
            c.setTime(datenow);
            c.set(Calendar.MONTH, 8);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.add(Calendar.YEAR, -1);
            long minDate = c.getTime().getTime();
            c.setTime(datenow);
            c.set(Calendar.MONTH, 9);
            c.set(Calendar.DAY_OF_MONTH, 0);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.add(Calendar.MONTH, 1);
            long maxDate = c.getTime().getTime();
            datePickerDialog.getDatePicker().setMaxDate(maxDate);
            datePickerDialog.getDatePicker().setMinDate(minDate);
            datePickerDialog.getDatePicker().setSpinnersShown(true);
            linear1 = v.findViewById(R.id.liner1);
            linear1.setWeightSum(1);
            for (int i = 0; i < 7; i++) {
                tv[i] = new TextView(getContext());
                tv[i].setId(i);
                tv[i].setGravity(Gravity.CENTER);
                tv[i].setTextColor(Color.WHITE);

                String s = days1[i] + "\n" + pageFragments.get(week[i] - 1).c.get(Calendar.DAY_OF_MONTH);
                Spannable spans = new SpannableString(s);
                spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.WHITE), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv[i].setText(spans);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p.weight = (float) 1 / 7;
                tv[i].setLayoutParams(p);
                final int finalI = i;
                tv[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        pager.setCurrentItem(pager.getCurrentItem() - (day - finalI - 1));
                    }
                });
                linear1.addView(tv[i]);
            }
            tv[day - 1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
            String s = days1[day - 1] + "\n" + pageFragments.get(week[day - 1] - 1).c.get(Calendar.DAY_OF_MONTH);
            Spannable spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.BLACK), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv[day - 1].setText(spans);
            first = false;
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setTitle("Дневник");
            setHasOptionsMenu(true);
            ((MainActivity) getActivity()).setSupportActionBar(toolbar);
            v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.layout_fragment_bp_list).setVisibility(View.VISIBLE);
            log("end");
        } catch (Exception e) {
            loge("show: " + e.toString());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
                    toolbar.setTitle("Дневник");
                    toolbar.setClickable(false);
                    setHasOptionsMenu(true);
                    ((MainActivity) getActivity()).setSupportActionBar(toolbar);
                    v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    v.findViewById(R.id.layout_fragment_bp_list).setVisibility(View.VISIBLE);
                }
            });
        }
    }
    public void okras(TextView [] tv){
        for (int i = 0; i < 7; i++) {
            tv[i].setBackground(null);
            tv[i].setTextColor(Color.WHITE);
            String s = days1[i] + "\n" + pageFragments.get(week[i] - 1).c.get(Calendar.DAY_OF_MONTH);
            Spannable spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.WHITE), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv[i].setText(spans);
        }
    }

    void Download1() {
        new Thread() {
            @SuppressLint("SimpleDateFormat")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    sasha("here if fuck");
                    JSONArray array3 = new JSONArray(
                            connect("https://app.eschool.center/ec-server/dict/periods2?year=" + yearname,
                            null, getContext()));
                    for (int i = 0; i < array3.length(); i++) {
                        if (array3.getJSONObject(i).getInt("typeId") == 1) {
                            JSONArray array4 = array3.getJSONObject(i).getJSONArray("items");
                            for (int j = 0; j < array4.length(); j++) {
                                JSONObject ob = array4.getJSONObject(j);
                                if (ob.getString("typeCode").equals("Y")) {
                                    periods[0].datefinish = ob.getLong("date2");
                                    periods[0].datestart = ob.getLong("date1");
                                    periods[0].name = ob.getString("name");
                                    periods[0].id = ob.getInt("id");
                                    period[0] = periods[0].name;
                                } else if (ob.getString("typeCode").equals("HY")) {
                                    if (ob.getInt("num") - 1 == 0) {
                                        periods[1].datefinish = ob.getLong("date2");
                                        periods[1].datestart = ob.getLong("date1");
                                        periods[1].name = ob.getString("name");
                                        periods[1].id = ob.getInt("id");
                                        period[1] = periods[1].name;
                                    } else {
                                        periods[2].datefinish = ob.getLong("date2");
                                        periods[2].datestart = ob.getLong("date1");
                                        periods[2].name = ob.getString("name");
                                        periods[2].id = ob.getInt("id");
                                        period[2] = periods[2].name;
                                    }
                                } else if (ob.getString("typeCode").equals("Q")) {
                                    if (ob.getInt("num") - 1 == 0) {
                                        periods[3].datefinish = ob.getLong("date2");
                                        periods[3].datestart = ob.getLong("date1");
                                        periods[3].name = ob.getString("name");
                                        periods[3].id = ob.getInt("id");
                                        period[3] = periods[3].name;
                                    } else if (ob.getInt("num") - 2 == 0) {
                                        periods[4].datefinish = ob.getLong("date2");
                                        periods[4].datestart = ob.getLong("date1");
                                        periods[4].name = ob.getString("name");
                                        periods[4].id = ob.getInt("id");
                                        period[4] = periods[4].name;
                                    } else if (ob.getInt("num") - 3 == 0) {
                                        periods[5].datefinish = ob.getLong("date2");
                                        periods[5].datestart = ob.getLong("date1");
                                        periods[5].name = ob.getString("name");
                                        periods[5].id = ob.getInt("id");
                                        period[5] = periods[5].name;
                                    } else {
                                        periods[6].datefinish = ob.getLong("date2");
                                        periods[6].datestart = ob.getLong("date1");
                                        periods[6].name = ob.getString("name");
                                        periods[6].id = ob.getInt("id");
                                        period[6] = periods[6].name;
                                    }
                                }
                            }
                        }
                    }
                    if (datenow.getTime() >= periods[3].datestart && datenow.getTime() <= periods[3].datefinish) {
                        Download2(periods[3].id, 3);
                    } else if (datenow.getTime() >= periods[4].datestart && datenow.getTime() <= periods[4].datefinish) {
                        Download2(periods[4].id, 4);
                    } else if (datenow.getTime() >= periods[5].datestart && datenow.getTime() <= periods[5].datefinish) {
                        Download2(periods[5].id, 5);
                    } else {
                        Download2(periods[6].id, 6);
                    }
                } catch (Exception e) {
                    sasha(String.valueOf(e));
                }
            }
        }.start();
    }

    void Download2(final int id, final int h) {
        pernum = h;
        periods[pernum].days = new ArrayList<>();
        periods[pernum].subjects = new ArrayList<>();
        periods[pernum].lins = new ArrayList<>();
        periods[pernum].cells = new ArrayList<>();
        new Thread() {
            @SuppressLint("SimpleDateFormat")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    //------------------------------------------------------------------------------------------------
                    JSONObject object = new JSONObject(
                            connect("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=" + id,
                                    null, getContext()));
                    JSONArray array = object.getJSONArray("result");
                    for (int i = 0; i < array.length(); i++) {
                        periods[pernum].subjects.add(new PeriodFragment.Subject());
                        JSONObject obj = array.getJSONObject(i);
                        if (obj.has("overMark")) {
                            double d = obj.getDouble("overMark");
                            String s = String.valueOf(d);
                            if (s.length() > 4) {
                                s = String.format(Locale.UK, "%.2f", d);
                            }
                            periods[pernum].subjects.get(i).avg = Double.valueOf(s);
                        }
                        if (obj.has("totalMark"))
                            periods[pernum].subjects.get(i).totalmark = obj.getString("totalMark");
                        if (obj.has("unitName"))
                            periods[pernum].subjects.get(i).name = obj.getString("unitName");
                        if (obj.has("rating"))
                            periods[pernum].subjects.get(i).rating = obj.getString("rating");
                        if (obj.has("unitId"))
                            periods[pernum].subjects.get(i).unitid = obj.getInt("unitId");
                        periods[pernum].subjects.get(i).cells = new ArrayList<>();
                    }
                    JSONObject object1 = new JSONObject(
                            connect("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=" + id,
                                    null, getContext()));
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
                        periods[pernum].cells.add(call);
                    }

                    String s1 = periods[pernum].cells.get(0).date;
                    String s2 = periods[pernum].cells.get(periods[pernum].cells.size() - 1).date;
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    Long d1 = format.parse(s1).getTime();
                    Long d2 = format.parse(s2).getTime();
                    JSONObject object2 = new JSONObject(
                            connect("https://app.eschool.center/ec-server/student/diary?" +
                                    "userId=" + USER_ID + "&d1=" + d1 + "&d2=" + d2, null, getContext()));
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
                                periods[pernum].days
                                        .add(thisday);
                            }
                            PeriodFragment.Lesson lesson = new PeriodFragment.Lesson();
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
                            lesson.homeWork = new PeriodFragment.HomeWork();
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
                            periods[pernum].days.get(index).lessons.add(lesson);
                        }
                        day1 = date1;
                    }

                    for (int i = 0; i < periods[pernum].days.size(); i++) {
                        for (int j = 0; j < periods[pernum].cells.size(); j++) {
                            s1 = periods[pernum].cells.get(j).date;
                            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                            d1 = format.parse(s1).getTime();
                            if (periods[pernum].cells.get(j).mktWt != 0) {
                                if (periods[pernum].days.get(i).daymsec - d1 == 0 || periods[pernum].days.get(i).daymsec.equals(d1)) {
                                    for (int k = 0; k < periods[pernum].days.get(i).lessons.size(); k++) {
                                        if (periods[pernum].days.get(i).lessons.get(k).id.equals(periods[pernum].cells.get(j).lessonid)) {
                                            PeriodFragment.Mark mark = new PeriodFragment.Mark();
                                            mark.cell = periods[pernum].cells.get(j);
                                            mark.idlesson = periods[pernum].cells.get(j).lessonid;
                                            mark.coefficient = periods[pernum].cells.get(j).mktWt;
                                            if (periods[pernum].cells.get(j).markvalue != null)
                                                mark.value = periods[pernum].cells.get(j).markvalue;
                                            else
                                                mark.value = "";
                                            mark.teachFio = periods[pernum].cells.get(j).teachFio;
                                            mark.markdate = periods[pernum].cells.get(j).markdate;
                                            mark.date = periods[pernum].cells.get(j).date;

                                            mark.topic = periods[pernum].cells.get(j).lptname;
                                            mark.unitid = periods[pernum].cells.get(j).unitid;
                                            for (int l = 0; l < periods[pernum].subjects.size() - 1; l++) {
                                                if (periods[pernum].subjects.get(l).unitid == mark.unitid) {
                                                    periods[pernum].subjects.get(l).cells.add(periods[pernum].cells.get(j));
                                                    if (periods[pernum].subjects.get(l).shortname != null)
                                                        periods[pernum].subjects.get(l).shortname = periods[pernum].days.get(i).lessons.get(k).shortname;
                                                    if (periods[pernum].days.get(i).lessons.get(k).shortname.equals("Обществозн."))
                                                        periods[pernum].subjects.get(l).shortname = "Общест.";
                                                    if (periods[pernum].days.get(i).lessons.get(k).shortname.equals("Физ. культ."))
                                                        periods[pernum].subjects.get(l).shortname = "Физ-ра";
                                                    if (periods[pernum].days.get(i).lessons.get(k).shortname.equals("Инф. и ИКТ"))
                                                        periods[pernum].subjects.get(l).shortname = "Информ.";
                                                }
                                            }
                                            periods[pernum].days.get(i).lessons.get(k).marks.add(mark);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ready = true;
                    for (int i = 0; i < periods[pernum].days.size(); i++) {
                        sasha(periods[pernum].days.get(i).day);
                        if (periods[pernum].days.get(i) != null) {
                            LinearLayout lin = new LinearLayout(getContext());
                            boolean ask = true;
                            lin.setOrientation(LinearLayout.VERTICAL);
                            lin.setGravity(Gravity.CENTER);
                            if (i + 1 - periods[pernum].days.size() != 0) {
                                lin.setBackground(getResources().getDrawable(R.drawable.cell_phone2));
                            }
                            lin.setPadding(10, 0, 10, 0);

                            LinearLayout lin2 = new LinearLayout(getContext());
                            lin2.setOrientation(LinearLayout.HORIZONTAL);
                            lin2.setGravity(Gravity.CENTER);
                            TextView txt = new TextView(getActivity().getApplicationContext());
                            txt.setGravity(Gravity.CENTER);
                            txt.setTextSize(20);
                            String s = "";
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 10, 10);
                            txt.setLayoutParams(lp);
                            try {
                                Date date = new Date();
                                date.setTime(periods[pernum].days.get(i).daymsec);
                                format = new SimpleDateFormat("dd.MM", Locale.ENGLISH);
                                s = format.format(date);
                            } catch (Exception e) {
                                sasha("this " + e);
                            }
                            sasha(s);
                            txt.setText(s);
                            txt.setTextColor(Color.LTGRAY);
                            lin2.addView(txt);
                            lin.addView(lin2);
                            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp2.setMargins(10, 0, 10, 0);
                            lin.setLayoutParams(lp2);
                            for (int j = 0; j < periods[pernum].subjects.size() - 1; j++) {
                                LinearLayout lin1 = new LinearLayout(getContext());
                                lin1.setOrientation(LinearLayout.HORIZONTAL);
                                lin1.setGravity(Gravity.CENTER);
                                for (int k = 0; k < periods[pernum].days.get(i).lessons.size(); k++) {
                                    final PeriodFragment.Lesson less = periods[pernum].days.get(i).lessons.get(k);
                                    if (periods[pernum].subjects.get(j).unitid - less.unitId == 0) {
                                        if (less.marks != null) {
                                            for (int l = 0; l < less.marks.size(); l++) {
                                                TextView txt1 = new TextView(getActivity().getApplicationContext());
                                                txt1.setGravity(Gravity.CENTER);
                                                txt1.setTextColor(Color.WHITE);
                                                txt1.setPadding(15, 0, 15, 0);
                                                final Double d = less.marks.get(l).coefficient;
                                                if (d <= 0.5)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff1));
                                                else if (d <= 1)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff2));
                                                else if (d <= 1.25)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff3));
                                                else if (d <= 1.35)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff4));
                                                else if (d <= 1.5)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff5));
                                                else if (d <= 1.75)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff6));
                                                else if (d <= 2)
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff7));
                                                else
                                                    txt1.setBackgroundColor(getResources().getColor(R.color.coff8));
                                                txt1.setTextSize(20);
                                                try {
                                                    final int finalJ = j;
                                                    final int finalL = l;
                                                    txt1.setOnClickListener(new View.OnClickListener() {

                                                        @Override
                                                        public void onClick(View v) {

                                                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                            MarkFragment fragment = new MarkFragment();
                                                            transaction.replace(R.id.frame, fragment);
                                                            try {
                                                                fragment.coff = d;
                                                                fragment.data = less.marks.get(finalL).date;
                                                                fragment.markdata = less.marks.get(finalL).markdate;
                                                                fragment.teachname = less.marks.get(finalL).teachFio;
                                                                fragment.topic = less.marks.get(finalL).topic;
                                                                fragment.value = less.marks.get(finalL).value;
                                                                fragment.subject = periods[pernum].subjects.get(finalJ).name;
                                                            } catch (Exception e) {
                                                            }
                                                            transaction.addToBackStack(null);
                                                            transaction.commit();
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                }
                                                ask = false;
                                                LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                lp3.setMargins(0, 0, 0, 10);
                                                txt1.setLayoutParams(lp3);
                                                if (less.marks.get(l).value == null || less.marks.get(l).value.equals("")) {
                                                    txt1.setText("  ");
                                                } else {
                                                    txt1.setText(less.marks.get(l).value);
                                                }
                                                lin1.addView(txt1);
                                            }
                                        }
                                    }
                                }
                                if (ask) {
                                    lin1 = new LinearLayout(getContext());
                                    lin1.setOrientation(LinearLayout.HORIZONTAL);
                                    lin1.setGravity(Gravity.CENTER);
                                    TextView txt1 = new TextView(getActivity().getApplicationContext());
                                    txt1.setGravity(Gravity.CENTER);
                                    txt1.setTextSize(20);
                                    LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lp3.setMargins(0, 0, 0, 10);
                                    txt1.setLayoutParams(lp3);
                                    txt1.setText("—");
                                    txt1.setTextColor(Color.LTGRAY);
                                    lin1.addView(txt1);
                                    lin.addView(lin1);
                                }
                                ask = true;
                                if (lin1.getParent() != null)
                                    ((ViewGroup) lin1.getParent()).removeView(lin1);
                                lin.addView(lin1);
                            }
                            periods[pernum].lins.add(lin);
                        }
                    }
                    sasha("set subjects: " + periods[pernum].subjects.size() + " and days: " + periods[pernum].days.size());
                    ((MainActivity) getActivity()).set(periods, pernum);
                    READY = true;
                    if(getActivity()!=null) {
                        if(((MainActivity)getActivity()).getStackTop() instanceof ScheduleFragment)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    show();
                                }
                            });
                    }
                    //---------------------------------------------------------------------------------------------------------------------------------
                } catch (Exception e) {
                    sasha("second " + e);
                    periods[pernum].days = new ArrayList<>();
                    periods[pernum].subjects = new ArrayList<>();
                    periods[pernum].lins = new ArrayList<>();
                    periods[pernum].cells = new ArrayList<>();
                    Download2(id, h);

                }
            }
        }.start();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c1 = pageFragments.get(pager.getCurrentItem()).c;
        Calendar c2 = Calendar.getInstance();
        c1.setTime(datenow);
        c1.set(Calendar.YEAR, year);
        c1.set(Calendar.MONTH, month);
        c1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        c2.setTime(datenow);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);
        sasha(c1.getTime() + " " + c2.getTime());

        Long s = (c1.getTimeInMillis() - c2.getTimeInMillis()) / 86400000;
        sasha("this: " + s);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pager.setCurrentItem(pageCount / 2 + 1 + Integer.valueOf(Math.toIntExact(s)));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        MenuItem item = menu.add(0, 2, 0, "Calendar");
        item.setIcon(R.drawable.calendar);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item = menu.add(0, 3, 0, "Settings");
        item.setIcon(R.drawable.settings);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                ((MainActivity) getActivity()).quit();
                break;
            case 2:
                datePickerDialog.show();
                break;
            case 3:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data!=null)
            if(data.hasExtra("goal"))
                if(data.getStringExtra("goal").equals("quit"))
                    ((MainActivity) getActivity()).quit();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            log("position " + position);
            if(position == 0)
                return pageFragments.get(pageFragments.size()-1);
            return pageFragments.get(position - 1);
        }

        @Override
        public int getCount() {
            return pageCount;
        }
    }

    class Period {
        Long datestart;
        Long datefinish;
        String name;
        int num;
        int id;
        ArrayList<PeriodFragment.Subject> subjects;
        ArrayList<PeriodFragment.Day> days;
        ArrayList<PeriodFragment.Cell> cells;
        ArrayList<LinearLayout> lins;

        Period() {
        }
    }
}