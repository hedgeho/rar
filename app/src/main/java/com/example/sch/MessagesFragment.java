package com.example.sch;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewUtils;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    String COOKIE, ROUTE;
    int USER_ID, PERSON_ID;
    String[] senders, topics, f_topics;
    int[] threadIds;
    int[] users = null;
    ArrayList<String> f_senders;
    ArrayList<Integer> f_users, f_threadIds;
    int count = 25;
    boolean first_time = true;


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
                    f_users = new ArrayList<>();
                    f_threadIds = new ArrayList<>();
                    f_senders = new ArrayList<>();
                    for (int i = 0; i < users.length; i++) {
                        f_users.add(users[i]);
                        f_threadIds.add(threadIds[i]);
                        f_senders.add(senders[i]);
                    }
//                    all_senders = senders;
//                    all_topics = topics;
//                    all_threadIds = threadIds;
//                    all_users = users;
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
        View view = inflater.inflate(R.layout.messages, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Messages");

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        // Inflate the layout for this fragment``
        //((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        return view;
    }

    boolean uploading = false;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        while(true) {
            if(!(users == null))
                break;
        }
        log("onViewCreated");
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
            tv.setText(Html.fromHtml(topics[i]));
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
                    loge("j: " + j);
                    loge("senders l: " + senders.length);
                    loge("f_threads l: " + f_threadIds.size());
                    fragment.threadId = f_threadIds.get(j);
                    fragment.threadName = f_senders.get(j);
                    ((MainActivity)getActivity()).set_visible(false);
                    transaction.replace(R.id.chat_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
            container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container.addView(inflater.inflate(R.layout.divider, container, false));
        }
        final ScrollView scroll = view.findViewById(R.id.scroll);
        scroll.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (scroll.getChildAt(0).getBottom()-200
                                <=(scroll.getHeight() + scroll.getScrollY()) && !uploading) {
                            log("bottom!!");
                            if(count == -1) {
                                log("all threads are shown");
                                return;
                            }
                            uploading = true;
                            @SuppressLint("HandlerLeak") final Handler h = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
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
                                                log(f_threadIds.toString());
                                                loge("j: " + j);
                                                loge("count: " + count);
                                                loge("f_threads l: " + f_threadIds.size());
                                                if(count != -1) {
                                                    fragment.threadId = f_threadIds.get(count - (senders.length - j));
                                                    fragment.threadName = f_senders.get(count - (senders.length - j));
                                                } else {
                                                    fragment.threadId = f_threadIds.get(f_threadIds.size() - (senders.length - j));
                                                    fragment.threadName = f_senders.get(f_threadIds.size() - (senders.length - j));
                                                }
                                                ((MainActivity)getActivity()).set_visible(false);
                                                transaction.replace(R.id.chat_container, fragment);
                                                transaction.addToBackStack(null);
                                                transaction.commit();
                                            }
                                        });
                                        container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        container.addView(inflater.inflate(R.layout.divider, container, false));
                                    }
                                    if(count!=-1)
                                        count += senders.length;
                                    uploading = false;
                                }
                            };
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        URL url = new URL("https://app.eschool.center/ec-server/chat/threads?newOnly=false&row=" + (count+1) + "&rowsCount=25");
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

                                        log("adding" + users.length);
                                        JSONArray array = new JSONArray(result.toString());
                                        senders = new String[array.length()];
                                        topics = new String[array.length()];
                                        users = new int[array.length()];
                                        threadIds = new int[array.length()];
                                        JSONObject obj;
                                        String a, b, c;
                                        log(array.length() + "");
                                        if(array.length() < 25)
                                            count = -1;
                                        for (int i = 0; i < array.length(); i++) {
                                            obj = array.getJSONObject(i);
                                            a = obj.getString("senderFio").split(" ")[0];
                                            b = obj.getString("senderFio").split(" ")[1];
                                            if(obj.getString("senderFio").split(" ").length <= 2) {
                                                loge("fio strange");
                                                loge(obj.toString());
                                                c = "a";
                                            } else
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
                                        for (int i = 0; i < users.length; i++) {
                                            f_users.add(users[i]);
                                            f_threadIds.add(threadIds[i]);
                                            f_senders.add(senders[i]);
                                        }
                                        log("first thread: " + senders[0]);
                                        h.sendEmptyMessage(0);
                                    } catch (Exception e) {
                                        loge(e.toString());
                                    }
                                }
                            }.start();
                        }
                    }
                });
    }
}
