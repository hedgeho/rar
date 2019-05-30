package com.example.sch;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Countcoff extends Fragment {

    String subname;
    String periodname = "4 четверть"; // пока так
    ArrayList<PeriodFragment.Subject> subjects;
    int j;
    int dell = 7;
    Double avg;
    ArrayList<String> strings;
    TextView txt1;
    boolean modRemove = false;
    boolean modAdd = false;
    boolean nodChange = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    Button btn1, btn2;
    LinearLayout layout;
    int colinlast = 0;

    public Countcoff() {
    }

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_countcoff, container, false);
        strings = new ArrayList<>();
        txt1 = v.findViewById(R.id.txt1);
        String s = String.valueOf(avg);
        Spannable spans = new SpannableString(s);
        spans.setSpan(new RelativeSizeSpan(2f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt1.setText(s);
        btn1 = v.findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colinlast++;
                TextView tv1 = new TextView(getActivity().getApplicationContext());
                tv1.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(10, 10, 10, 10);
                tv1.setLayoutParams(lp);
                String s1 = "  ";
                tv1.setPadding(15, 0, 15, 0);
                Spannable spans1 = new SpannableString(s1);
                spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv1.setText(spans1);
                tv1.setBackgroundColor(getResources().getColor(R.color.coff6));
                if (colinlast > 7) {
                    LinearLayout lin = new LinearLayout(getContext());
                    lin.setOrientation(LinearLayout.HORIZONTAL);
                    lin.setGravity(Gravity.CENTER);
                    colinlast = 1;
                    lin.addView(tv1);
                    layout.addView(lin);
                } else {
                    ((ViewGroup) layout.getChildAt(layout.getChildCount() - 1)).addView(tv1);
                }

                sasha("linear ch");
            }
        });
        btn2 = v.findViewById(R.id.button2);
        layout = v.findViewById(R.id.linear2);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 30, 30);
        layout.setLayoutParams(lp);
        for (PeriodFragment.Subject i : subjects) {
            strings.add(i.name);
            if (subname.equals(i.name)) {
                j = subjects.indexOf(i);
            }
            System.out.println(i);
        }

        LinearLayout lin = null;
        for (int i = 0; i < subjects.get(j).cells.size(); i++) {
            if (i % dell == 0 && i != subjects.get(j).cells.size() - 1) {
                if (i != 0) {
                    layout.addView(lin);
                }
                lin = new LinearLayout(getContext());
                lin.setOrientation(LinearLayout.HORIZONTAL);
                lin.setGravity(Gravity.CENTER);
            }
            TextView tv1 = new TextView(getActivity().getApplicationContext());
            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String s1;
            if (subjects.get(j).cells.get(i).markvalue == null) {
                s1 = "  ";
            } else {
                s1 = subjects.get(j).cells.get(i).markvalue;
            }
            tv1.setPadding(15, 0, 15, 0);
            Spannable spans1 = new SpannableString(s1);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            final double d = subjects.get(j).cells.get(i).mktWt;
            if (d <= 0.5)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff1));
            else if (d <= 1)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff2));
            else if (d <= 1.25)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff3));
            else if (d <= 1.35)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff4));
            else if (d <= 1.5)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff5));
            else if (d <= 1.75)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff6));
            else if (d <= 2)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff7));
            else
                tv1.setBackgroundColor(getResources().getColor(R.color.coff8));
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 10, 10, 10);
            tv1.setLayoutParams(lp);
            lin.addView(tv1);
            colinlast = i % dell + 1;
        }
        layout.addView(lin);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, strings);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = v.findViewById(R.id.spsubject);
        spinner.setAdapter(adapter1);
        spinner.setPrompt("Выберите предмет");
        spinner.setSelection(j);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Toast.makeText(getActivity(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(periodname);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        menu.add(0, 1, 0, "Выход");
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
