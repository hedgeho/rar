package com.example.sch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.example.sch.LoginActivity.log;
import static com.example.sch.LoginActivity.loge;

public class KnockFragment extends Fragment {

    View view;
    String id, token, auth_token, name, icon;
    WebSocket socket_read, socket_write;
    private boolean sending;
    private JSONObject last_msg;
    Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chat, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Общий чат");
        setHasOptionsMenu(true);
        // todo toolbar subtitle
        // toolbar.setSubtitle("subtitle");
        if(getActivity() != null)
            context = getActivity();

        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        // Inflate the layout for this fragment``
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        this.view = view;

        view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = view.findViewById(R.id.et);
                final String text = et.getText().toString();
                et.setText("");
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
                                    .put("time", getDateString())
                                    .put("type", "text")
                                    .put("token", token)
                                    .put("admin", "true")
                                    .put("uuid", id);
                            socket_write.sendText(object.toString());
                            socket_write.disconnect();
                        } catch (Exception e) {loge(e.toString());}
                    }
                }.start();
            }
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
                    if(spl.length > 10) {
                        name = spl[3];
                        getActivity().getSharedPreferences("pref", 0).edit().putString("knock_name", name).apply();
                        icon = spl[5];
                        token = spl[7];
                    }
                    /*s = connect("https://warm-bayou-37022.herokuapp.com/login?type=lp&login=123" +
                            "&password=321&save=true", null);
                    String[] spl = s.split("\"");
                    if(spl.length > 10) {
                        id = spl[1];
                        auth_token = spl[3];
                        name = spl[5];
                        icon = spl[7];
                        token = spl[9];
//                        if(spl[13].equals("false"))
//                            admin = "false";
//                        else
//                            admin = "true";
                    }
                    loge(s);*/

                    socket_read = new WebSocketFactory().createSocket("wss://warm-bayou-37022.herokuapp.com/receive");
                    socket_write = new WebSocketFactory().createSocket("wss://warm-bayou-37022.herokuapp.com/submit");

                    socket_read.addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) throws Exception {
                            log("received /receive: " + text);
                            newMessage(text);
                        }

                        @Override
                        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            if (frame.getPayload() == null) {
                                loge("null frame SENT");
                                return;
                            }
                            log("sent /receive: " + frame.getPayloadText());
//                            if (frame.getPayloadText().equals("\u0003�No more WebSocket frame from the server.") || frame.getPayloadText().contains("No more WebSocket frame from the server")) {
//                                log(2,"TRYING TO RECONNECT...");
//                                h.sendEmptyMessage(RECONNECT);
//                            }
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                            loge("/receive: " + cause.toString());
//                            if (cause.toString().contains("Failed to connect")) {
//                                socket_read.connect();
//                            }
                        }
                    });
                    socket_write.addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) throws Exception {
                            log("received /submit: " + text);
                            if(text.contains("wrong token")) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            getNewToken();
                                        } catch (Exception e) {
                                            loge(e.toString());
                                        }
                                    }
                                }.start();
                            }
                        }

                        @Override
                        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            if (frame.hasPayload())
//                                if(frame.getPayloadText().contains("ping") || frame.getPayloadText().contains("pong"))
//                                    log(0, "SENDING MESSAGE /submit : " + frame.getPayloadText());
//                                else
//                                    log(1, "SENDING MESSAGE /submit : " + frame.getPayloadText());
                                log("sent /submit: " + frame.getPayloadText());
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                            loge("/submit: " + cause.toString());
//                            Thread.sleep(10);
//                            socket_write.connect();
//                            snackbar.show();
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
                            while(true) {
                                try {
                                    Thread.sleep(1000);
                                    //log(socket_write.getState().name());
//                                    if(socket_write.getState().name().equals("CLOSED")) {
//                                        socket_write.sendClose();
//                                        socket_write = socket_write.recreate();
//                                        socket_write.connect();
//                                    }
                                    JSONObject object = new JSONObject();
                                    object.put("system", "true")
                                            .put("uuid", id)
                                            //.put("key", "key")
                                            .put("name", "name")
                                            .put("token", token)
                                            .put("event", "ping");
                                    socket_write.sendText(object.toString());
                                } catch (Exception e) {log( e.toString());}}
                        }
                    }.start();

                    JSONObject object = new JSONObject();
                    object.put("key", id)
                            .put("token", token)
                            .put("msg", "true")
                            .put("name", name);
                    log("key " + id + ", token " + token);
                    sending = true;
                    socket_read.sendText(object.toString());

                } catch (Exception e) {
                    loge(e.toString());
                }
            }
        };
        if(pref.getString("knock_token", "").equals("")) {
            view.findViewById(R.id.l_new).setVisibility(View.VISIBLE);
            final EditText et = view.findViewById(R.id.et_nickname);
            view.findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String s = et.getText().toString();
                    et.setText("");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                getActivity().getSharedPreferences("pref", 0).edit().putString("knock_name", s).apply();
                                String login = randomize(s);
                                String password = randomize(s);
                                String query = "type=lp&login=" + login + "&password=" + password + "&info=можно не надо&name=" + s;
                                String s = connect("https://warm-bayou-37022.herokuapp.com/reg", query);//obj.toString());
                                loge("registration: " + s);

                                s = connect("https://warm-bayou-37022.herokuapp.com/login?type=lp&login=" + login + "&" +
                                        "password=" + password + "&save=true", null);
                                log("login: " + s);
                                String[] spl = s.split("\"");
                                if(spl.length > 10) {
                                    pref.edit().putString("knock_id", spl[1]).putString("knock_token", spl[3]).apply();
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.findViewById(R.id.l_new).setVisibility(View.INVISIBLE);
                                    }
                                });
                                thread.start();
                            } catch (Exception e) {loge(e.toString());}
                        }
                    }.start();
                }
            });
        } else
            thread.start();
        return view;
    }

    void newMessage(String text) throws JSONException {
        final JSONObject object = new JSONObject(text), last = last_msg;
        last_msg = object;
        if (object.has("system") && object.has("uuid")
                && object.has("type") && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ViewGroup container = view.findViewById(R.id.main_container);
                        View item;
                        if (!object.getString("uuid").equals(id))
                            item = getLayoutInflater().inflate(R.layout.chat_item_left, container, false);
                        else
                            item = getLayoutInflater().inflate(R.layout.chat_item, container, false);
                        TextView tv = item.findViewById(R.id.chat_tv_sender);

                        if(tv != null) {
                            if (last != null) {
                                if (last.getString("uuid").equals(object.getString("uuid"))) {
                                    tv.setVisibility(View.GONE);
                                } else {
                                    tv.setText(object.getString("name"));
                                }
                            } else {
                                tv.setText(object.getString("name"));
                            }
                        }
                        tv = item.findViewById(R.id.tv_text);
                        tv.setText(object.getString("text"));
                        tv = item.findViewById(R.id.tv_time);
                        Calendar c = toCalendar(object.getString("time"));
                        tv.setText(String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
                        container.addView(item);
                        final ScrollView scroll = view.findViewById(R.id.scroll);
                        scroll.post(new Runnable() {
                            @Override
                            public void run() {
                                scroll.scrollTo(0, scroll.getChildAt(0).getBottom());
                            }
                        });
                    } catch (Exception e) {loge(e.toString());}
                }
            });
        }
    }

    void getNewToken() throws Exception {
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

    static String connect(String url, String query) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        if(query == null) {
            con.setRequestMethod("GET");
            con.connect();
        } else {
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.connect();
            con.getOutputStream().write(query.getBytes());
        }
        if(con.getResponseCode() != 200) {
            loge("connect failed, code " + con.getResponseCode() + ", message: " + con.getResponseMessage());
            loge(url);
            loge("query: '" + query + "'");
            return "";
        }
        if(con.getInputStream() != null) {
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
    }

    @Override
    public Context getContext() {
        return context;
    }

    static String randomize(String s) {
        long a = s.length()*System.currentTimeMillis();
        StringBuilder response = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 20; i++) {
            response.append((char)((a + random.nextInt())% (i<s.length()?s.charAt(i):100000) % ('Z'-'a') + 'a'));
        }
        return response.toString();
    }

    static final SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.UK);
    static final SimpleDateFormat format1 = new SimpleDateFormat(" YYYY, h:mm:ss a", Locale.UK);

    static String getDateString() {
        return format.format(new Date()) + "th" + format1.format(new Date());
    }

    static Calendar toCalendar(String s) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        // June 3th 2019, 1:10:58 pm

        String[] spl = s.split("th");
        if(spl.length<2) {
            spl = s.split("nd");
        }
        if(spl.length<2) {
            spl = s.split("st");
        }
        if(spl.length<2) {
            spl = s.split("rd");
        }
        if (spl.length >= 2) {
            Date date1 = format.parse(spl[0]);
            Date date2 = format1.parse(spl[1]);
            calendar.set(date2.getYear(), date1.getMonth(), date1.getDay(), date2.getHours(), date2.getMinutes(), date2.getSeconds());
        }
        return calendar;
    }
}
