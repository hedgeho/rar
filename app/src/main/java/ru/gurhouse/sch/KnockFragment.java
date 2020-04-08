package ru.gurhouse.sch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.net.ssl.SSLException;

import static ru.gurhouse.sch.LoginActivity.log;
import static ru.gurhouse.sch.LoginActivity.loge;

public class KnockFragment extends Fragment {

    private View view;
    private String id, token, auth_token, name;
    private WebSocket socket_read, socket_write;
    private JSONObject last_msg, last_msg_up, first_msg;
    private Activity context;
//    boolean first_time = true, uploading = false;
    private ArrayList<Ping> pings;
    private boolean uploading = false;
    private int upload_count = 0, umsg_num = 0;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chat, container, false);
        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Общий чат");
        setHasOptionsMenu(true);
        toolbar.setSubtitle("Загрузка...");
        if(getActivity() != null)
            context = getActivity();
        if(pings == null)
            pings = new ArrayList<>();
        ((MainActivity) getContext()).setSupActionBar(toolbar);
        // Inflate the layout for this fragment
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.view = view;

        view.findViewById(R.id.btn_send).setOnClickListener(v -> {
            EditText et = view.findViewById(R.id.et);
            final String text = et.getText().toString();
            et.setText("");
            //;[;
//                hideKeyboardFrom(context, et);
            et.clearFocus();
            //log("focus: " + container.requestFocus());

            new Thread() {
                @Override
                public void run() {
                    try {
                        socket_write = socket_write.recreate();
                        socket_write.connect();
                        //String message = "{\"system\":\"false\",\"uuid\":\"170.51287793191963\",\"text\":\"" + text + "\"," +
                        //        "\"name\":\"спамер\",\"icon\":\"3\",\n" +
                        //        "\t\"time\":\"October 26th 2018, 3:58:49 pm\",\"type\":\"text\"}\n";
                        JSONObject object = new JSONObject();
                        object.put("icon", "3")// + icon)
                                .put("name", name)
                                .put("system", "false")
                                .put("text", text)
                                .put("time", new Date().getTime())
                                .put("type", "text")
                                .put("token", token)
                                .put("admin", "true")
                                .put("uuid", id);
                        socket_write.sendText(object.toString());
                        //socket_write.disconnect();
                    } catch (Exception e) {
                        loge(e);
                        if(e.getMessage().contains("503 Service Unavailable")) {
                            Toast.makeText(context, "Сервер недоступен (#503)", Toast.LENGTH_SHORT).show();
                        }}
                }
            }.start();
        });
        view.findViewById(R.id.btn_refresh).setOnClickListener(v -> {
            view.findViewById(R.id.tv_error).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.btn_refresh).setVisibility(View.INVISIBLE);
            onCreateView(inflater, container, savedInstanceState);
        });
        final SharedPreferences pref = getContext().getSharedPreferences("pref", 0);
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    auth_token = pref.getString("knock_token", "");
                    id = pref.getString("knock_id", "");
                    String s = connect("https://warm-bayou-37022.herokuapp.com/check?cookie=" + auth_token + "&id=" + id, null);
                    String[] spl = s.split("\"");
                    if (spl.length > 10) {
                        name = spl[3];
                        getContext().getSharedPreferences("pref", 0).edit().putString("knock_name", name).apply();
                        //icon = spl[5];
                        token = spl[7];
                    }

                    log("read: " + socket_read);

                    socket_read = new WebSocketFactory().createSocket("wss://warm-bayou-37022.herokuapp.com/receive");
                    socket_write = new WebSocketFactory().createSocket("wss://warm-bayou-37022.herokuapp.com/submit");

                    socket_read.addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) throws Exception {
                            log("received /receive: " + text);
                            newMessage(text);
                        }

                        @Override
                        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) {
                            if (frame.getPayload() == null) {
                                loge("null frame SENT");
                                return;
                            }
                            log("sent /receive: " + frame.getPayloadText());
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) {
                            loge("/receive: " + cause.toString());
                        }
                    });
                    socket_write.addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) {
                            log("received /submit: " + text);
                            if (text.contains("wrong token")) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            getNewToken();
                                        } catch (Exception e) {
                                            loge(e);
                                        }
                                    }
                                }.start();
                            }
                        }

                        @Override
                        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) {
                            if (frame.hasPayload())
                                log("sent /submit: " + frame.getPayloadText());
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) {
                            loge("/submit: " + cause.toString());
                        }
                    });

                    socket_read.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
                    socket_write.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);

                    socket_read.setMissingCloseFrameAllowed(false);
                    socket_write.setMissingCloseFrameAllowed(false);

                    socket_read.connect();
                    socket_write.connect();

                    new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    Thread.sleep(1000);
                                    if (isVisible()) {
                                        JSONObject object = new JSONObject();
                                        object.put("system", "true")
                                                .put("uuid", id)
                                                //.put("key", "key")
                                                .put("name", "name")
                                                .put("token", token)
                                                .put("event", "ping");
                                        socket_write.sendText(object.toString());
                                    }
                                } catch (Exception e) {
                                    loge(e);
                                }
                            }
                        }
                    }.start();

                    JSONObject object = new JSONObject();
                    object.put("key", id)
                            .put("token", token)
                            .put("msg", "true")
                            .put("name", name);
                    log("key " + id + ", token " + token);
//                    sending = true;
                    socket_read.sendText(object.toString());
                } catch (LoginActivity.NoInternetException e) {
                    getContext().runOnUiThread(() -> {
                        TextView tv = view.findViewById(R.id.tv_error);
                        tv.setText("Нет доступа к Интернету");
                        tv.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.btn_refresh).setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    loge(e);
                }
            }
        };
        if(pref.getString("knock_token", "").equals("")) {
            view.findViewById(R.id.l_new).setVisibility(View.VISIBLE);
            final EditText et = view.findViewById(R.id.et_nickname);
            view.findViewById(R.id.btn_go).setOnClickListener(v -> {
                final String s = et.getText().toString();
                if(s.replaceAll(" ", "").equals("") || s.length() > 50)
                    return;
                et.setText("");
                view.findViewById(R.id.l_new).setVisibility(View.INVISIBLE);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            getContext().getSharedPreferences("pref", 0).edit().putString("knock_name", s).apply();
                            String login = randomize(s);
                            String password = randomize(s);
                            String query = "type=lp&login=" + login + "&password=" + password + "&info=можно не надо&name=" + s;
                            String s = connect("https://warm-bayou-37022.herokuapp.com/reg", query);//obj.toString());
                            loge("registration: " + s);

                            s = connect("https://warm-bayou-37022.herokuapp.com/login?type=lp&login=" + login + "&" +
                                    "password=" + password + "&save=true", null);
                            log("login: " + s);
                            String[] spl = s.split("\"");
                            if (spl.length > 10) {
                                pref.edit().putString("knock_id", spl[1]).putString("knock_token", spl[3]).apply();
                            }
                            thread.start();
                        } catch (LoginActivity.NoInternetException e) {
                            getContext().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Нет доступа к интернету", Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {loge(e);}
                    }
                }.start();
            });
        } else
            thread.start();
        new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        if(isVisible()) {
                            ArrayList<Integer> delete = new ArrayList<>();
                            for (int i = 0; i < pings.size(); i++) {
                                if(System.currentTimeMillis() - pings.get(i).time > 11000)
                                    delete.add(i);
                            }
                            for (int i = delete.size()-1; i >= 0; i--) {
                                pings.remove((int) delete.get(i));
                            }
                            if(delete.size() != 0)
                                log(delete.size() + " users are not more online");
                            if(pings.size() == 0)
                                continue;
                            final int count = pings.size()-1;
                            getContext().runOnUiThread(() -> {
                                String end;
                                if(count < 10 || count > 19)
                                    switch (count % 10) {
                                        case 1:
                                            end = "ь";
                                            break;
                                        case 2:
                                        case 3:
                                        case 4:
                                            end = "я";
                                            break;
                                        default:
                                            end = "ей";
                                    }
                                else
                                    end = "ей";
                                toolbar.setSubtitle(count + " пользовател" + end + " онлайн");
                            });
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        loge(e);}
                }
            }
        }.start();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ScrollView scroll = view.findViewById(R.id.scroll);
        ViewTreeObserver.OnScrollChangedListener listener = () -> {
            if (scroll.getScrollY() == 0 && !uploading) {
                log("top!!");
                uploading = true;
                upload_count++;
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("lim", 30);
                    obj.put("start", 30*upload_count);
                    obj.put("msg", "false");
                    obj.put("key", id);
                    obj.put("token", token);
                    socket_read.sendText(obj.toString());
                } catch (Exception e) {
                    loge(e);
                }
            }
        };
        scroll.getViewTreeObserver().addOnScrollChangedListener(listener);
    }

    private void newMessage(String text) throws JSONException {
        final boolean uploading = this.uploading;
        final JSONObject object = new JSONObject(text), last = (uploading?last_msg_up:last_msg);
        if(!object.has("system")) {
            loge("no system tag:");
            loge(object.toString());
            return;
        }

        // case usual message
        if (object.has("uuid") && object.has("type") && getContext() != null) {
            if(first_msg == null)
                first_msg = object;
            if(uploading)
                last_msg_up = object;
            else
                last_msg = object;

            getContext().runOnUiThread(() -> {
                try {
                    ViewGroup container = getView().findViewById(R.id.main_container);
                    View item;
                    if (!object.getString("uuid").equals(id))
                        item = getLayoutInflater().inflate(R.layout.chat_item_left, container, false);
                    else
                        item = getLayoutInflater().inflate(R.layout.chat_item, container, false);
                    TextView tv = item.findViewById(R.id.chat_tv_sender);
                    if(tv != null) {
                        if(last != null)
                            if (!last.getString("uuid").equals(object.getString("uuid"))) {
                                tv.setText(object.getString("name"));
                                tv.setVisibility(View.VISIBLE);
                            } else
                                tv.setVisibility(View.GONE);
                    }
                    tv = item.findViewById(R.id.tv_text);
                    if(object.getString("type").equals("text"))
                        tv.setText(object.getString("text"));
                     else {
                        tv.setVisibility(View.GONE);
                        if(object.getString("type").equals("img")) {
                            tv = new TextView(context);
                            String[] spl = object.getString("text").split("/");
                            final String name = spl[spl.length - 1];
                            tv.setText(name);
                            tv.setTextColor(getResources().getColor(R.color.two));
                            final String link = object.getString("text");
                            tv.setOnClickListener(v -> ((MainActivity) getContext()).saveFile(link, name, true));
                            ((ViewGroup) item.findViewById(R.id.attach)).addView(tv);
                        }
                    }
                    tv = item.findViewById(R.id.tv_time);
                    tv.setText(String.format(Locale.UK, "%02d:%02d", new Date(object.getLong("time")).getHours(), new Date(object.getLong("time")).getMinutes()));
                    if(uploading)
                        container.addView(item, umsg_num++%30);
                    else
                        container.addView(item);
                    final ScrollView scroll = view.findViewById(R.id.scroll);
                    scroll.post(() -> {
                        /*if(uploading && umsg_num%30 == 29) {
                            int s = ((ViewGroup) scroll.getChildAt(0)).getChildAt(24).getBottom();
                            log("scroll " + s);
                            scroll.scrollTo(0, s);
                        } else*/ if(!uploading)
                            scroll.scrollTo(0, scroll.getChildAt(0).getBottom());
                    });
                    log("text: " + object.getString("text"));
                } catch (Exception e) {
                    loge("m: " + e.toString());}
            });
        }
        // case ping
        else if(object.getString("system").equals("true") && object.has("event")) {
            if(object.getString("event").equals("ping")) {
                String uuid = object.getString("uuid");
                int index = -1;
                for (int i = 0; i < pings.size(); i++) {
                    if(pings.get(i).uuid.equals(uuid)) {
                        index = i;
                        break;
                    }
                }
                if(index == -1)
                    pings.add(new Ping(uuid, System.currentTimeMillis()));
                else
                    pings.get(index).time = System.currentTimeMillis();
            }
        }
        // case end of uploading
        else if(object.has("end")) {
            this.uploading = false;
            last_msg_up = null;
        }
    }

    private void getNewToken() throws Exception {
        log("getting new token, auth token: "+ auth_token + ", id: " + id);

        String[] spl = connect("https://warm-bayou-37022.herokuapp.com/check?cookie=" + auth_token + "&id=" + id,
                null).split("\"");
        String token;
        if (spl.length > 7) {
            token = spl[7];
        } else
            return;
        this.token = token;

//        JSONObject object = new JSONObject();
//        //object.put("key", 170.51287793191963);
//        object.put("key", id)
//                .put("token", token)
//                .put("msg", "true")
//                .put("name", name);
//        sending = true;
//        socket_read.sendText(object.toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    static String connect(String url, String query) throws IOException, LoginActivity.NoInternetException {
        log("connect w/o cookies: " + url);
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            if (query == null) {
                con.setRequestMethod("GET");
                con.connect();
            } else {
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.connect();
                con.getOutputStream().write(query.getBytes());
            }
            if (con.getResponseCode() != 200) {
                loge("connect failed, code " + con.getResponseCode() + ", message: " + con.getResponseMessage());
                loge("url: " + url);
                loge("query: '" + query + "'");
                return "";
            }
            if (con.getInputStream() != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                return result.toString();
            } else
                return "";
        } catch (UnknownHostException e) {
            throw new LoginActivity.NoInternetException();
        } catch (SSLException e) {
            throw new LoginActivity.NoInternetException();
        } catch (ConnectException e) {
            throw new LoginActivity.NoInternetException();
        }
    }

    @Override
    public Activity getContext() {
        return (context==null?getActivity():context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null)
            context = getActivity();
    }

    private static String randomize(String s) {
        long a = s.length()*System.currentTimeMillis();
        StringBuilder response = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 20; i++) {
            response.append((char)((a + random.nextInt())% (i<s.length()?s.charAt(i):100000) % ('Z'-'a') + 'a'));
        }
        return response.toString();
    }

    private class Ping {
        String uuid;
        long time;
        Ping(String uuid, long time) {
            this.uuid = uuid;
            this.time = time;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(socket_read != null) {
            socket_read.sendClose();
            socket_write.sendClose();
        }
    }
}
