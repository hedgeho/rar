package ru.gurhouse.sch;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.MainActivity.TYPE_SEM;
import static ru.gurhouse.sch.PeriodFragment.coefs;
import static ru.gurhouse.sch.PeriodFragment.colors;
import static ru.gurhouse.sch.SettingsActivity.getColorFromAttribute;

public class ScheduleFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    boolean READY = false;
    boolean shown = false;
    final int pageCount = 5001;
    boolean ready = false;
    String[] period;
    boolean first = true;
    private int USER_ID;
    Activity context;
    long time;

    TextView []tv = new TextView[7];
    int day;
    Date datenow;
    int pernum;
    String[] days1 = {"пн", "вт", "ср", "чт", "пт", "сб", "вс"};
    ViewPager pager;
    PagerAdapter pagerAdapter;
    int lastposition;
    ArrayList<PageFragment> pageFragments;
    View v;
    DatePickerDialog datePickerDialog;
    int[] week;
    static int yearname = Calendar.getInstance().get(Calendar.MONTH) < Calendar.JULY ? Calendar.getInstance().get(Calendar.YEAR)-1 : Calendar.getInstance().get(Calendar.YEAR);
    KindaList[] s = new KindaList[11];
    String[] name = {"1 четверть", "2 четверть", "1 полугодие", "3 четверть", "4 четверть",
            "2 полугодие", "Годовая оценка", "Экзамен", "Оценка за ОГЭ", "Оценка в аттестат"};
    Period[] periods = new Period[7];

    public ScheduleFragment () {
        s[0] = new KindaList();
        s[0].add("");
        for (int i = 1; i < s.length; i++) {
            s[i] = new KindaList();
            s[i].add(name[i - 1]);
        }
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

        pageFragments = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            pageFragments.add(new PageFragment());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(datenow);
            calendar.add(Calendar.DAY_OF_MONTH, (i - (pageCount / 2) + 1));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            pageFragments.get(i).c = calendar;
            calendar.add(Calendar.DAY_OF_WEEK, -1);
            pageFragments.get(i).dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
            pageFragments.get(i).periods = periods;
        }
    }

    void start() {
        USER_ID = TheSingleton.getInstance().getUSER_ID();
        Download1();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(super.getActivity() != null)
            context = super.getActivity();
        if(v == null)
            v = inflater.inflate(R.layout.fragment_schedule, container, false);
        if(!READY) {
            v = inflater.inflate(R.layout.fragment_schedule, container, false);
            return v;
        }
        log("shown? " + shown);
        if(!shown && periods[pernum].days != null)
            show();
        return v;
    }

    void show() {
        show(pernum);
    }

    void show(int pernum, int i) {
        show(pernum);
        if(i != -1 && pager != null) pager.setCurrentItem(i);
    }

    void show(int pernum) {
        log("show SF");
//        v = context.getLayoutInflater().inflate(R.layout.fragment_schedule,
//                context.findViewById(R.id.frame), false);

        boolean autoChangingDate =
                getContext().getSharedPreferences("pref", 0).getBoolean("nextday", true);
        shown = true;
        try {
            for (int i = 0; i < pageCount; i++) {
                //log("i" + i);
                pageFragments.get(i).subjects = periods[pernum].subjects;
                pageFragments.get(i).periods = periods;
            }
            int y = 0;
//            log("size: " + periods[pernum].days.length);
            long daymsec = periods[pernum].days[y].daymsec;
            for (int i = 0; i < pageCount; i++) {
                //log("page " + periods[pernum].days[y]);
                if (pageFragments.get(i).c.getTimeInMillis() == daymsec) {
                    //z = true;
                    pageFragments.get(i).day = periods[pernum].days[y];
                    if (y + 1 - periods[pernum].days.length == 0) {
                        log("diary drawing ended");
                        break;
                    } else {
                        y++;
                        daymsec = periods[pernum].days[y].daymsec;
                    }
                }
//                log("daymsec " + new Date(pageFragments.get(i).c.getTimeInMillis()).toString() + "; " + new Date(daymsec).toString());
            }
//            for (PageFragment f: pageFragments) {
//                f.draw();
//            }
            ((MainActivity) getContext()).updatePages();
//            log("y: " + y);
            pager = v.findViewById(R.id.pager);
//            pager.setSaveFromParentEnabled(false);
//            pager.setAdapter(null);
            if(pager.getAdapter() == null) {
                pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
                log("pager adapter created");
                pager.setAdapter(pagerAdapter);
            }
            pager.setCurrentItem(pageCount / 2 + 1);
            lastposition = pager.getCurrentItem();

            Calendar date = Calendar.getInstance();
            if(autoChangingDate && date.get(Calendar.HOUR_OF_DAY) >= 16) {
                date.add(Calendar.DATE, 1);
                pager.setCurrentItem(++lastposition);
            }
            if(autoChangingDate && date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                date.add(Calendar.DATE, 1);
                pager.setCurrentItem(++lastposition);
            }
            datePickerDialog = new DatePickerDialog(getContext(), ScheduleFragment.this, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
            ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    int w;
                    if(pager.getCurrentItem() == -1) {
                        loge("current item is -1");
                        return;
                    }
                    if(pager.getCurrentItem() == 0)
                        pager.setCurrentItem(pageCount/2 + 1);
                    if(pageFragments.size() <= pager.getCurrentItem()-1) {
                        loge("pageFragments.size(): " + pageFragments.size());
                        loge("pager.getCurrentItem()-1: " + (pager.getCurrentItem()-1));
                        return;
                    }
                    if (pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                        w = 7;
                    } else
                        w = pageFragments.get(pager.getCurrentItem() - 1).c.get(Calendar.DAY_OF_WEEK) - 1;

                    for (int i = 0; i < 7; i++) {
                        week[i] = pager.getCurrentItem() + i - w + 1;
                    }
                    Calendar date = Calendar.getInstance();
                    if(autoChangingDate && date.get(Calendar.HOUR_OF_DAY) >= 16)
                        date.add(Calendar.DATE, 1);
                    if(autoChangingDate && date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                        date.add(Calendar.DATE, 1);
                    date.add(Calendar.DATE, position-lastposition);
                    datePickerDialog.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
                    Calendar MaxOrMinDate = Calendar.getInstance();
                    MaxOrMinDate.add(Calendar.YEAR,1);
                    datePickerDialog.getDatePicker().setMaxDate(MaxOrMinDate.getTimeInMillis());
                    MaxOrMinDate.add(Calendar.YEAR,-2);
                    datePickerDialog.getDatePicker().setMinDate(MaxOrMinDate.getTimeInMillis());
                    datePickerDialog.getDatePicker().setSpinnersShown(true);
                    day = w;
                    date.add(Calendar.DAY_OF_WEEK, -1);
                    okras(tv, date.get(Calendar.DAY_OF_WEEK)-1);
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
            pager.addOnPageChangeListener(onPageChangeListener);
            onPageChangeListener.onPageSelected(pager.getCurrentItem());

//            for (int i = 0; i < 7; i++) {
//                tv[i].setTextColor(getColorFromAttribute(R.attr.white, getContext().getTheme()));
//
//                String s = days1[i] + "\n" + pageFragments.get(week[i] - 1).c.get(Calendar.DAY_OF_MONTH);
//                Spannable spans = new SpannableString(s);
//                spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//                spans.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.white, getContext().getTheme())), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//                spans.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.white, getContext().getTheme())), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                tv[i].setText(spans);
//
//            }
            first = false;
            Toolbar toolbar = getContext().findViewById(R.id.toolbar);
            if(((MainActivity) getContext()).getStackTop() instanceof ScheduleFragment) {
                toolbar.setTitle("Дневник");
            }
            setHasOptionsMenu(true);
            ((MainActivity) context).setSupportActionBar(toolbar);
            v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.layout_fragment_bp_list).setVisibility(View.VISIBLE);
            if(TheSingleton.getInstance().t1 > 0 && time == 0) {
                time = System.currentTimeMillis() - TheSingleton.getInstance().t1;
                loge("SF: loading ended in " + time + " ms");
                SharedPreferences pref = getContext().getSharedPreferences("pref", 0);
//                pref.edit().putLong("sum", 0).putInt("count", 0).apply();
                long sum = pref.getLong("sum", 0)+time;
                int count = pref.getInt("count", 0)+1;
                log("average: " + (sum/count));
                pref.edit().putLong("sum", sum).putInt("count", count).apply();

                //TheSingleton.getInstance().t1 = 0;
            }

        } catch (Exception e) {
            loge(e);
            loge("show");
            Toolbar toolbar = context.findViewById(R.id.toolbar);
            toolbar.setClickable(false);
            setHasOptionsMenu(true);
            ((MainActivity) context).setSupportActionBar(toolbar);
            if(v == null)
                shown = false;
            else {
                toolbar.setTitle("Дневник");
                v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.layout_fragment_bp_list).setVisibility(View.VISIBLE);
            }
        }
    }

    public void okras(TextView [] tv, int selected){
        for (int i = 0; i < 7; i++) {
            if(tv[i] == null) {
                tv[i] = new TextView(getContext());
                tv[i].setId(i);
                tv[i].setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p.weight = (float) 1 / 7;
                tv[i].setLayoutParams(p);
                final int finalI = i;
                tv[i].setOnClickListener(v -> {
                    pager.setCurrentItem(pager.getCurrentItem() - (day - finalI - 1));
                    day = finalI + 1;
                });
                LinearLayout linear1 = v.findViewById(R.id.liner1);
                linear1.setWeightSum(1);
                linear1.addView(tv[i]);
            }
            ForegroundColorSpan color;

            if(i == selected){
                tv[i].setBackground(getResources().getDrawable(R.drawable.cell_phone1, getContext().getTheme()));
                tv[i].setTextColor(getColorFromAttribute(R.attr.day_cell_text_selected, getContext().getTheme()));
                color = new ForegroundColorSpan(getColorFromAttribute(R.attr.day_cell_text_selected, getContext().getTheme()));
            } else {
                tv[i].setBackground(null);
                tv[i].setTextColor(getColorFromAttribute(R.attr.main_font, getContext().getTheme()));
                color = new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme()));
            }

            String s = days1[i] + "\n" + pageFragments.get(week[i] - 1).c.get(Calendar.DAY_OF_MONTH);
            Spannable spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(color, 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(color, s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv[i].setText(spans);
        }
    }

    void Download1() {
        log("Download1()");
        new Thread() {
            @Override
            public void run() {
                try {
                    /*String periods2;
                    SharedPreferences pref = getContext().getSharedPreferences("pref", 0);
                    if(pref.getString("periods2", "").equals("")) {
                        periods2 = connect("https://app.eschool.center/ec-server/dict/periods2?year=" + yearname,
                                null, getContext());
                        pref.edit().putString("periods2", periods2).apply();
                    } else
                        periods2 = pref.getString("periods2", "");
                    JSONArray array3 = new JSONArray(periods2);*/
                    JSONArray array3 = new JSONArray(
                            connect("https://app.eschool.center/ec-server/dict/periods2?year=" + yearname,
                                    null, getContext()));
                    for (int i = 0; i < array3.length(); i++) {
                        if (array3.getJSONObject(i).getInt("typeId") == 1) {
                            JSONArray array4 = array3.getJSONObject(i).getJSONArray("items");
                            for (int j = 0; j < array4.length(); j++) {
                                JSONObject ob = array4.getJSONObject(j);
                                switch (ob.getString("typeCode")) {
                                    case "Y":
                                        periods[0].datefinish = ob.getLong("date2");
                                        periods[0].datestart = ob.getLong("date1");
                                        periods[0].name = ob.getString("name");
                                        periods[0].id = ob.getInt("id");
                                        period[0] = periods[0].name;
                                        break;
                                    case "HY":
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
                                        break;
                                    case "Q":
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
                                        break;
                                }
                            }
                            break;
                        }
                    }
                    periods[3].datefinish = periods[4].datestart;
                    periods[4].datefinish = periods[5].datestart;
                    periods[5].datefinish = periods[6].datestart;

                    if (datenow.getTime() >= periods[3].datestart && datenow.getTime() <= periods[3].datefinish) {
                        Download2(3);
                        while(syncing) {
                            Thread.sleep(10);
                        }
                        Download2(1,  false);
//                        while (syncing) {
//                            Thread.sleep(10);
//                        }
//                        getContext().runOnUiThread(() -> ((MainActivity) getContext()).updateSubjects(periods, 3));
                    } else if (datenow.getTime() >= periods[4].datestart && datenow.getTime() <= periods[4].datefinish) {
                        Download2(4);
                        while(syncing) {
                            Thread.sleep(10);
                        }
                        Download2(1, false);
//                        while (syncing) {
//                            Thread.sleep(10);
//                        }
//                        getContext().runOnUiThread(() -> ((MainActivity) getContext()).updateSubjects(periods, 4));

                    } else if (datenow.getTime() >= periods[5].datestart && datenow.getTime() <= periods[5].datefinish) {
                        Download2(5);
                        while(syncing) {
                            Thread.sleep(10);
                        }
                        Download2(2, false);
//                        while (syncing || periods[pernum].subjects == null || periods[pernum].subjects.length == 0) {
//                            Thread.sleep(10);
//                        }
//                        getContext().runOnUiThread(() -> ((MainActivity) getContext()).updateSubjects(periods, 5));
                    } else if (datenow.getTime() >= periods[6].datestart && datenow.getTime() <= periods[6].datefinish){
                        Download2(6);
                        while(syncing) {
                            Thread.sleep(10);
                        }
                        Download2(2, false);
//                        while (syncing) {
//                            Thread.sleep(10);
//                        }
//                        getContext().runOnUiThread(() -> ((MainActivity) getContext()).updateSubjects(periods, 6));
                    } // else summer holidays / other year
                } catch (LoginActivity.NoInternetException e) {
//                    getContext().runOnUiThread(() ->
//                            Toast.makeText(context, "Нет интернета", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    loge(e);
                }
            }
        }.start();
    }

    JSONObject object1 = null;
    boolean first_downl = true;
    static boolean syncing = false;
    void Download2(final int h) {
        Download2(h,true);
    }
    void Download2(final int pernum, final boolean show) {
        if(syncing) return;
        syncing = true;
        shown = false;

        final int id = periods[pernum].id;

        log("SchF: Download2(), period " + pernum + ", show " + show);

        final long t0 = System.currentTimeMillis();
        object1 = null;
        new Thread(() -> {
            try {
                object1 = new JSONObject(
                        connect("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=" + id,
                                null, getContext()));
            } catch (LoginActivity.NoInternetException e) {
                loge(e);
            } catch (Exception e) {
                loge(e);
            }
        }).start();


        if(first_downl && TheSingleton.getInstance().t1 == 0) {
            log("start");
            TheSingleton.getInstance().t1 = System.currentTimeMillis();
        }
        if(first_downl)
            first_downl = false;

        if(periods.length == 0)
            log("SchF/Download2: periods are empty");
        if(show)
            this.pernum = pernum;
        periods[pernum].days = null;
        periods[pernum].subjects = null;
        periods[pernum].lins = null;
        periods[pernum].cells = null;
        new Thread(() -> {
            try {
                //------------------------------------------------------------------------------------------------

                JSONObject object = new JSONObject(
                        connect("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=" + id,
                                null, getContext()));
                if (!object.has("result"))
                    log("lol no result: " + object.toString());
                JSONArray array = object.getJSONArray("result");
                PeriodFragment.Subject subject;
                JSONObject obj;
                double D;
                String s;
                periods[pernum].subjects = new PeriodFragment.Subject[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    subject = new PeriodFragment.Subject();
                    obj = array.getJSONObject(i);
                    if (obj.has("overMark")) {
                        D = obj.getDouble("overMark");
                        s = String.valueOf(D);
                        if (s.length() > 4) {
                            s = String.format(Locale.UK, "%.2f", D);
                        }
                        subject.avg = Double.parseDouble(s);
                    }
                    if (obj.has("totalMark"))
                        subject.totalmark = obj.getString("totalMark");
                    if (obj.has("unitName"))
                        subject.name = obj.getString("unitName");
                    if (obj.has("rating"))
                        subject.rating = obj.getString("rating");
                    if (obj.has("unitId"))
                        subject.unitid = obj.getInt("unitId");
                    if (obj.has("ttlItCode") && pernum > 2) {
                        subject.periodType = obj.getString("ttlItCode").equals("Q");
                    } else if (pernum < 3) {
                        for (int j = 3; j < 7; j++) {
                            if (periods[j].subjects != null && periods[j].subjects.length != 0) {
                                for (int k = 0; k < periods[j].subjects.length; k++) {
                                    if (periods[j].subjects[k].unitid == subject.unitid) {
                                        subject.periodType = periods[j].subjects[k].periodType;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    subject.cells = null;
                    log((i + 1) + ". name (" + subject.name + "), avg (" + subject.avg + "), tm (" + subject.totalmark + ")," +
                            " rating (" + subject.rating + "), periodType = " + (subject.periodType == TYPE_SEM?"SEM":"Q"));
                    periods[pernum].subjects[i] = subject;
                }
                log("subjects ready: " + periods[pernum].subjects.length + ", pernum: " + pernum);
                log(-2);

                while (object1 == null) {
                    Thread.sleep(10);
                }

                long a1 = System.currentTimeMillis()-t0, t1 = System.currentTimeMillis();

                log(-1);
                if(!object1.has("result")) {
                    log("no result: " + object1.toString());
                }
                JSONArray arraydaylessons = object1.getJSONArray("result");
                log("SchF/Download2: arraydaylessons.length = " + arraydaylessons.length());
                PeriodFragment.Cell cell;
                periods[pernum].cells = new PeriodFragment.Cell[arraydaylessons.length()];
                for (int i = 0; i < arraydaylessons.length(); i++) {
                    object1 = arraydaylessons.getJSONObject(i);
                    cell = new PeriodFragment.Cell();
                    if(object1.has("attends")){
                        cell.attends = new PageFragment.Attends();
                        JSONArray ar = object1.getJSONArray("attends");
                        JSONObject o = (JSONObject) ar.get(0);
                        if(o.has("name"))
                            cell.attends.name = o.getString("name");
                        if(o.has("color"))
                            cell.attends.color = "50" + o.getString("color");
                        if(o.has("attendTypeId"))
                            cell.attends.id = o.getInt("attendTypeId");

                    }
                    if (object1.has("lptName"))
                        cell.lptname = object1.getString("lptName");
                    if (object1.has("markDate"))
                        cell.markdate = object1.getString("markDate");
                    if (object1.has("lessonId"))
                        cell.lessonid = object1.getLong("lessonId");
                    if (object1.has("markVal"))
                        cell.markvalue = object1.getString("markVal");
                    if (object1.has("mktWt"))
                        cell.mktWt = object1.getDouble("mktWt");
                    if (object1.has("teachFio"))
                        cell.teachFio = object1.getString("teachFio");
                    if (object1.has("startDt"))
                        cell.date = object1.getString("startDt");
                    if (object1.has("unitId"))
                        cell.unitid = object1.getInt("unitId");
                    periods[pernum].cells[i] = cell;
                }
                Date date = new Date();
                if (periods[pernum].cells.length == 0) {
                    if (periods[pernum].datestart <= date.getTime()/* && periods[pernum].datefinish >= date.getTime()*/) {
                        // ескул не прислал оценки, хотя должен был
                        log("trying again");
                        periods[pernum].days = null;
                        periods[pernum].subjects = null;
                        periods[pernum].lins = null;
                        periods[pernum].cells = null;;
                        syncing = false;
                        Download2(pernum, show);
                    } else {
                        periods[pernum].nullsub = true;
                        ((MainActivity) getContext()).nullsub(periods, pernum, show);
                    }
                    log("SchF/Download2: nullsub, pernum - " + pernum);
                } else {
                    String s1 = periods[pernum].cells[0].date;
                    String s2 = periods[pernum].cells[periods[pernum].cells.length - 1].date;
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    long d1 = format.parse(s1).getTime();
                    long d2 = format.parse(s2).getTime();
                    JSONObject object2 = new JSONObject(
                            connect("https://app.eschool.center/ec-server/student/diary?" +
                                    "userId=" + USER_ID + "&d1=" + d1 + "&d2=" + d2, null, getContext()));
                    JSONArray array2 = object2.getJSONArray("lesson");

                    long a2 = System.currentTimeMillis()-t1, t2 = System.currentTimeMillis();
                    long day1 = 0L, date1;
                    int isODOD;
                    PeriodFragment.Day thisday;
                    Date datathis;
                    SimpleDateFormat dateFormat2;
                    String dayOfTheWeek;
                    StringBuilder builder;
                    JSONArray ar, ar1, ar2;
                    JSONObject obj1;
                    PeriodFragment.File file;
                    PeriodFragment.Lesson lesson;

                    //PeriodFragment.Day thisday = new PeriodFragment.Day();
                    //thisday.odods = new ArrayList<>();
                    int index = -1, len = 0;
                    log(0);
                    for (int i = 0; i < array2.length(); i++) {
                        object2 = array2.getJSONObject(i);
                        date1 = Long.parseLong(object2.getString("date"));
                        isODOD = object2.getInt("isODOD");
                        if(isODOD == 0 && date1 != day1) {
                            date = new Date(date1);
                            date.setHours(0);
                            date.setMinutes(0);
                            date1 = date.getTime();
                            len++;
                        }
                        day1 = date1;
                    }
                    day1 = 0;
                    periods[pernum].days = new PeriodFragment.Day[len];
                    int ind=0;
                    for (int i = 0; i < array2.length(); i++) {
                        object2 = array2.getJSONObject(i);
                        date1 = Long.parseLong(object2.getString("date"));
//                        date = new Date(date1);
//                        Calendar c = Calendar.getInstance();
//                        Calendar c1 = Calendar.getInstance();
//                        c.setTime(date);
//                        date = new Date(day1);
//                        c1.setTime(date);
//                        date1 = c.getTime().getTime();
//                        date = new Date(date1);
                        isODOD = object2.getInt("isODOD");
                        if (isODOD == 0) {
                            if (date1 != day1) {
                                index++;
                                date = new Date(date1);
                                date.setHours(0);
                                date.setMinutes(0);
                                date1 = date.getTime();
                                thisday = new PeriodFragment.Day();
                                thisday.day = date.toString();
                                thisday.daymsec = date1;
                                datathis = new Date();
                                datathis.setTime(date1);
                                dateFormat2 = new SimpleDateFormat("EEE", Locale.ENGLISH);
                                dayOfTheWeek = dateFormat2.format(datathis);
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
                                periods[pernum].days[ind++] = thisday;
                                thisday.odods = new ArrayList<>();
                            }
                            lesson = new PeriodFragment.Lesson();
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
                            if (object2.has("meet") && object2.getJSONObject("meet").has("inviteText"))
                                lesson.meetingInvite = object2.getJSONObject("meet").getString("inviteText");
                            ar = object2.getJSONArray("part");
                            lesson.homeWork = new PeriodFragment.HomeWork();
                            builder = new StringBuilder();
                            lesson.homeWork.files = new ArrayList<>();
                            for (int j = 0; j < ar.length(); j++) {
                                obj1 = ar.getJSONObject(j);
                                if (obj1.getString("cat").equals("DZ")) {
                                    if (obj1.has("variant")) {
                                        ar1 = obj1.getJSONArray("variant");
                                        for (int k = 0; k < ar1.length(); k++) {
                                            if (ar1.getJSONObject(k).has("text")) {
                                                builder.append(ar1.getJSONObject(k).getString("text")).append("\n");
                                            }
                                            if (ar1.getJSONObject(k).has("file")) {
                                                ar2 = ar1.getJSONObject(k).getJSONArray("file");
                                                for (int l = 0; l < ar2.length(); l++) {
                                                    file = new PeriodFragment.File();
                                                    file.name = ar2.getJSONObject(l).getString("fileName");
                                                    file.id = ar2.getJSONObject(l).getInt("id");
                                                    lesson.homeWork.files.add(file);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            lesson.homeWork.stringwork = builder.toString();
                            periods[pernum].days[index].lessons.add(lesson);
                        } else {
                            PeriodFragment.ODOD odod = new PeriodFragment.ODOD();
                            odod.duration = object2.getInt("duration");
                            odod.ODODid = object2.getInt("isODOD");
                            odod.daymsec = object2.getLong("date");
                            odod.day = object2.getString("date_d");
                            odod.name = object2.getJSONObject("clazz").getString("name");
                            periods[pernum].days[index].odods.add(odod);
                        }
                        day1 = date1;
                    }

                    log(1);
                    LinkedList<PeriodFragment.Cell>[] subjects = new LinkedList[periods[pernum].subjects.length];
                    for (int i = 0; i < subjects.length; i++) {
                        subjects[i] = new LinkedList<>();
                    }
                    Arrays.sort(periods[pernum].subjects, (o1, o2) -> Integer.compare(o1.unitid, o2.unitid));
                    for (int i = 0; i < periods[pernum].subjects.length; i++) {
                        if (periods[pernum].subjects[i].shortname == null || periods[pernum].subjects[i].shortname.isEmpty()) {
                            subject = periods[pernum].subjects[i];
                            switch (subject.name) {
                                case "Физика":
                                case "Химия":
                                case "История":
                                case "Алгебра":
                                    subject.shortname = subject.name;
                                    break;
                                case "БЕСЕДЫ КЛ РУК":
                                    subject.shortname = "Кл. Час";
                                    break;
                                case "Иностранный язык":
                                    subject.shortname = "Ин. Яз.";
                                    break;
                                case "Алгебра и начала анализа":
                                    subject.shortname = "Алгебра";
                                    break;
                                case "Информатика и ИКТ":
                                    subject.shortname = "Информ.";
                                    break;
                                case "Биология":
                                    subject.shortname = "Биолог.";
                                    break;
                                case "География":
                                    subject.shortname = "Геогр.";
                                    break;
                                case "Геометрия":
                                    subject.shortname = "Геометр.";
                                    break;
                                case "Литература":
                                    subject.shortname = "Лит-ра";
                                    break;
                                case "Обществознание":
                                    subject.shortname = "Общ.";
                                    break;
                                case "Русский язык":
                                    subject.shortname = "Рус. Яз.";
                                    break;
                                case "Физическая культура":
                                    subject.shortname = "Физ-ра";
                                    break;
                                default:
                                    periods[pernum].subjects[i].shortname = periods[pernum].subjects[i].name.substring(0, 3);
                            }
                        }
                    }
                    for (int i = 0; i < periods[pernum].days.length; i++) {
                        for (int j = 0; j < periods[pernum].cells.length; j++) {
                            cell = periods[pernum].cells[j];
                            if(cell.date == null)
                                continue;
                            s1 = cell.date;
                            PageFragment.Attends at = cell.attends;
                            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                            d1 = format.parse(s1).getTime();

                            if (periods[pernum].days[i].daymsec - d1 == 0 || periods[pernum].days[i].daymsec == d1) {
                                for (int k = 0; k < periods[pernum].days[i].lessons.size(); k++) {
                                    if (periods[pernum].days[i].lessons.get(k).id == cell.lessonid) {
                                        periods[pernum].days[i].lessons.get(k).attends = at;
                                        if (cell.mktWt != 0) {
                                            PeriodFragment.Mark mark = new PeriodFragment.Mark();
                                            mark.cell = cell;
                                            mark.idlesson = cell.lessonid;
                                            mark.coefficient = cell.mktWt;
                                            if (cell.markvalue != null)
                                                mark.value = cell.markvalue;
                                            else
                                                mark.value = "";
                                            mark.teachFio = cell.teachFio;
                                            mark.markdate = cell.markdate;
                                            mark.date = cell.date;

                                            mark.topic = cell.lptname;
                                            mark.unitid = cell.unitid;
                                            for (int l = 0; l < periods[pernum].subjects.length; l++) {
                                                if (periods[pernum].subjects[l].unitid == mark.unitid) {
                                                    subjects[l].add(cell);
                                                }
                                            }
                                            periods[pernum].days[i].lessons.get(k).marks.add(mark);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (int i = 0; i < subjects.length; i++) {
                        periods[pernum].subjects[i].cells = subjects[i].toArray(new PeriodFragment.Cell[0]);
                    }
                    ready = true;

                    // showing schedule
                    if(getContext() != null)
                        getContext().runOnUiThread(() -> show(pernum, pager != null ? pager.getCurrentItem() : -1));

                    // цикл на ~2 секунды
                    // showing period
                    log(2);
                    long a = 0, b = 0, c = 0, de = 0, ef = 0;
                    TextView txt, txt1;
                    LinearLayout.LayoutParams lp, lp3;
                    LinearLayout lin, lin1, lin2;
                    int color_index, lin_count = 0, lin_index = 0;
                    for (int i = 0; i < periods[pernum].days.length; i++) {
                        if(periods[pernum].days[i] != null)
                            lin_count++;
                    }
                    periods[pernum].lins = new LinearLayout[lin_count];
                    // days x subjects x lessons x marks
                    for (int i = 0; i < periods[pernum].days.length; i++) {
                        if (periods[pernum].days[i] != null) {
                            lin = new LinearLayout(getContext());
                            boolean ask = true;
                            lin.setOrientation(LinearLayout.VERTICAL);
                            lin.setGravity(Gravity.CENTER);
                            if(i == 0) {
                                lin.setBackground(getResources().getDrawable(R.drawable.cell_phone6, getContext().getTheme()));
                            } else if (i + 1 != periods[pernum].days.length) {
                                lin.setBackground(getResources().getDrawable(R.drawable.cell_phone4, getContext().getTheme()));
                            } else {
                                lin.setBackground(getResources().getDrawable(R.drawable.cell_phone5, getContext().getTheme()));
                            }
                            lin.setPadding(30, 0, 30, 0);

                            lin2 = new LinearLayout(getContext());
                            lin2.setOrientation(LinearLayout.HORIZONTAL);
                            lin2.setGravity(Gravity.CENTER);
                            txt = new TextView(getContext());
                            txt.setGravity(Gravity.CENTER);
                            txt.setTextSize(20);
                            s = "";
                            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 0, 10);
                            txt.setLayoutParams(lp);
                            try {
                                date = new Date();
                                date.setTime(periods[pernum].days[i].daymsec);
                                format = new SimpleDateFormat("dd.MM", Locale.ENGLISH);
                                s = format.format(date);
                            } catch (Exception e) {
                                loge(e);
                            }
                            txt.setText(s);
                            txt.setTextColor(getColorFromAttribute(R.attr.second_font, getContext().getTheme()));
                            lin2.addView(txt);
                            lin.addView(lin2);
                            for (int j = 0; j < periods[pernum].subjects.length; j++) {
                                lin1 = new LinearLayout(getContext());
                                lin1.setOrientation(LinearLayout.HORIZONTAL);
                                lin1.setGravity(Gravity.CENTER);
                                //log(periods[pernum].days[i].lessons.size() + " " + periods[pernum].subjects.get(j).name);
                                for (int k = 0; k < periods[pernum].days[i].lessons.size(); k++) {
                                    final PeriodFragment.Lesson less = periods[pernum].days[i].lessons.get(k);
                                    if (periods[pernum].subjects[j].unitid == less.unitId) {
                                        if (less.marks != null) {
                                            for (int l = 0; l < less.marks.size(); l++) {
                                                t1 = System.currentTimeMillis();
                                                txt1 = new Kletka(getContext());
                                                a += System.currentTimeMillis() - t1;
                                                t1 = System.currentTimeMillis();
                                                final double d = less.marks.get(l).coefficient;
                                                color_index = Arrays.binarySearch(coefs, d);
                                                if(color_index < 0)
                                                    color_index = -color_index-1;
                                                txt1.setBackgroundColor(colors[color_index]);
                                                txt1.setTextSize(20);
                                                b += System.currentTimeMillis() - t1;
                                                t1 = System.currentTimeMillis();
                                                final int finalJ = j;
                                                final int finalL = l;
                                                txt1.setOnClickListener(v -> {
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
                                                        fragment.subject = periods[pernum].subjects[finalJ].name;
                                                    } catch (Exception e) {loge(e);}
                                                    transaction.addToBackStack(null);
                                                    transaction.commit();
                                                });
                                                c += System.currentTimeMillis() - t1;
                                                t1 = System.currentTimeMillis();
                                                ask = false;
                                                lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                lp3.setMargins(0, 0, 0, 10);
                                                txt1.setLayoutParams(lp3);
                                                if (less.marks.get(l).value == null || less.marks.get(l).value.equals("")) {
                                                    txt1.setText("  ");
                                                } else {
                                                    txt1.setText(less.marks.get(l).value);
                                                }
                                                lin1.addView(txt1);
                                                de += System.currentTimeMillis() - t1;
                                            }
                                        }
                                    }
                                }
                                t1 = System.currentTimeMillis();
                                if (ask) {
                                    lin1 = new LinearLayout(getContext());
                                    lin1.setOrientation(LinearLayout.HORIZONTAL);
                                    lin1.setGravity(Gravity.CENTER);
                                    txt1 = new TextView(getContext());
                                    txt1.setGravity(Gravity.CENTER);
                                    txt1.setTextSize(20);
                                    lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lp3.setMargins(0, 0, 0, 10);
                                    txt1.setLayoutParams(lp3);
                                    txt1.setText("—");
                                    txt1.setTextColor(getColorFromAttribute(R.attr.second_font, getContext().getTheme()));
                                    lin1.addView(txt1);
                                    lin.addView(lin1);
                                }
                                ef += System.currentTimeMillis() - t1;
                                ask = true;
                                if (lin1.getParent() != null)
                                    ((ViewGroup) lin1.getParent()).removeView(lin1);
                                lin.addView(lin1);
                            }
                            periods[pernum].lins[lin_index++] = lin;
                        }
                    }
                    log("a " + a);
                    log("b " + b);
                    log("c " + c);
                    log("e " + de);
                    log("f " + ef);
                    log(3);
                    ((MainActivity) getContext()).set(periods, pernum, show);
                    READY = true;

                    log("Download2() ended");
                    if (TheSingleton.getInstance().t1 > 0) {
                        final long t = System.currentTimeMillis() - t0;
                        log("download ended in " + t + " ms, including: ");
                        log("/getDiaryUnits & /getDiaryPeriod: " + a1);
                        log("/diary: " + a2);
                        log("cycles: " + (System.currentTimeMillis() - t2));
//                        TheSingleton.getInstance().t1 = 0;
                    }
                    first_downl = true;
                }
                syncing = false;

                //---------------------------------------------------------------------------------------------------------------------------------
            } catch (LoginActivity.NoInternetException e) {
                syncing = false;
//                getContext().runOnUiThread(() ->
//                        Toast.makeText(context, "Нет интернета", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                loge(e);
                periods[pernum].days = null;
                periods[pernum].subjects = null;
                periods[pernum].lins = null;
                periods[pernum].cells = null;;
                syncing = false;
                Download2(pernum, show);
            }
        }).start();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c1 = (Calendar) pageFragments.get(pager.getCurrentItem()).c.clone();
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

        int s = (int)((c1.getTimeInMillis() - c2.getTimeInMillis()) / 86400000);
        pager.setCurrentItem(pageCount / 2 + 1 + s);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        int color = getColorFromAttribute(R.attr.toolbar_icons, getContext().getTheme());

        MenuItem item = menu.add(0, 2, 0, "Календарь");
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.calendar);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        item.setIcon(wrappedDrawable);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 3, 0, "Настройки");
        unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.settings);
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        item.setIcon(wrappedDrawable);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 4, 2, "Обновить");
        unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.refresh);
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        item.setIcon(wrappedDrawable);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                datePickerDialog.show();
                break;
            case 3:
                if(!PeriodFragment.settingsClicked) {
                    PeriodFragment.settingsClicked = true;
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    startActivityForResult(intent, 0);
                }
                break;
            case 4:
                int num = getNum(periods, pageFragments.get(pager.getCurrentItem()-1).c);
                Download2(num, false);
//                final int current = pager.getCurrentItem();
//                new Thread(() -> {
//                    try {
//                    while(syncing) {
//                            Thread.sleep(10);
//                    }
//                    getActivity().runOnUiThread(() -> pager.setCurrentItem(current+5));
//                    Thread.sleep(10);
//                    getActivity().runOnUiThread(() -> pager.setCurrentItem(current));
//
//                } catch (Exception ignore) {}

//        }).start();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        if(!shown && periods[pernum].days != null && periods[pernum].days.length > 0) show();
        if(getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            if(toolbar != null)
                toolbar.setTitle("Дневник");
        }
        super.onResume();
    }

    public Activity getContext() {
        return (context==null?getActivity():context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null)
            context = getActivity();
    }

    static int getNum(Period[] periods, Calendar c) {
        int id = 0;
        for (int i = 0; i < periods.length; i++) {
            if(c.getTimeInMillis() > periods[i].datestart)
                id = i;
        }
        return id;
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
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
        long datestart;
        long datefinish;
        String name;
        boolean nullsub = false;
        int id;
//        ArrayList<PeriodFragment.Subject> subjects;
        PeriodFragment.Subject[] subjects;
//        ArrayList<PeriodFragment.Day> days;
        PeriodFragment.Day[] days;
//        ArrayList<PeriodFragment.Cell> cells;
        PeriodFragment.Cell[] cells;
//        ArrayList<LinearLayout> lins;
        LinearLayout[] lins;

        Period() {
        }
    }

    class Kletka extends android.support.v7.widget.AppCompatTextView {

        public Kletka(Context context) {
            super(context);
            this.setGravity(Gravity.CENTER);
            this.setTextColor(getColorFromAttribute(R.attr.main_font, getContext().getTheme()));
            this.setPadding(15, 0, 15, 0);
        }
    }

    class KindaList extends ArrayList<String> {}
}