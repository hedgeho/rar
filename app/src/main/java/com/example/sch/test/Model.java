package com.example.sch.test;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Model {
    @SerializedName("message")
    @Expose
    Message message;

    @NonNull
    @Override
    public String toString() {
        return message.senderFio;
    }
}
class Message {
    @SerializedName("senderFio")
    @Expose
    String senderFio;
}
