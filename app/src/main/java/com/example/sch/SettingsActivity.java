package com.example.sch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Switch;

import static com.example.sch.KnockFragment.connect;
import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class SettingsActivity extends AppCompatActivity {

    EditText et;
    Button send;
    String s = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.gr1));
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
                    send.setEnabled(false);
                } else
                    send.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = et.getText().toString();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            connect("https://still-cove-90434.herokuapp.com/new_event",
                                    "firebase_id=" + TheSingleton.getInstance().getFb_id() + "&event=feedback" +
                                            "&msg=" + text + "&time=" + System.nanoTime());
                        } catch (Exception e) {loge(e.toString());}
                    }
                }.start();
                et.setText("");
                startActivity(new Intent(getApplicationContext(), ThanksActivity.class));
            }
        });

        Switch auto = findViewById(R.id.switch_auto);
        final SharedPreferences pref = getSharedPreferences("pref", 0);
        auto.setChecked(pref.getBoolean("auto", true));
        auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                log("switch1: " + isChecked);
                pref.edit().putBoolean("auto", isChecked).apply();
            }
        });

        Switch period = findViewById(R.id.switch_period);
        period.setChecked(pref.getBoolean("period_normal", false));
        period.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pref.edit().putBoolean("period_normal", isChecked).apply();
            }
        });

        findViewById(R.id.btn_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent().putExtra("goal", "quit"));
                finish();
            }
        });

        Switch chat = findViewById(R.id.switch_chat);
        chat.setChecked(pref.getBoolean("show_chat", true));
        chat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pref.edit().putBoolean("show_chat", isChecked).apply();
            }
        });

        EditText et_nickname = findViewById(R.id.et_nickname);
        if(pref.getString("knock_name", "").equals(""))
            et_nickname.setEnabled(false);
        else {
            et_nickname.setEnabled(true);
            et_nickname.setText(pref.getString("knock_name", ""));
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        connect("https://warm-bayou-37022.herokuapp.com/update",
                                "column=name&id=" + pref.getString("knock_id", "") + "&value=" + s);
                    } catch (Exception e) {loge(e.toString());}
                }
            };
            et_nickname.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable e) {
                    // todo кнопка
                    if(thread.isAlive())
                        thread.stop();
                    s = e.toString();
                    //thread.start();
                }
            });
        }

        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Quit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        connect("https://still-cove-90434.herokuapp.com/logout",
                                "firebase_id=" + TheSingleton.getInstance().getFb_id());
                    } catch (Exception e) {
                        loge("logout: " + e.toString());
                    }
                }
            }.start();
            SharedPreferences pref = getSharedPreferences("pref", 0);
            pref.edit().putBoolean("first_time", true).apply();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
