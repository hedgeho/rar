package com.example.sch;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    Button btn_hash;
    EditText et_login, et_password, et_hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab_go);
        btn_hash = findViewById(R.id.btn_hash);
        et_login = findViewById(R.id.et_login);
        et_password = findViewById(R.id.et_password);
        et_hash = findViewById(R.id.et_hash);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onClick(false);
            }
        });
        btn_hash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onClick(true);
            }
        });
    }

    void onClick(boolean hash) {
        final String login = et_login.getText().toString();
        String password;
        if(hash)
            password = et_hash.getText().toString();
        else
            password = et_password.getText().toString();
        try {
            if(!hash) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashb = digest.digest(password.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();
                for (byte aHashb : hashb) {
                    String hex = Integer.toHexString(0xff & aHashb);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                password = hexString.toString();
            }

            final String hex_password = password;

            new Thread() {
                @Override
                public void run() {
                    try {
                        URL url;
                        HttpURLConnection con;
                        StringBuilder result;
                        BufferedReader rd;
                        String line;

                        result = new StringBuilder();
                        url = new URL("https://app.eschool.center/ec-server/esia/useType");
                        con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.setRequestProperty("Cookie", "site_ver=app; _pk_ses.1.81ed=*; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                        con.getInputStream();
                        System.out.println("header: " + con.getHeaderField("Set-Cookie"));

                        String route = con.getHeaderFields().values().toArray()[9].toString().split("route=")[1].split(";")[0];
                        log("route: " + route);
                        String COOKIE = con.getHeaderField("Set-Cookie").split(";")[0];
                        log(COOKIE);
                        rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }
                        rd.close();
                        log("useType " + result);

                        url = new URL("https://app.eschool.center/ec-server/login");
                        con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Cookie", COOKIE + "; route=" + route + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                        con.setDoOutput(true);
                        con.connect();
                        OutputStream os = con.getOutputStream();
                        os.write(("username=" + login + "&password=" + hex_password).getBytes());
                        String COOKIE2 = con.getHeaderField("Set-Cookie").split(";")[0];
                        log("login: " + COOKIE2);
                        //new Scanner(System.in).nextLine();

                        url = new URL("https://app.eschool.center/ec-server/state");
                        con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.setRequestProperty("Cookie", COOKIE2 + "; site_ver=app; route=" + route + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");// "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.13.1554062260.1554051192.");
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
                        int userId = -1;
                        if(obj.has("userId"))
                            userId = obj.getInt("userId");
                        startActivity(new Intent(getApplicationContext(), DiaryActivity.class)
                                .putExtra("cookie", COOKIE2)
                                .putExtra("route", route)
                                .putExtra("userId", userId));
                    } catch (Exception e) {
                        Log.e("mylog", e.toString());
                    }
                }
            }.start();
        } catch (Exception e) {
            Log.e("mylog", e.toString());
        }
    }

    static void log(String msg) {
        Log.v("mylog", msg);
    }
}
