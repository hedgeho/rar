package ru.gurhouse.sch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static ru.gurhouse.sch.LoginActivity.log;

public class PageFragment extends Fragment {

    TableLayout tableLayout;
    LinearLayout linearLayout;
    PeriodFragment.Day day;
    PeriodFragment.Subject[] subjects;
    Calendar c;
    ScheduleFragment.Period[] periods;
    int dayofweek;
    Context context;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page, container, false);
        tableLayout = v.findViewById(R.id.table);
        linearLayout = v.findViewById(R.id.lin);
        if(getContext() != null)
            context = super.getContext();
        draw();

        return v;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void CreateTable() {
        for (int i = 0; i < day.lessons.size(); i++) {
            final PeriodFragment.Lesson lesson = day.lessons.get(i);
            TableRow tbrow = new TableRow(getContext());
            tbrow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tbrow.setBaselineAligned(false);

            TextView tv21 = new TextView(getContext());
            tv21.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView tv22 = new TextView(getContext());
            tv22.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            LinearLayout linearLayout2 = new LinearLayout(getContext());

            TextView tv1 = new TextView(getContext());
            tv1.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            TextView tv3 = new TextView(getContext());
            tv3.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            tv21.setGravity(Gravity.CENTER_VERTICAL);
            tv22.setGravity(Gravity.CENTER_VERTICAL);
            tv1.setGravity(Gravity.CENTER);
            tv3.setGravity(Gravity.CENTER);

            tv1.setId(i);
            tv1.setTextColor(Color.WHITE);
            tv21.setId(i);
            tv22.setId(i);
            tv3.setId(i);

            try {
                tbrow.setOnClickListener(v -> {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    DayFragment fragment = new DayFragment();
                    transaction.replace(R.id.frame, fragment);
                    try {
                        fragment.homework = lesson.homeWork.stringwork;
                        fragment.files = lesson.homeWork.files;
                        fragment.teachername = lesson.teachername;
                        fragment.topic = lesson.topic;
                        fragment.marks = lesson.marks;
                        fragment.subjects = subjects;
                        fragment.name = lesson.name;
                        fragment.attends = lesson.attends;
                    } catch (Exception ignore) {
                    }
                    transaction.addToBackStack(null);
                    transaction.commit();
                });
            } catch (Exception ignore) { }

            if (i - day.lessons.size() + 1 == 0) {
                tv1.setBackground(getResources().getDrawable(R.drawable.cell_phone2));
                tv21.setBackground(getResources().getDrawable(R.drawable.cell_phone2));
                linearLayout2.setBackground(getResources().getDrawable(R.drawable.cell_phone2));
            } else {
                tv1.setBackground(getResources().getDrawable(R.drawable.cell_phone));
                linearLayout2.setBackground(getResources().getDrawable(R.drawable.cell_phone));
                tv21.setBackground(getResources().getDrawable(R.drawable.cell_phone2));
                tv3.setBackground(getResources().getDrawable(R.drawable.cell_phone3));
            }
            tv1.setText(String.valueOf(lesson.numInDay));
            try {
                String s = lesson.name;
                Spannable spans = new SpannableString(s);
                spans.setSpan(new RelativeSizeSpan(1.5f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv21.setText(spans);
            } catch (Exception ignore) {
            }
            if(lesson.attends != null && lesson.attends.name != null) {
                switch (lesson.attends.id) {
                    case 1:
                        if (i - day.lessons.size() + 1 == 0)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellsick1));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellsick0));
                        break;
                    case 6:
                        if (i - day.lessons.size() + 1 == 0)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellun1));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellun0));
                        break;
                    case 2:
                        if (i - day.lessons.size() + 1 == 0)
                            tv1.setBackground(getResources().getDrawable(R.drawable.celllate1));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.celllate0));
                        break;
                    case 3:
                        if (i - day.lessons.size() + 1 == 0)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellrel1));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellrel0));
                        break;
                    case 4:
                        if (i - day.lessons.size() + 1 == 0)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellab1));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellab0));
                        break;
                    case 8:
                        if (i - day.lessons.size() + 1 == 0)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellall1));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellall0));
                        break;
                }
            }
            try {
                String s = lesson.homeWork.stringwork;
                Spannable spans = new SpannableString(s);
                spans.setSpan(new RelativeSizeSpan(1f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv22.setText(spans);
            } catch (Exception ignore) {
            }
            tv1.setPadding(15, 0, 30, 0);
            tv21.setPadding(30, 30, 30, 15);
            tv22.setPadding(30, 0, 30, 30);
            tv3.setPadding(30, 0, 30, 0);
            tv21.setMaxLines(1);
            tv22.setMaxLines(1);
            tv21.setEllipsize(TextUtils.TruncateAt.END);
            tv22.setEllipsize(TextUtils.TruncateAt.END);
            try {
                StringBuilder s1 = new StringBuilder();
                for (int j = 0; j < lesson.marks.size(); j++) {
                    if (lesson.marks.get(j).value != null && !lesson.marks.get(j).value.equals(" ") && !lesson.marks.get(j).value.equals("")) {
                        s1.append(lesson.marks.get(j).value);
                        if (lesson.marks.size() > 1 && j != lesson.marks.size() - 1 ) {
                            s1.append("/");
                        }
                    }
                }
                if(s1.toString().charAt(s1.length()-1) == '/'){
                    s1.deleteCharAt(s1.length()-1);
                }
                s1.append("\n");
                for (int j = 0; j < lesson.marks.size(); j++) {
                    if (lesson.marks.get(j).value != null && !lesson.marks.get(j).value.equals(" ") && !lesson.marks.get(j).value.equals("")) {
                        s1.append(lesson.marks.get(j).coefficient);
                        if (lesson.marks.size() > 1 && j != lesson.marks.size() - 1 ) {
                            s1.append("/");
                        }
                    }
                }
                if(s1.toString().charAt(s1.length()-1) == '/'){
                    s1.deleteCharAt(s1.length()-1);
                }
                Spannable spans1 = new SpannableString(s1.toString());
                spans1.setSpan(new RelativeSizeSpan(1.4f), 0, s1.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv3.setText(spans1);
            } catch (Exception ignored) {
            }

            tbrow.addView(tv1);
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(tv21);

            linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
            ImageView image = new ImageView(getContext());
            image.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.attach), tv22.getLineHeight(), tv22.getLineHeight(), true));
            image.setPadding(30,0,0,10);

            if(lesson.homeWork.files != null && !lesson.homeWork.files.isEmpty()){
                linearLayout2.addView(image);
            }
            linearLayout2.addView(tv22);

            linearLayout.addView(linearLayout2);
            tbrow.addView(linearLayout);
            tbrow.addView(tv3);
            tableLayout.addView(tbrow);
        }
    }

    public void CreateODOD(){
        TextView txt = new TextView(getContext());
        txt.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        String s1 = "ОДОД";
        Spannable spans1 = new SpannableString(s1);
        spans1.setSpan(new RelativeSizeSpan(1.4f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt.setText(spans1);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(150, 30, 150, 30);
        txt.setBackground(getResources().getDrawable(R.drawable.cell_phone7));
        linearLayout.addView(txt);
        for (int i = 0; i < day.odods.size(); i++) {
            TextView txt1 = new TextView(getContext());
            txt1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView txt2 = new TextView(getContext());
            txt2.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            int dur = day.odods.get(i).duration;
            long data = day.odods.get(i).daymsec;
            Date date = new Date(data);
            Calendar cal0 = Calendar.getInstance();
            cal0.setTime(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MINUTE, dur);
            String smo = cal0.get(Calendar.MINUTE) + "";
            String sm = cal.get(Calendar.MINUTE) + "";
            if(cal0.get(Calendar.MINUTE) < 10)
                smo = "0" + smo;
            if(cal.get(Calendar.MINUTE) < 10)
                sm = "0" + sm;
            String s = cal0.get(Calendar.HOUR_OF_DAY) + ":" + smo + " - " + cal.get(Calendar.HOUR_OF_DAY) + ":" + sm;
            Spannable spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txt2.setText(spans);
            s = day.odods.get(i).name;
            spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txt1.setText(spans);
            txt1.setMaxLines(1);
            int pd = 60;
            if(i > 0)
                pd = 40;
            txt2.setPadding(70, pd, 0, 5);
            txt1.setPadding(50, 0, 30, 0);
            linearLayout.addView(txt2);
            linearLayout.addView(txt1);
        }
    }

    public void draw() {
        if(tableLayout == null || getContext() == null) {
            return;
        }
        log("draw(), " + dayofweek);
        if (day != null && day.lessons != null) {
            tableLayout.setColumnStretchable(1, true);
            tableLayout.setColumnShrinkable(1, true);
            tableLayout.removeAllViews();
            linearLayout.removeAllViews();
            linearLayout.addView(tableLayout);
            CreateTable();
        } else {
            boolean ok = true;
            if(periods != null) {
                int pernum = 0;
                for (int i = 3; i < periods.length; i++) {
                    ScheduleFragment.Period period = periods[i];
                    if (period.datestart <= c.getTimeInMillis() && period.datefinish >= c.getTimeInMillis()) {
                        if (period.days != null || period.nullsub){
                            ok = true;
                            break;
                        } else {
                            ok = false;
                            pernum = i;
                        }
                    }
                }
                if(!ok) {
                    ((MainActivity) getContext()).scheduleFragment.Download2(pernum, false);
                }
            }
            tableLayout.removeAllViews();
            tableLayout.setColumnStretchable(0, true);
            tableLayout.setColumnShrinkable(0, true);
            TableRow tbrow1 = new TableRow(getContext());
            tbrow1.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            final TextView tv1 = new TextView(getContext());
            if(ok) {
                tv1.setText("Уроков нет");
            } else {
                tv1.setText("Загрузка...");
            }
            tv1.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            tbrow1.addView(tv1);
            tv1.setTextColor(Color.GRAY);
            tv1.setGravity(Gravity.CENTER);
            tv1.setTextSize(30);
            tableLayout.addView(tbrow1);
        }
        if(day != null && day.odods != null && day.odods.size() > 0
                && getContext().getSharedPreferences("pref", 0).getBoolean("odod", true)){
            CreateODOD();
        }
    }

    public Context getContext() {
        return (context == null?super.getContext():context);
    }

    static class Attends {
        String name;
        String color;
        int id;
        Attends(){}
    }

}