package ru.gurhouse.sch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static ru.gurhouse.sch.LoginActivity.connect;
import static ru.gurhouse.sch.LoginActivity.log;

/** under construction - screen showing total marks of the user*/
public class TotalMarks extends Fragment {

    ArrayList<String> s = new ArrayList<>();
    TableLayout tableLayout;
    Toolbar toolbar;
    int yearid;

    public TotalMarks() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private int USER_ID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_total_marks, container, false);
        tableLayout = v.findViewById(R.id.table);
        Download3();
        return v;
    }

    String ss(String s) {
        String f = "";
        for (int i = 0; i < s.length(); i++) {
            f += s.charAt(i);
            if (i != s.length() - 1)
                f += "\n";
        }
        return f;
    }

    void start() {
        USER_ID = TheSingleton.getInstance().getUSER_ID();
    }

    void Download3() {
        log("Dowload3()");
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONArray array1 = new JSONArray(
                            connect("https://app.eschool.center/ec-server/yearplan/academyears",
                                    null, getContext()));
                    JSONObject object1;
                    for (int i = 0; i < array1.length(); i++) {
                        object1 = array1.getJSONObject(i);
                        if (object1.getInt("y1") == 2018) {
                            yearid = object1.getInt("yearId");
                            break;
                        }
                    }
                    JSONObject object = new JSONObject(
                            connect("https://app.eschool.center/ec-server/student/getTotalMarks?yearid=" + yearid + "&userid=" + USER_ID + "&json=true",
                                    null, getContext()));
                    object = object.getJSONObject("RegisterOfClass");
                    object = object.getJSONObject("User");
                    JSONArray array = object.getJSONArray("Result");
//                    String lastname = " ";
//                    for (int i = 0; i < array.length(); i++) {
//                        object = array.getJSONObject(i);
//                        if (!object.getString("UnitName").equals(lastname)) {
//                            s[0].add(object.getString("UnitName"));
//                            lastname = object.getString("UnitName");
//                            for (int j = 1; j < s.length; j++) {
//                                s[j].add("");
//                            }
//                        } else {
//                            for (int j = 1; j < s.length; j++) {
//                                if (s[j].get(0).equals(object.getString("PeriodName"))) {
//                                    s[j].set(s[j].size() - 1, String.valueOf(object.getInt("Value5")));
//                                }
//                            }
//                        }
//                    }
                } catch (Exception e) {
                }
            }
        }.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

}
