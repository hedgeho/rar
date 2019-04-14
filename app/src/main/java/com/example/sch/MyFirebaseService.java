package com.example.sch;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.sch.LoginActivity.log;


public class MyFirebaseService extends FirebaseMessagingService {

    public MyFirebaseService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log("message received");
        log("from: " + remoteMessage.getFrom()); 
        log("msg id: " + remoteMessage.getMessageId());
        log("data: " + remoteMessage.toIntent().getStringExtra("key"));
        if(remoteMessage.getNotification() != null) {
            log(remoteMessage.getNotification().getTitle());
            log(remoteMessage.getNotification().getBody());
        }
        //Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getTitle() + ":\n" + remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeletedMessages() {
        log("onDeletedMessage");
    }
}
