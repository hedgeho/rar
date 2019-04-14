package com.example.sch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import static com.example.sch.LoginActivity.log;

public class MainActivity extends AppCompatActivity {

    PeriodFragment periodFragment;
    MessagesFragment messagesFragment;
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
    }

    void loadFragment(Fragment fragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
    }

    public void setSupActionBar(android.support.v7.widget.Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onBackPressed() {
        log("fragments on MainActivity: " + getSupportFragmentManager().getBackStackEntryCount());
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
                // todo closing fragment
                onBackPressed();
                break;
            case 1:
                SharedPreferences pref = getSharedPreferences("pref", 0);
                //if (pref.get)
        }
        return super.onOptionsItemSelected(item);
    }
}
