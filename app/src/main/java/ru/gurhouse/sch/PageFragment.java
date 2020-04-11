package ru.gurhouse.sch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.SettingsActivity.getColorFromAttribute;

public class PageFragment extends Fragment {

    TableLayout tableLayout;
    LinearLayout linearLayout;
    PeriodFragment.Day day;
    PeriodFragment.Subject[] subjects;
    Calendar c;
    ScheduleFragment.Period[] periods;
    int dayofweek;
    Context context;
    public static HashMap<Integer, Bitmap> bitmaps;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page, container, false);
        tableLayout = v.findViewById(R.id.table);
        linearLayout = v.findViewById(R.id.lin);
        if(getContext() != null)
            context = super.getContext();
        if(bitmaps == null)
            bitmaps = new HashMap<>();
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
            tv1.setTextColor(getColorFromAttribute(R.attr.main_font, getContext().getTheme()));
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
                        fragment.meetingInvite = lesson.meetingInvite;
                    } catch (Exception e) {
                        loge(e);
                    }
                    transaction.addToBackStack(null);
                    transaction.commit();
                });
            } catch (Exception e) {
                loge(e);
            }

            if (i == day.lessons.size() - 1) {
                tv1.setBackground(getResources().getDrawable(R.drawable.cell_phone2, getContext().getTheme()));
                tv21.setBackground(getResources().getDrawable(R.drawable.cell_phone2, getContext().getTheme()));
                linearLayout2.setBackground(getResources().getDrawable(R.drawable.cell_phone2, getContext().getTheme()));
            } else {
                tv1.setBackground(getResources().getDrawable(R.drawable.cell_phone, getContext().getTheme()));
                linearLayout2.setBackground(getResources().getDrawable(R.drawable.cell_phone, getContext().getTheme()));
                tv21.setBackground(getResources().getDrawable(R.drawable.cell_phone2, getContext().getTheme()));
                tv3.setBackground(getResources().getDrawable(R.drawable.cell_phone3, getContext().getTheme()));
            }
            tv1.setText(String.valueOf(lesson.numInDay));
            try {
                String s = lesson.name;
                Spannable spans = new SpannableString(s);
                spans.setSpan(new RelativeSizeSpan(1.5f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv21.setText(spans);
            } catch (Exception e) {
                loge(e);
            }
            if(lesson.attends != null && lesson.attends.name != null) {
                switch (lesson.attends.id) {
                    case 1:
                        if (i == day.lessons.size() - 1)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellsick1, getContext().getTheme()));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellsick0, getContext().getTheme()));
                        break;
                    case 2:
                        if (i == day.lessons.size() - 1)
                            tv1.setBackground(getResources().getDrawable(R.drawable.celllate1, getContext().getTheme()));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.celllate0, getContext().getTheme()));
                        break;
                    case 3:
                        if (i == day.lessons.size() - 1)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellrel1, getContext().getTheme()));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellrel0, getContext().getTheme()));
                        break;
                    case 4:
                        if (i == day.lessons.size() - 1)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellab1, getContext().getTheme()));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellab0, getContext().getTheme()));
                        break;
                    case 6:
                        if (i == day.lessons.size() - 1)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellun1, getContext().getTheme()));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellun0, getContext().getTheme()));
                        break;
                    case 8:
                        if (i == day.lessons.size() - 1)
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellall1, getContext().getTheme()));
                        else
                            tv1.setBackground(getResources().getDrawable(R.drawable.cellall0, getContext().getTheme()));
                        break;
                }
            }
            try {
                String s = lesson.homeWork.stringwork;
                Spannable spans = new SpannableString(s);
                if(s.contains("\n"))
                    spans.setSpan(new RelativeSizeSpan(1f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv22.setText(spans);
            } catch (Exception e) {
                loge(e);
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
                spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.marks, getContext().getTheme())), 0, s1.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans1.setSpan(new RelativeSizeSpan(1.1f), s1.indexOf("\n"), s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), s1.indexOf("\n"), s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv3.setText(spans1);
            } catch (Exception ignored) {
            }

            tbrow.addView(tv1);
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(tv21);
            int size = tv22.getLineHeight();

            linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
            if(lesson.homeWork.files != null && !lesson.homeWork.files.isEmpty()){
                ImageView image = new ImageView(getContext());
                image.setImageBitmap(getBitmap(R.drawable.attach, size));
                image.setPadding(30,0,0,10);
                linearLayout2.addView(image);
            }

            if(lesson.meetingInvite != null && !lesson.meetingInvite.replaceAll(" ", "").equals("")) {
                ImageView image = new ImageView(getContext());
                image.setImageBitmap(getBitmap(R.drawable.video, size));
                image.setPadding(30,0,0,10);
                linearLayout2.addView(image);
            }

            linearLayout2.addView(tv22);

            linearLayout.addView(linearLayout2);
            tbrow.addView(linearLayout);
            tbrow.addView(tv3);
            tableLayout.addView(tbrow);
        }
    }

    Bitmap getBitmap(int drawable, int size) {
        if(bitmaps.containsKey(drawable))
            return bitmaps.get(drawable);
        log("drawable not found");

        Bitmap bitmap = paintAndScale(drawable, size);
        bitmaps.put(drawable, bitmap);
        return bitmap;
    }

    Bitmap paintAndScale(int drawable, int size) {
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), drawable);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.icons, getContext().getTheme()));
        return Bitmap.createScaledBitmap(drawableToBitmap(wrappedDrawable), size, size, true);
    }

    public void CreateODOD(){
        TextView txt = new TextView(getContext());
        txt.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        String s1 = "ОДОД";
        Spannable spans1 = new SpannableString(s1);
        spans1.setSpan(new RelativeSizeSpan(1.4f), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spans1.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt.setText(spans1);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(150, 30, 150, 30);
        txt.setBackground(getResources().getDrawable(R.drawable.cell_phone7, getContext().getTheme()));
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
            spans.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txt2.setText(spans);
            s = day.odods.get(i).name;
            spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(new ForegroundColorSpan(getColorFromAttribute(R.attr.main_font, getContext().getTheme())), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
//        log("draw(), " + dayofweek);
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
            tv1.setTextColor(getColorFromAttribute(R.attr.tv_no_lessons, getContext().getTheme()));
            tv1.setGravity(Gravity.CENTER);
            tv1.setTextSize(30);
            tableLayout.addView(tbrow1);
        }
        if(day != null && day.odods != null && day.odods.size() > 0
                && getContext().getSharedPreferences("pref", 0).getBoolean("odod", true)){
            CreateODOD();
        }
    }

//    static int getDrawableFromAttribute(int attr, Resources.Theme theme) {
//        TypedArray a = theme.obtainStyledAttributes(R.style.AppTheme, new int[] {attr});
//        int attributeResourceId = a.getResourceId(0, 0);
//        a.recycle();
//        return attributeResourceId;
//    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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