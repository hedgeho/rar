package com.example.sch;

import android.content.BroadcastReceiver;

class TheSingleton {
    private static final TheSingleton ourInstance = new TheSingleton();
    private String COOKIE, ROUTE;
    private int USER_ID, PERSON_ID;
    private BroadcastReceiver receiver;

    static TheSingleton getInstance() {
        return ourInstance;
    }

    private TheSingleton() {}

    String getCOOKIE() {
        return COOKIE;
    }

    void setCOOKIE(String COOKIE) {
        this.COOKIE = COOKIE;
    }

    int getUSER_ID() {
        return USER_ID;
    }

    void setUSER_ID(int USER_ID) {
        this.USER_ID = USER_ID;
    }

    String getROUTE() {
        return ROUTE;
    }

    void setROUTE(String ROUTE) {
        this.ROUTE = ROUTE;
    }

    int getPERSON_ID() {
        return PERSON_ID;
    }

    void setPERSON_ID(int PERSON_ID) {
        this.PERSON_ID = PERSON_ID;
    }

    BroadcastReceiver getReceiver() {
        return receiver;
    }

    void setReceiver(BroadcastReceiver receiver) {
        this.receiver = receiver;
    }
}
