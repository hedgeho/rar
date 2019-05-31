package com.example.sch;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class Countcoff extends Fragment {

    String subname;
    String periodname = "4 четверть"; // пока так
    ArrayList<PeriodFragment.Cell> cells;
    int j;
    int dell = 7;
    String[] period;
    int pernum = 6;
    Double avg;
    boolean newm = false;
    String[] strings;
    TextView txt1, txt2;
    AlertDialog.Builder alr, alr1;
    ScheduleFragment.Period[] periods = new ScheduleFragment.Period[7];
    String[] marks = {"1", "2", "3", "4", "5"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    ImageView img;
    LinearLayout layout, cont;

    public Countcoff() {
    }

    static void sasha(String s) {
        Log.v("sasha", s);
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == Dialog.BUTTON_POSITIVE) {
                subname = periods[pernum].subjects.get(lv.getCheckedItemPosition()).name;
                txt2.setText(subname);
                cells = new ArrayList<>(periods[pernum].subjects.get(lv.getCheckedItemPosition()).cells);
                txt1.setText(String.valueOf(periods[pernum].subjects.get(lv.getCheckedItemPosition()).avg));
                j = lv.getCheckedItemPosition();
                makeMarks();
            } else {
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_countcoff, container, false);
        periodname = period[pernum];
        txt1 = v.findViewById(R.id.txt1);
        cont = v.findViewById(R.id.cont);
        txt2 = v.findViewById(R.id.txt2);
        String s = String.valueOf(avg);
        Spannable spans = new SpannableString(s);
        spans.setSpan(new RelativeSizeSpan(2f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spans.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt1.setText(s);
        img = v.findViewById(R.id.imageView);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = new TextView(getContext());
                final String[] newMark = new String[1];
                final Double[] f = new Double[1];
                alr1 = new AlertDialog.Builder(getContext());
                alr1.setTitle("Создайте оценку");
                View item;
                LayoutInflater inflater = getLayoutInflater();
                item = inflater.inflate(R.layout.mark_item, cont, false);
                Button btn1 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(0));
                Button btn2 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(1));
                btn2.setClickable(false);
                final Button btn3 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(2));
                final TextView txt1 = ((TextView) ((ViewGroup) item).getChildAt(0));
                TextView txt2 = ((TextView) ((ViewGroup) item).getChildAt(1));
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
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        sasha(String.valueOf(et2.getText()));
                        try {
                            f[0] = Double.valueOf(String.valueOf(et2.getText()));
                            btn3.setClickable(true);
                        } catch (Exception e) {
                            btn3.setClickable(false);
                        }
                    }
                });
                alr1.setView(item);
                final AlertDialog show = alr1.show();
                btn1.setVisibility(View.INVISIBLE);
                btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        show.dismiss();
                    }
                });
                btn3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Spannable spans1 = new SpannableString(newMark[0]);
                        spans1.setSpan(new RelativeSizeSpan(1.7f), 0, newMark[0].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newMark[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        PeriodFragment.Cell cell = new PeriodFragment.Cell();
                        cell.markvalue = newMark[0];
                        cell.mktWt = f[0];
                        cells.add(cell);
                        makeMarks();
                        show.dismiss();
                    }
                });
            }
        });

        layout = v.findViewById(R.id.linear2);
        //ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        //lp.setMargins(30, 30, 30, 0);
        //layout.setLayoutParams(lp);
        int y = 0;
        strings = new String[periods[pernum].subjects.size()];

        for (PeriodFragment.Subject i : periods[pernum].subjects) {
            strings[y] = i.name;
            y++;
            if (subname.equals(i.name)) {
                j = periods[pernum].subjects.indexOf(i);
            }
            sasha(i.name);
        }
        cells = new ArrayList<>(periods[pernum].subjects.get(j).cells);

        makeMarks();

        alr = new AlertDialog.Builder(getContext());
        alr.create();
        alr.setSingleChoiceItems(strings, j, myClickListener);
        alr.setTitle("Выбирете предмет");
        alr.setPositiveButton("ok", myClickListener);
        txt2.setText(subname);
        txt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alr.show();
            }
        });
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(periodname);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        return v;
    }

    void makeMarks() {
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
            final TextView tv1 = new TextView(getActivity().getApplicationContext());
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
            tv1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] newMark = new String[1];
                    final Double[] f = new Double[1];
                    alr1 = new AlertDialog.Builder(getContext());
                    alr1.setTitle("Измините оценку");
                    View item;
                    LayoutInflater inflater = getLayoutInflater();
                    item = inflater.inflate(R.layout.mark_item, cont, false);
                    Button btn1 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(0));
                    Button btn2 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(1));
                    final Button btn3 = ((Button) ((ViewGroup) ((ViewGroup) item).getChildAt(5)).getChildAt(2));
                    final TextView txt1 = ((TextView) ((ViewGroup) item).getChildAt(0));
                    TextView txt2 = ((TextView) ((ViewGroup) item).getChildAt(1));
                    Spinner spinner = ((Spinner) ((ViewGroup) item).getChildAt(2));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, marks);
                    spinner.setGravity(Gravity.CENTER);
                    spinner.setAdapter(adapter);
                    spinner.setPrompt(s1[0]);
                    try {
                        int y = Integer.valueOf(s1[0]);
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
                            sasha(String.valueOf(et2.getText()));
                            try {
                                f[0] = Double.valueOf(String.valueOf(et2.getText()));
                                btn3.setClickable(true);
                            } catch (Exception e) {
                                sasha("errr");
                                btn3.setClickable(false);
                            }
                        }
                    });
                    alr1.setView(item);
                    final AlertDialog show = alr1.show();
                    btn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            newMark[0] = "--";
                            Spannable spans1 = new SpannableString(newMark[0]);
                            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, newMark[0].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newMark[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv1.setText(spans1);
                            cells.remove(finalI);
                            makeMarks();
                            show.dismiss();
                        }
                    });
                    btn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            show.dismiss();
                        }
                    });
                    btn3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Spannable spans1 = new SpannableString(newMark[0]);
                            spans1.setSpan(new RelativeSizeSpan(1.7f), 0, newMark[0].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            spans1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newMark[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv1.setText(spans1);
                            d[0] = f[0];
                            cells.get(finalI1).markvalue = newMark[0];
                            cells.get(finalI1).mktWt = f[0];
                            makeMarks();
                            show.dismiss();
                        }
                    });
                }
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
        txt1.setText(countNewCoff());
        layout.addView(lin);
    }

    String countNewCoff() {
        Double d = 0.;
        Double f = 0.;
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).markvalue != null && !cells.get(i).markvalue.equals(" "))
                if (cells.get(i).markvalue.equals("1") || cells.get(i).markvalue.equals("2") || cells.get(i).markvalue.equals("3")
                        || cells.get(i).markvalue.equals("4") || cells.get(i).markvalue.equals("5")) {
                    d += Double.valueOf(cells.get(i).markvalue) * cells.get(i).mktWt;
                    f += cells.get(i).mktWt;
                }
        }
        String s = String.valueOf(d / f);
        if (s.length() > 4) {
            s = String.format(Locale.UK, "%.2f", d / f);
        }
        return s;
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
        menu.add(0, 2, 0, "Сброс");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                ((MainActivity) getActivity()).quit();
                break;
            case 2:
                cells = new ArrayList<>(periods[pernum].subjects.get(j).cells);
                makeMarks();
                sasha("rar");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
