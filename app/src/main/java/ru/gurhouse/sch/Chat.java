package ru.gurhouse.sch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static android.view.View.GONE;
import static java.util.Calendar.YEAR;
import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;

public class Chat extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    int threadId = 0;
    String threadName = "";
    int searchMsgId = -1;
    boolean group = false;
    Context context;
    private int PERSON_ID;
    List<File> attach = new ArrayList<>();

    public Chat() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static Chat newInstance(int columnCount) {
        Chat fragment = new Chat();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    DataAdapter adapter;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        PERSON_ID = TheSingleton.getInstance().getPERSON_ID();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(threadName);
        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).setSupActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new DataAdapter();
        recyclerView.setAdapter(adapter);
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if(newState == 0 && adapter.getItemCount() != 0 && !adapter.scrolling){
//                    syncMessages();
//                }
//            }
//        });
        syncMessages();

        ImageView btn_send = view.findViewById(R.id.btn_send);
        btn_send.setOnClickListener(v->{
            EditText et = view.findViewById(R.id.et);
            final String text = et.getText().toString();
            //attach = null;
            Chat.this.sendMessage(threadId, text);
            et.setText("");
        });

        ImageView btn_file = view.findViewById(R.id.btn_file);
        btn_file.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, 43);
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getContext() != null) {
            menu.clear();
            MenuItem ref = menu.add(0, 2, 0, "Refresh");
            ref.setIcon(getResources().getDrawable(R.drawable.refresh));
            ref.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            ((MainActivity) getActivity()).quit();
        } else if (item.getItemId() == 2) {

        }
        return super.onOptionsItemSelected(item);
    }

    public void syncMessages(){
        new Thread(()-> {
            try {
                Msg d = adapter.getMsg();
                JSONArray array = new JSONArray(connect("https://app.eschool.center/ec-server/chat/messages?getNew=false&isSearch=false&" +
                        "rowStart=1&rowsCount=25&threadId=" + threadId + (d != null ? "&msgStart=" + d.msg_id : ""), null, getContext()));
                for (int i = 0; i < array.length(); i++) {
                    Msg msg = new Msg();
                    JSONObject tmp = array.getJSONObject(i);
                    msg.time = new Date(tmp.getLong("sendDate"));
//                    if (tmp.getInt("attachCount") <= 0) {
//                        msg.files = null;
//                    } else {
//                        msg.files = new ArrayList<>();
//                        for (int j = 0; j < tmp.getInt("attachCount"); j++) {
//                            JSONObject tmp1 = tmp.getJSONArray("attachInfo").getJSONObject(j);
//                            msg.files.set(j, new ChatFragment.Attach(tmp1.getInt("fileId"), tmp1.getInt("fileSize"),
//                                    tmp1.getString("fileName"), tmp1.getString("fileType")));
//                        }
//                    }
                    if(tmp.has("senderId")) msg.user_id = tmp.getInt("senderId");
                    if(tmp.has("msgNum")) msg.msg_id = tmp.getInt("msgNum");
                    if(tmp.has("senderFio")) msg.sender = tmp.getString("senderFio");
                    if(tmp.has("msg")) msg.text = tmp.getString("msg");
                    adapter.addDated(msg);
                }
                getActivity().runOnUiThread(()->adapter.update());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    File pinned = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 124);
            pinned = new File(ImageFilePath.getPath(getContext(), data.getData()));
        } else {
            System.out.println("result");
            uploadFile(new File(ImageFilePath.getPath(getContext(), data.getData())));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 124) {
            uploadFile(pinned);
        }
    }

    public void uploadFile(File file) {
        attach.add(file);
    }

    void DrawMsg(Chat.Msg msg){
        adapter.addDated(msg);
        adapter.update();
        recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
    }

    private void sendMessage(int threadId, String text) {
        Date date = new Date();
        Msg msg = new Msg();
        msg.msg_id = 0;
        msg.sender = threadName;
        msg.text = text;
        msg.time = date;
        msg.user_id = PERSON_ID;
        DrawMsg(msg);
        new Thread(() -> {
            try {
                HttpPost post = new HttpPost("https://app.eschool.center/ec-server/chat/sendNew");
                HttpClient httpAsyncClient = AndroidHttpClient.newInstance("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393", getContext());
                MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
                reqEntity.setBoundary("----WebKitFormBoundaryfgXAnWy3pntveyQZ");
                for (File f : attach)
                    reqEntity.addBinaryBody("file", f, ContentType.create("image/jpeg"), f.getName());
                attach.clear();
                reqEntity.addTextBody("threadId", "" + threadId);
                reqEntity.addTextBody("msgUID", "" + date.getTime());
                reqEntity.addTextBody("msgText", text, ContentType.parse("text/plain; charset=utf-8"));
                post.setHeader("Cookie", TheSingleton.getInstance().getCOOKIE() + "; site_ver=app; route=" + TheSingleton.getInstance().getROUTE() + "; _pk_id.1.81ed=de563a6425e21a4f.1553009060.16.1554146944.1554139340.");
                post.setHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryfgXAnWy3pntveyQZ ");
                post.setEntity(reqEntity.build());
                int code = httpAsyncClient.execute(post).getStatusLine().getStatusCode();
                System.out.println(code);

                log("sending file code " + code);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static class Msg extends Dated {
        ArrayList<DataAdapter.Attach> files;
        int user_id, msg_id;
        String text="", sender;
    }

    public static class Dated{
        Date time;
        enum MsgType{my, other, bubble}
    }

    public static class Bubble extends Dated{
        Bubble(Date time){
            this.time = time;
        }
    }

    class DataAdapter extends RecyclerView.Adapter<Chat.DataAdapter.ViewHolder> {
        private List<Dated> msgList;

        DataAdapter() {
            msgList = new ArrayList<>();
        }

        public boolean scrolling = false;

        public void addDated(Dated dated){
            msgList.add(dated);
        }

        public Msg getMsg(){
            for(Dated d : msgList){
                if(d instanceof Msg)
                    return (Msg)d;
            }
            return null;
        }

        public Dated getDated(int i){
            return msgList.get(i);
        }

        public void clear(){msgList.clear();}

        public void update(){
            if(msgList.isEmpty()) return;
            Collections.sort(msgList, (o1, o2) -> o1.time.compareTo(o2.time));
            addBubbles();
            notifyDataSetChanged();

            scrolling = true;
            recyclerView.scrollToPosition(adapter.getItemCount()-1);
            scrolling = false;
        }

        public void addBubbles(){
            msgList.add(0,new Bubble(msgList.get(0).time));
            for(int i = 1; i < msgList.size(); i++){
                if(msgList.get(i).time.getDay() != msgList.get(i-1).time.getDay()){
                    msgList.add(i,new Bubble(msgList.get(i).time));
                }
            }
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == Msg.MsgType.my.ordinal())
                return new DataAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
            else if(viewType == Msg.MsgType.other.ordinal())
                return new DataAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false));
            return new DataAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.date_divider, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            if (msgList.get(position) instanceof Msg) {
                if(((Msg)msgList.get(position)).user_id == PERSON_ID){
                    return Msg.MsgType.my.ordinal();
                } else {
                    return Msg.MsgType.other.ordinal();
                }
            }
            return Msg.MsgType.bubble.ordinal();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(msgList.get(position) instanceof Msg) {
                Msg thsMsg = (Msg) msgList.get(position);
                if (holder.chat_sender != null) {
                    holder.chat_sender.setVisibility(GONE);
                } else if (holder.chat_sender != null) {
                    holder.chat_sender.setText(thsMsg.sender);
                    holder.chat_sender.setVisibility(View.VISIBLE);
                }
                holder.text.setText(Html.fromHtml(thsMsg.text));
                holder.time.setText(String.format(Locale.UK, "%02d:%02d", thsMsg.time.getHours(), thsMsg.time.getMinutes()));

//            if (thsMsg.files != null) {
//                for (final ChatFragment.Attach a : thsMsg.files) {
//                    TextView tv_attach = new TextView(getContext());
//                    float size = a.size;
//                    String s = "B";
//                    if (size > 900) {
//                        s = "KB";
//                        size /= 1024;
//                    }
//                    if (size > 900) {
//                        s = "MB";
//                        size /= 1024;
//                    }
//                    tv_attach.setText(String.format(Locale.getDefault(), a.name + " (%.2f " + s + ")", size));
//                    tv_attach.setTextColor(getResources().getColor(R.color.two));
//                    tv_attach.setOnClickListener(v -> {
//                        String url = "https://app.eschool.center/ec-server/files/" + a.fileId;
//                        ((MainActivity) getActivity()).saveFile(url, a.name, true);
//                    });
//                    holder.attach.addView(tv_attach);
//                }
//            } else
//                holder.attach.setVisibility(GONE);
            }else{
                final String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
                        "октября", "ноября", "декабря"};

                int day = msgList.get(position).time.getDay(),
                        month = msgList.get(position).time.getMonth(),
                        year = msgList.get(position).time.getYear();
                Calendar current = Calendar.getInstance();
                if(current.get(YEAR) != year)
                    holder.time.setText(String.format(Locale.getDefault(), "%02d.%02d.%02d", day, month+1, year));
                if(current.get(Calendar.DAY_OF_MONTH) == day)
                    holder.time.setText("Сегодня");
                else if(current.get(Calendar.DAY_OF_MONTH) + 1 == day)
                    holder.time.setText("Вчера");
                holder.time.setText(String.format(Locale.getDefault(), "%d " + months[month], day));
            }
        }

        @Override
        public int getItemCount() {
            return msgList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text, time, chat_sender;
//            LinearLayout attach;

            ViewHolder(View view) {
                super(view);
                text = view.findViewById(R.id.tv_text);
                time = view.findViewById(R.id.tv_time);
            }
        }

        public class Attach {
            int fileId, size;
            String name, type;

            Attach(int fileId, int size, String name, String type) {
                this.fileId = fileId;
                this.size = size;
                this.name = name;
                this.type = type;
            }
        }
    }
}
