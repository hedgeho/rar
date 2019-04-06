package com.example.sch;

public class TheSingleton {
    private static final TheSingleton ourInstance = new TheSingleton();
    private String COOKIE, ROUTE;
    private int USER_ID;

    static TheSingleton getInstance() {
        return ourInstance;
    }

    private TheSingleton() {
    }

    public String getCOOKIE() {
        return COOKIE;
    }

    public void setCOOKIE(String COOKIE) {
        this.COOKIE = COOKIE;
    }

    public int getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(int USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getROUTE() {
        return ROUTE;
    }

    public void setROUTE(String ROUTE) {
        this.ROUTE = ROUTE;
    }
}
