package ru.gurhouse.sch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class PeriodFragment extends Fragment {

    ArrayList<TextView> txts;
    View view;
    String[] period;
    ScheduleFragment.Period[] periods = new ScheduleFragment.Period[7];
    int pernum = 0;
    Toolbar toolbar;
    AlertDialog.Builder alr;
    boolean shown = false;
    String periodname;
    boolean first_time = true;
    boolean nullsub = false;
    TextView txtnull;
    boolean mode;
    SwipeRefreshLayout refreshL;
    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                TheSingleton.getInstance().t1 = System.currentTimeMillis();
                pernum = lv.getCheckedItemPosition();
                if (periods[pernum].subjects == null) {
                    normallog("PerF: change of period and downloud (" + pernum + "), name - " + periods[pernum].name + ", id - " + periods[pernum].id);
                    ((MainActivity) getActivity()).scheduleFragment.Download2(periods[pernum].id, pernum, false, true);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scrollView2).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.txtnull).setVisibility(View.INVISIBLE);
                } else {
                    normallog("PerF: change of period (" + pernum + "), name - " + periods[pernum].name);
                    periodname = periods[pernum].name;
                    alr.setSingleChoiceItems(period, pernum, myClickListener);
                    toolbar.setTitle(periodname);
                    if (periods[pernum].nullsub) {
                        normallog("PerF: nullshow()");
                        ((MainActivity) getActivity()).nullsub(periods, pernum);
                    } else {
                        normallog("PerF: show()");
                        if (((MainActivity) getActivity()).getMode0()) {
                            normallog("PerF");
                            ((MainActivity) getActivity()).set(periods, pernum, 2);
                        } else {
                            normallog("PerF1");
                            ((MainActivity) getActivity()).set(periods, pernum, 1);
                        }
                    }
                }
            }
        }
    };
    LinearLayout layout1, layout2, layout3, layout;

    public PeriodFragment() {
        txts = new ArrayList<>();
    }

    static void normallog(String s) {
        Log.v("normallog", s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        normallog("PerF/OnCreateView (alternative)");
        first_time = false;
        if (view == null)
            view = inflater.inflate(R.layout.diary, container, false);
        if (period == null && !nullsub)
            return view;
        toolbar = getActivity().findViewById(R.id.toolbar);
        alr = new AlertDialog.Builder(getContext());
        alr.create();
        periodname = period[pernum];
        toolbar.setTitle(periodname);
        alr.setSingleChoiceItems(period, pernum, myClickListener);
        alr.setTitle("Выберите период");
        alr.setPositiveButton("ok", myClickListener);
        toolbar.setOnClickListener(v -> alr.show());
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        if (nullsub) {
            nullshow();
            return view;
        }
        if (periods[pernum] != null && !shown)
            show();
        return view;
    }

    void nullshow() {
        txtnull = view.findViewById(R.id.txtnull);
        txtnull.setTextSize(8 * getResources().getDisplayMetrics().density);
        txtnull.setTextColor(Color.LTGRAY);
        //txtnull.setText("А оценочек то нет. Че съел?  Ну давай быкани быкани! Ты че решил ко мне лезть? Ублюдок, мать твою, а ну иди сюда говно собачье, решил ко мне лезть? Ты, засранец вонючий, мать твою, а? Ну иди сюда, попробуй меня трахнуть, я тебя сам трахну ублюдок, онанист чертов, будь ты проклят, иди идиот, трахать тебя и всю семью, говно собачье, жлоб вонючий, дерьмо, сука, падла, иди сюда, мерзавец, негодяй, гад, иди сюда ты - говно, ЖОПА!");
        txtnull.setText("Нет оценок за выбранный период");
        txtnull.setPadding(90, 0, 90, 0);
        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        txtnull.setVisibility(View.VISIBLE);
        view.findViewById(R.id.scrollView2).setVisibility(View.INVISIBLE);
        normallog("PerF/OnCreateView: nullsub");
    }

    void show() {
        layout = view.findViewById(R.id.linear);
        layout1 = view.findViewById(R.id.linear1);
        layout1.removeAllViews();
        layout2 = view.findViewById(R.id.linear2);
        layout2.removeAllViews();
        layout3 = view.findViewById(R.id.linear3);
        layout3.removeAllViews();
        normallog("PerF/show: names of subjects");
        for (int i = 0; i < periods[pernum].subjects.size(); i++) {
            normallog((i + 1) + ". " + periods[pernum].subjects.get(i).shortname);
        }
        for (int i = 0; i < periods[pernum].subjects.size(); i++) {
            TextView txt1 = new TextView(getActivity());
            TextView txt2 = new TextView(getActivity());
            LinearLayout linearLayout = new LinearLayout(getActivity());
            txt1.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 40, 10);
            txt1.setLayoutParams(lp);
            txt1.setGravity(Gravity.CENTER);
            txt1.setTextSize(9 * getResources().getDisplayMetrics().density);
            txt2.setTextSize(9 * getResources().getDisplayMetrics().density);
            txt2.setLayoutParams(lp);
            txt2.setTextColor(getResources().getColor(R.color.two));
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
            final int finalI1 = i;
            txt2.setOnClickListener(v -> SwitchToSubjectFragment(periods[pernum].subjects.get(finalI1).avg, periods[pernum].subjects.get(finalI1).cells, periods[pernum].subjects.get(finalI1).name, periods[pernum].subjects.get(finalI1).rating, periods[pernum].subjects.get(finalI1).totalmark));
            txt1.setOnClickListener(v -> SwitchToSubjectFragment(periods[pernum].subjects.get(finalI1).avg, periods[pernum].subjects.get(finalI1).cells, periods[pernum].subjects.get(finalI1).name, periods[pernum].subjects.get(finalI1).rating, periods[pernum].subjects.get(finalI1).totalmark));
            layout2.addView(txt2);
            layout1.addView(txt1);

            int g = 0;
            for (int j = 0; j < periods[pernum].subjects.get(i).cells.size(); j++) {
                if (periods[pernum].subjects.get(i).cells.get(j).markvalue != null && periods[pernum].subjects.get(i).cells.get(j).markvalue != "") {
                    g++;
                    double d = periods[pernum].subjects.get(i).cells.get(j).mktWt;
                    txts.add(new TextView(getActivity()));
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp1.setMargins(0, 0, 10, 10);
                    txts.get(txts.size() - 1).setLayoutParams(lp1);
                    try {
                        final int finalI = i;
                        final int finalJ = j;
                        txts.get(txts.size() - 1).setOnClickListener(v -> {

                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            MarkFragment fragment = new MarkFragment();
                            transaction.replace(R.id.frame, fragment);
                            try {
                                fragment.coff = periods[pernum].subjects.get(finalI).cells.get(finalJ).mktWt;
                                fragment.data = periods[pernum].subjects.get(finalI).cells.get(finalJ).date;
                                fragment.markdata = periods[pernum].subjects.get(finalI).cells.get(finalJ).markdate;
                                fragment.teachname = periods[pernum].subjects.get(finalI).cells.get(finalJ).teachFio;
                                fragment.topic = periods[pernum].subjects.get(finalI).cells.get(finalJ).lptname;
                                fragment.value = periods[pernum].subjects.get(finalI).cells.get(finalJ).markvalue;
                                fragment.subject = periods[pernum].subjects.get(finalI).name;
                            } catch (Exception ignore) {
                            }
                            transaction.addToBackStack(null);
                            transaction.commit();
                        });
                    } catch (Exception ignore) {
                    }
                    txts.get(txts.size() - 1).setTextSize(9 * getResources().getDisplayMetrics().density);
                    txts.get(txts.size() - 1).setTextColor(Color.WHITE);
                    txts.get(txts.size() - 1).setBackground(getResources().getDrawable(R.drawable.gradient_list));
                    txts.get(txts.size() - 1).setPadding(15, 0, 15, 0);
//
                    if (d <= 0.5)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff1));
                    else if (d <= 1)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff2));
                    else if (d <= 1.25)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff3));
                    else if (d <= 1.35)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff4));
                    else if (d <= 1.5)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff5));
                    else if (d <= 1.75)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff6));
                    else if (d <= 2)
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff7));
                    else
                        txts.get(txts.size() - 1).setBackgroundColor(getResources().getColor(R.color.coff8));

                    if (periods[pernum].subjects.get(i).cells.get(j).markvalue != null)
                        txts.get(txts.size() - 1).setText(periods[pernum].subjects.get(i).cells.get(j).markvalue);
                    else {
                        txts.get(txts.size() - 1).setText("7");
                        txts.get(txts.size() - 1).setTextColor(Color.TRANSPARENT);
                    }
                    linearLayout.addView(txts.get(txts.size() - 1));
                }
            }
            if (g == 0) {
                TextView txtnull = new TextView(getActivity());
                txtnull.setText("7");
                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp1.setMargins(0, 0, 10, 10);
                txtnull.setLayoutParams(lp1);
                txtnull.setTextSize(9 * getResources().getDisplayMetrics().density);
                txtnull.setTextColor(Color.TRANSPARENT);
                txtnull.setPadding(15, 0, 15, 0);
                linearLayout.addView(txtnull);
            }
            layout3.addView(linearLayout);
        }

        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
        view.findViewById(R.id.txtnull).setVisibility(View.INVISIBLE);
    }

    void refresh() {
        ((MainActivity) getActivity()).scheduleFragment.Download2(periods[pernum].id, pernum, false, true);
    }

    public void SwitchToSubjectFragment(Double avg, ArrayList<Cell> cells, String name, String rating, String totalmark) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SubjectFragment fragment = new SubjectFragment();
        transaction.replace(R.id.frame, fragment);
        try {
            fragment.avg = avg;
            fragment.subname = name;
            fragment.rating = rating;
            fragment.totalmark = totalmark;
            fragment.period = period;
            fragment.pernum = pernum;
            fragment.periods = periods;
        } catch (Exception ignore) {
        }
        transaction.addToBackStack(null);
        transaction.commit();
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
        item = menu.add(0, 4, 0, "Перезагрузить");
        item.setIcon(R.drawable.refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    static class Day {
        Long daymsec;
        String day;
        int numday;
        ArrayList<Lesson> lessons;

        Day() {
        }
    }

    static class Lesson {
        int numInDay, numDay;
        String name = "", teachername = "", topic = "", shortname = "";
        HomeWork homeWork;
        ArrayList<Mark> marks = new ArrayList<>();
        Long id;
        long unitId = 0;

        Lesson() {
        }

    }

    static class HomeWork {
        ArrayList<File> files;
        String stringwork = "";
        HomeWork() {
        }
    }


    static class File {
        String name;
        int id;
    }

    static class Mark {
        int unitid;
        String value, teachFio, date, topic, markdate;
        double coefficient;
        Long idlesson;
        Cell cell;

        Mark() {
        }
    }

    static class Subject {
        String name, rating = "", shortname = "", totalmark;
        double avg = 0;
        int unitid;
        ArrayList<Cell> cells;

        Subject() {
        }
    }

    static class Cell {
        String lptname, markvalue, date;
        double mktWt = 0;
        Long lessonid;
        String markdate, teachFio;
        int unitid;

        Cell() {
        }

        Cell(Cell cell) {
            if (cell.markvalue != null)
                markvalue = cell.markvalue;
            mktWt = cell.mktWt;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // under construction (disabled)
            case 2:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                TotalMarks fragment = new TotalMarks();
                fragment.start();
                transaction.replace(R.id.frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 3:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 4:
                refresh();
                break;
            case 5:
                transaction = getFragmentManager().beginTransaction();
                Countcoff fragment2 = new Countcoff();
                transaction.replace(R.id.frame, fragment2);
                try {
                    fragment2.periods = periods;
                    fragment2.period = period;
                    fragment2.pernum = pernum;
                    fragment2.subname = periods[pernum].subjects.get(0).name;
                    fragment2.avg = periods[pernum].subjects.get(0).avg;
                } catch (Exception ignore) {}
                transaction.addToBackStack(null);
                transaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }
}