package ru.gurhouse.sch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.isQuit;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;

public class MainActivity extends AppCompatActivity {

    public static final boolean TYPE_Q = true; // четверти
    public static final boolean TYPE_SEM = false; // полугодия

    private PeriodFragment periodFragment;
    private PeriodFragment1 periodFragment1;
    private MessagesFragment messagesFragment;
    private ConstraintLayout main, chat;
    private String[] period;
    private int state = 2;
    private BroadcastReceiver receiver;
    private BottomNavigationView bottomnav;
    private BottomNavigationItemView itemView;
    private boolean mode0 = false;
    private int tap = 0;
    private FrameLayout frameLayout;

    ScheduleFragment scheduleFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mNavigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if(getSupportActionBar() == null)
                setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            switch (item.getItemId()) {
                case R.id.navigation_period:
                    tap = 0;
                    toolbar.setClickable(true);
                    state = 1;
                    if(mode0)
                        loadFragment(periodFragment);
                    else
                        loadFragment(periodFragment1);
                    break;
                case R.id.navigation_diary:
                    tap++;
                    toolbar.setTitle("Дневник");
                    toolbar.setClickable(false);
                    if(tap > 2) {
                        tap = 1;
                        try {
                            scheduleFragment.pager.setCurrentItem(scheduleFragment.pageCount / 2 + 1);
                        } catch (Exception e) {
                        }
                    } else if(state == 2) {
                        if (scheduleFragment.pager != null && scheduleFragment.pager.getCurrentItem() == scheduleFragment.pageCount / 2 + 1)
                            tap = 1;
                    } else
                        loadFragment(scheduleFragment);
                    state = 2;
                    break;
                case R.id.navigation_messages:
                    tap = 0;
                    toolbar.setTitle("Сообщения");
                    toolbar.setClickable(false);
                    if(getStackTop() instanceof MessagesFragment)
                        ((MessagesFragment) getStackTop()).refresh();
                    else
                        loadFragment(messagesFragment);
                    state = 3;
                    break;
                default:
                    state = 0;
                    return false;
            }
            setSupportActionBar(toolbar);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mode0 = getSharedPreferences("pref", 0).getBoolean("period_normal", false);

        periodFragment1 = new PeriodFragment1();
        periodFragment = new PeriodFragment();
        scheduleFragment = new ScheduleFragment();
        messagesFragment = new MessagesFragment();
        frameLayout = findViewById(R.id.frame);

        final String login = getIntent().getStringExtra("login"), hash = getIntent().getStringExtra("hash");
        new Thread() {
            @Override
            public void run() {
                try {
                    login(login, hash);
                } catch (Exception e) {
                    loge("login: " + e.toString());}
            }
        }.start();

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
                    try {
                        ArrayList<ChatFragment.Attach> files = new ArrayList<>();
                        JSONArray arr = new JSONArray(intent.getStringExtra("attach"));
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tmp1 = arr.getJSONObject(j);
                            files.add(new ChatFragment.Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
                                    tmp1.getString("fileName"), tmp1.getString("fileType")));
                        }

                        ((ChatFragment) getStackTop()).newMessage(intent.getStringExtra("text"), new Date(intent.getLongExtra("time", 0)),
                                intent.getIntExtra("sender_id", 0), intent.getIntExtra("thread_id", 0),
                                intent.getStringExtra("sender_fio"), files,true);
                    } catch (JSONException e) {
                        loge(e);
                    }
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

        main = findViewById(R.id.main_container);
        chat = findViewById(R.id.chat_container);

        //snackbar = Snackbar.make(findViewById(R.id.frame), "Нет подключения к интернету", Snackbar.LENGTH_INDEFINITE);

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
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                loge(e);}
            runOnUiThread(() -> {
                if(findViewById(R.id.refresh) != null) {
                    findViewById(R.id.refresh).setOnClickListener((view) -> {
                        view.setVisibility(View.INVISIBLE);
                        findViewById(R.id.tv_error).setVisibility(View.INVISIBLE);
                        findViewById(R.id.frame).setVisibility(View.VISIBLE);
                        if (getIntent().getStringExtra("login") != null) {
                            new Thread(() -> {
                                try {
                                    login(getIntent().getStringExtra("login"), getIntent().getStringExtra("hash"));
                                } catch (LoginActivity.NoInternetException e) {
                                    runOnUiThread(() -> {
                                        view.setVisibility(View.VISIBLE);
                                        findViewById(R.id.tv_error).setVisibility(View.VISIBLE);
                                        findViewById(R.id.frame).setVisibility(View.INVISIBLE);
                                    });
                                } catch (Exception e) {
                                    loge(e);
                                }
                            }).start();
                        }
                    });
                }
            });
        }).start();
    }
    static int newCount = 0;
    private final Runnable r = () -> {
        if(getStackTop() instanceof MessagesFragment)
            messagesFragment.show();
        else
            runOnUiThread(() -> {
                TextView tv = itemView.findViewById(R.id.tv_badge);
                if(newCount == 0)
                    tv.setVisibility(View.INVISIBLE);
                else
                    tv.setText(newCount + "");
            });
    };

    private void login(String login, String hash) throws IOException, JSONException, LoginActivity.NoInternetException {
        int userId = -1, prsId;
        String name;
        SharedPreferences pref = getSharedPreferences("pref", 0);
        if(pref.getInt("userId", -1) == -1 || !pref.getBoolean("auto", true)) {
         //if(true) {
            log("userId not found, calling state");
            String result = connect("https://app.eschool.center/ec-server/state?menu=false", null, this);

            log( "state: " + result);
            JSONObject obj = new JSONObject(result);
            if (obj.has("userId"))
                userId = obj.getInt("userId");
            if (!obj.getJSONObject("user").getJSONArray("roles").getString(0).equals("ROLE_STUDENT")) {
                result = connect("https://app.eschool.center/ec-server/profile/" + userId + "/children", null, this);
                obj = new JSONArray(result).getJSONObject(0);
                userId = obj.getInt("userId");
                prsId = obj.getInt("prsId");
                name = obj.getString("fio");
            } else {
                prsId = obj.getJSONObject("user").getInt("prsId");
                name = obj.getJSONObject("profile").getString("firstName");
            }
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
        scheduleFragment.context = this;
        new Thread() {
            @Override
            public void run() {
                try {
                    scheduleFragment.start();
                } catch (Exception e) {
                    loge(e);
                }
            }
        }.start();
        if(getIntent().getBooleanExtra("notif", false)) {
            if(getIntent().getStringExtra("type").equals("msg")) {
                messagesFragment.fromNotification = true;
                messagesFragment.notifThreadId = getIntent().getIntExtra("threadId", -1);
                messagesFragment.notifCount = getIntent().getIntExtra("count", -1);
                messagesFragment.notifSenderFio = getIntent().getStringExtra("senderFio");
                scheduleFragment.context = this;
                periodFragment.context = this;
                periodFragment1.context = this;
            }
        }
        messagesFragment.context = this;
        messagesFragment.start(r);
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
                bundle.putString(FirebaseAnalytics.Param.METHOD, "bruh");
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            if(TheSingleton.getInstance().getFb_id() != null)
                KnockFragment.connect("https://still-cove-90434.herokuapp.com/login",
                        "login=" + login + "&password=" + hash + "&firebase_id=" + TheSingleton.getInstance().getFb_id()
                                + "&version=" + version);
//            log("login=" + login + "&password=" + hash + "&firebase_id=" + TheSingleton.getInstance().getFb_id()
//                + "&version=" + version);
        } catch (PackageManager.NameNotFoundException e) {
            loge(e);
        }
    }

    void set(ScheduleFragment.Period[] periods, int pernum) {
        set(periods, pernum, true);
    }
    void set(ScheduleFragment.Period[] periods, int pernum, boolean show) {
        period = scheduleFragment.period;
        if(!show) {
            periodFragment.periods = periods;
            periodFragment1.periods = periods;
            return;
        }
        periodFragment1 = new PeriodFragment1();
        periodFragment1.period = period;
        periodFragment1.pernum = pernum;
        periodFragment1.periods = periods;
        if (state == 1 && !mode0)
            loadFragment(periodFragment1);
        periodFragment = new PeriodFragment();
        periodFragment.period = period;
        periodFragment.pernum = pernum;
        periodFragment.periods = periods;
        periodFragment.mode = !mode0;
        if (state == 1 && mode0)
            loadFragment(periodFragment);
       // TheSingleton.getInstance().setDays(days);
    }

    private void loadFragment(Fragment fragment) {
        printStack();
        log("loading " + fragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        printStack();
    }

    public void setSupActionBar(android.support.v7.widget.Toolbar toolbar) {
        //if(getSupportActionBar() == null)
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

    public void saveFile(String url, String name) {
        log("saving " + name);
        int permissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permissionCheck < 0) {
            log("requesting permission to save file");
            this.url = url;
            this.name = name;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 12345);
        } else {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            try {
                request.setDescription("Downloading file from " + new URL(url).getHost());
            } catch (MalformedURLException e) {
                loge(e);
                request.setDescription("Downloading file");
            }
            request.setTitle(name);
            SharedPreferences pref = getSharedPreferences("pref", 0);
            new Thread(() -> {
                if(System.currentTimeMillis() - pref.getLong("cookieTime", 0) > 60*60*1000) {
                    try {
                        LoginActivity.login(getApplicationContext());
                    } catch (LoginActivity.NoInternetException e) {
                        loge(e);
                    }
                }
                runOnUiThread(() -> {
                    request.addRequestHeader("Cookie", pref.getString("cookie", "") + "; site_ver=app;" +
                            " route=" + pref.getString("route", "") + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);

                    // get download service and enqueue file
                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                });
            }).start();
        }
    }

    private String url = null, name;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(url != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            log("permission granted: " + permissions[0]);
            saveFile(url, name);
        } else
            log("permission denied: " + permissions[0]);
        url = null;
    }

    void nullsub(ScheduleFragment.Period[] periods, int pernum) {
        nullsub(periods, pernum, true);
    }
    void nullsub(ScheduleFragment.Period[] periods, int pernum, boolean show) {
        log("nullsub");
        period = scheduleFragment.period;
        periodFragment.periods = periods;
        if(show) {
            if(mode0) {
                periodFragment = new PeriodFragment();
                periodFragment.period = period;
                periodFragment.periods = periods;
                periodFragment.pernum = pernum;
                periodFragment.nullsub = true;
                if (state == 1)
                    loadFragment(periodFragment);
            } else {
                periodFragment1 = new PeriodFragment1();
                periodFragment1.period = period;
                periodFragment1.periods = periods;
                periodFragment1.pernum = pernum;
                periodFragment1.nullsub = true;
                if (state == 1)
                    loadFragment(periodFragment1);
            }
        }
        runOnUiThread(this::updatePages);
    }

    public void updateSubjects(ScheduleFragment.Period[] periods, int pernum) {
        // todo for average in notifications ?
        if(pernum < 3)
            return;
        PeriodFragment.Subject[] array = periods[pernum].subjects, bigarray;
        if (pernum == 3 || pernum == 4)
            bigarray = periods[1].subjects;
        else
            bigarray = periods[2].subjects;
        try {
            JSONArray subjects = new JSONArray();
            JSONObject object;
            PeriodFragment.Cell[] cells;
            double d;
            double f;
            long lastMark = 0;
            for (int i = 0; i < array.length; i++) {
                d = 0;
                f = 0;
                object = new JSONObject();
                if(array[i].periodType == TYPE_SEM)
                    cells = bigarray[i].cells;
                else
                    cells = array[i].cells;
                object.put("name", array[i].name);
                object.put("unitid", array[i].unitid);
                for (int j = 0; j < cells.length; j++) {
                    if(cells[j].markvalue != null)
                        if (cells[j].markvalue != null && !cells[j].markvalue.equals(" "))
                            if (cells[j].markvalue.equals("1") || cells[j].markvalue.equals("2")
                                    || cells[j].markvalue.equals("3") || cells[j].markvalue.equals("4")
                                    || cells[j].markvalue.equals("5")) {
                                d += Double.parseDouble(cells[j].markvalue) * cells[j].mktWt;
                                f += cells[j].mktWt;
                                lastMark = cells[j].lessonid;
                            }
                }
                object.put("d", d);
                object.put("f", f);
                object.put("lastMark", lastMark);
                subjects.put(object);
            }
            getSharedPreferences("pref", MODE_PRIVATE).edit().putString("subjects", subjects.toString()).apply();
        } catch (Exception e) {
            loge(e);
        }
    }

    public void updatePages() {
        List<Fragment> a = getSupportFragmentManager().getFragments();
        for (int i = 0; i < a.size(); i++) {
            if(a.get(i) instanceof PageFragment) {
                PageFragment p = (PageFragment) a.get(i);
                p.draw();
            }
        }
    }

    public void printStack() {
        log("fragments on MainActivity: " + getSupportFragmentManager().getFragments().size());
        List<Fragment> a = getSupportFragmentManager().getFragments();
        for (int i = 0; i < a.size(); i++) {
            log(a.get(i).toString());
        }
    }

    @Override
    public void onBackPressed() {
        log("onBackPressed");
        printStack();
        //log("top: " + getStackTop());
        List<Fragment> a = getSupportFragmentManager().getFragments();
        if(getStackTop() instanceof ChatFragment || getStackTop() instanceof DayFragment || getStackTop() instanceof MarkFragment ||
                getStackTop() instanceof SubjectFragment || getStackTop() instanceof KnockFragment || getStackTop() instanceof Countcoff) {
            set_visible(true);
            if (getStackTop() instanceof ChatFragment || getStackTop() instanceof KnockFragment) {
                getSupportActionBar().setTitle("Сообщения");
                getSupportActionBar().setSubtitle("");
            }
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        if(!(getSupportFragmentManager().getBackStackEntryCount() == 0))
            if(!(getStackTop() instanceof PeriodFragment || getStackTop() instanceof PeriodFragment1
                    || getStackTop() instanceof ScheduleFragment// || getStackTop() instanceof ScheduleFragment1
                    || getStackTop() instanceof MessagesFragment))
                getSupportFragmentManager().popBackStack();
        if(getStackTop() instanceof ChatFragment && a.get(a.size()-2) instanceof MessagesFragment)
            ((MessagesFragment) a.get(a.size()-2)).refresh(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null)
            if(data.hasExtra("goal"))
                if(data.getStringExtra("goal").equals("quit"))
                    quit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume MainActivity");
        try {
            registerReceiver(receiver, new IntentFilter("ru.gurhouse.sch.action"));
        } catch (Exception e) {
            loge(e);
        }
        mode0 = getSharedPreferences("pref", 0).getBoolean("period_normal", false);
        if (state == 1 && !(getStackTop() instanceof SubjectFragment || getStackTop() instanceof MarkFragment
            || getStackTop() instanceof Countcoff)) {
            log("heeeree, Mode0=" + mode0);
//            if(!(getStackTop() instanceof PageFragment))
//                getSupportFragmentManager().popBackStack();
//            if(getStackTop() instanceof PeriodFragment != mode0)
//                getSupportFragmentManager().popBackStack();
            if (mode0) {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .detach(periodFragment1)
//                        .attach(periodFragment)
//                        .commit();
//                periodFragment1.view.setVisibility(View.INVISIBLE);
                //getSupportFragmentManager().popBackStack("PeriodFragment1", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                loadFragment(periodFragment1);
                loadFragment(periodFragment);
            } else {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .detach(periodFragment)
//                        .attach(periodFragment1)
//                        .commit();
                //periodFragment1.view.setVisibility(View.VISIBLE);
                //getSupportFragmentManager().popBackStack("PeriodFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                loadFragment(periodFragment);
                loadFragment(periodFragment1);
            }
        }
        updatePages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            loge(e);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        log("onRestoreInstanceState");
        LoginActivity.isRestoreInstance = true;
        finish();

                /*LoginActivity.login(
                        getSharedPreferences("pref", 0).getString("login", ""),
                        getSharedPreferences("pref", 0).getString("hash", ""));
                runOnUiThread(()->{
                    bottomnav = findViewById(R.id.bottomnav);
                    bottomnav.setSelectedItemId(R.id.navigation_diary);
                    set_visible(true);
                    Toolbar toolbar = findViewById(R.id.toolbar);
                    toolbar.setTitle("Дневник");
                    toolbar.setClickable(false);
                    scheduleFragment = new ScheduleFragment();
                    loadFragment(scheduleFragment);
                    scheduleFragment.show();
                });*/
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
        pref.edit().putBoolean("first_time", true).putInt("userId", -1).putInt("prsId", -1).putString("name", "")
                .putString("knock_token", "").putString("knock_name", "").putString("knock_id", "")
                .putString("muted", "[]").putString("login", "").putString("hash", "").putString("periods2", "")
                .putString("cookie", "").putString("route", "").putLong("cookie_time", 0).apply();
        isQuit = true;
        finish();
    }

    Fragment getStackTop() {
        List<Fragment> a = getSupportFragmentManager().getFragments();
        if(a.size()==0)
            return null;
        return a.get(a.size() - 1);
    }

    /*public static boolean hasConnection(final Context context) {
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
    }*/
}
