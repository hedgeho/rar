package ru.gurhouse.sch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class ThanksActivity extends AppCompatActivity {

    private int count = 0;

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
        setContentView(R.layout.activity_thanks);

        /* пасхалка

        ImageView smile = findViewById(R.id.img_smile);
        smile.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
            if(!pref.getBoolean("easteregg1", false)) {
                count++;
                log("clicked " + count + " times");
                if (count == 5) {
                    Toast.makeText(getApplicationContext(), "Поздравляю! Вы открыли пасхалку!", Toast.LENGTH_LONG).show();
                    pref.edit().putBoolean("easteregg1", true).apply();
                }
            } else
                log("пасхалка уже открыта");
        });*/
        Button esc = findViewById(R.id.btn_ok);
        esc.setOnClickListener(v -> finish());
    }
}
