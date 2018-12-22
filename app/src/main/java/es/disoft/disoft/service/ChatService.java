package es.disoft.disoft.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

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
