package ru.gurhouse.sch;

import android.app.Activity;
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

import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.SettingsActivity.getColorFromAttribute;


public class DayFragment extends Fragment {

    String homework = "", name = "", topic = "", teachername = "", meetingInvite;
    ArrayList<PeriodFragment.File> files = new ArrayList<>();
    ArrayList<PeriodFragment.Mark> marks = new ArrayList<>();
    PeriodFragment.Subject[] subjects;
    Activity context;
    PageFragment.Attends attends;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getActivity() != null)
            context = getActivity();

        View view = inflater.inflate(R.layout.fragment_day, container, false);

        LinearLayout linearLayout = view.findViewById(R.id.container);
        linearLayout.setBaselineAligned(false);
        TextView tv1;
        if (!homework.replaceAll(" ", "").equals("")) {
            tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = "Домашнее задание:" + "\n" + homework;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setTextIsSelectable(true);
            tv1.setAutoLinkMask(Linkify.ALL);
            tv1.setPadding(50, 50, 50, 0);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if(files.size() > 0) {
            tv1 = new TextView(getContext());
            Spannable sp = new SpannableString("Файлы:");
            sp.setSpan(new RelativeSizeSpan(1.7f), 0, 5, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            sp.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(sp);
            tv1.setPadding(50, 50, 50, 10);
            linearLayout.addView(tv1);
        }
        for (int i = 0; i < files.size(); i++) {
            final PeriodFragment.File file = files.get(i);
            tv1 = new TextView(getContext());
            tv1.setText(file.name);
            tv1.setTextColor(getColorFromAttribute(R.attr.file, getContext().getTheme()));
            tv1.setOnClickListener(v -> {
                try {
                    String url = "https://app.eschool.center/ec-server/files/" + file.id;
                    ((MainActivity) getContext()).saveFile(url, file.name, true);
                } catch (Exception e) {loge(e);}
            });
            tv1.setPadding(50, 10, 50, 0);
            linearLayout.addView(tv1);
        }
        if (marks.size() != 0) {
            int g = 0;
            for (int i = 0; i < marks.size(); i++) {
                if (marks.get(i).value != null && !marks.get(i).value.equals("") && !marks.get(i).value.equals(" ")) {
                    g++;
                }
            }
            if (g > 0) {
                TextView tv2 = new TextView(getContext());
                tv2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                String s2;
                if (g > 1) {
                    s2 = "Оценки:";
                } else {
                    s2 = "Оценкa:";
                }
                Spannable spans2 = new SpannableString(s2);
                spans2.setSpan(new RelativeSizeSpan(1.7f), 0, s2.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans2.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv2.setText(spans2);
                tv2.setPadding(50, 50, 50, 10);
                linearLayout.addView(tv2);
                for (int i = 0; i < marks.size(); i++) {
                    if (marks.get(i).value != null && !marks.get(i).value.equals("") && !marks.get(i).value.equals(" ")) {
                        tv1 = new TextView(getContext());
                        String s1 = marks.get(i).value + "   " + marks.get(i).coefficient;
                        Spannable spans1 = new SpannableString(s1);
                        spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("   "), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s1.indexOf("   "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spans1.setSpan(new RelativeSizeSpan(1.2f), s1.indexOf("   "), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), s1.indexOf("   "), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv1.setText(spans1);
                        tv1.setGravity(Gravity.CENTER);
                        tv1.setPadding(80, 10, 80, 10);
                        final int finalI = i;
                        tv1.setOnClickListener(v -> {
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
                                for (int j = 0; j < subjects.length; j++) {
                                    if (marks.get(finalI).unitid - subjects[j].unitid == 0) {
                                        fragment.subject = subjects[j].name;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                loge(e);
                            }
                            transaction.addToBackStack(null);
                            transaction.commit();
                        });
                        linearLayout.addView(tv1);
                    }
                }
            }
        }
        if (!topic.equals(" ") && !topic.equals("")) {
            tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = "Тема:" + "\n" + topic;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans1.setSpan(new RelativeSizeSpan(1.2f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 50);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if (attends != null) {
            tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            boolean flag = true;

            String s1 = "Присутствие:\n";
            Spannable spans1 = new SpannableString(s1);
            switch (attends.id) {
                case 1:
                    s1 += "По причине болезни";
                    spans1 = new SpannableString(s1);
                    break;
                case 6:
                    s1 += "Неизвестно";
                    spans1 = new SpannableString(s1);
                    break;
                case 2:
                    s1 += "Опоздал";
                    spans1 = new SpannableString(s1);
                    break;
                case 3:
                    s1 += "Освобожден";
                    spans1 = new SpannableString(s1);
                    break;
                case 4:
                    s1 += "Прогул";
                    spans1 = new SpannableString(s1);
                    break;
                case 8:
                    s1 += "По уважительной причине";
                    spans1 = new SpannableString(s1);
                    break;
                default:
                    flag = false;
            }
            if(flag) {
                spans1.setSpan(new RelativeSizeSpan(1.2f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv1.setText(spans1);
                tv1.setPadding(50, 50, 50, 50);
                tv1.setGravity(Gravity.CENTER_VERTICAL);
                linearLayout.addView(tv1);
            }
        }
        if(meetingInvite != null && !meetingInvite.replaceAll(" ", "").equals("")) {
            tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tv1.setPadding(50, 50, 50, 50);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            tv1.setAutoLinkMask(Linkify.ALL);
            tv1.setTextIsSelectable(true);
            String s = "Видеоконференция:\n" + meetingInvite;
            Spannable spannable = new SpannableString(s);
            spannable.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(1.7f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            tv1.setText(spannable);
            linearLayout.addView(tv1);
        }
        if (!teachername.replaceAll(" ", "").equals("")) {
            tv1 = new TextView(getContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = teachername;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.2f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setGravity(Gravity.CENTER);
            tv1.setPadding(50, 50, 50, 50);
            linearLayout.addView(tv1);
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        setHasOptionsMenu(true);
        ((MainActivity) getContext()).setSupActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null)
            context = getActivity();
    }

    @Override
    @NonNull
    public Activity getContext() {
        return (context==null?getActivity():context);
    }
}

