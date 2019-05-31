package com.example.sch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PeriodFragment1 extends Fragment {


    ArrayList<TextView> txts;
    String[] period;
    ScheduleFragment.Period[] periods = new ScheduleFragment.Period[7];
    int pernum = 6;
    LinearLayout layout, layout1, layout2, layout3;
    View view;
    String periodname;
    Toolbar toolbar;
    AlertDialog.Builder alr;
    boolean shown = false;
    boolean first_time = true;

    public PeriodFragment1() {
        txts = new ArrayList<>();
    }

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                pernum = lv.getCheckedItemPosition();
                if (periods[pernum].subjects == null) {
                    ((MainActivity) getActivity()).scheduleFragment.Download2(periods[pernum].id, pernum, true, false);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scrollView2).setVisibility(View.INVISIBLE);
                } else {
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    show();
                }
                sasha("------------------------");
            } else {
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        first_time = false;
        if(view == null)
            view = inflater.inflate(R.layout.diary, container, false);
        if(period==null)
            return view;

        toolbar = getActivity().findViewById(R.id.toolbar);
        periodname = period[pernum];
        alr = new AlertDialog.Builder(getContext());
        alr.create();
        alr.setSingleChoiceItems(period, pernum, myClickListener);
        alr.setTitle("Выберите период");
        alr.setPositiveButton("ok", myClickListener);
        toolbar.setTitle(periodname);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alr.show();
            }
        });
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        sasha("sasa");
        if(periods[pernum] != null && !shown)
            show();
        return view;
    }

    void show() {
        sasha("asdfghjkl");
        shown = true;
        StringBuilder y = new StringBuilder();
        if (getActivity().getSharedPreferences("pref", 0).getString("firstperiod", "").equals("")) {
            Toast.makeText(getContext(), "Вы можете поменять участок времени, нажав на него в верху экрана", Toast.LENGTH_LONG).show();
            getActivity().getSharedPreferences("pref", 0).edit().putString("firstperiod", "dvssc").apply();
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
        sasha("subjects: " + periods[pernum].subjects.size());
        for (int i = -1; i < periods[pernum].subjects.size(); i++) {
            TextView txt1 = new TextView(getActivity().getApplicationContext());
            TextView txt2 = new TextView(getActivity().getApplicationContext());
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
                if (periods[pernum].subjects.get(i).avg > 0)
                    txt2.setText(String.valueOf(periods[pernum].subjects.get(i).avg));
                final int finalI1 = i;
                try {
                    txt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SwitchToSubjectFragment(periods[pernum].subjects.get(finalI1).avg, periods[pernum].subjects.get(finalI1).cells, periods[pernum].subjects.get(finalI1).name, periods[pernum].subjects.get(finalI1).rating, periods[pernum].subjects.get(finalI1).totalmark);
                        }
                    });
                } catch (Exception e) {
                }
                try {
                    txt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SwitchToSubjectFragment(periods[pernum].subjects.get(finalI1).avg, periods[pernum].subjects.get(finalI1).cells, periods[pernum].subjects.get(finalI1).name, periods[pernum].subjects.get(finalI1).rating, periods[pernum].subjects.get(finalI1).totalmark);
                        }
                    });
                } catch (Exception e) {
                }
            }
            layout2.addView(txt2);
            layout1.addView(txt1);
        }
        sasha("lins size: " + periods[pernum].lins.size());
        for (int j = 0; j < periods[pernum].lins.size(); j++) {
            if (periods[pernum].lins.get(j).getParent() != null)
                ((ViewGroup) periods[pernum].lins.get(j).getParent()).removeView(periods[pernum].lins.get(j));
            layout3.addView(periods[pernum].lins.get(j));
        }
        setHasOptionsMenu(true);

        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
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
        } catch (Exception e) {
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    void set() {}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}