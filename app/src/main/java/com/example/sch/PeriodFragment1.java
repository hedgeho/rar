package com.example.sch;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

public class PeriodFragment1 extends Fragment {


    ArrayList<PeriodFragment.Subject> subjects;
    ArrayList<TextView> txts;
    ArrayList<PeriodFragment.Day> days;
    ArrayList<LinearLayout> lins;
    LinearLayout layout, layout1, layout2, layout3;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary, container, false);
        // todo same with period0
        layout = view.findViewById(R.id.linear);
        layout1 = view.findViewById(R.id.linear1);
        layout2 = view.findViewById(R.id.linear2);
        layout3 = view.findViewById(R.id.linear3);
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout3.setOrientation(LinearLayout.HORIZONTAL);
        sasha("subjects: " + subjects.size());
        for (int i = -1; i < subjects.size(); i++) {
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
                txt1.setText(subjects.get(i).shortname);
                if (subjects.get(i).avg > 0)
                    txt2.setText(String.valueOf(subjects.get(i).avg));
                final int finalI1 = i;
                try {
                    txt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SwitchToSubjectFragment(subjects.get(finalI1).avg, subjects.get(finalI1).cells, subjects.get(finalI1).name, subjects.get(finalI1).rating, subjects.get(finalI1).totalmark);
                        }
                    });
                } catch (Exception e) {
                }
                try {
                    txt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SwitchToSubjectFragment(subjects.get(finalI1).avg, subjects.get(finalI1).cells, subjects.get(finalI1).name, subjects.get(finalI1).rating, subjects.get(finalI1).totalmark);
                        }
                    });
                } catch (Exception e) {
                }
            }
            layout2.addView(txt2);
            layout1.addView(txt1);
        }
        sasha("lins size: " + lins.size());
        for (int j = 0; j < lins.size(); j++) {
            if (lins.get(j).getParent() != null)
                ((ViewGroup) lins.get(j).getParent()).removeView(lins.get(j));
            layout3.addView(lins.get(j));
        }
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Оценки за период");
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
        return view;
    }

    public void SwitchToSubjectFragment(Double avg, ArrayList<PeriodFragment.Cell> cells, String name, String rating, String totalmark) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SubjectFragment fragment = new SubjectFragment();
        transaction.replace(R.id.frame, fragment);
        try {
            fragment.avg = avg;
            fragment.cells = cells;
            fragment.subname = name;
            fragment.rating = rating;
            fragment.totalmark = totalmark;
            fragment.subjects = subjects;
        } catch (Exception e) {
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
//        menu.add(0, 1, 0, "Quit");
//        menu.add(0, 2, 0, "CRASH");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            ((MainActivity) getActivity()).quit();
        } else if (item.getItemId() == 2) {
            Crashlytics.getInstance().crash();
        }
        return super.onOptionsItemSelected(item);
    }
}