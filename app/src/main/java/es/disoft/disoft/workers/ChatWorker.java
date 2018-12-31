package es.disoft.disoft.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.User;
import es.disoft.disoft.notification.NotificationUtils;
import es.disoft.disoft.user.Messages;

import static es.disoft.disoft.workers.ChatWorker.checkMessagesEvery30sc.checkMessages;

public class ChatWorker extends Worker {

    public ChatWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        checkMessagesEvery30sc.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();
            checkMessages();
            return Result.success();
        } catch (Exception e) {
            Log.e("WORKER", "doWork: ", e);
            return Result.retry();
        }
    }






    public static class checkMessagesEvery30sc {

        public static Context context;
        private static Thread thread;

        public void setContext(Context context) {
            this.context = context;
        }

        public static void start() {
            createThread();
            thread.start();
        }

        public static void stop() {
            thread.interrupt();
        }

        private static void createThread() {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!thread.isInterrupted()) {
                        try {
                            // TODO cambiar tiempo a 15 segundos!!!
                            Thread.sleep(5 * 1000);
                            checkMessages();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
        }


        public static void checkMessages() {
            Log.i("vivo", "checkMessages: ");
            if (User.currentUser != null) {
                if (Messages.update(context)) {

                    NotificationUtils mNotififacionUtils = new NotificationUtils(context);
                    for (Message message : Messages.getUpdated()) {
                        int messagesCount = message.getMessages_count();
                        String text       = messagesCount > 1 ? context.getString(R.string.new_messages_from) : context.getString(R.string.new_message_from);

                        int id       = message.getFrom_id();
                        String from  = message.getFrom();
                        String title = User.currentUser.getDbAlias();
                        text         = messagesCount + " " + text + " " + from;

                        mNotififacionUtils.createNotification(id, title, text);
                        mNotififacionUtils.show();
                    }

                    for (Message message : Messages.getDeleted()) {
                        Log.w("mensajeee", "clear: " + message.toString());
                        mNotififacionUtils.clear(message.getFrom_id());
                    }
                }
            }
        }
    }
}
