package com.example.sch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class DayFragment extends Fragment {

    String homework = "";
    ArrayList<ScheduleFragment.Mark> marks = new ArrayList<>();
    String topic = "";
    String teachername = "";

    public DayFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);

        LinearLayout linearLayout = view.findViewById(R.id.ll);
        linearLayout.setBaselineAligned(false);
        if (homework != " " && homework != "") {
            TextView tv1 = new TextView(getActivity().getApplicationContext());
            String s1 = new StringBuilder().append("Домашнее задание:").append("\n").append(homework).toString();
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 10);
            tv1.setGravity(Gravity.NO_GRAVITY);
            linearLayout.addView(tv1);
        }
        if (marks.size() != 0) {
            TextView tv2 = new TextView(getActivity().getApplicationContext());
            String s2;
            if (marks.size() > 1) {
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
                TextView tv1 = new TextView(getActivity().getApplicationContext());
                String s1 = new StringBuilder().append(marks.get(i).value).append("   ").append(marks.get(i).coefficient).toString();
                Spannable spans1 = new SpannableString(s1);
                spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("   "), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("   "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("   "), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("   "), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv1.setText(spans1);
                tv1.setGravity(Gravity.CENTER);
                tv1.setPadding(80, 10, 80, 10);
                linearLayout.addView(tv1);
            }
        }
        if (topic != " " && topic != "") {
            TextView tv1 = new TextView(getActivity().getApplicationContext());
            String s1 = new StringBuilder().append("Тема:").append("\n").append(topic).toString();
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 50);
            linearLayout.addView(tv1);
        }
        if (teachername != " " && teachername != "") {
            TextView tv1 = new TextView(getActivity().getApplicationContext());
            String s1 = new StringBuilder().append(teachername).toString();
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.1f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setGravity(Gravity.CENTER);
            tv1.setPadding(50, 50, 50, 50);
            linearLayout.addView(tv1);
        }
        return view;
    }
}

