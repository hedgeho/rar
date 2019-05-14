package com.example.sch;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class ScheduleFragment extends Fragment {

    static final String TAG = "myLogs";
    static int pageCount = 101;
    TextView []tv = new TextView[7];
    int day;
    Date datenow;
    String[] days1 = {"пн", "вт", "ср", "чт", "пт", "сб", "вс"};
    ArrayList<PeriodFragment.Day> days;
    ViewPager pager;
    PagerAdapter pagerAdapter;
    LinearLayout linear1;
    int daynum;
    int index;
    int lastposition;
    Boolean isnull = false;
    ArrayList<PageFragment> pageFragments;

    public ScheduleFragment () {
        datenow = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(datenow);
        day = c.get(Calendar.DAY_OF_WEEK);
        sasha("Day of week:" + day + "  " + datenow);
        pageFragments = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            pageFragments.add(new PageFragment());
        }

    }

    void setDays(ArrayList<PeriodFragment.Day> days) {
        this.days = days;
        SimpleDateFormat dateFormat = new SimpleDateFormat("D", Locale.ENGLISH);
        int A = Integer.parseInt(dateFormat.format(datenow));
        pageFragments.get(pageCount / 2).dayofyear = A;
        Long daymsec = days.get(0).daymsec;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(daymsec);
        int B = calendar.get(Calendar.DAY_OF_YEAR);
        for (int i = pageCount / 2 + 1; i < pageCount; i++) {
            pageFragments.get(i).dayofyear = pageFragments.get(i - 1).dayofyear + 1;
            pageFragments.get(pageCount - i - 1).dayofyear = pageFragments.get(pageCount - i).dayofyear - 1;
        }
        for (int i = 0; i < pageCount; i++) {
            if (pageFragments.get(i).dayofyear == B) {
                daynum = i;
                break;
            }
        }
        index = 0;
        for (int i = daynum; i < pageCount; i++) {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTimeInMillis(days.get(index).daymsec);
            int C = calendar1.get(Calendar.DAY_OF_YEAR);
            if (C == pageFragments.get(i).dayofyear) {
                pageFragments.get(i).day = days.get(index);
                if (index + 1 == days.size()) {
                    break;
                } else {
                    index++;
                }
            }
        }
    }

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        linear1 = v.findViewById(R.id.liner1);
        linear1.setWeightSum(1);
        pager = v.findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(pageCount / 2 + 1);
        for (int i = 0; i < 7; i++) {
            tv[i] = new TextView(getActivity().getApplicationContext());
            tv[i].setId(i);
            tv[i].setGravity(Gravity.CENTER);
            tv[i].setTextColor(Color.WHITE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(datenow);
            calendar.add(Calendar.DAY_OF_MONTH, i + 1 - day);
            String s = days1[i] + "\n" + calendar.get(Calendar.DAY_OF_MONTH);
            Spannable spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.WHITE), s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv[i].setText(days1[i]);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = (float) 1 / 7;
            tv[i].setLayoutParams(p);
            final int finalI = i;
            tv[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    pager.setCurrentItem(pager.getCurrentItem() - (day - finalI - 1));
                    day = finalI + 1;
                    okras(tv);
                    tv[finalI].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
                    tv[finalI].setTextColor(Color.BLACK);
                }
            });
            linear1.addView(tv[i]);
        }
        tv[day - 1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
        tv[day - 1].setTextColor(Color.BLACK);
        lastposition = pager.getCurrentItem();
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                okras(tv);
                day -= lastposition - position;
                if (day < 1) {
                    day = 7;
                }
                if (day > 7) {
                    day = 1;
                }
                tv[day - 1].setBackground(getResources().getDrawable(R.drawable.cell_phone1));
                tv[day - 1].setTextColor(Color.BLACK);
                lastposition = pager.getCurrentItem();

                sasha(String.valueOf(position));
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Schedule");
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        return v;
    }

    public void okras(TextView [] tv){
        for (int i = 0; i < 7; i++) {
            tv[i].setBackground(null);
            tv[i].setTextColor(Color.WHITE);
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return pageFragments.get(position);
        }

        @Override
        public int getCount() {
            return pageCount;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        menu.add(0, 1, 0, "Выход");
        MenuItem item = menu.add(0, 2, 0, "Настройки");
        item.setIcon(R.drawable.settings);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            ((MainActivity) getActivity()).quit();
        }
        return super.onOptionsItemSelected(item);
    }
}