package ru.gurhouse.sch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;

import java.util.HashMap;

import static ru.gurhouse.sch.KnockFragment.connect;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.PageFragment.bitmaps;

public class SettingsActivity extends AppCompatActivity {

    private EditText et, et_nickname;
    private ImageView send;
    private boolean kk_enabled = false;
    private static int scrollTo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences pref = getSharedPreferences("pref", 0);
        switch (pref.getString("theme", "dark")) {
            case "dark":
                setTheme(R.style.MyDarkTheme);
                break;
            case "light":
                setTheme(R.style.MyLightTheme);
        }
        setContentView(R.layout.activity_settings);

        ScrollView scroll = findViewById(R.id.scroll_settings);
        scroll.post(() -> {
            scroll.scrollTo(0, scrollTo);
            scrollTo = 0;
        });

        PeriodFragment.settingsClicked = false;
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getColorFromAttribute(R.attr.main, getTheme()));
        }

        if(pref.getBoolean("easter_egg", false)) {
            findViewById(R.id.divider3).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_themes).setVisibility(View.VISIBLE);
            findViewById(R.id.rg_theme).setVisibility(View.VISIBLE);
        }

        et = findViewById(R.id.et_feedback);
        send = findViewById(R.id.btn_fbsend);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().equals("")) {
                    send.setVisibility(View.GONE);
                } else
                    send.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        send.setOnClickListener(v -> {
            final String text = et.getText().toString();
            new Thread() {
                @Override
                public void run() {
                    try {
                        connect("https://still-cove-90434.herokuapp.com/new_event",
                                "firebase_id=" + TheSingleton.getInstance().getFb_id() + "&event=feedback" +
                                        "&msg=" + text + "&time=" + System.currentTimeMillis());
                    } catch (Exception e) {loge(e);}
                }
            }.start();
            et.setText("");
            startActivity(new Intent(getApplicationContext(), ThanksActivity.class));
        });

//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setShape(GradientDrawable.RECTANGLE);
//        drawable.setCornerRadius(50);
//        drawable.setColor(getColorFromAttribute(R.attr.main, getTheme()));
//        drawable.setStroke(3, Color.WHITE);
//
//        FrameLayout color = findViewById(R.id.color_background);
//        EditText et = findViewById(R.id.et_backgroundColor);
//        color.setBackground(drawable);
//        color.setOnClickListener(v -> {
//            AlertDialog.Builder alert = new AlertDialog.Builder(this);
//            alert.setTitle("Основной фон");
//            View view = getLayoutInflater().inflate(R.layout.color_picker, null, false);
//            ColorPicker picker = view.findViewById(R.id.picker);
//            picker.addSVBar(view.findViewById(R.id.svbar));
//            alert.setView(view);
//            alert.setPositiveButton("OK", (dialog, which) -> {
//                pref.edit().putString("backgroundColor", String.format("#%06X", 0xFFFFFF & picker.getColor())).apply();
//                refreshColor("backgroundColor", color, et, true);
//            });
//            alert.show();
//        });

//        et.setText(pref.getString("backgroundColor", "#303030"));
//        et.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.length() == 7 && s.charAt(0) == '#') {
//                    for (int i = 1; i <= 6; i++) {
//                        if(!Character.isDigit(s.charAt(i)))
//                            return;
//                    }
//                    pref.edit().putString("backgroundColor", s.toString()).apply();
//                    refreshColor("backgroundColor", color, et, false);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        Switch auto = findViewById(R.id.switch_auto);
        auto.setChecked(pref.getBoolean("auto", true));
        auto.setOnCheckedChangeListener((buttonView, isChecked) -> pref.edit().putBoolean("auto", isChecked).apply());

        Switch period = findViewById(R.id.switch_period);
        period.setChecked(pref.getBoolean("period_normal", false));
        period.setOnCheckedChangeListener((buttonView, isChecked) -> pref.edit().putBoolean("period_normal", isChecked).apply());

        Switch nextday = findViewById(R.id.switch_nextday);
        nextday.setChecked(pref.getBoolean("nextday", true));
        nextday.setOnCheckedChangeListener((buttonView, isChecked) -> pref.edit().putBoolean("nextday", isChecked).apply());

        Switch avgfixed = findViewById(R.id.switch_avgfixed);
        avgfixed.setChecked(pref.getBoolean("avg_fixed", false));
        avgfixed.setOnCheckedChangeListener((buttonView, isChecked) -> pref.edit().putBoolean("avg_fixed", isChecked).apply());

        Switch odod = findViewById(R.id.switch_odod);
        odod.setChecked(pref.getBoolean("odod", true));
        odod.setOnCheckedChangeListener((buttonView, isChecked) -> pref.edit().putBoolean("odod", isChecked).apply());

        findViewById(R.id.btn_quit).setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent().putExtra("goal", "quit"));
            finish();
        });

        Switch chat = findViewById(R.id.switch_chat);
        chat.setChecked(pref.getBoolean("show_chat", false));
        chat.setOnCheckedChangeListener((buttonView, isChecked) -> pref.edit().putBoolean("show_chat", isChecked).apply());

        et_nickname = findViewById(R.id.et_nickname);
        kk_enabled = !pref.getString("knock_name", "").equals("");
        if(!kk_enabled) {
            et_nickname.setEnabled(false);
            et_nickname.setText("");
        } else {
            et_nickname.setEnabled(true);
            et_nickname.setText(pref.getString("knock_name", ""));
        }

        findViewById(R.id.btn_ok).setOnClickListener(v -> {
            if(kk_enabled) {
                final String text = et_nickname.getText().toString();
                if(text.replaceAll(" ", "").equals(""))
                    return;
                et_nickname.clearFocus();
                hideKeyboard(SettingsActivity.this, et_nickname);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            connect("https://warm-bayou-37022.herokuapp.com/update",
                                    "column=name&id=" + pref.getString("knock_id", "") + "&value=" + text);
                        } catch (Exception e) {loge(e);}
                    }
                }.start();
            }
        });

        RadioGroup rg = findViewById(R.id.rg_theme);

        switch (pref.getString("theme", "dark")) {
            case "dark":
                rg.check(R.id.rb_dark);
                break;
            case "light":
                rg.check(R.id.rb_light);
        }
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.rb_dark) {
                if(!pref.getString("theme", "dark").equals("dark")) {
                    scrollTo = scroll.getScrollY();
                    bitmaps = new HashMap<>();
                    pref.edit().putString("theme", "dark").apply();
                    restartActivity(this, true);
                }
                pref.edit().putString("theme", "dark").apply();
            } else if(checkedId == R.id.rb_light) {
                if(!pref.getString("theme", "light").equals("light")) {
                    scrollTo = scroll.getScrollY();
                    bitmaps = new HashMap<>();
                    pref.edit().putString("theme", "light").apply();
                    restartActivity(this, true);
                }
                pref.edit().putString("theme", "light").apply();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle("Настройки");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

//    void refreshColor(String colorName, FrameLayout color, EditText et, boolean setText) {
//        log("refreshing color " + colorName);
//        String s = getSharedPreferences("pref", 0).getString(colorName, "");
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setShape(GradientDrawable.RECTANGLE);
//        drawable.setCornerRadius(50);
//        drawable.setColor(Color.parseColor(s));
//        drawable.setStroke(3, Color.WHITE);
//
//        color.setBackground(drawable);
//        if(setText)
//            et.setText(s);
//    }

    public static int getColorFromAttribute(int attr, Resources.Theme theme) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attr, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    public static void restartActivity(Activity activity, boolean anim) {
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        if(anim)
            activity.overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_out);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
//        else {
//            new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        connect("https://still-cove-90434.herokuapp.com/logout",
//                                "firebase_id=" + TheSingleton.getInstance().getFb_id());
//                    } catch (Exception e) {
//                        loge("logout: " + e.toString());
//                    }
//                }
//            }.start();
//            SharedPreferences pref = getSharedPreferences("pref", 0);
//            pref.edit().putBoolean("first_time", true).apply();
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//        }
        return super.onOptionsItemSelected(item);
    }

    private static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}