package es.disoft.disoft.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.Map;

import es.disoft.disoft.notification.NotificationUtils;
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

                        NotificationUtils mNotififacionUtils = new NotificationUtils(context);

                if (User.isLogged(context)) {
                    Boolean updated = Messages.update(context);
                    if (updated) {


//                        int randomNum = ThreadLocalRandom.current().nextInt(0, 100);
//
//                        NotificationCompat.Builder nb = mNotififacionUtils.getAndroidChannelNotification("Titulo", "Texto");
//                        NotificationManagerCompat.from(context).notify(randomNum, nb.build());

//                        mNotififacionUtils.createNotification("Titulo", "Texto");
//                        mNotififacionUtils.show();

                        Map<?,?> messages = Messages.get(context);
                        Log.i("mensajes", "run: " + messages.toString());
                    }
                }


                for (int i = 0; i < 55; i++) {
                    try {
                        mNotififacionUtils.createNotification(12, "Titulo", "Texto");
                        if (i == 2) mNotififacionUtils.show();
                        if (i == 6) mNotififacionUtils.show();
                        if (i == 9) mNotififacionUtils.show();
                        if (i == 12) mNotififacionUtils.show();
                        if (i == 16) mNotififacionUtils.show();
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
