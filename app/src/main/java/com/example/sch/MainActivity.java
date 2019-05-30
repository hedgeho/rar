package com.example.sch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.sch.LoginActivity.connect;
import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class MainActivity extends AppCompatActivity {

    PeriodFragment periodFragment;
    PeriodFragment1 periodFragment1;
    MessagesFragment messagesFragment;
    ConstraintLayout main, chat;
    ScheduleFragment scheduleFragment;
    ArrayList<PeriodFragment.Subject> subjects;
    ArrayList<PeriodFragment.Day> days;
    Snackbar snackbar;
    String[] period;
    int pernum = 6;
    static LayoutInflater layoutInflater;
    BroadcastReceiver receiver, internet_receiver;
    BottomNavigationView bottomnav;
    boolean LOAD_READY = false;
    BottomNavigationItemView itemView;
    boolean mode0 = false;

    // to.do fix period1: загрузка
    // to.do исчезание чата
    // todo адресат в нокноке и чатах
    // todo смена ника, пролистывание, нормальный фидбек settings

    // завтра
    // todo переключение периодов
    // todo pageAdapter при переключении во время загрузки

    private BottomNavigationView.OnNavigationItemSelectedListener mNavigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            switch (item.getItemId()) {
                case R.id.navigation_period:
                    toolbar.setTitle("Оценки за период");
                    toolbar.setClickable(true);
                    if(mode0)
                        loadFragment(periodFragment);
                    else
                        loadFragment(periodFragment1);
                    return true;
                case R.id.navigation_diary:
                    toolbar.setTitle("Дневник");
                    toolbar.setClickable(false);
                    loadFragment(scheduleFragment);
                    return true;
                case R.id.navigation_messages:
                    toolbar.setTitle("Сообщения");
                    toolbar.setClickable(false);
                    if(getStackTop() instanceof MessagesFragment)
                        ((MessagesFragment) getStackTop()).refresh();
                    else
                        loadFragment(messagesFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mode0 = getSharedPreferences("pref", 0).getBoolean("period_normal", false);

        periodFragment1 = new PeriodFragment1();
        periodFragment = new PeriodFragment();
        scheduleFragment = new ScheduleFragment();
        messagesFragment = new MessagesFragment();

        final String login = getIntent().getStringExtra("login"), hash = getIntent().getStringExtra("hash");
        new Thread() {
            @Override
            public void run() {
                try {
                    login(login, hash);
                } catch (Exception e) {loge("login: " + e.toString());}
            }
        }.start();
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                loge("caught");
                SharedPreferences pref = getSharedPreferences("pref", 0);
                pref.edit().putString("error", t.toString() + ": " + e.toString()).apply();
                throw new RuntimeException(e);
            }
        });*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.gr1));
        }

        new Thread() {
            @Override
            public void run() {
                final FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());
                Bundle bundle = new Bundle();
                bundle.putString("text", "test_text");
                bundle.putString("info", "test_info");
                bundle.putLong(FirebaseAnalytics.Param.VALUE, 1);
                bundle.putString("currency", "rub");
                analytics.logEvent("test_conversion", bundle);
                /*bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "item_id!");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "item_name!");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content_type!");
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);*/
            }
        };//.start();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getStackTop() instanceof MessagesFragment) {
                    ((MessagesFragment) getStackTop()).newMessage(intent.getStringExtra("text"),
                            intent.getLongExtra("time", 0), intent.getIntExtra("sender_id", 0),
                            intent.getIntExtra("thread_id", 0));
                } else if (getStackTop() instanceof ChatFragment) {
                    ((ChatFragment) getStackTop()).newMessage(intent.getStringExtra("text"), intent.getLongExtra("time", 0),
                            intent.getIntExtra("sender_id", 0), intent.getIntExtra("thread_id", 0),
                            intent.getStringExtra("sender_fio"));
                } else {
                    BottomNavigationMenuView bottomNavigationMenuView =
                            (BottomNavigationMenuView) bottomnav.getChildAt(0);
                    View v = bottomNavigationMenuView.getChildAt(2);
                    BottomNavigationItemView itemView = (BottomNavigationItemView) v;
                    TextView tv = itemView.findViewById(R.id.tv_badge);
                    if(tv.getVisibility() == View.INVISIBLE) {
                        tv.setVisibility(View.VISIBLE);
                        tv.setText("1");
                    } else
                        tv.setText(Integer.parseInt(tv.getText().toString()) + 1 + "");
                }
            }
        };
        internet_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(hasConnection(context)) {
                    snackbar.dismiss();
                } else {
                    snackbar.show();
                }
            }
        };

        int permissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        log("permission check: " + permissionCheck);
        if (permissionCheck < 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 12345);
        }
        main = findViewById(R.id.main_container);
        chat = findViewById(R.id.chat_container);

        snackbar = Snackbar.make(main, "No internet connection", Snackbar.LENGTH_INDEFINITE);

        layoutInflater = getLayoutInflater();

        bottomnav = findViewById(R.id.bottomnav);
        bottomnav.setOnNavigationItemSelectedListener(mNavigationListener);
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomnav.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(2);
        itemView = (BottomNavigationItemView) v;
        /*View badge = */getLayoutInflater().inflate(R.layout.badge, itemView, true);


        if(getIntent().getBooleanExtra("notif", false)) {
            log("notif");
            if(getIntent().getStringExtra("type").equals("msg")) {
                log("type - msg");
                bottomnav.setSelectedItemId(R.id.navigation_messages);
                messagesFragment.fromNotification = true;
                messagesFragment.notifThreadId = getIntent().getIntExtra("threadId", -1);
                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle("Сообщения");
                setSupportActionBar(toolbar);
                loadFragment(messagesFragment);
            }
        } else {
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Дневник");
            setSupportActionBar(toolbar);
            loadFragment(scheduleFragment);
            bottomnav.setSelectedItemId(R.id.navigation_diary);
        }
    }

    final Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(getStackTop() instanceof MessagesFragment)
                messagesFragment.show();
        }
    };

    void login(String login, String hash) throws IOException, JSONException {
        URL url;
        HttpURLConnection con;
        StringBuilder result;
        BufferedReader rd;
        String line;

        int userId = -1, prsId;
        String name;
        SharedPreferences pref = getSharedPreferences("pref", 0);
        if(pref.getInt("userId", -1) == -1) {
         //if(true) {
            log("userId not found, calling state");
            url = new URL("https://app.eschool.center/ec-server/state?menu=false");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Cookie", TheSingleton.getInstance().getCOOKIE() + "; site_ver=app; route=" + TheSingleton.getInstance().getROUTE() + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
            con.connect();
            result = new StringBuilder();
            log(con.getResponseMessage());

            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            log("state: " + result.toString());
            JSONObject obj = new JSONObject(result.toString());
            if (obj.has("userId"))
                userId = obj.getInt("userId");
            prsId = obj.getJSONObject("user").getInt("prsId");
            name = obj.getJSONObject("profile").getString("firstName");
            pref.edit().putInt("userId", userId).putInt("prsId", prsId)
                    .putString("name", name).apply();
        } else {
            userId = pref.getInt("userId", -1);
            prsId = pref.getInt("prsId", -1);
            name = pref.getString("name", "");
        }
        log("userId: " + userId + ", prsId: " + prsId + ", name: " + name);
        TheSingleton.getInstance().setUSER_ID(userId);
        TheSingleton.getInstance().setPERSON_ID(prsId);
        LOAD_READY = true;
        scheduleFragment.start();
        messagesFragment.start(h);
        new Thread() {
            @Override
            public void run() {
                try {
                    final String s = connect("https://app.eschool.center/ec-server/chat/count?prsId=" + TheSingleton.getInstance().getPERSON_ID(),
                            null, getApplicationContext());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = itemView.findViewById(R.id.tv_badge);
                            if(s.equals("0"))
                                tv.setVisibility(View.INVISIBLE);
                            else
                                tv.setText(s);
                        }
                    });
                } catch (Exception e) {
                    loge(e.toString());
                }
            }
        }.start();
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        switch (getIntent().getIntExtra("mode", -1)) {
            case 0:
                bundle.putString(FirebaseAnalytics.Param.METHOD, "test");
                break;
            case 1:
                bundle.putString(FirebaseAnalytics.Param.METHOD, "auto");
                log("auto");
                break;
            case 2:
                bundle.putString(FirebaseAnalytics.Param.METHOD, "password");
                break;
            case 3:
                bundle.putString(FirebaseAnalytics.Param.METHOD, "hash");
                break;
            default:
                bundle.putString(FirebaseAnalytics.Param.METHOD, "some_method");
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

        url = new URL("https://still-cove-90434.herokuapp.com/login");
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.connect();
        OutputStream os = con.getOutputStream();
        os.write(("login=" + login + "&password=" + hash + "&firebase_id=" + TheSingleton.getInstance().getFb_id()).getBytes());
        log("login=" + login + "&password=" + hash + "&firebase_id=" + TheSingleton.getInstance().getFb_id());

        loge(con.getResponseMessage());
    }

    void sasha(String s) {
        Log.v("sasha", s);
    }

    void set(ScheduleFragment.Period[] periods, int pernum) {
        sasha(" PeriodFragment1");
//        periodFragment1 = new PeriodFragment1();
        period = scheduleFragment.period;
        periodFragment1.period = period;
        periodFragment1.pernum = pernum;
        periodFragment1.periods = periods;
        if(!periodFragment1.first_time&&!periodFragment1.shown) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    periodFragment1.show();
                }
            });
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                bottomnav.setVisibility(View.VISIBLE);
            }
        });
        TheSingleton.getInstance().setSubjects(subjects);
        TheSingleton.getInstance().setDays(days);
    }

    void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setSupActionBar(android.support.v7.widget.Toolbar toolbar) {
        if(getSupportActionBar() == null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void set_visible(boolean b) {
        if(b) {
            main.setVisibility(View.VISIBLE);
            chat.setVisibility(View.INVISIBLE);
        } else {
            main.setVisibility(View.INVISIBLE);
            chat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        log("fragments on MainActivity: " + getSupportFragmentManager().getFragments().size());
        List<Fragment> a = getSupportFragmentManager().getFragments();
        for (int i = 0; i < a.size(); i++) {
            log(a.get(i).toString());
        }
        if(getStackTop() instanceof ChatFragment || getStackTop() instanceof DayFragment || getStackTop() instanceof MarkFragment ||
                getStackTop() instanceof SubjectFragment || getStackTop() instanceof KnockFragment) {
            set_visible(true);
            getSupportActionBar().setTitle("Сообщения");
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        if(!(getSupportFragmentManager().getBackStackEntryCount() == 0))
            if(!(getStackTop() instanceof PeriodFragment || getStackTop() instanceof PeriodFragment1
                    || getStackTop() instanceof ScheduleFragment || getStackTop() instanceof ScheduleFragment1
                    || getStackTop() instanceof MessagesFragment))
                getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
        try {
            registerReceiver(receiver, new IntentFilter("com.example.sch.action"));
            registerReceiver(internet_receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e) {
            loge(e.toString());
        }
        mode0 = getSharedPreferences("pref", 0).getBoolean("period_normal", false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
        try {
            unregisterReceiver(receiver);
            unregisterReceiver(internet_receiver);
        } catch (Exception e) {
            loge(e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    void quit() {
        new Thread() {
            @Override
            public void run() {
                try {
                    connect("https://still-cove-90434.herokuapp.com/logout",
                            "firebase_id=" + TheSingleton.getInstance().getFb_id(), getApplicationContext());
                } catch (Exception e) {
                    loge("logout: " + e.toString());
                }
            }
        }.start();
        TheSingleton.getInstance().setPERSON_ID(-1);
        TheSingleton.getInstance().setUSER_ID(-1);
        SharedPreferences pref = getSharedPreferences("pref", 0);
        pref.edit().putBoolean("first_time", true).putInt("userId", -1).putInt("prsId", -1).putString("name", "").apply();
        finish();
    }

    Fragment getStackTop() {
        List<Fragment> a = getSupportFragmentManager().getFragments();
        if(a.size()==0)
            return null;
        return a.get(a.size() - 1);
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }
}
