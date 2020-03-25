package ru.gurhouse.sch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.MainActivity.TYPE_SEM;

public class Countcoff extends Fragment {

    String subname;
    String periodname = "4 четверть";
    ArrayList<PeriodFragment.Cell> cells;
    int j;
    int dell = 7;
    String[] period, periodSEM;
    int pernum = 6;
    double avg;
    boolean periodType;
    Toolbar toolbar;
    String[] strings;
    TextView txt1, txt0, txt, txt2;
    AlertDialog.Builder alr, alr1, alr2;
    ScheduleFragment.Period[] periods = new ScheduleFragment.Period[7];
    String[] marks = {"1", "2", "3", "4", "5"};

    Activity context;

    ImageView img;
    LinearLayout layout, cont, linearLayout;

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                j = lv.getCheckedItemPosition();
                subname = periods[pernum].subjects[lv.getCheckedItemPosition()].name;
                periodType = periods[pernum].subjects[lv.getCheckedItemPosition()].periodType;
                log("periodtype: " + (periodType==TYPE_SEM?"SEM":"Q"));
                txt2.setText(subname);
                alr.setSingleChoiceItems(strings, j, myClickListener);
                if(periodType == TYPE_SEM) {
                    if (pernum == 3 || pernum == 4) {
                        pernum = 1;
                    } else if (pernum == 5 || pernum == 6) {
                        pernum = 2;
                    }
                    log("pernum " + pernum);
                    alr2.setSingleChoiceItems(periodSEM, pernum, myClickListener2);
                } else {
                    alr2.setSingleChoiceItems(period, pernum, myClickListener2);
                }
                periodname = periods[pernum].name;
                toolbar.setTitle(periodname);
                cells = new ArrayList<>();
                for (int i = 0; i < periods[pernum].subjects[j].cells.length; i++) {
                    cells.add(new PeriodFragment.Cell(periods[pernum].subjects[j].cells[i]));
                }
                avg = periods[pernum].subjects[lv.getCheckedItemPosition()].avg;
                makeMarks(true);
            }
        }
    };

    DialogInterface.OnClickListener myClickListener2 = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                TheSingleton.getInstance().t1 = System.currentTimeMillis();
                pernum = lv.getCheckedItemPosition();
                if (periods[pernum].subjects == null) {
                    periodname = periods[pernum].name;
                    toolbar.setTitle(periodname);
                    Download2(() -> {
                        cells = new ArrayList<>();
                        for (int i = 0; i < periods[pernum].subjects[j].cells.length; i++) {
                            cells.add(new PeriodFragment.Cell(periods[pernum].subjects[j].cells[i]));
                        }
                        if(periods[pernum].nullsub)
                            avg = 0d;
                        else
                            avg = periods[pernum].subjects[j].avg;
                        log("avg: " + avg);
                        if(periodType == TYPE_SEM)
                            alr2.setSingleChoiceItems(periodSEM, pernum, myClickListener2);
                        else
                            alr2.setSingleChoiceItems(period, pernum, myClickListener);
                        makeMarks(true);
                    });
                } else {
                    periodname = periods[pernum].name;
                    toolbar.setTitle(periodname);
                    cells = new ArrayList<>();
                    for (int i = 0; i < periods[pernum].subjects[j].cells.length; i++) {
                        cells.add(new PeriodFragment.Cell(periods[pernum].subjects[j].cells[i]));
                    }
                    avg = periods[pernum].subjects[j].avg;
                    log("avg: " + avg);
                    if(periodType == TYPE_SEM)
                        alr2.setSingleChoiceItems(periodSEM, pernum, myClickListener2);
                    else
                        alr2.setSingleChoiceItems(period, pernum, myClickListener);
                    makeMarks(true);
                }
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if(getActivity() != null)
            context = getActivity();

        log("Countcoff: type " + (periodType == TYPE_SEM?"SEM":"Q"));
        periodSEM = new String[period.length-4];
        System.arraycopy(period, 0, periodSEM, 0, period.length-4);

        if(periodType == TYPE_SEM) {
            if (pernum == 3 || pernum == 4) {
                pernum = 1;
            } else if (pernum == 5 || pernum == 6) {
                pernum = 2;
            }
        }

        View v = inflater.inflate(R.layout.fragment_countcoff, container, false);
        periodname = period[pernum];
        linearLayout = v.findViewById(R.id.linearLayout2);
        cont = v.findViewById(R.id.cont);
        txt2 = v.findViewById(R.id.txt2);
        txt1 = new TextView(getContext());
        txt1.setPadding(30, 0, 30, 0);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(txt1);
        String s = String.valueOf(avg);
        Spannable spans = new SpannableString(s);
        spans.setSpan(new RelativeSizeSpan(2f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt1.setText(s);
        img = v.findViewById(R.id.imageView);
        img.setOnClickListener(v1 -> {
            final String[] newMark = new String[1];
            final Double[] f = new Double[1];
            alr1 = new AlertDialog.Builder(getContext());
            alr1.setTitle("Создайте оценку");
            View item;
            LayoutInflater inflater1 = getLayoutInflater();
            item = inflater1.inflate(R.layout.mark_item, cont, false);
            Button btn1 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(0));
            Button btn2 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(1));
            btn2.setClickable(false);
            final Button btn3 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(2));
            Spinner spinner = ((Spinner) ((ViewGroup) item).getChildAt(2));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, marks);
            spinner.setGravity(Gravity.CENTER);
            spinner.setAdapter(adapter);
            spinner.setSelection(4);
            newMark[0] = "5";
            f[0] = 1.0;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    newMark[0] = marks[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
            final EditText et2 = ((EditText) ((ViewGroup) item).getChildAt(3));
            et2.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s1) {
                }

                @Override
                public void beforeTextChanged(CharSequence s1, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s1, int start, int before, int count) {
                    try {
                        f[0] = Double.parseDouble(et2.getText().toString());
                        btn3.setClickable(true);
                    } catch (Exception e) {
                        btn3.setClickable(false);
                    }
                }
            });
            alr1.setView(item);
            final AlertDialog show = alr1.show();
            btn1.setVisibility(View.INVISIBLE);
            btn2.setOnClickListener(v11 -> show.dismiss());
            btn3.setOnClickListener(v112 -> {
                Spannable spans1 = new SpannableString(newMark[0]);
                spans1.setSpan(new RelativeSizeSpan(1.7f), 0, newMark[0].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newMark[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                PeriodFragment.Cell cell = new PeriodFragment.Cell();
                cell.markvalue = newMark[0];
                cell.mktWt = f[0];
                cells.add(cell);
                makeMarks();
                show.dismiss();
            });
        });

        layout = v.findViewById(R.id.linear2);
        int y = 0;
        strings = new String[periods[pernum].subjects.length];

        for (PeriodFragment.Subject i : periods[pernum].subjects) {
            strings[y] = i.name;
            y++;
            if (subname.equals(i.name)) {
                for (int k = 0; k < periods[pernum].subjects.length; k++) {
                    if(periods[pernum].subjects[k].equals(i)) {
                        j = k;
                        break;
                    }
                }
            }
        }
        cells = new ArrayList<>();
        if(periods[pernum].subjects.length != 0) {
            for (int i = 0; i < periods[pernum].subjects[j].cells.length; i++) {
                cells.add(new PeriodFragment.Cell(periods[pernum].subjects[j].cells[i]));
            }

            makeMarks(true);
        } else {
            getContext().onBackPressed();
        }


        alr = new AlertDialog.Builder(getContext());
        alr.create();
        alr.setSingleChoiceItems(strings, j, myClickListener);
        alr.setTitle("Выберите предмет");
        alr.setPositiveButton("ok", myClickListener);
        txt2.setText(subname);
        txt2.setOnClickListener(v12 -> alr.show());

        toolbar = getActivity().findViewById(R.id.toolbar);
        alr2 = new AlertDialog.Builder(getContext());
        alr2.create();
        periodname = period[pernum];
        //toolbar.setTitle(periodname);
        if(periodType == TYPE_SEM)
            alr2.setSingleChoiceItems(periodSEM, pernum, myClickListener2);
        else
            alr2.setSingleChoiceItems(period, pernum, myClickListener2);
        alr2.setTitle("Выберите период");
        alr2.setPositiveButton("ok", myClickListener2);
        toolbar.setOnClickListener(v2 -> alr2.show());
        setHasOptionsMenu(true);
        toolbar.setTitle(periodname);
        ((MainActivity) getActivity()).setSupActionBar(toolbar);
        return v;
    }

    void makeMarks() {makeMarks(false);}
    void makeMarks(boolean main) {
        layout.removeAllViews();
        LinearLayout lin = new LinearLayout(getContext());
        for (int i = 0; i < cells.size(); i++) {
            if (i % dell == 0 && i != cells.size() - 1) {
                if (i != 0) {
                    layout.addView(lin);
                }
                lin = new LinearLayout(getContext());
                lin.setOrientation(LinearLayout.HORIZONTAL);
                lin.setGravity(Gravity.CENTER);
            }
            final TextView tv1 = new TextView(getContext());
            tv1.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
            final String[] s1 = new String[1];
            if (cells.get(i).markvalue == null) {
                s1[0] = "  ";
            } else {
                s1[0] = cells.get(i).markvalue;
            }
            final Double[] d = {cells.get(i).mktWt};
            tv1.setPadding(15, 0, 15, 0);
            Spannable spans1 = new SpannableString(s1[0]);
            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, s1[0].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s1[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv1.setText(spans1);
            final int finalI = i;
            final int finalI1 = i;
            tv1.setOnClickListener(v -> {
                final String[] newMark = new String[1];
                final Double[] f = new Double[1];
                alr1 = new AlertDialog.Builder(getContext());
                alr1.setTitle("Измените оценку");
                View item;
                LayoutInflater inflater = getLayoutInflater();
                item = inflater.inflate(R.layout.mark_item, cont, false);
                Button btn1 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(0));
                Button btn2 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(1));
                final Button btn3 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(2));
                Spinner spinner = ((Spinner) ((ViewGroup) item).getChildAt(2));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, marks);
                spinner.setGravity(Gravity.CENTER);
                spinner.setAdapter(adapter);
                spinner.setPrompt(s1[0]);
                try {
                    int y = Integer.parseInt(s1[0]);
                    spinner.setSelection(y - 1);
                    newMark[0] = s1[0];
                } catch (Exception e) {
                    spinner.setSelection(0);
                    newMark[0] = "1";
                }
                f[0] = d[0];
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        newMark[0] = marks[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
                final EditText et2 = ((EditText) ((ViewGroup) item).getChildAt(3));
                et2.setText(String.valueOf(d[0]));
                et2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            f[0] = Double.parseDouble(et2.getText().toString());
                            btn3.setClickable(true);
                        } catch (Exception e) {
                            loge(e);
                            btn3.setClickable(false);
                        }
                    }
                });
                alr1.setView(item);
                final AlertDialog show = alr1.show();
                btn1.setOnClickListener(v1 -> {
                    cells.remove(finalI);
                    makeMarks();
                    show.dismiss();
                });
                btn2.setOnClickListener(v12 -> show.dismiss());
                btn3.setOnClickListener(v13 -> {
                    Spannable spans11 = new SpannableString(newMark[0]);
                    spans11.setSpan(new RelativeSizeSpan(1.7f), 0, newMark[0].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    spans11.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newMark[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv1.setText(spans11);
                    d[0] = f[0];
                    cells.get(finalI1).markvalue = newMark[0];
                    cells.get(finalI1).mktWt = f[0];
                    makeMarks();
                    show.dismiss();
                });
            });

            if (d[0] <= 0.5)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff1));
            else if (d[0] <= 1)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff2));
            else if (d[0] <= 1.25)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff3));
            else if (d[0] <= 1.35)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff4));
            else if (d[0] <= 1.5)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff5));
            else if (d[0] <= 1.75)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff6));
            else if (d[0] <= 2)
                tv1.setBackgroundColor(getResources().getColor(R.color.coff7));
            else
                tv1.setBackgroundColor(getResources().getColor(R.color.coff8));
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 10, 10, 10);
            tv1.setLayoutParams(lp);
            lin.addView(tv1);
        }
        countNewCoff(main);
        layout.addView(lin);
    }

    void countNewCoff(boolean main) {
        double d = 0.;
        double f = 0.;
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).markvalue != null && !cells.get(i).markvalue.equals(" "))
                if (cells.get(i).markvalue.equals("1") || cells.get(i).markvalue.equals("2") || cells.get(i).markvalue.equals("3")
                        || cells.get(i).markvalue.equals("4") || cells.get(i).markvalue.equals("5")) {
                    d += Double.parseDouble(cells.get(i).markvalue) * cells.get(i).mktWt;
                    f += cells.get(i).mktWt;
                }
        }
        double newAvg = Math.round(d / f*100)/100d;
        avg = Math.round(avg*100)/100d;
        log("avg: " + avg + ", new: " + newAvg);
        if(main)
            avg = newAvg;
        String s = String.valueOf(newAvg);
        if (s.length() > 4) {
            s = String.format(Locale.UK, "%.2f", d / f);
        }
        linearLayout.removeAllViews();
        txt = new TextView(getContext());
        txt0 = new TextView(getContext());
        txt.setText(String.format(Locale.UK, "%.2f", Double.parseDouble(s) - avg));
        txt0.setText(s);
        txt1.setText(String.valueOf(avg));
        txt.setPadding(30, 0, 30, 0);
        txt0.setTextSize(9 * getResources().getDisplayMetrics().density);
        txt.setTextSize(9 * getResources().getDisplayMetrics().density);
        txt1.setTextSize(9 * getResources().getDisplayMetrics().density);
        txt0.setPadding(30, 0, 30, 0);
        linearLayout.addView(txt1);
        if (newAvg > avg) {
            txt.setText("+" + txt.getText());
            txt.setTextColor(getResources().getColor(R.color.plus));
            linearLayout.addView(txt);
        } else if (newAvg < avg) {
            txt.setTextColor(getResources().getColor(R.color.mn));
            linearLayout.addView(txt);
        }
        if (newAvg != avg)
            linearLayout.addView(txt0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        menu.add(0, 2, 0, "Сброс");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 2) {
            cells = new ArrayList<>();
            for (int i = 0; i < periods[pernum].subjects[j].cells.length; i++) {
                cells.add(new PeriodFragment.Cell(periods[pernum].subjects[j].cells[i]));
            }
            makeMarks();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Activity getContext() {
        return (context==null?getActivity():context);
    }

    void Download2(Runnable onFinish) {
        if(TheSingleton.getInstance().t1 == 0) {
            log("countcoff start");
            TheSingleton.getInstance().t1 = System.currentTimeMillis();
        }
        int id = periods[pernum].id;
        periods[pernum].days = null;
        periods[pernum].subjects = null;
        periods[pernum].lins = null;
        periods[pernum].cells = null;
        new Thread() {
            JSONObject object1;
            int USER_ID = TheSingleton.getInstance().getUSER_ID();

            @SuppressLint("SimpleDateFormat")
            @Override
            public void run() {
                try {

                    //------------------------------------------------------------------------------------------------

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                object1 = new JSONObject(
                                        connect("https://app.eschool.center/ec-server/student/getDiaryPeriod?userId=" + USER_ID + "&eiId=" + id,
                                                null, getContext()));
                            } catch (LoginActivity.NoInternetException ignore) {
                            } catch (Exception e) {
                                loge(e);
                            }
                        }
                    }.start();

                    JSONObject object = new JSONObject(
                            connect("https://app.eschool.center/ec-server/student/getDiaryUnits?userId=" + USER_ID + "&eiId=" + id,
                                    null, getContext()));
                    if(!object.has("result"))
                        log("lol no result: " + object.toString());
                    log("RR: " + object.toString());
                    JSONArray array = object.getJSONArray("result");
                    periods[pernum].subjects = new PeriodFragment.Subject[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        PeriodFragment.Subject subject = new PeriodFragment.Subject();
                        JSONObject obj = array.getJSONObject(i);
                        if (obj.has("overMark")) {
                            double d = obj.getDouble("overMark");
                            String s = String.valueOf(d);
                            if (s.length() > 4) {
                                s = String.format(Locale.UK, "%.2f", d);
                            }
                            subject.avg = Double.valueOf(s);
                        }
                        if (obj.has("totalMark"))
                            subject.totalmark = obj.getString("totalMark");
                        if (obj.has("unitName"))
                            subject.name = obj.getString("unitName");
                        if (obj.has("rating"))
                            subject.rating = obj.getString("rating");
                        if (obj.has("unitId"))
                            subject.unitid = obj.getInt("unitId");
                        subject.cells = null;
                        periods[pernum].subjects[i] = subject;
                        log("subject " + subject.name + ", avg: " + subject.avg);
                    }

                    Arrays.sort(periods[pernum].subjects, (o1, o2) -> Integer.compare(o1.unitid,o2.unitid));

                    while (object1 == null) {
                        Thread.sleep(10);
                    }

                    JSONArray arraydaylessons = object1.getJSONArray("result");
                    periods[pernum].cells = new PeriodFragment.Cell[arraydaylessons.length()];
                    for (int i = 0; i < arraydaylessons.length(); i++) {
                        object1 = arraydaylessons.getJSONObject(i);
                        PeriodFragment.Cell cell = new PeriodFragment.Cell();
                        if(object.has("attends")){
                            JSONArray as = object.getJSONArray("attends");
                            JSONObject a = as.getJSONObject(0);

                        }
                        if (object1.has("lptName"))
                            cell.lptname = object1.getString("lptName");
                        if (object1.has("markDate"))
                            cell.markdate = object1.getString("markDate");
                        if (object1.has("lessonId"))
                            cell.lessonid = object1.getLong("lessonId");
                        if (object1.has("markVal"))
                            cell.markvalue = object1.getString("markVal");
                        if (object1.has("mktWt"))
                            cell.mktWt = object1.getDouble("mktWt");
                        if (object1.has("teachFio"))
                            cell.teachFio = object1.getString("teachFio");
                        if (object1.has("startDt"))
                            cell.date = object1.getString("startDt");
                        if (object1.has("unitId"))
                            cell.unitid = object1.getInt("unitId");
                        periods[pernum].cells[i] = cell;
                    }
                    Date date = new Date();
                    if (periods[pernum].cells.length == 0) {
                        if (periods[pernum].datestart <= date.getTime() && periods[pernum].datefinish >= date.getTime()) {
                            periods[pernum].days = null;
                            periods[pernum].subjects = null;
                            periods[pernum].lins = null;
                            periods[pernum].cells = null;
                            Download2(onFinish);
                        } else {
                            periods[pernum].nullsub = true;
                        }
                    } else {
                        String s1 = periods[pernum].cells[0].date;
                        String s2 = periods[pernum].cells[periods[pernum].cells.length - 1].date;
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                        long d1 = format.parse(s1).getTime();
                        long d2 = format.parse(s2).getTime();
                        JSONObject object2 = new JSONObject(
                                connect("https://app.eschool.center/ec-server/student/diary?" +
                                        "userId=" + USER_ID + "&d1=" + d1 + "&d2=" + d2, null, getContext()));
                        JSONArray array2 = object2.getJSONArray("lesson");

                        long day1 = 0L, date1;
                        int isODOD;
                        int index = -1, len = 0;
                        log(0);
                        for (int i = 0; i < array2.length(); i++) {
                            object2 = array2.getJSONObject(i);
                            date1 = Long.parseLong(object2.getString("date"));
                            isODOD = object2.getInt("isODOD");
                            if(isODOD == 0 && date1 != day1) {
                                date = new Date(date1);
                                date.setHours(0);
                                date.setMinutes(0);
                                date1 = date.getTime();
                                len++;
                            }
                            day1 = date1;
                        }
                        day1 = 0;
                        periods[pernum].days = new PeriodFragment.Day[len];
                        int ind=0;
                        for (int i = 0; i < array2.length(); i++) {
                            object2 = array2.getJSONObject(i);
                            date1 = Long.parseLong(object2.getString("date"));
                            isODOD = object2.getInt("isODOD");
                            if (isODOD == 0) {
                                if (date1 != day1) {
                                    index++;
                                    date = new Date(date1);
                                    date.setHours(0);
                                    date.setMinutes(0);
                                    date1 = date.getTime();
                                    PeriodFragment.Day thisday = new PeriodFragment.Day();
                                    thisday.day = String.valueOf(date);
                                    thisday.daymsec = date1;
                                    Date datathis = new Date();
                                    datathis.setTime(date1);
                                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE", Locale.ENGLISH);
                                    String dayOfTheWeek = dateFormat2.format(datathis);
                                    switch (dayOfTheWeek) {
                                        case "Mon":
                                            thisday.numday = 1;
                                            break;
                                        case "Tue":
                                            thisday.numday = 2;
                                            break;
                                        case "Wed":
                                            thisday.numday = 3;
                                            break;
                                        case "Thu":
                                            thisday.numday = 4;
                                            break;
                                        case "Fri":
                                            thisday.numday = 5;
                                            break;
                                        case "Sat":
                                            thisday.numday = 6;
                                            break;
                                        case "Sun":
                                            thisday.numday = 7;
                                            break;
                                    }
                                    thisday.lessons = new ArrayList<>();
//                                log("added day " + date.toString());
                                    periods[pernum].days[ind++] = thisday;
                                }
                                PeriodFragment.Lesson lesson = new PeriodFragment.Lesson();
                                lesson.id = object2.getLong("id");
                                lesson.numInDay = object2.getInt("numInDay");
                                if (object2.getJSONObject("unit").has("id"))
                                    lesson.unitId = object2.getJSONObject("unit").getLong("id");
                                if (object2.getJSONObject("unit").has("name"))
                                    lesson.name = object2.getJSONObject("unit").getString("name");
                                if (object2.getJSONObject("unit").has("short"))
                                    lesson.shortname = object2.getJSONObject("unit").getString("short");
                                if (object2.getJSONObject("tp").has("topicName"))
                                    lesson.topic = object2.getJSONObject("tp").getString("topicName");
                                if (object2.getJSONObject("teacher").has("factTeacherIN"))
                                    lesson.teachername = object2.getJSONObject("teacher").getString("factTeacherIN");
                                JSONArray ar = object2.getJSONArray("part");
                                lesson.homeWork = new PeriodFragment.HomeWork();
                                lesson.homeWork.stringwork = "";
                                lesson.homeWork.files = new ArrayList<>();
                                for (int j = 0; j < ar.length(); j++) {
                                    if (ar.getJSONObject(j).getString("cat").equals("DZ")) {
                                        if (ar.getJSONObject(j).has("variant")) {
                                            JSONArray ar1 = ar.getJSONObject(j).getJSONArray("variant");
                                            for (int k = 0; k < ar1.length(); k++) {
                                                if (ar1.getJSONObject(k).has("text")) {
                                                    lesson.homeWork.stringwork += ar1.getJSONObject(k).getString("text") + "\n";
                                                }
                                                if (ar1.getJSONObject(k).has("file")) {
                                                    JSONArray ar2 = ar1.getJSONObject(k).getJSONArray("file");
                                                    PeriodFragment.File file;
                                                    for (int l = 0; l < ar2.length(); l++) {
                                                        file = new PeriodFragment.File();
                                                        file.name = ar2.getJSONObject(l).getString("fileName");
                                                        file.id = ar2.getJSONObject(l).getInt("id");
                                                        lesson.homeWork.files.add(file);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                periods[pernum].days[index].lessons.add(lesson);
                            }
                            day1 = date1;
                        }

                        log(1);
                        LinkedList<PeriodFragment.Cell>[] subjects = new LinkedList[periods[pernum].subjects.length];
                        for (int i = 0; i < subjects.length; i++) {
                            subjects[i] = new LinkedList<>();
                        }
                        for (int i = 0; i < periods[pernum].days.length; i++) {
                            for (int j = 0; j < periods[pernum].cells.length; j++) {
                                PeriodFragment.Cell cell = periods[pernum].cells[j];
                                s1 = cell.date;
                                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                                d1 = format.parse(s1).getTime();
                                if (cell.mktWt != 0) {
                                    if (periods[pernum].days[i].daymsec - d1 == 0 || periods[pernum].days[i].daymsec == d1) {
                                        for (int k = 0; k < periods[pernum].days[i].lessons.size(); k++) {
                                            if (periods[pernum].days[i].lessons.get(k).id == cell.lessonid) {
                                                PeriodFragment.Mark mark = new PeriodFragment.Mark();
                                                mark.cell = cell;
                                                mark.idlesson = cell.lessonid;
                                                mark.coefficient = cell.mktWt;
                                                if (cell.markvalue != null)
                                                    mark.value = cell.markvalue;
                                                else
                                                    mark.value = "";
                                                mark.teachFio = cell.teachFio;
                                                mark.markdate = cell.markdate;
                                                mark.date = cell.date;

                                                mark.topic = cell.lptname;
                                                mark.unitid = cell.unitid;
                                                for (int l = 0; l < periods[pernum].subjects.length; l++) {
                                                    if (periods[pernum].subjects[l].unitid == mark.unitid) {
                                                        subjects[l].add(cell);
                                                    }
                                                    if (periods[pernum].subjects[l].shortname == null || periods[pernum].subjects[l].shortname.isEmpty()) {
                                                        PeriodFragment.Subject subject = periods[pernum].subjects[l];
                                                        switch (subject.name) {
                                                            case "Физика":
                                                            case "Химия":
                                                            case "История":
                                                            case "Алгебра":
                                                                subject.shortname = subject.name;
                                                                break;
                                                            case "БЕСЕДЫ КЛ РУК":
                                                                subject.shortname = "Кл. Час";
                                                                break;
                                                            case "Иностранный язык":
                                                                subject.shortname = "Ин. Яз.";
                                                                break;
                                                            case "Алгебра и начала анализа":
                                                                subject.shortname = "Алгебра";
                                                                break;
                                                            case "Информатика и ИКТ":
                                                                subject.shortname = "Информ.";
                                                                break;
                                                            case "Биология":
                                                                subject.shortname = "Биолог.";
                                                                break;
                                                            case "География":
                                                                subject.shortname = "Геогр.";
                                                                break;
                                                            case "Геометрия":
                                                                subject.shortname = "Геометр.";
                                                                break;
                                                            case "Литература":
                                                                subject.shortname = "Лит-ра";
                                                                break;
                                                            case "Обществознание":
                                                                subject.shortname = "Общ.";
                                                                break;
                                                            case "Русский язык":
                                                                subject.shortname = "Рус. Яз.";
                                                                break;
                                                            case "Физическая культура":
                                                                subject.shortname = "Физ-ра";
                                                                break;
                                                            default:
                                                                periods[pernum].subjects[l].shortname = periods[pernum].subjects[l].name.substring(0, 3);
                                                        }

                                                    }
                                                }
                                                periods[pernum].days[i].lessons.get(k).marks.add(mark);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (int i = 0; i < subjects.length; i++) {
                            periods[pernum].subjects[i].cells = subjects[i].toArray(new PeriodFragment.Cell[0]);
                        }
                    }

                    getActivity().runOnUiThread(onFinish);
                    //---------------------------------------------------------------------------------------------------------------------------------
                } catch (LoginActivity.NoInternetException ignored) {
                } catch (Exception e) {
                    loge(e);
                    periods[pernum].days = null;
                    periods[pernum].subjects = null;
                    periods[pernum].lins = null;
                    periods[pernum].cells = null;
                    Download2(onFinish);
                }
            }
        }.start();
    }
}
