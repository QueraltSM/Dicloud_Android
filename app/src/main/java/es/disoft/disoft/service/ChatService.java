package es.disoft.disoft.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import java.util.Map;

import es.disoft.disoft.user.User;

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

                Context context = getApplicationContext();

                if (User.isLogged(context)) {
                    Boolean updated = Messages.update(context);
                    if (updated) {
                        Map<?,?> messages = Messages.get(context);
                        Log.i("mensajes", "run: " + messages.toString());
                    }
                }


                for (int i = 0; i < 55; i++) {
                    try {
                        String TAG = "servicio_";
                        Thread.sleep(1000);
                        Log.i(TAG, "run: " + i);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }
}
