package ru.gurhouse.sch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.loge;
import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.SettingsActivity.getColorFromAttribute;

public class MessagesFragment extends Fragment {

    private static final Set<String> GROUP_NAMES = ImmutableSet.of("Директор", "Завуч", "Педагог дополнительного образования", "Педагог-психолог", "Преподаватель");
    private int PERSON_ID;
    private ArrayList<MsgThread> threads_list;
    private ArrayList<String> s_senders = null, s_messages, s_time, s_topics;
    private ArrayList<Integer> s_threadIds, s_msgIds, f_types;
    private ArrayList<Boolean> s_group;
    private String s_query = "";
    private int count = 25, s_count = 0;
    private boolean first_time = true;
    private LinearLayout container;
    private ViewTreeObserver.OnScrollChangedListener scrollListener;
    private MenuItem searchView = null;

    boolean fromNotification = false;
    int notifThreadId, notifCount;
    String notifSenderFio;

    private View savedView = null, view;
    private int search_mode = -1;
    private Person[] olist;
    Activity context;
    private boolean READY = false, shown = false;
    private SwipeRefreshLayout refreshL;
    private boolean refreshing = false;

/*  for future
  private ActionMode actionMode;
    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuItem item = menu.add(0, 0, 0, "Выйти");
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            item.setIcon(R.drawable.logout);
//            item = menu.add(0, 1, 0, "Отключить уведомления");
//            item.setIcon()
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case 0:
                    new Thread(() -> {
                        try {
                            connect("https://app.eschool.center/ec-server/chat/leave?threadId=" + threadId, null);
                        } catch (LoginActivity.NoInternetException e) {
                            //Toast.makeText(getContext(), "Нет интернета", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {loge(e);}
                    }).start();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            actionMode = null;
        }
    };*/

    public MessagesFragment() {}

    public void start(Runnable r) {
        PERSON_ID = TheSingleton.getInstance().getPERSON_ID();

        new Thread(() -> {
                try {
                    download(null);
                    int count = 0;
                    for (int i = 0; i < threads_list.size(); i++) {
                        count += threads_list.get(i).newCount;
                    }
                    MainActivity.newCount = count;
                    getContext().runOnUiThread(r);
                    JSONArray userList = new JSONArray(connect("https://app.eschool.center/ec-server/usr/getUserListSearch", null, getContext()));
                    JSONObject obj;
                    String fio, info;
//                    olist = new Person[array.length()];
                    // counting parents
                    int parents_l = 0;
                    for (int i = 0; i < userList.length(); i++) {
                        obj = userList.getJSONObject(i);
                        if(obj.has("isParent") && obj.getInt("isParent") == 1)
                            parents_l++;
                    }

                    JSONArray tree = new JSONArray(connect("https://app.eschool.center/ec-server/groups/tree?bEmployees=true", null,
                            getContext()));
                    JSONArray array = new JSONArray();
                    for (int i = 0; i < tree.length(); i++) {
                        if(tree.getJSONObject(i).getInt("groupTypeId") == 1) {
                            array = tree.getJSONObject(i).getJSONArray("children");
                        }
                    }
                    int classes_count = 0;
                    for (int i = 0; i < array.length(); i++) {
                        classes_count += array.getJSONObject(i).getJSONArray("children").length();
                    }
                    JSONArray[] classes = new JSONArray[classes_count];
                    String[] class_names = new String[classes_count];
                    int ind = 0;
                    JSONArray children;
                    for (int i = 0; i < array.length(); i++) {
                        children = array.getJSONObject(i).getJSONArray("children");
                        for (int j = 0; j < children.length(); j++) {
                            class_names[ind] = children.getJSONObject(j).getString("groupTypeName");
                            classes[ind++] = new JSONArray(connect(
                                    "https://app.eschool.center/ec-server/groups/groupPersons?groupId=" +
                                            children.getJSONObject(j)
                                                    .getJSONArray("groups").getJSONObject(0).getInt("groupId"),
                                    null, getContext()));
                        }
                    }
                    int people_count = 0;
                    // counting students
                    for (int i = 0; i < classes_count; i++) {
                        people_count += classes[i].length();
                    }
                    // counting teachers
                    array = null;
                    for (int i = 0; i < tree.length(); i++) {
                        if(tree.getJSONObject(i).getInt("groupTypeId") == -10000) {
                            array = tree.getJSONObject(i).getJSONArray("groups");
                        }
                    }
                    if(array != null) {
                        HashSet<Integer> prsIds = new HashSet<>();
                        String groupName;
                        JSONArray a;
                        for (int i = 0; i < array.length(); i++) {
                            groupName = array.getJSONObject(i).getString("groupName");
                            if(GROUP_NAMES.contains(groupName)) {
                                a = array.getJSONObject(i).getJSONArray("users");
                                for (int j = 0; j < a.length(); j++) {
                                    prsIds.add(a.getJSONObject(j).getInt("prsId"));
                                }
                            }
                        }
                        people_count+=prsIds.size();
                    }

                    olist = new Person[parents_l + people_count];

                    ind = 0;
                    Person person;
                    HashSet<Integer> prsIds = new HashSet<>();
                    if(array != null) {
                        String groupName;
                        JSONArray a;
                        for (int i = 0; i < array.length(); i++) {
                            groupName = array.getJSONObject(i).getString("groupName");
                            if (GROUP_NAMES.contains(groupName)) {
                                a = array.getJSONObject(i).getJSONArray("users");
                                for (int j = 0; j < a.length(); j++) {
                                    if(prsIds.add(a.getJSONObject(j).getInt("prsId"))) {
                                        obj = a.getJSONObject(j);
                                        person = new Person();
                                        person.prsId = obj.getInt("prsId");
                                        person.fio = obj.getString("fio");
                                        person.info = groupName;
                                        olist[ind++] = person;
                                    }
                                }
                            }
                        }

                    }
                    for (int i = 0; i < userList.length(); i++) {
                        obj = userList.getJSONObject(i);
                        if(!obj.has("isParent") || obj.getInt("isParent") != 1 || !prsIds.add(obj.getInt("prsId")))
                            continue;
                        person = new Person();
                        fio = obj.getString("fio");
                        if (fio == null) {
                            person.info = "";
                            olist[ind++] = person;
                            continue;
                        }
                        person.prsId = obj.getInt("prsId");
                        person.fio = fio;
                        person.info = "Родитель";
                        olist[ind++] = person;
                    }
                    for (int i = 0; i < classes_count; i++) {
                        for (int j = 0; j < classes[i].length(); j++) {
                            obj = classes[i].getJSONObject(j);
                            person = new Person();
                            person.fio = obj.getString("fio");
                            person.prsId = obj.getInt("prsId");
                            person.info = "Ученик (" + class_names[i] + ")";
                            olist[ind++] = person;
                        }
                    }

                    log("olist l: " + olist.length);
                    READY = true;
                    getContext().runOnUiThread(r);
                } catch (LoginActivity.NoInternetException e) {
                    new Thread (() -> {
                        while(true) {
                            if(getContext() != null) {
                                getContext().runOnUiThread(() -> {
                                    if(getContext() != null) {
                                        TextView tv = getContext().findViewById(R.id.tv_error);
                                        if (tv != null) {
                                            tv.setText("Нет подключения к Интернету");
                                            tv.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    loge(e);
                }
        }).start();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null)
            context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup contain,
                             Bundle savedInstanceState) {
        if(getActivity() != null)
            context = getActivity();
        if(savedView != null)
            return savedView;
        view = inflater.inflate(R.layout.messages, contain, false);
        container = view.findViewById(R.id.container);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflate) {
        inflate.inflate(R.menu.toolbar_nav, menu);
        final MenuItem myActionMenuItem = this.searchView = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            String query;
            Person[] result;
            String error;
            @SuppressLint("HandlerLeak")
            final Handler h = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    container.removeAllViews();

                    if(msg.what == 123 && getView() != null) {
                        getView().findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.scroll).setVisibility(View.VISIBLE);
                        TextView tv = getView().findViewById(R.id.tv_error);
                        if(tv != null) {
                            tv.setText(error);
                            tv.setVisibility(View.VISIBLE);
                        }
                        return;
                    }

                    LayoutInflater inflater = getLayoutInflater();
                    View item;
                    TextView tv;
                    ImageView img;
                    int index;
                    Spannable spannable;
                    Spanned mess;
                    String s;
                    if(search_mode == 1) {
                        log("result: " + result.length);
                        s_count = -1;
                        count = -1;
                        Person person;
                        for (int i = 0; i < Math.min(result.length, 100); i++) {
                            person = result[i];
                            item = inflater.inflate(R.layout.person_item, container, false);
                            tv = item.findViewById(R.id.tv_fio);
//                            index = person.fio.toLowerCase().indexOf(query.toLowerCase());
//                            int start, end;
                            if(person.fio.toLowerCase().contains(query.toLowerCase())) {
//                                if (index > 30)
//                                    start = index - 30;
//                                else
//                                    start = 0;
//                                if (person.fio.length() > index + query.length() + 30)
//                                    end = index + query.length() + 30;
//                                else
//                                    end = person.fio.length() - 1;
//
//                                s = (start == 0 ? "" : "...") + person.fio.subSequence(start, end + 1).toString() + (end == person.fio.length() - 1 ? "" : "...");
                                s = person.fio;
                                spannable = new SpannableString(s);
                                s = s.toLowerCase();
                                int ind = s.indexOf(query.toLowerCase());
                                spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), ind, ind + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                tv.setText(spannable);
                                tv = item.findViewById(R.id.tv_info);
                                tv.setText(person.info);
                            } else if(person.info.toLowerCase().contains(query.toLowerCase())) {
                                tv.setText(person.fio);
                                s = person.info;
                                spannable = new SpannableString(s);
                                s = s.toLowerCase();
                                int ind = s.indexOf(query.toLowerCase());
                                if(ind == -1) {
                                    loge(s + ",," + query);
                                }
                                spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)),
                                        ind, ind + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                tv = item.findViewById(R.id.tv_info);
                                tv.setText(spannable);
                            } else {
                                loge("bruh wtf " + person.fio + ", " + person.info + ", " + query);
                            }
                            //tv.setText(person.fio);
                            final int prsId = person.prsId;
                            final String fio = person.fio;
                            item.setOnClickListener(v -> new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        final JSONObject threads = new JSONObject(connect("https://app.eschool.center/ec-server/chat/privateThreads", null, getContext()));
                                        if (threads.has(prsId + "")) {
                                            getContext().runOnUiThread(() -> {
                                                try {
                                                    MsgThread thread = new MsgThread();
                                                    thread.threadId = threads.getInt(prsId + "");
                                                    thread.sender = fio;
                                                    thread.topic = "";
                                                    thread.type = 2;
                                                    thread.user = 0;
                                                    loadChat(thread, -1);
                                                } catch (JSONException e) {
                                                    loge(e);
                                                }
                                            });
                                        } else {
                                            log("hasn't prsId " + prsId);
                                            final int threadId = Integer.parseInt(connect("https://app.eschool.center/ec-server/chat/saveThread",
                                                    "{\"threadId\":null,\"senderId\":null,\"imageId\":null,\"subject\":null,\"isAllowReplay\":2,\"isGroup\":false,\"interlocutor\":" + prsId + "}",
                                                    true, getContext()));
                                            final MsgThread thread = new MsgThread();
                                            thread.threadId = threads.getInt(prsId + "");
                                            thread.sender = fio;
                                            thread.topic = "";
                                            thread.type = 2;
                                            thread.user = 0;
                                            getActivity().runOnUiThread(() -> loadChat(thread, -1));
                                        }
                                    } catch (LoginActivity.NoInternetException e) {
                                        getContext().runOnUiThread(() ->
                                                Toast.makeText(context, "Нет интернета", Toast.LENGTH_SHORT).show());
                                    } catch (Exception e) {loge(e);}
                                }
                            }.start());

                            container.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            container.addView(inflater.inflate(R.layout.divider, container, false));
                        }
                        getView().findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.scroll).setVisibility(View.VISIBLE);
                        return;
                    }
                    for (int i = 0; i < s_senders.size(); i++) {
                        item = inflater.inflate(R.layout.thread_item, container, false);
                        tv = item.findViewById(R.id.tv_sender);
                        tv.setText(s_senders.get(i));
                        tv = item.findViewById(R.id.tv_topic);
                        mess = Html.fromHtml(s_messages.get(i).replace("\n","<br>"));

                        ImageView muted = item.findViewById(R.id.muted);
                        if(context.getSharedPreferences("pref", 0).getString("muted", "[]")
                                .contains("" + s_threadIds.get(i)))
                            muted.setVisibility(View.VISIBLE);
                        else
                            muted.setVisibility(View.INVISIBLE);

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
                        s = (start == 0?"":"...") + mess.subSequence(start, end+1).toString() + (end == mess.toString().length()-1?"":"...");
                        spannable = new SpannableString(s);
                        if(s.toLowerCase().contains(query.toLowerCase()))
                            spannable.setSpan(new BackgroundColorSpan(getColorFromAttribute(R.attr.second_font, getContext().getTheme())),
                                    s.toLowerCase().indexOf(query.toLowerCase()), s.toLowerCase().indexOf(query.toLowerCase()) + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv.setText(spannable);

                        tv = item.findViewById(R.id.tv_users);
                        tv.setText("");
                        tv = item.findViewById(R.id.tv_time);
                        tv.setText(s_time.get(i));
                        img = item.findViewById(R.id.img);
                        img.setVisibility(View.GONE);
                        final int j = i;
                        MsgThread thread = new MsgThread();
                        thread.threadId = s_threadIds.get(i);
                        thread.sender = s_senders.get(i);
                        thread.topic = s_topics.get(i);
//                        thread.type = s_ty.get(i);;
                        thread.user = 0;
                        loadChat(thread, -1);
//                        item.setOnClickListener(v ->
//                                loadChat(s_threadIds.get(j), s_senders.get(j), s_topics.get(j), s_msgIds.get(j), -1, s_group.get(j)));
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
            public boolean onQueryTextSubmit(String q) {
                if(q.replaceAll(" ", "").length() < 3)
                    return false;
                if(q.charAt(q.length()-1) == ' ')
                    q = q.substring(0, q.length()-1);
                if(q.charAt(0) == ' ')
                    q = q.substring(1, q.length()-1);
                final String query = q;
                getView().findViewById(R.id.tv_error).setVisibility(View.INVISIBLE);
                log( "query: '" + query + "'");

                s_query = query;
                this.query = query;
                getView().findViewById(R.id.loading_bar).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.scroll).setVisibility(View.INVISIBLE);

                log("search mode " + search_mode);
                if(search_mode == 0) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                s_senders = new ArrayList<>();
                                s_messages = new ArrayList<>();
                                s_threadIds = new ArrayList<>();
                                s_time = new ArrayList<>();
                                s_msgIds = new ArrayList<>();
                                s_group = new ArrayList<>();
                                s_topics = new ArrayList<>();
                                f_types = new ArrayList<>();

                                String result = connect("https://app.eschool.center/ec-server/chat/searchThreads?rowStart=1&rowsCount=15&text=" + query, null, getContext());
                                log("search result: " + result);
                                JSONArray array = new JSONArray(result), a, b;
                                if (array.length() == 0) {
                                    error = "Нет сообщений, удовлетворяющих условиям поиска";
                                    h.sendEmptyMessage(123);
                                    return;
                                }
                                JSONObject obj, c;
                                String A, B, C;
                                for (int i = 0; i < array.length(); i++) {
                                    obj = array.getJSONObject(i);
                                    if (obj.has("filterNumbers")) {
                                        a = obj.getJSONArray("filterNumbers");
                                        for (int j = 0; j < a.length(); j++) {
                                            result = connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&rowStart=0&rowsCount=1" +
                                                    "&threadId=" + obj.getInt("threadId") + "&msgStart=" + (a.optInt(j) + 1), null, getContext());
                                            log("result: " + result);
                                            b = new JSONArray(result);
                                            c = b.getJSONObject(0);
                                            obj = array.getJSONObject(i);
                                            A = obj.getString("senderFio").split(" ")[0];
                                            B = obj.getString("senderFio").split(" ")[1];
                                            if (obj.getString("senderFio").split(" ").length <= 2) {
                                                loge("fio strange:");
                                                loge(obj.toString());
                                                C = "";
                                            } else
                                                C = obj.getString("senderFio").split(" ")[2];
                                            if(!A.equals("Служба") || !B.equals("поддержки")) {
                                                if (C.length() > 0)
                                                    s_senders.add(A + " " + B.charAt(0) + ". " + C.charAt(0) + ".");
                                                else
                                                    s_senders.add(A + " " + B.charAt(0) + ".");
                                            } else {
                                                s_senders.add("Служба подержки");
                                            }

                                            s_messages.add(c.getString("msg"));
                                            s_threadIds.add(c.getInt("threadId"));
                                            if(c.has("isAllowReplay"))
                                                f_types.add(c.getInt("isAllowReplay"));
                                            s_msgIds.add(a.optInt(j));
                                            if(c.has("subject"))
                                                s_topics.add(c.getString("subject"));
                                            else
                                                s_topics.add("");
                                            Date date = new Date(c.getLong("createDate"));
                                            s_time.add(String.format(Locale.UK, "%02d:%02d", date.getHours(), date.getMinutes()));

                                            if (c.has("addrCnt"))
                                                s_group.add(c.getInt("addrCnt") > 2);
                                            else
                                                s_group.add(false);
                                        }
                                    }
                                }
                                if (array.length() < 15)
                                    s_count = -1;
                                h.sendEmptyMessage(0);
                            } catch (LoginActivity.NoInternetException e) {
                                error = "Нет подключения к Интернету";
                            } catch (Exception e) {
                                loge(e);
                            }
                        }
                    }.start();
                } else if(search_mode == 1) {
                    s_count = -1;
                    new Thread() {
                        @Override
                        public void run() {
                            if(olist == null)
                                return;
                            result = new Person[0];
                            LinkedList<Person> list = new LinkedList<>();
                            for (Person person: olist) {
                                if(person == null)
                                    continue;
                                if(person.fio.toLowerCase().contains(query.toLowerCase()) ||
                                        person.info.toLowerCase().contains(query.toLowerCase()))
                                    list.add(person);
                            }
                            result = list.toArray(new Person[0]);
                            if (result.length == 0) {
                                error = "Нет адресатов, удовлетворяющих условиям поиска";
                                h.sendEmptyMessage(123);
                            } else
                                h.sendEmptyMessage(0);
                        }
                    }.start();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                log("query changed: " + s);
                String q = s;
                if(s.replaceAll(" ", "").equals(""))
                    return false;
                if(q.charAt(q.length()-1) == ' ')
                    q = q.substring(0, q.length()-1);
                if(q.charAt(0) == ' ')
                if(q.charAt(0) == ' ')
                    q = q.substring(1, q.length()-1);
                query = q;
                if (search_mode == 1) {
                    s_count = -1;
                    new Thread() {
                        @Override
                        public void run() {
                            result = new Person[0];
                            LinkedList<Person> list = new LinkedList<>();
                            for (Person person: olist) {
                                if(person == null)
                                    continue;
                                if(person.fio.toLowerCase().contains(query.toLowerCase()) ||
                                        person.info.toLowerCase().contains(query.toLowerCase()))
                                    list.add(person);
                            }
                            result = list.toArray(new Person[0]);
                            if (result.length == 0) {
                                error = "Нет адресатов, удовлетворяющих условиям поиска";
                                h.sendEmptyMessage(123);
                            } else {
                                getContext().runOnUiThread(() ->
                                        getView().findViewById(R.id.tv_error).setVisibility(View.INVISIBLE));
                                h.sendEmptyMessage(0);
                            }
                        }
                    }.start();
                }
                return false;
            }
        });
        myActionMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return olist != null;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                log("collapsing");
                Bundle bundle = new Bundle();
                bundle.putBoolean("collapsing", true);
                count = 25;
                s_count = 0;
                show();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                break;
            case R.id.new_dialog:
                search_mode = 1;
                searchView.expandActionView();
                view.findViewById(R.id.knock_l).setVisibility(View.GONE);
                break;
            case R.id.action_search:
                search_mode = 0;
                break;
        }
        log("options item selected");
        return super.onOptionsItemSelected(item);
    }

    private boolean uploading = false;

    @Override
    public void onResume() {
        Toolbar toolbar = getContext().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        if(!shown)
            show();
        super.onResume();
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        if(READY)
            if(getContext().getSharedPreferences("pref", 0).getBoolean("show_chat", false))
                view.findViewById(R.id.knock_l).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.knock_l).setVisibility(View.GONE);
        if(savedView != null)
            if(savedInstanceState == null)
                return;
            else if(!savedInstanceState.getBoolean("collapsing"))
                return;
        log("onViewCreated");
        if(fromNotification) {
            log("fromNotif");
            MsgThread thread = new MsgThread();
            thread.threadId = notifThreadId;
            thread.sender = notifSenderFio;
            thread.topic = "";
            thread.type = (notifCount > 2? 2:0);
            thread.user = notifCount;
            loadChat(thread, -1);
        }
        if(READY && !shown)
            show();
        if(view == null || threads_list == null) {
            loge("null in MessagesFragment");
        }

    }

    View makeThreadItem(MsgThread thread, ViewGroup parent) {
        View item = getLayoutInflater().inflate(R.layout.thread_item, parent, false);
        TextView tv = item.findViewById(R.id.tv_sender);
        tv.setText(thread.sender);
        tv = item.findViewById(R.id.tv_topic);
        tv.setText(Html.fromHtml(thread.topic.replace("\n","<br>")));
        tv = item.findViewById(R.id.tv_users);
        ImageView img = item.findViewById(R.id.img);

        ImageView muted = item.findViewById(R.id.muted);
        if(context.getSharedPreferences("pref", 0).getString("muted", "[]")
                .contains("" + thread.threadId))
            muted.setVisibility(View.VISIBLE);
        else
            muted.setVisibility(View.INVISIBLE);

        Drawable wrappedDrawable, unwrappedDrawable;
        if (thread.user == 0 || thread.user == 2) {
            unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.dialog);
            wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.icons, getContext().getTheme()));
            img.setImageDrawable(wrappedDrawable);
            tv.setText("");
        } else if (thread.user == 1) {
            unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.monolog);
            wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.icons, getContext().getTheme()));
            img.setImageDrawable(wrappedDrawable);
            tv.setText("");
        } else {
            unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.group);
            wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, getColorFromAttribute(R.attr.icons, getContext().getTheme()));
            img.setImageDrawable(wrappedDrawable);
            tv.setText(thread.user + "");
        }
        item.setTag(thread.threadId);
        tv = item.findViewById(R.id.tv_new);
        if (thread.newCount > 0) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(thread.newCount + "");
            loge("new msg: " + thread.sender);
        }
        final int users = thread.user;
        item.setOnClickListener(v -> {
            if(v instanceof ViewGroup) {
                TextView textv = v.findViewById(R.id.tv_new);
                textv.setText("");
                textv.setVisibility(View.INVISIBLE);
            }
            loadChat(thread, -1);
        });
        registerForContextMenu(item);
        item.setOnCreateContextMenuListener((contextMenu, view, contextMenuInfo) -> {
            contextMenu.add(0, 0, 0,
                    context.getSharedPreferences("pref", 0).getString("muted", "[]")
                            .contains("" + thread.threadId)?"Включить уведомления":"Отключить уведомления")
                    .setIntent(new Intent().putExtra("threadId", thread.threadId));
            if(users > 2)
                contextMenu.add(0, 1, 0, "Покинуть диалог");
        });
        return item;
    }

    void show() {
        log("show MF");
        if(view == null || threads_list == null) {
            return;
        }
        final LinearLayout container1 = view.findViewById(R.id.container);
        if(container1 == null) {
            loge("container null in MessagesFragment");
            return;
        }
        count = 25;
        s_count = 0;
        container1.removeAllViews();
        if(getContext().getSharedPreferences("pref", 0).getBoolean("show_chat", false)) {
            view.findViewById(R.id.knock_l).setVisibility(View.VISIBLE);
            view.findViewById(R.id.knock_l).setOnClickListener(v -> {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                KnockFragment fragment = new KnockFragment();
                ((MainActivity) getContext()).set_visible(false);
                transaction.replace(R.id.chat_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        } else
            view.findViewById(R.id.knock_l).setVisibility(View.GONE);
        TextView tv = view.findViewById(R.id.tv_error);
        tv.setText("");
        tv.setVisibility(View.INVISIBLE);

        final ScrollView scroll = view.findViewById(R.id.scroll);

        View[] fitems = new View[threads_list.size()];
        int c = 0;
        for (int i = 0; i < threads_list.size(); i++) {
            final MsgThread thread = threads_list.get(i);
            c += thread.newCount;
            fitems[i] = makeThreadItem(thread, container1);
        }
        final int C = c;
        for (View fitem : fitems) {
            container1.addView(fitem, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container1.addView(getLayoutInflater().inflate(R.layout.divider, container1, false));
        }
        BottomNavigationView bottomnav = getContext().findViewById(R.id.bottomnav);
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomnav.getChildAt(0);
        final BottomNavigationItemView itemView = (BottomNavigationItemView)  bottomNavigationMenuView.getChildAt(2);
        log("unread messages count: " + C);
        tv = itemView.findViewById(R.id.tv_badge);
        if(C > 0) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(C + "");
        } else
            tv.setVisibility(View.INVISIBLE);

        scroll.scrollTo(0, 0);
        refreshL = view.findViewById(R.id.l_refresh);
        refreshL.setOnRefreshListener(() -> {
            log("refreshing rar");
            refresh();
        });
        savedView = view;

        if(first_time) {
            new Thread(() -> {
                    scrollListener = () -> {
                        if (scroll.getChildAt(0).getBottom() - 200
                                <= (scroll.getHeight() + scroll.getScrollY()) && !uploading) {
                            log("bobottom");
                            if (count == -1) {
                                log("all threads are shown");
                                return;
                            }
                            uploading = true;
                            @SuppressLint("HandlerLeak") final Handler h = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 0) {
                                        if(!shown)
                                            return;
                                        LinearLayout container = view.findViewById(R.id.container);

                                        View item1;
                                        LayoutInflater inflater1 = getLayoutInflater();
                                        MsgThread thread;
                                        for (int i = msg.arg1; i < threads_list.size(); i++) {
                                            thread = threads_list.get(i);
                                            item1 = makeThreadItem(thread, container);
                                            container.addView(item1, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            container.addView(inflater1.inflate(R.layout.divider, container, false));
                                        }
                                        if (count != -1)
                                            count = threads_list.size();
                                        uploading = false;
                                    } else if (msg.what == 1) {
                                        LayoutInflater inflater1 = getLayoutInflater();
                                        View item1;
                                        TextView tv1;
                                        int index;
                                        Spanned mess;
                                        String s;
                                        Spannable spannable;
                                        for (int i = s_count; i < s_senders.size(); i++) {
                                            item1 = inflater1.inflate(R.layout.thread_item, container, false);
                                            tv1 = item1.findViewById(R.id.tv_sender);
                                            tv1.setText(s_senders.get(i));
                                            tv1 = item1.findViewById(R.id.tv_topic);
                                            mess = Html.fromHtml(s_messages.get(i).replace("\n","<br>"));

                                            ImageView muted = item1.findViewById(R.id.muted);
                                            if(context.getSharedPreferences("pref", 0).getString("muted", "[]")
                                                    .contains("" + s_threadIds.get(i)))
                                                muted.setVisibility(View.VISIBLE);
                                            else
                                                muted.setVisibility(View.INVISIBLE);

                                            index = mess.toString().toLowerCase().indexOf(s_query.toLowerCase());
                                            log("index: " + index);
                                            int start, end;
                                            if (index > 30)
                                                start = index - 30;
                                            else
                                                start = 0;
                                            if (mess.toString().length() > index + s_query.length() + 30)
                                                end = index + s_query.length() + 30;
                                            else
                                                end = mess.toString().length() - 1;
                                            s = (start == 0 ? "" : "...") + mess.subSequence(start, end).toString() + (end == mess.toString().length() - 1 ? "" : "...");
                                            spannable = new SpannableString(s);
                                            spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), s.toLowerCase().indexOf(s_query.toLowerCase()), s.toLowerCase().indexOf(s_query.toLowerCase()) + s_query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            tv1.setText(spannable);
                                            tv1 = item1.findViewById(R.id.tv_users);
                                            tv1.setText("");
//                        img = item.findViewById(R.id.img);
                                            final int j = i;
                                            final MsgThread thread = new MsgThread();
                                            thread.threadId = s_threadIds.get(j);
                                            thread.topic = s_topics.get(j);
                                            thread.sender = s_senders.get(j);
                                            thread.type = 0;
                                            thread.user = s_group.get(j)?3:0;
                                            item1.setOnClickListener(v ->
                                                    loadChat(thread, s_msgIds.get(j)));
                                            container.addView(item1, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            container.addView(inflater1.inflate(R.layout.divider, container, false));
                                        }
                                        if (msg.arg1 == 0) {
                                            s_count = s_senders.size();
                                        } else
                                            s_count = -1;
                                    }
                                }
                            };
                            new Thread(() -> {
                                    try {
                                        if (s_senders == null) {
                                            if (count == -1 || threads_list.size() == 0)
                                                return;
                                            JSONArray array = new JSONArray(connect("https://app.eschool.center/ec-server/chat/threads?newOnly=false&row="
                                                    + (count + 1) + "&rowsCount=25", null, getContext()));
                                            JSONObject obj;
                                            String a, b, c1;
                                            log("uploaded " + array.length() + " threads");
//                                            if (array.length() < 25)
//                                                count = -1;
                                            int first_index = threads_list.size();
                                            MsgThread thread;
                                            for (int i = 0; i < array.length(); i++) {
                                                thread = new MsgThread();
                                                obj = array.getJSONObject(i);
                                                a = obj.getString("senderFio").split(" ")[0];
                                                b = obj.getString("senderFio").split(" ")[1];
                                                if (obj.getString("senderFio").split(" ").length <= 2) {
                                                    loge("fio strange:");
                                                    loge(obj.toString());
                                                    c1 = "";
                                                } else
                                                    c1 = obj.getString("senderFio").split(" ")[2];
                                                if(!a.equals("Служба") || !b.equals("поддержки")) {
                                                    if (c1.length() > 0)
                                                        thread.sender = a + " " + b.charAt(0) + ". " + c1.charAt(0) + ".";
                                                    else
                                                        thread.sender = a + " " + b.charAt(0) + ".";
                                                } else {
                                                    thread.sender = "Служба подержки";
                                                }
                                                if (obj.getString("subject").replaceAll(" ", "").equals(""))
                                                    if (obj.has("msgPreview"))
                                                        thread.topic = obj.getString("msgPreview");
                                                    else
                                                        thread.topic = "";
                                                else
                                                    thread.topic = obj.getString("subject");
                                                thread.user = obj.getInt("addrCnt");
                                                if (obj.getInt("senderId") == PERSON_ID) {
                                                    thread.user = 1;
                                                }
                                                thread.threadId = obj.getInt("threadId");
                                                thread.newCount = obj.getInt("newReplayCount");
                                                thread.type = obj.getInt("isAllowReplay");
                                                threads_list.add(thread);
                                            }
                                            if(array.length() > 0)
                                                log("first thread: " + threads_list.get(first_index).sender);
                                            h.sendMessage(h.obtainMessage(0, first_index, 0));
                                        } else if (s_count != -1 && search_mode != 1) {
                                            String result = connect("https://app.eschool.center/ec-server/chat/searchThreads?rowStart=" + s_count + "&rowsCount=25&text=" + s_query, null, getContext());
                                            log("search result: " + result);
                                            JSONArray array = new JSONArray(result), a, b;
                                            if (array.length() == 0) {
                                                h.sendEmptyMessage(2);
                                                return;
                                            }
                                            JSONObject obj, c1;
                                            for (int i = 0; i < array.length(); i++) {
                                                obj = array.getJSONObject(i);
                                                a = obj.getJSONArray("filterNumbers");
                                                for (int j = 0; j < a.length(); j++) {
                                                    result = connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&rowStart=0&rowsCount=1" +
                                                            "&threadId=" + obj.getInt("threadId") + "&msgStart=" + (/*obj.getInt("msgNum")*/a.optInt(j) + 1), null, getContext());

                                                    b = new JSONArray(result);
                                                    c1 = b.getJSONObject(0);
                                                    s_senders.add(c1.getString("senderFio"));
                                                    s_messages.add(c1.getString("msg"));
                                                    s_threadIds.add(c1.getInt("threadId"));
                                                    s_msgIds.add(a.optInt(j));
                                                    s_group.add(c1.getInt("addrCnt") > 2);
                                                    Date date = new Date(c1.getLong("createDate"));
                                                    s_time.add(String.format(Locale.UK, "%02d:%02d", date.getHours(), date.getMinutes()));
                                                }
                                            }
                                            h.sendMessage(h.obtainMessage(1, array.length() == 25 ? 0 : 1, 0));
                                        }
                                    } catch (LoginActivity.NoInternetException e) {
                                        getContext().runOnUiThread(() ->
                                                Toast.makeText(context, "Нет доступа к интернету", Toast.LENGTH_SHORT).show());
                                    } catch (Exception e) {
                                        loge(e);
                                    }
                            }).start();
                        }
                    };
                    getContext().runOnUiThread(() -> scroll.getViewTreeObserver()
                            .addOnScrollChangedListener(scrollListener));
                }).start();
            Toolbar toolbar = getContext().findViewById(R.id.toolbar);
            if(((MainActivity) getContext()).getStackTop() instanceof MessagesFragment)
                toolbar.setTitle("Сообщения");
            toolbar.setOnClickListener(v -> {
                log("click on toolbar");
                if(!(((MainActivity) getContext()).getStackTop() instanceof MessagesFragment))
                    return;
                final ScrollView scroll1 = view.findViewById(R.id.scroll);
                scroll1.post(() -> scroll1.scrollTo(0, 0));
            });
            setHasOptionsMenu(true);
            ((MainActivity) getContext()).setSupportActionBar(toolbar);
            first_time = false;
        }

        view.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.l_refresh).setVisibility(View.VISIBLE);
//        if(fromNotification) {
//            log("fromNotif");
//            int j = f_threadIds.indexOf(notifThreadId);
//            loadChat(notifThreadId, f_senders.get(j), s_topics.get(j),s_threadIds.get(j), -1, notifCount > 2);
//        }
        shown = true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                SharedPreferences pref = context.getSharedPreferences("pref", 0);
                if(item.getTitle().equals("Отключить уведомления")) {
                    log("mute " + item.getIntent().getIntExtra("threadId", -1));
                    try {
                        JSONArray array = new JSONArray(pref.getString("muted", "[]"));
                        array.put(item.getIntent().getIntExtra("threadId", -1));
                        pref.edit().putString("muted", array.toString()).apply();
                    } catch (Exception e) {loge(e);}
                    Toast.makeText(context, "Уведомления отключены", Toast.LENGTH_SHORT).show();
                } else {
                    log("unmute " + item.getIntent().getIntExtra("threadId", -1));
                    try {
                        JSONArray array = new JSONArray(pref.getString("muted", "[]")), a = new JSONArray();
                        for (int i = 0; i < array.length(); i++) {
                            if(!(array.getInt(i) == item.getIntent().getIntExtra("threadId", -1))) {
                                a.put(array.getInt(i));
                            }
                        }
                        pref.edit().putString("muted", a.toString()).apply();
                        Toast.makeText(context, "Уведомления включены", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {loge(e);}
                }
                return true;
            case 1:
                log("leave " + item.getIntent().getIntExtra("threadId", -1));
                new Thread(() -> {
                    try {
                        connect("https://app.eschool.center/ec-server/chat/leave?threadId=" +
                                item.getIntent().getIntExtra("threadId", -1), null, getContext());
                        refresh();
                    } catch (LoginActivity.NoInternetException e) {
                        getContext().runOnUiThread(() -> Toast.makeText(context, "Нет интернета", Toast.LENGTH_SHORT).show());

                    } catch (Exception e) {loge(e);}
                }).start();
        }
        return super.onContextItemSelected(item);
    }

    void newMessage(String text, long time, int sender_id, int thread_id) {
        log("new message in MessagesFragment");
        LinearLayout container = view.findViewById(R.id.container);
        View thread = container.findViewWithTag(thread_id);
        if(thread == null) {
            loge("this thread was not found");
            return;
        }
        TextView tv = thread.findViewById(R.id.tv_new);
        if(tv.getVisibility() == View.INVISIBLE) {
            tv.setVisibility(View.VISIBLE);
            tv.setText("1");
        } else {
            tv.setText(Integer.parseInt(tv.getText().toString()) + 1 + "");
        }
        tv = thread.findViewById(R.id.tv_topic);
        tv.setText(text);
        BottomNavigationView bottomnav = getContext().findViewById(R.id.bottomnav);
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomnav.getChildAt(0);
        final BottomNavigationItemView itemView = (BottomNavigationItemView)  bottomNavigationMenuView.getChildAt(2);
        tv = itemView.findViewById(R.id.tv_badge);
        if(tv.getVisibility() == View.INVISIBLE) {
            tv.setVisibility(View.VISIBLE);
            tv.setText("1");
        } else
            tv.setText(Integer.parseInt(tv.getText().toString()) + 1 + "");
        ScrollView scroll = view.findViewById(R.id.scroll);
        scroll.scrollTo(0, container.getBottom());
    }

    private void download(Handler handler) throws JSONException, IOException, LoginActivity.NoInternetException {
        JSONArray array = new JSONArray(
                connect("https://app.eschool.center/ec-server/chat/threads?newOnly=false&row=1&rowsCount=25",
                        null, getContext()));

        threads_list = new ArrayList<>();
        JSONObject obj;
        String a, b, c;
        MsgThread thread;
        log(array.length() + "");
        for (int i = 0; i < array.length(); i++) {
            thread = new MsgThread();
            obj = array.getJSONObject(i);
            String[] senderFio = obj.getString("senderFio").split(" ");
            a = "";
            b = "";
            c = "";
            if(senderFio.length > 0)
                a = senderFio[0];
            if(senderFio.length > 1)
                b = senderFio[1];
            if(senderFio.length > 2)
                c = senderFio[2];
            thread.sender = a + " " + (b.equals("")?"":b.charAt(0) + ". ") + (c.equals("")?"":c.charAt(0) + ".");
//            senders[i] = a + " " + (b.equals("")?"":b.charAt(0) + ". ") + (c.equals("")?"":c.charAt(0) + ".");
            if(obj.getString("subject").equals(" "))
                if(obj.has("msgPreview"))
                    thread.topic = obj.getString("msgPreview");
//                    topics[i] = obj.getString("msgPreview");
                else
                    thread.topic = "";
//                    topics[i] = "";
            else
                thread.topic = obj.getString("subject");
//                topics[i] = obj.getString("subject");
//            users[i] = obj.getInt("addrCnt");
            thread.user = obj.getInt("addrCnt");
            thread.type = obj.getInt("isAllowReplay");
//            types[i] = obj.getInt("isAllowReplay");
            if(obj.getInt("senderId") == PERSON_ID) {
//                users[i] = 1;
                thread.user = 1;
            }
            thread.threadId = obj.getInt("threadId");
            thread.newCount = obj.getInt("newReplayCount");
//            threadIds[i] = obj.getInt("threadId");
//            newCounts[i] = obj.getInt("newReplayCount");
            threads_list.add(thread);
        }
//        f_users = new ArrayList<>();
//        f_threadIds = new ArrayList<>();
//        f_senders = new ArrayList<>();
//        f_topics = new ArrayList<>();
//        f_newCounts = new ArrayList<>();
//        f_types = new ArrayList<>();
//        for (int i = 0; i < users.length; i++) {
//            f_users.add(users[i]);
//            f_threadIds.add(threadIds[i]);
//            f_senders.add(senders[i]);
//            f_topics.add(topics[i]);
//            f_newCounts.add(newCounts[i]);
//            f_types.add(types[i]);
//        }
        log("first thread: " + /*senders[0]*/threads_list.get(0).sender);
        if(handler != null)
            handler.sendEmptyMessage(0);
        if(!shown)
            getContext().runOnUiThread(this::show);
    }

    void refresh(final boolean refreshl) {
        if(refreshing)
            return;
        if(refreshl && refreshL == null)
            return;
        if(refreshl)
            refreshL.setRefreshing(true);

        @SuppressLint("HandlerLeak") final Handler h = new Handler() {
            View[] items;
            @Override
            public void handleMessage(Message msg) {
                items = new View[threads_list.size()];
                final LinearLayout container1 = view.findViewById(R.id.container);
                if(container1 == null)
                    return;
                container1.removeAllViews();

                if(searchView != null) {
                    if(searchView.isActionViewExpanded()) {
                        searchView.collapseActionView();

                        if(getContext().getSharedPreferences("pref", 0).getBoolean("show_chat", false)) {
                            view.findViewById(R.id.knock_l).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.knock_l).setOnClickListener(v ->  {
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                KnockFragment fragment = new KnockFragment();
                                ((MainActivity) getActivity()).set_visible(false);
                                transaction.replace(R.id.chat_container, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            });
                        } else
                            view.findViewById(R.id.knock_l).setVisibility(View.GONE);
                    }
                }

                final Handler h = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        TextView tv = view.findViewById(R.id.tv_error);
                        tv.setText("");
                        tv.setVisibility(View.INVISIBLE);
                        for (int i = 0; i < items.length; i++) {
//                            if(i < items.length)
                            container1.addView(items[i], ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            container1.addView(getLayoutInflater().inflate(R.layout.divider, container1, false));
                        }
                        final ScrollView scroll = view.findViewById(R.id.scroll);
                        scroll.scrollTo(0, 0);
                        BottomNavigationView bottomnav = getContext().findViewById(R.id.bottomnav);
                        BottomNavigationMenuView bottomNavigationMenuView =
                                (BottomNavigationMenuView) bottomnav.getChildAt(0);
                        final BottomNavigationItemView itemView = (BottomNavigationItemView)  bottomNavigationMenuView.getChildAt(2);
                        tv = itemView.findViewById(R.id.tv_badge);
                        if(msg.what == 0) {
                            tv.setVisibility(View.INVISIBLE);
                        } else {
                            tv.setVisibility(View.VISIBLE);
                            tv.setText(msg.what + "");
                        }
                        if(refreshl)
                            refreshL.setRefreshing(false);
                        refreshing = false;
                    }
                };
                new Thread(() -> {
                        View item;
                        int c = 0;
                        for (int i = 0; i < items.length; i++) {
                            MsgThread thread = threads_list.get(i);
                            item = makeThreadItem(thread, container1);
                            items[i] = item;
                        }
                        h.sendEmptyMessage(c);
                    }).start();
            }
        };
        new Thread(() -> {
                try {
                    download(h);
                } catch (LoginActivity.NoInternetException e) {
                    getContext().runOnUiThread(() -> {
                            Toast.makeText(context, "Нет доступа к интернету", Toast.LENGTH_SHORT).show();
                            if(refreshl)
                                refreshL.setRefreshing(false);}
                        );
                    refreshing = false;
                } catch (Exception e) {
                    loge("refreshing: " + e.toString());}
            }).start();
    }
    void refresh (){refresh(true);}

    private void loadChat(/*int threadId, String threadName, String topic, int type,*/
            MsgThread thread, int searchId/*, boolean group*/) {
        fromNotification = false;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        ChatFragment fragment = new ChatFragment();
        log("chat thread " + thread.threadId);
        fragment.threadId = thread.threadId;//f_threadIds.get(j);
        fragment.threadName = thread.sender;//f_senders.get(j);
        fragment.context = context;
        fragment.group = thread.user > 2;
        fragment.topic = thread.topic;
        fragment.type = thread.type;
        if(searchId != -1)
            fragment.searchMsgId = searchId;
        ((MainActivity) getContext()).set_visible(false);
        transaction.replace(R.id.chat_container, fragment);
        transaction.addToBackStack(null);
        try {
            transaction.commit();
        } catch (IllegalStateException e) {
            loge(e);
        }
    }

    public View getView() {
        if(view == null) {
            loge("null view!!");
            if(super.getView() != null)
                return super.getView();
            else
                return new View(getContext());
        } else
            return view;
    }

    static class MsgThread {
        String sender, topic;
        int user, threadId, newCount, type;
    }

    static class Person {
        String fio;
        String info;
        int prsId;
        Person() {}
    }

    public Activity getContext() {
        return (context==null?getActivity():context);
    }
}

