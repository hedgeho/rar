package ru.gurhouse.sch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.ScheduleFragment.syncing;

public class PeriodFragment1 extends Fragment {
    ArrayList<TextView> txts;
    String[] period;
    ScheduleFragment.Period[] periods;
    int pernum = 6;
    LinearLayout layout, layout1, layout2, layout3;
    View view;
    ScrollView scrollView;
    String periodname;
    Toolbar toolbar;
    AlertDialog.Builder alr;
    int f = 0;
    boolean shown = false;
    boolean first_time = true;
    boolean avg_fixed;
    Activity context;

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                pernum = lv.getCheckedItemPosition();
                if (periods[pernum].subjects == null) {
                    ((MainActivity) getContext()).scheduleFragment.Download2(pernum/*, true, false*/);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scrollView3).setVisibility(View.INVISIBLE);
                } else {
                    log("PerF1: change of period (" + pernum + "), name - " + periods[pernum].name);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    if (periods[pernum].nullsub) {
                        log("PerF: nullshow()");
                        ((MainActivity) getContext()).nullsub(periods, pernum);
                    } else {
                        log("PerF: show()");
                        ((MainActivity) getContext()).set(periods, pernum);
                        show();
                    }
                }
            }
        }
    };

    public PeriodFragment1() {
        txts = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView PerF1: " + this);
        if(getActivity() != null)
            context = getActivity();
        first_time = false;
        if(view == null)
            view = inflater.inflate(R.layout.diary1, container, false);
        if (view == null) {
            avg_fixed = getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false);
            if (avg_fixed)
                view = inflater.inflate(R.layout.diary1_fixed, container, false);
            else
                view = inflater.inflate(R.layout.diary1, container, false);
        } else if(avg_fixed ^ getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false)) {
            avg_fixed = getContext().getSharedPreferences("pref", 0).getBoolean("avg_fixed", false);
            if (avg_fixed)
                view = inflater.inflate(R.layout.diary1_fixed, container, false);
            else
                view = inflater.inflate(R.layout.diary1, container, false);
            shown = false;
        }
        if(period==null)
            return view;

        toolbar = getContext().findViewById(R.id.toolbar);
        scrollView = view.findViewById(R.id.scrollView3);
        periodname = period[pernum];
        alr = new AlertDialog.Builder(getContext());
        alr.create();
        alr.setSingleChoiceItems(period, pernum, myClickListener);
        alr.setTitle("Выберите период");
        alr.setPositiveButton("ok", myClickListener);
        toolbar.setTitle(periodname);
        toolbar.setOnClickListener(v -> alr.show());
        setHasOptionsMenu(true);

//        refreshL = view.findViewById(R.id.refr1);
//        refreshL.setOnRefreshListener(() -> {
//            refresh();
//        });

        ((MainActivity) getContext()).setSupportActionBar(toolbar);
        if(periods[pernum] != null && !shown)
            show();
        return view;
    }

    void show() {
        log("show() PerF1, pernum " + pernum);
        shown = true;
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
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout3.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = -1; i < periods[pernum].subjects.size(); i++) {
            TextView txt1 = new TextView(getContext().getApplicationContext());
            TextView txt2 = new TextView(getContext().getApplicationContext());
            txt1.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 40, 10);
            txt1.setLayoutParams(lp);
            txt1.setGravity(Gravity.CENTER);
            txt1.setTextSize(20);
            txt2.setTextSize(20);
            txt2.setLayoutParams(lp);
            txt2.setTextColor(getResources().getColor(R.color.two));
            if (i + 1 > 0) {
                txt1.setText(periods[pernum].subjects.get(i).shortname);
                if (periods[pernum].subjects.get(i).avg > 0) {
                    txt2.setText(String.valueOf(periods[pernum].subjects.get(i).avg));
                } else {
                    periods[pernum].subjects.get(i).avg = 0;
                    Double d = 0.;
                    Double f = 0.;
                    int c = 0;
                    for (int g = 0; g < periods[pernum].subjects.get(i).cells.size(); g++) {
                        if (periods[pernum].subjects.get(i).cells.get(g).markvalue != null)
                            if (periods[pernum].subjects.get(i).cells.get(g).markvalue.equals("1") || periods[pernum].subjects.get(i).cells.get(g).markvalue.equals("2") || periods[pernum].subjects.get(i).cells.get(g).markvalue.equals("3")
                                    || periods[pernum].subjects.get(i).cells.get(g).markvalue.equals("4") || periods[pernum].subjects.get(i).cells.get(g).markvalue.equals("5")) {
                                d += Double.valueOf(periods[pernum].subjects.get(i).cells.get(g).markvalue) * periods[pernum].subjects.get(i).cells.get(g).mktWt;
                                f += periods[pernum].subjects.get(i).cells.get(g).mktWt;
                                c++;
                            }
                    }

                    if (c > 0) {
                        String s = String.valueOf(d / f);
                        if (s.length() > 4) {
                            s = String.format(Locale.UK, "%.2f", d / f);
                        }
                        periods[pernum].subjects.get(i).avg = Double.valueOf(s);
                        txt2.setText(String.valueOf(periods[pernum].subjects.get(i).avg));
                    } else
                        txt2.setText(" ");
                }
                final PeriodFragment.Subject sub = periods[pernum].subjects.get(i);
                txt2.setOnClickListener(v ->
                        SwitchToSubjectFragment(sub.avg, sub.cells, sub.name, sub.rating, sub.totalmark));
                txt1.setOnClickListener(v -> SwitchToSubjectFragment(sub.avg, sub.cells, sub.name, sub.rating, sub.totalmark));
            }
            layout2.addView(txt2);
            layout1.addView(txt1);
        }
        for (int j = 0; j < periods[pernum].lins.size(); j++) {
            if (periods[pernum].lins.get(j).getParent() != null)
                ((ViewGroup) periods[pernum].lins.get(j).getParent()).removeView(periods[pernum].lins.get(j));
            layout3.addView(periods[pernum].lins.get(j));
        }
        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.scrollView3).setVisibility(View.VISIBLE);
        f = 1;
    }

    void refresh() {
        ((MainActivity) getContext()).scheduleFragment.Download2(pernum/*, true, false*/);
    }

    public void SwitchToSubjectFragment(Double avg, ArrayList<PeriodFragment.Cell> cells, String name, String rating, String totalmark) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SubjectFragment fragment = new SubjectFragment();
        transaction.replace(R.id.frame, fragment);
        try {
            fragment.avg = avg;
            fragment.subname = name;
            fragment.rating = rating;
            fragment.totalmark = totalmark;
            fragment.periods = periods;
            fragment.period = period;
            fragment.pernum = pernum;
        } catch (Exception ignore) {
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*void F() {
        if (pernum != 4) {
            ((MainActivity) getContext()).scheduleFragment.Download2(periods[4].id, 4, false, false);
        }
        if (pernum != 5) {
            ((MainActivity) getContext()).scheduleFragment.Download2(periods[5].id, 5, false, false);
        }
    }*/

    boolean recreating = false;
    @Override
    public void onResume() {
        log("onResume PerF1");
        if(getContext() != null) {
            ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayShowHomeEnabled(false);
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

        if(getContext().getSharedPreferences("pref", 0).getBoolean("period_normal", false))
            view.setVisibility(View.INVISIBLE);
        else
            view.setVisibility(View.VISIBLE);
        super.onResume();
    }

    public Activity getContext() {
        return (context == null? getActivity(): context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null)
            context = getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        MenuItem item;
//        item = menu.add(0, 2, 0, "Оценки");
//        item.setIcon(R.drawable.results);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 5, 0, "Калькулятор");
        item.setIcon(R.drawable.calculator);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 3, 1, "Настройки");
        item.setIcon(R.drawable.settings);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item = menu.add(0, 4, 2, "Обновить");
        item.setIcon(R.drawable.refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(((MainActivity) getContext()).getStackTop() instanceof PeriodFragment)
            return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            // under construction - screen showing total marks of the user (disabled)
            case 2:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                TotalMarks fragment = new TotalMarks();
                fragment.start();
                transaction.replace(R.id.frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 3:
                //((AppCompatActivity) getContext()).getSupportFragmentManager().popBackStack();
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 4:
                item.setEnabled(false);
                toolbar.getMenu().getItem(0).setEnabled(false);
                refresh();
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        getContext().runOnUiThread(() -> {
                            item.setEnabled(true);
                            toolbar.getMenu().getItem(0).setEnabled(false);
                        });
                    } catch (Exception e) {e.printStackTrace();}
                }).start();
                break;
            case 5:
                if(!syncing) {
                    transaction = getFragmentManager().beginTransaction();
                    Countcoff fragment2 = new Countcoff();
                    transaction.replace(R.id.frame, fragment2);
                    try {
                        fragment2.periods = periods;
                        fragment2.period = period;
                        fragment2.pernum = pernum;
                        fragment2.subname = periods[pernum].subjects.get(0).name;
                        fragment2.avg = periods[pernum].subjects.get(0).avg;
                    } catch (Exception ignore) {
                    }
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
        }
        return super.onOptionsItemSelected(item);
    }
}