package es.disoft.disoft.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import es.disoft.disoft.R;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.User;
import es.disoft.disoft.notification.NotificationUtils;

public class ChatService extends IntentService {

    public ChatService() {
        super("ChatService");
    }

    public ChatService(String name) {
        super(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String TAG = "servicio_";
        if (intent == null) {
            Log.i(TAG, "onHandleIntent: ");
        }


        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.wtf("START", "run: ");

                doSomething();

                for (int i = 0; i < 55; i++) {
                    try {
                        if (i == 5)  doSomething();
                        if (i == 10) doSomething();
                        if (i == 15) doSomething();
                        if (i == 20) doSomething();
                        if (i == 25) doSomething();
                        if (i == 30) doSomething();
                        if (i == 35) doSomething();
                        if (i == 40) doSomething();
                        if (i == 45) doSomething();
                        if (i == 50) doSomething();
                        if (i == 54) doSomething();
                        String TAG = "servicio_";
                        Thread.sleep(1000);
                        Log.i(TAG, "run: " + i);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    private void doSomething() {

        Context context = getApplicationContext();

        if (User.currentUser != null) {
            Boolean updated = Messages.update(context);
            if (updated) {
                ArrayList<Message> messages = Messages.getUpdated();
                for (Message message : messages) {

                    int messagesCount = message.getMessages_count();
                    String text       = messagesCount > 1 ? getString(R.string.new_messages_from) : getString(R.string.new_message_from);

                    int id       = message.getFrom_id();
                    String from  = message.getFrom();
                    String title = getString(R.string.app_name);
                    text         = messagesCount + " " + text + " " + from;

                    NotificationUtils mNotififacionUtils = new NotificationUtils(context);
                    mNotififacionUtils.createNotification(id, title, text);
                    mNotififacionUtils.show();
                }
                Log.i("mensajes", "run: " + messages.toString());
            }
        }
    }
}
