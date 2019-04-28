package com.example.sch;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewUtils;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.sch.LoginActivity.connect;
import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class MessagesFragment extends Fragment {

    String COOKIE, ROUTE;
    int USER_ID, PERSON_ID;
    String[] senders, topics;
    int[] threadIds;
    int[] users = null;
    ArrayList<String> f_senders, f_topics, s_senders = null, s_messages;
    ArrayList<Integer> f_users = null, f_threadIds, s_threadIds;
    String s_query = "";
    int count = 25, s_count = 0;
    boolean first_time = true;
    LinearLayout container;

    public MessagesFragment() {}

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
                    f_topics = new ArrayList<>();
                    for (int i = 0; i < users.length; i++) {
                        f_users.add(users[i]);
                        f_threadIds.add(threadIds[i]);
                        f_senders.add(senders[i]);
                        f_topics.add(topics[i]);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup contain,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.messages, contain, false);
        container = view.findViewById(R.id.container1);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        //toolbar.setTitle("Messages");

        log("ssss");
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        // Inflate the layout for this fragment``
        //((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflate) {
        inflate.inflate(R.menu.toolbar_nav, menu);
        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            String query;
            @SuppressLint("HandlerLeak")
            final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                container.removeAllViews();
                LayoutInflater inflater = getLayoutInflater();
                View item;
                TextView tv;
                ImageView img;
                String message;
                int index;
                Spannable spannable;
                for (int i = 0; i < s_senders.size(); i++) {
                    item = inflater.inflate(R.layout.thread_item, container, false);
                    tv = item.findViewById(R.id.tv_sender);
                    tv.setText(s_senders.get(i));
                    tv = item.findViewById(R.id.tv_topic);
                    Spanned mess = Html.fromHtml(s_messages.get(i));

                    String s;
                    index = mess.toString().toLowerCase().indexOf(query.toLowerCase());
                    log("index: " + index);
                    int start, end;
                    if(index > 30)
                        start = index-30;
                    else
                        start = 0;
                    if(mess.toString().length() > index + query.length() + 30)
                        end = index + query.length() + 30;
                    else
                        end = mess.toString().length()-1;
                        s = (start == 0?"":"...") + mess.subSequence(start, end).toString() + (end == mess.toString().length()-1?"":"...");
                        spannable = new SpannableString(s);
                        spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), s.toLowerCase().indexOf(query.toLowerCase()), s.toLowerCase().indexOf(query.toLowerCase()) + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv.setText(spannable);

                    tv = item.findViewById(R.id.tv_users);
                    tv.setText("");
//                            img = item.findViewById(R.id.img);
                    final int j = i;
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            ChatFragment fragment = new ChatFragment();
                            fragment.threadId = s_threadIds.get(j);
                            fragment.threadName = s_senders.get(j);
                            ((MainActivity)getActivity()).set_visible(false);
                            transaction.replace(R.id.chat_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    });
                    container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    container.addView(inflater.inflate(R.layout.divider, container, false));
                }
                log("shown count: " + s_senders.size());
                if(s_count != -1)
                    s_count = s_senders.size();

                getView().findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.scroll).setVisibility(View.VISIBLE);
            }
        };
            @Override
            public boolean onQueryTextSubmit(final String query) {
                log( "query: " + query);
                /*if(!searchView.isIconified()) {
                    Log.e("mylog", "dd");
                    //searchView.setIconified(true);
                }*/
                s_query = query;
                this.query = query;
                getView().findViewById(R.id.loading_bar).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.scroll).setVisibility(View.INVISIBLE);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            s_senders = new ArrayList<>();
                            s_messages = new ArrayList<>();
                            s_threadIds = new ArrayList<>();

                            String result = connect("https://app.eschool.center/ec-server/chat/searchThreads?rowStart=1&rowsCount=25&text=" + query, null);
                            log("search result: " + result);
                            JSONArray array = new JSONArray(result), a, b;
                            JSONObject obj, c;
                            for (int i = 0; i < array.length(); i++) {
                                obj = array.getJSONObject(i);
                                a = obj.getJSONArray("filterNumbers");
                                for (int j = 0; j < a.length(); j++) {
                                    result = connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&rowStart=0&rowsCount=2" +
                                            "&threadId=" + obj.getInt("threadId") +"&msgStart=" + (/*obj.getInt("msgNum")*/a.optInt(j)+1), null);

                                    b = new JSONArray(result);
                                    c = b.getJSONObject(0);
                                    s_senders.add(c.getString("senderFio"));
                                    s_messages.add(c.getString("msg"));
                                    s_threadIds.add(c.getInt("threadId"));
                                }
                            }
                            if(array.length() < 25)
                                s_count = -1;
                            h.sendEmptyMessage(0);
                        } catch (Exception e) {
                            loge(e.toString());
                        }
                    }
                }.start();
                //myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                log("query changed: " + s);
                if(s.trim().equals("")) {
//                    h.sendEmptyMessage(2);
                    onViewCreated(getView(), null);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflate);
    }

    void newMessage(String text, long time, String sender_id) {
        log("new message in MessageFragment");
    }

    boolean uploading = false;
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        log("onViewCreated");
        while(true) {
            if(!(users == null))
                break;
        }

        LinearLayout container1 = view.findViewById(R.id.container1);
        container1.removeAllViews();

        View item;
        TextView tv;
        ImageView img;
        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < f_senders.size(); i++) {
            item = inflater.inflate(R.layout.thread_item, container1, false);
            tv = item.findViewById(R.id.tv_sender);
            tv.setText(f_senders.get(i));
            tv = item.findViewById(R.id.tv_topic);
            tv.setText(Html.fromHtml(f_topics.get(i)));
            tv = item.findViewById(R.id.tv_users);
            img = item.findViewById(R.id.img);
            if(f_users.get(i) == 0 || f_users.get(i) == 2) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.dialog));
                tv.setText("");
            } else if(f_users.get(i) == 1) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.monolog));
                tv.setText("");
            } else {
                img.setImageDrawable(getResources().getDrawable(R.drawable.group));
                tv.setText(f_users.get(i) + "");
            }
            final int j = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    ChatFragment fragment = new ChatFragment();
                    fragment.threadId = f_threadIds.get(j);
                    fragment.threadName = f_senders.get(j);
                    ((MainActivity)getActivity()).set_visible(false);
                    transaction.replace(R.id.chat_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
            container1.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container1.addView(inflater.inflate(R.layout.divider, container1, false));
        }
        final ScrollView scroll = view.findViewById(R.id.scroll);

        if(first_time) {
            scroll.getViewTreeObserver()
                    .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged() {
                            if (scroll.getChildAt(0).getBottom() - 200
                                    <= (scroll.getHeight() + scroll.getScrollY()) && !uploading) {
                                log("bottom");
                                if (count == -1) {
                                    log("all threads are shown");
                                    return;
                                }
                                uploading = true;
                                @SuppressLint("HandlerLeak") final Handler h = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {

                                        if(msg.what == 0) {
                                            LinearLayout container = view.findViewById(R.id.container1);

                                            View item;
                                            TextView tv;
                                            ImageView img;
                                            LayoutInflater inflater = getLayoutInflater();
                                            final int l = senders.length;
                                            final int f_count = count;
                                            for (int i = 0; i < l; i++) {
                                                item = inflater.inflate(R.layout.thread_item, container, false);
                                                tv = item.findViewById(R.id.tv_sender);
                                                tv.setText(senders[i]);
                                                tv = item.findViewById(R.id.tv_topic);
                                                tv.setText(topics[i]);
                                                tv = item.findViewById(R.id.tv_users);
                                                img = item.findViewById(R.id.img);
                                                if (users[i] == 0 || users[i] == 2) {
                                                    img.setImageDrawable(getResources().getDrawable(R.drawable.dialog));
                                                    tv.setText("");
                                                } else if (users[i] == 1) {
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
                                                        log("count: " + f_count);
                                                        log("senders l: " + l);
                                                        log("j: " + j);
                                                        log("size: " + f_threadIds.size());
                                                        if (f_count != -1) {
                                                            fragment.threadId = f_threadIds.get(f_count + 25 - (l - j));
                                                            fragment.threadName = f_senders.get(f_count + 25 - (l - j));
                                                        } else {
                                                            fragment.threadId = f_threadIds.get(f_threadIds.size() - (l - j));
                                                            fragment.threadName = f_senders.get(f_threadIds.size() - (l - j));
                                                        }
                                                        ((MainActivity) getActivity()).set_visible(false);
                                                        transaction.replace(R.id.chat_container, fragment);
                                                        transaction.addToBackStack(null);
                                                        transaction.commit();
                                                    }
                                                });
                                                container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                container.addView(inflater.inflate(R.layout.divider, container, false));
                                            }
                                            if (count != -1)
                                                count += senders.length;
                                            uploading = false;
                                        }
                                        else {
                                            LayoutInflater inflater = getLayoutInflater();
                                            View item;
                                            TextView tv;
                                            ImageView img;
                                            for (int i = s_count; i < s_senders.size(); i++) {
                                                item = inflater.inflate(R.layout.thread_item, container, false);
                                                tv = item.findViewById(R.id.tv_sender);
                                                tv.setText(s_senders.get(i));
                                                tv = item.findViewById(R.id.tv_topic);
                                                tv.setText(Html.fromHtml(s_messages.get(i)));
                                                tv = item.findViewById(R.id.tv_users);
                                                tv.setText("");
//                            img = item.findViewById(R.id.img);
                                                final int j = i;
                                                item.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                        ChatFragment fragment = new ChatFragment();
                                                        fragment.threadId = s_threadIds.get(j);
                                                        fragment.threadName = s_senders.get(j);
                                                        ((MainActivity)getActivity()).set_visible(false);
                                                        transaction.replace(R.id.chat_container, fragment);
                                                        transaction.addToBackStack(null);
                                                        transaction.commit();
                                                    }
                                                });
                                                container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                container.addView(inflater.inflate(R.layout.divider, container, false));
                                            }
                                            if(msg.arg1==0) {
                                                s_count = s_senders.size();
                                            } else
                                                s_count = -1;
                                        }
                                    }
                                };
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (s_senders == null) {
                                                if(count == -1)
                                                    return;
                                                JSONArray array = new JSONArray(connect("https://app.eschool.center/ec-server/chat/threads?newOnly=false&row=" + (count + 1) + "&rowsCount=25", null));
                                                senders = new String[array.length()];
                                                topics = new String[array.length()];
                                                users = new int[array.length()];
                                                threadIds = new int[array.length()];
                                                JSONObject obj;
                                                String a, b, c;
                                                log(array.length() + "");
                                                if (array.length() < 25)
                                                    count = -1;
                                                for (int i = 0; i < array.length(); i++) {
                                                    obj = array.getJSONObject(i);
                                                    a = obj.getString("senderFio").split(" ")[0];
                                                    b = obj.getString("senderFio").split(" ")[1];
                                                    if (obj.getString("senderFio").split(" ").length <= 2) {
                                                        loge("fio strange");
                                                        loge(obj.toString());
                                                        c = "a";
                                                    } else
                                                        c = obj.getString("senderFio").split(" ")[2];
                                                    senders[i] = a + " " + b.charAt(0) + ". " + c.charAt(0) + ".";
                                                    if (obj.getString("subject").equals(" "))
                                                        if (obj.has("msgPreview"))
                                                            topics[i] = obj.getString("msgPreview");
                                                        else
                                                            topics[i] = "";
                                                    else
                                                        topics[i] = obj.getString("subject");
                                                    users[i] = obj.getInt("addrCnt");
                                                    if (obj.getInt("senderId") == PERSON_ID) {
                                                        users[i] = 1;
                                                    }
                                                    threadIds[i] = obj.getInt("threadId");
                                                }
                                                for (int i = 0; i < users.length; i++) {
                                                    f_users.add(users[i]);
                                                    f_threadIds.add(threadIds[i]);
                                                    f_senders.add(senders[i]);
                                                    f_topics.add(topics[i]);
                                                }
                                                log("first thread: " + senders[0]);
                                                h.sendEmptyMessage(0);
                                            } else if(s_count != -1) {

                                                String result = connect("https://app.eschool.center/ec-server/chat/searchThreads?rowStart=" + s_count + "&rowsCount=25&text=" + s_query, null);
                                                log("search result: " + result);
                                                JSONArray array = new JSONArray(result), a, b;
                                                JSONObject obj, c;
                                                for (int i = 0; i < array.length(); i++) {
                                                    obj = array.getJSONObject(i);
                                                    a = obj.getJSONArray("filterNumbers");
                                                    for (int j = 0; j < a.length(); j++) {
                                                        result = connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&rowStart=0&rowsCount=1" +
                                                                "&threadId=" + obj.getInt("threadId") +"&msgStart=" + (/*obj.getInt("msgNum")*/a.optInt(j)+1), null);

                                                        b = new JSONArray(result);
                                                        c = b.getJSONObject(0);
                                                        s_senders.add(c.getString("senderFio"));
                                                        s_messages.add(c.getString("msg"));
                                                        s_threadIds.add(c.getInt("threadId"));
                                                    }
                                                }
                                                h.sendMessage(h.obtainMessage(1, array.length()==25?0:1, 0));
                                            }
                                        } catch (Exception e) {
                                            loge(e.toString());
                                        }
                                    }
                                }.start();
                            }
                        }
                    });
            first_time = false;
        }
    }


}
