package com.example.sch;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    String COOKIE, ROUTE;
    int USER_ID, PERSON_ID;
    String[] senders;
    String[] topics;
    int[] threadIds;
    int[] users = null;


    public MessagesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void start() {
        COOKIE = TheSingleton.getInstance().getCOOKIE();
        ROUTE = TheSingleton.getInstance().getROUTE();
        USER_ID = TheSingleton.getInstance().getUSER_ID();
        PERSON_ID = TheSingleton.getInstance().getPERSON_ID();

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    URL url = new URL("https://app.eschool.center/ec-server/chat/threads?newOnly=false&row=1&rowsCount=25");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Cookie", COOKIE + "; site_ver=app; route=" + ROUTE + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                    con.connect();
                    StringBuilder result = new StringBuilder();
                    System.out.println(con.getResponseMessage());

                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();

                    JSONArray array = new JSONArray(result.toString());
                    senders = new String[array.length()];
                    topics = new String[array.length()];
                    users = new int[array.length()];
                    threadIds = new int[array.length()];
                    JSONObject obj;
                    String a, b, c;
                    log(array.length() + "");
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        a = obj.getString("senderFio").split(" ")[0];
                        b = obj.getString("senderFio").split(" ")[1];
                        c = obj.getString("senderFio").split(" ")[2];
                        senders[i] = a + " " + b.charAt(0) + ". " + c.charAt(0) + ".";
                        if(obj.getString("subject").equals(" "))
                            if(obj.has("msgPreview"))
                                topics[i] = obj.getString("msgPreview");
                            else
                                topics[i] = "";
                        else
                            topics[i] = obj.getString("subject");
                        users[i] = obj.getInt("addrCnt");
                        if(obj.getInt("senderId") == PERSON_ID) {
                            users[i] = 1;
                        }
                        threadIds[i] = obj.getInt("threadId");
                    }
                    log("first thread: " + senders[0]);
                } catch (Exception e) {
                    loge(e.toString());
                }
            }
        }.start();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        while(true) {
            if(!(users == null))
                break;
        }
        LinearLayout container = view.findViewById(R.id.container1);

        View item;
        TextView tv;
        ImageView img;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < senders.length; i++) {
            item = inflater.inflate(R.layout.thread_item, container, false);
            tv = item.findViewById(R.id.tv_sender);
            tv.setText(senders[i]);
            tv = item.findViewById(R.id.tv_topic);
            tv.setText(topics[i]);
            tv = item.findViewById(R.id.tv_users);
            img = item.findViewById(R.id.img);
            if(users[i] == 0 || users[i] == 2) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.dialog));
                tv.setText("");
            } else if(users[i] == 1) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.monolog));
                tv.setText("");
            } else {
                img.setImageDrawable(getResources().getDrawable(R.drawable.group));
                tv.setText(users[i] + "");
            }
            final int j = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    ChatFragment fragment = new ChatFragment();
                    fragment.threadId = threadIds[j];
                    fragment.threadName = senders[j];
                    transaction.replace(R.id.frame, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
            container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container.addView(inflater.inflate(R.layout.divider, container, false));
        }
    }
}
