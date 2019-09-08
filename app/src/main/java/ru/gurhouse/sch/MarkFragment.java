package ru.gurhouse.sch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MarkFragment extends Fragment {

    String value;
    Double coff;
    String teachname;
    String data;
    private static DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols() {

        @Override
        public String[] getWeekdays() {
            return new String[]{" ", "воскресенье", "понедельник", "вторник", "среду", "четверг", "пятницу", "субботу"};
        }

        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }

    };
    String topic;
    String subject;

    public MarkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    String markdata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_mark, container, false);
        LinearLayout linearLayout = v.findViewById(R.id.container);
        if (topic != null && !topic.equals("") && !topic.equals(" ")) {
            TextView tv1 = new TextView(getActivity());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = topic + ":";
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.25f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 10);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if (value != null && !value.equals("") && !value.equals(" ")) {
            TextView tv1 = new TextView(getActivity());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = value;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(2.5f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 10, 50, 10);
            tv1.setGravity(Gravity.CENTER);
            linearLayout.addView(tv1);
        }
        if (coff != null && coff != 0) {
            TextView tv1 = new TextView(getActivity());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = String.valueOf(coff);
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.25f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 10, 50, 10);
            tv1.setGravity(Gravity.CENTER);
            linearLayout.addView(tv1);
        }
        if (data != null && !data.equals("") && !data.equals(" ")) {
            TextView tv1 = new TextView(getActivity());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = data;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            try {
                Date date = format.parse(s1);
                format = new SimpleDateFormat("EEEE, d MMMM yyyy", myDateFormatSymbols);
                System.out.println(date);
                s1 = format.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            s1 = "Оценка поставлена на \n" + s1;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.25f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 10);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if (markdata != null && !markdata.equals("") && !markdata.equals(" ")) {
            TextView tv1 = new TextView(getActivity());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = markdata;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            try {
                Date date = format.parse(s1);
                format = new SimpleDateFormat("EEEE, d MMMM yyyy, 'в' HH:mm ", myDateFormatSymbols);
                s1 = format.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            s1 = "Оценка выставлена в \n" + s1;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.25f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 10);
            tv1.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.addView(tv1);
        }
        if (teachname != null && !teachname.equals("") && !teachname.equals(" ")) {
            TextView tv1 = new TextView(getActivity());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1 = teachname;
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            tv1.setPadding(50, 50, 50, 10);
            tv1.setGravity(Gravity.CENTER);
            linearLayout.addView(tv1);
        }
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(subject);
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}
