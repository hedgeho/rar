package com.example.sch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toolbar;

import java.util.List;

import static com.example.sch.LoginActivity.log;

public class MainActivity extends AppCompatActivity {

    PeriodFragment periodFragment;
    MessagesFragment messagesFragment;
    ConstraintLayout main, chat;
    ScheduleFragment scheduleFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mNavigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_period:
                    loadFragment(periodFragment);
                    setTitle("Period");
                    return true;
                case R.id.navigation_diary:
                    setTitle("Diary");
                    loadFragment(scheduleFragment);
                    return true;
                case R.id.navigation_messages:
                    setTitle("Messages");
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

        periodFragment = new PeriodFragment();
        periodFragment.start(getApplicationContext());
        messagesFragment = new MessagesFragment();
        messagesFragment.start();
        scheduleFragment = new ScheduleFragment();
        scheduleFragment.start();

        loadFragment(periodFragment);
        //loadFragment(messagesFragment);
        BottomNavigationView bottomnav = findViewById(R.id.bottomnav);
        bottomnav.setOnNavigationItemSelectedListener(mNavigationListener);

        main = findViewById(R.id.main_container);
        chat = findViewById(R.id.chat_container);
    }

    void loadFragment(Fragment fragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
    }

    public void setSupActionBar(android.support.v7.widget.Toolbar toolbar) {
        //setSupportActionBar(toolbar);
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
        log("fragments on MainActivity: " + getSupportFragmentManager().getBackStackEntryCount());
        List<Fragment> a = getSupportFragmentManager().getFragments();
        if(a.get(a.size()-1) instanceof ChatFragment) {
            log("last in stack = ChatFragment");
            set_visible(true);
            getSupportActionBar().setTitle("Messages");
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        if(!(getSupportFragmentManager().getBackStackEntryCount() == 0))
            getSupportFragmentManager().popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "quit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case 1:
                SharedPreferences pref = getSharedPreferences("pref", 0);
                pref.edit().putBoolean("first_time", true).apply();
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
