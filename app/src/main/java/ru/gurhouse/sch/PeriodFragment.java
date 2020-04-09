package ru.gurhouse.sch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.ScheduleFragment.syncing;
import static ru.gurhouse.sch.SettingsActivity.getColorFromAttribute;

public class PeriodFragment extends Fragment {

    ArrayList<TextView> txts;
    View view;
    String[] period;
    ScheduleFragment.Period[] periods = new ScheduleFragment.Period[7];
    int pernum = 0;
    Toolbar toolbar;
    AlertDialog.Builder alr;
    boolean shown = false;
    String periodname;
    boolean first_time = true;
    boolean nullsub = false;
    TextView txtnull;
    boolean mode, avg_fixed;
    Activity context;

     static final double[] coefs = {0.5, 1, 1.25, 1.35, 1.5, 1.75, 2};
     static int[] colors = {R.color.coff1, R.color.coff2, R.color.coff3, R.color.coff4, R.color.coff5,
            R.color.coff6, R.color.coff7, R.color.coff8};

    static boolean settingsClicked = false;

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                TheSingleton.getInstance().t1 = System.currentTimeMillis();
                pernum = lv.getCheckedItemPosition();
                if (periods[pernum].subjects == null) {
                    log("PerF: change of period and download (" + pernum + "), name - " + periods[pernum].name + ", id - " + periods[pernum].id);
                    ((MainActivity) getContext()).scheduleFragment.Download2(pernum/*, false, true*/);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scrollView2).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.txtnull).setVisibility(View.INVISIBLE);
                } else {
                    log("PerF: change of period (" + pernum + "), name - " + periods[pernum].name);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    if (periods[pernum].nullsub) {
                        log("PerF: nullshow()");
                        ((MainActivity) getContext()).nullsub(periods, pernum);
                    } else {
                        log("PerF: show()");
                        ((MainActivity) getContext()).set(periods, pernum);
                        /*if (((MainActivity) getContext()).getMode0()) {
                            log("PerF");

                        } else {
                            log("PerF1");

                        }*/
                    }
                }
            }
        }
    };
    LinearLayout layout1, layout2, layout3, layout;

    public PeriodFragment() {
        txts = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        log("PerF/OnCreateView (alternative)");
        log("onCreateView PerF");
        if(getActivity() != null)
            context = getActivity();
        first_time = false;
        if (view == null) {
            avg_fixed = getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false);
            if (avg_fixed)
                view = inflater.inflate(R.layout.diary_fixed, container, false);
            else
                view = inflater.inflate(R.layout.diary, container, false);
        } else if(avg_fixed ^ getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false)) {
            avg_fixed = getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false);
            if (avg_fixed)
                view = inflater.inflate(R.layout.diary_fixed, container, false);
            else
                view = inflater.inflate(R.layout.diary, container, false);
            shown = false;
        }
        log("avgfixed " + avg_fixed);
        Resources.Theme theme = getContext().getTheme();
        colors = new int[]{
                getColorFromAttribute(R.attr.coff1, theme),
                getColorFromAttribute(R.attr.coff2, theme),
                getColorFromAttribute(R.attr.coff3, theme),
                getColorFromAttribute(R.attr.coff4, theme),
                getColorFromAttribute(R.attr.coff5, theme),
                getColorFromAttribute(R.attr.coff6, theme),
                getColorFromAttribute(R.attr.coff7, theme),
                getColorFromAttribute(R.attr.coff8, theme)
        };

        if (period == null && !nullsub)
            return view;
        toolbar = getContext().findViewById(R.id.toolbar);
        alr = new AlertDialog.Builder(getContext());
        alr.create();
        periodname = period[pernum];
        toolbar.setTitle(periodname);
        alr.setSingleChoiceItems(period, pernum, myClickListener);
        alr.setTitle("Выберите период");
        alr.setPositiveButton("ok", myClickListener);
        toolbar.setOnClickListener(v -> alr.show());
        setHasOptionsMenu(true);

        ((MainActivity) getContext()).setSupportActionBar(toolbar);
        if (nullsub) {
            nullshow();
            return view;
        }
        if (periods[pernum] != null && !shown)
            show();
        return view;
    }

    void nullshow() {
        txtnull = view.findViewById(R.id.txtnull);
        txtnull.setTextSize(20);
        txtnull.setTextColor(getColorFromAttribute(R.attr.second_font, getContext().getTheme()));
        txtnull.setText("Нет оценок за выбранный период");
        txtnull.setPadding(90, 0, 90, 0);
        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        txtnull.setVisibility(View.VISIBLE);
        view.findViewById(R.id.scrollView2).setVisibility(View.INVISIBLE);
        log("PerF/OnCreateView: nullsub");
    }

    void show() {
        shown = true;
        log("show() PerF, pernum " + pernum + ", len: " + periods[pernum].subjects.length);
        if (getContext().getSharedPreferences("pref", 0).getString("firstperiod", "").equals("")) {
            Toast.makeText(getContext(), "Вы можете поменять участок времени, нажав на него в верху экрана", Toast.LENGTH_LONG).show();
            getContext().getSharedPreferences("pref", 0).edit().putString("firstperiod", "dvssc").apply();
        }
        layout = view.findViewById(R.id.linear);
        layout1 = view.findViewById(R.id.linear1);
        layout1.removeAllViews();
        layout2 = view.findViewById(R.id.linear2);
        layout2.removeAllViews();
        layout3 = view.findViewById(R.id.linear3);
        layout3.removeAllViews();
        log("PerF/show: names of subjects");
        for (int i = 0; i < periods[pernum].subjects.length; i++) {
            log((i + 1) + ". " + periods[pernum].subjects[i].shortname);
        }
        for (int i = 0; i < periods[pernum].subjects.length; i++) {
            TextView txt1 = new TextView(getContext());
            TextView txt2 = new TextView(getContext());
            LinearLayout linearLayout = new LinearLayout(getContext());
            txt1.setTextColor(getColorFromAttribute(R.attr.main_font, getContext().getTheme()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 40, 10);
            txt1.setLayoutParams(lp);
            txt1.setGravity(Gravity.CENTER);
            txt1.setTextSize(20);
            txt2.setTextSize(20);
            txt2.setLayoutParams(lp);
            txt2.setTextColor(getColorFromAttribute(R.attr.avg, getContext().getTheme()));
            txt1.setText(periods[pernum].subjects[i].shortname);

            if (periods[pernum].subjects[i].avg > 0) {
                txt2.setText(String.valueOf(periods[pernum].subjects[i].avg));
            } else {
                periods[pernum].subjects[i].avg = 0;
                double d = 0.;
                double f = 0.;
                int c = 0;
                for (int g = 0; g < periods[pernum].subjects[i].cells.length; g++) {
                    if (periods[pernum].subjects[i].cells[g].markvalue != null)
                        if (periods[pernum].subjects[i].cells[g].markvalue.equals("1") || periods[pernum].subjects[i].cells[g].markvalue.equals("2") || periods[pernum].subjects[i].cells[g].markvalue.equals("3")
                                || periods[pernum].subjects[i].cells[g].markvalue.equals("4") || periods[pernum].subjects[i].cells[g].markvalue.equals("5")) {
                            d += Double.parseDouble(periods[pernum].subjects[i].cells[g].markvalue) * periods[pernum].subjects[i].cells[g].mktWt;
                            f += periods[pernum].subjects[i].cells[g].mktWt;
                            c++;
                        }
                }
                if (c > 0) {
                    String s = String.valueOf(d / f);
                    if (s.length() > 4) {
                        s = String.format(Locale.UK, "%.2f", d / f);
                    }
                    periods[pernum].subjects[i].avg = Double.parseDouble(s);
                    txt2.setText(String.valueOf(periods[pernum].subjects[i].avg));
                } else
                    txt2.setText(" ");
            }
            final Subject sub = periods[pernum].subjects[i];
            txt2.setOnClickListener(v -> SwitchToSubjectFragment(sub.avg, sub.name, sub.rating, sub.totalmark, sub.periodType));
            txt1.setOnClickListener(v -> SwitchToSubjectFragment(sub.avg, sub.name, sub.rating, sub.totalmark, sub.periodType));
            layout2.addView(txt2);
            layout1.addView(txt1);

            int g = 0, color;
            for (int j = 0; j < periods[pernum].subjects[i].cells.length; j++) {
                if (periods[pernum].subjects[i].cells[j].markvalue != null && periods[pernum].subjects[i].cells[j].markvalue != "") {
                    g++;
                    double d = periods[pernum].subjects[i].cells[j].mktWt;
                    txts.add(new TextView(getContext()));
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp1.setMargins(0, 0, 10, 10);
                    txts.get(txts.size() - 1).setLayoutParams(lp1);
                    try {
                        final int finalI = i;
                        final int finalJ = j;
                        txts.get(txts.size() - 1).setOnClickListener(v -> {

                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            MarkFragment fragment = new MarkFragment();
                            transaction.replace(R.id.frame, fragment);
                            try {
                                fragment.coff = periods[pernum].subjects[finalI].cells[finalJ].mktWt;
                                fragment.data = periods[pernum].subjects[finalI].cells[finalJ].date;
                                fragment.markdata = periods[pernum].subjects[finalI].cells[finalJ].markdate;
                                fragment.teachname = periods[pernum].subjects[finalI].cells[finalJ].teachFio;
                                fragment.topic = periods[pernum].subjects[finalI].cells[finalJ].lptname;
                                fragment.value = periods[pernum].subjects[finalI].cells[finalJ].markvalue;
                                fragment.subject = periods[pernum].subjects[finalI].name;
                            } catch (Exception e) {
                                loge(e);
                            }
                            transaction.addToBackStack(null);
                            transaction.commit();
                        });
                    } catch (Exception e) {
                        loge(e);
                    }
                    txts.get(txts.size() - 1).setTextSize(20);
                    txts.get(txts.size() - 1).setTextColor(getColorFromAttribute(R.attr.main_font, getContext().getTheme()));
                    txts.get(txts.size() - 1).setBackground(getResources().getDrawable(R.drawable.gradient_list, getContext().getTheme()));
                    txts.get(txts.size() - 1).setPadding(15, 0, 15, 0);

                    color = Arrays.binarySearch(coefs, d);
                    if(color < 0)
                        color = -color-1;

                    txts.get(txts.size() - 1).setBackgroundColor(colors[color]);

                    if (periods[pernum].subjects[i].cells[j].markvalue != null)
                        txts.get(txts.size() - 1).setText(periods[pernum].subjects[i].cells[j].markvalue);
                    else {
                        txts.get(txts.size() - 1).setText("7");
                        txts.get(txts.size() - 1).setTextColor(Color.TRANSPARENT);
                    }
                    linearLayout.addView(txts.get(txts.size() - 1));
                }
            }
            if (g == 0) {
                TextView txtnull = new TextView(getContext());
                txtnull.setText("7");
                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp1.setMargins(0, 0, 10, 10);
                txtnull.setLayoutParams(lp1);
                txtnull.setTextSize(20);
                txtnull.setTextColor(Color.TRANSPARENT);
                txtnull.setPadding(15, 0, 15, 0);
                linearLayout.addView(txtnull);
            }
            layout3.addView(linearLayout);
        }

        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
        view.findViewById(R.id.txtnull).setVisibility(View.INVISIBLE);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    void refresh() {
        ((MainActivity) getContext()).scheduleFragment.Download2(pernum);
    }

    public void SwitchToSubjectFragment(Double avg, String name, String rating, String totalmark, boolean periodType) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SubjectFragment fragment = new SubjectFragment();
        transaction.replace(R.id.frame, fragment);
        try {
            fragment.avg = avg;
            fragment.subname = name;
            fragment.rating = rating;
            fragment.totalmark = totalmark;
            fragment.period = period;
            fragment.pernum = pernum;
            fragment.periods = periods;
            fragment.periodType = periodType;
        } catch (Exception e) {
            loge(e);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    boolean recreating = false;
    @Override
    public void onResume() {
        log("onResume PerF");
        if(getContext() != null) {
            ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if(!recreating) {
            ((AppCompatActivity) getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
            recreating = true;
        } else
            recreating = false;
//        if(avg_fixed ^ getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false)) {
//            avg_fixed = getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false);
//            if (avg_fixed)
//                view = getContext().getLayoutInflater().inflate(R.layout.diary_fixed, getContext().findViewById(R.id.frame), false);
//            else
//                view = getContext().getLayoutInflater().inflate(R.layout.diary, getContext().findViewById(R.id.frame), false);
//            show();
//        }
        //((MainActivity) getContext()).getStackTop() instanceof PeriodFragment1)
        if(getContext().getSharedPreferences("pref", 0).getBoolean("period_normal", false))
            view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.INVISIBLE);
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        MenuItem item;
        item = menu.add(0, 2, 0, "Итоговые");
//        item.setIcon(R.drawable.results);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 5, 0, "Калькулятор");
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.calculator);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.toolbar_icons, getContext().getTheme()));
        item.setIcon(wrappedDrawable);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 3, 1, "Настройки");
        unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.settings);
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.toolbar_icons, getContext().getTheme()));
        item.setIcon(wrappedDrawable);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 4, 2, "Обновить");
        unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.refresh);
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.toolbar_icons, getContext().getTheme()));
        item.setIcon(wrappedDrawable);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    static class ODOD {
        long daymsec;
        String day, name;
        int duration;
        int ODODid;

        ODOD() {
        }
    }

    static class Day {
        long daymsec;
        String day;
        int numday;
        ArrayList<ODOD> odods;
        ArrayList<Lesson> lessons;

        Day() {
        }
    }

    static class Lesson {
        int numInDay;
        String name = "", teachername = "", topic = "", shortname = "";
        HomeWork homeWork;
        ArrayList<Mark> marks = new ArrayList<>();
        long id;
        long unitId = 0;
        String meetingInvite;
        PageFragment.Attends attends;
        Lesson() {
        }

    }

    static class HomeWork {
        ArrayList<File> files;
        String stringwork = "";
        HomeWork() {
        }
    }

    static class File {
        String name;
        int id;
    }

    static class Mark {
        int unitid;
        String value, teachFio, date, topic, markdate;
        double coefficient;
        long idlesson;
        Cell cell;

        Mark() {
        }
    }

    static class Subject {
        String name, rating = "", shortname = "", totalmark;
        double avg = 0;
        int unitid;
        Cell[] cells;
        boolean periodType;

        Subject() {
        }
    }

    static class Cell {
        String lptname, markvalue, date;
        double mktWt = 0;
        long lessonid;
        String markdate, teachFio;
        int unitid;
        PageFragment.Attends attends;

        Cell() {
        }

        Cell(Cell cell) {
            if (cell.markvalue != null)
                markvalue = cell.markvalue;
            mktWt = cell.mktWt;
        }
    }

    public Activity getContext() {
        return (context == null? getActivity():context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null)
            context = getActivity();
    }

    //MenuItem itemRefresh;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(((MainActivity) getContext()).getStackTop() instanceof PeriodFragment1)
            return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            // total marks under construction (disabled)
            case 2:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                TotalMarks fragment = new TotalMarks();
                fragment.start();
                transaction.replace(R.id.frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 3:
                if(!settingsClicked) {
                    settingsClicked = true;
                    //((AppCompatActivity) getContext()).getSupportFragmentManager().popBackStack();
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    startActivityForResult(intent, 0);
                }
                break;
            case 4:
                item.setEnabled(false);
                toolbar.getMenu().getItem(0).setEnabled(false);
                //itemRefresh = item;
                refresh();
                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                        getContext().runOnUiThread(() -> {
                            item.setEnabled(true);
                            toolbar.getMenu().getItem(0).setEnabled(false);
                        });
                    } catch (Exception e) {loge(e);}
                }).start();
                break;
            case 5:
                if(!syncing && period != null /*&& (itemRefresh == null || itemRefresh.isEnabled())*/) {
                    transaction = getFragmentManager().beginTransaction();
                    Countcoff fragment2 = new Countcoff();
                    transaction.replace(R.id.frame, fragment2);
                    fragment2.periods = periods;
                    fragment2.period = period;
                    fragment2.pernum = pernum;
                    fragment2.subname = periods[pernum].subjects[0].name;
                    fragment2.avg = periods[pernum].subjects[0].avg;
                    fragment2.periodType = periods[pernum].subjects[0].periodType;
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
        }
        return super.onOptionsItemSelected(item);
    }
}