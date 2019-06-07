package com.example.sch;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class DayFragment extends Fragment {

    String homework = "";
    ArrayList<PeriodFragment.Mark> marks = new ArrayList<>();
    String topic = "";
    String teachername = "";
    ArrayList<PeriodFragment.Subject> subjects;
    Context context;

    public DayFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getActivity() != null)
            context = getActivity();

        View view = inflater.inflate(R.layout.fragment_day, container, false);

        LinearLayout linearLayout = view.findViewById(R.id.container);
        linearLayout.setBaselineAligned(false);
        if (homework != " " && homework != "") {
            TextView tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = new StringBuilder().append("Домашнее задание:").append("\n").append(homework).toString();
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            try {
                Linkify.addLinks(tv1, Linkify.WEB_URLS);
                tv1.setLinksClickable(true);
            } catch (Exception e) {
                System.out.println(e);
            }
            tv1.setPadding(50, 50, 50, 10);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if (marks.size() != 0) {
            int g = 0; // та самая мусорная переменная (количество непустых оценок)
            for (int i = 0; i < marks.size(); i++) {
                if (marks.get(i).value != null && marks.get(i).value != "" && marks.get(i).value != " ") {
                    g++;
                }
            }
            if (g > 0) {
                TextView tv2 = new TextView(getContext());
                tv2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                String s2;
                if (g > 1) {
                    s2 = new StringBuilder().append("Оценки:").toString();
                } else {
                    s2 = new StringBuilder().append("Оценкa:").toString();
                }
                Spannable spans2 = new SpannableString(s2);
                spans2.setSpan(new RelativeSizeSpan(1.7f), 0, s2.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv2.setText(spans2);
                tv2.setPadding(50, 50, 50, 10);
                linearLayout.addView(tv2);
                for (int i = 0; i < marks.size(); i++) {
                    if (marks.get(i).value != null && marks.get(i).value != "" && marks.get(i).value != " ") {
                        TextView tv1 = new TextView(getContext());
                        String s1 = new StringBuilder().append(marks.get(i).value).append("   ").append(marks.get(i).coefficient).toString();
                        Spannable spans1 = new SpannableString(s1);
                        spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("   "), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("   "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("   "), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("   "), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv1.setText(spans1);
                        tv1.setGravity(Gravity.CENTER);
                        tv1.setPadding(80, 10, 80, 10);
                        final int finalI = i;
                        final int finalJ;
                        tv1.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                MarkFragment fragment = new MarkFragment();
                                transaction.replace(R.id.frame, fragment);
                                try {
                                    PeriodFragment.Cell cell = marks.get(finalI).cell;
                                    fragment.coff = cell.mktWt;
                                    fragment.data = cell.date;
                                    fragment.markdata = cell.markdate;
                                    fragment.teachname = cell.teachFio;
                                    fragment.topic = cell.lptname;
                                    fragment.value = cell.markvalue;
                                    for (int j = 0; j < subjects.size(); j++) {
                                        if (marks.get(finalI).unitid - subjects.get(j).unitid == 0) {
                                            fragment.subject = subjects.get(j).name;
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                }
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                        });
                        linearLayout.addView(tv1);
                    }
                }
            }
        }
        if (topic != " " && topic != "") {
            TextView tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = new StringBuilder().append("Тема:").append("\n").append(topic).toString();
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 50);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if (teachername != " " && teachername != "") {
            TextView tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = new StringBuilder().append(teachername).toString();
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.1f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setGravity(Gravity.CENTER);
            tv1.setPadding(50, 50, 50, 50);
            linearLayout.addView(tv1);
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        //  todo заголовок на тулбаре - дата
        toolbar.setTitle("Day");
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public Context getContext() {
        return context;
    }
}

