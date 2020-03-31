package es.disoft.dicloud.workers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import es.disoft.dicloud.R;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Message;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.notification.NotificationUtils;
import es.disoft.dicloud.user.NewsMessages;

public class NewsWorker extends Worker {

    public NewsWorker(
            @NonNull Context news_context,
            @NonNull WorkerParameters workerParams) {
        super(news_context, workerParams);
        checkMessagesEvery5sc.news_context = news_context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();
            checkMessages(getApplicationContext());
            Log.wtf("WORKER", "doWork: HE ENTRADO SEEEEEEEEEEEEEEEH");
            return Result.success();
        } catch (Exception e) {
            Log.wtf("WORKER", "doWork: ", e);
            return Result.retry();
        }
    }

    public static void runMessagesWork(String UID, int repeatInterval) {
        if (repeatInterval != -1) {
            PeriodicWorkRequest.Builder logCheckBuilder =
                    new PeriodicWorkRequest.Builder(
                            NewsWorker.class,
                            repeatInterval,
                            TimeUnit.MINUTES);
            PeriodicWorkRequest messagesWork = logCheckBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                    UID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    messagesWork);
        } else{
            cancelWork(UID);
        }
    }

    public static void cancelWork(String UID) {
        WorkManager.getInstance().cancelAllWorkByTag(UID);
    }

    public static class checkMessagesEvery5sc {

        @SuppressLint("StaticFieldLeak")
        public static Context news_context;
        private static Thread thread;

        public void setContext(Context cnt) {
            news_context = cnt;
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
                            Thread.sleep(5 * 1000);
                            checkMessages(news_context);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
        }
    }

    private static void checkMessages(Context news_context) {
        Log.i("vivo - NewsWorker", "checkMessages: ");
        if (User.currentUser != null)
            if (NewsMessages.update(news_context)) notificateMessages(news_context, NewsMessages.getUpdated());
    }

    public static void notificateMessages(Context news_context, List<?> messages) {
        System.out.println("entro en notificar mensaje");
        NotificationUtils mNotififacionUtils = new NotificationUtils(news_context);
        for (Object message : messages) {
            int messagesCount, id;
            String from;
            if (message instanceof Message) {
                messagesCount = ((Message) message).getMessages_count();
                from          = ((Message) message).getFrom();
                id            = ((Message) message).getFrom_id();
            } else {
                messagesCount = ((Message.EssentialInfo) message).getMessages_count();
                from          = ((Message.EssentialInfo) message).getFrom();
                id            = ((Message.EssentialInfo) message).getFrom_id();
            }

            String text = messagesCount > 1 ? news_context.getString(R.string.new_messages_from) : news_context.getString(R.string.new_message_from);
            text = messagesCount + " " + text + " " + from;
            String title = User.currentUser.getDbAlias();
            mNotififacionUtils.createNotification(id, title, text);
            mNotififacionUtils.show();
        }
        ArrayList<Message> deletedMessages = NewsMessages.getDeleted();
        if (deletedMessages != null) {
            for (Message message : deletedMessages)
                mNotififacionUtils.clear(message.getFrom_id());
        }
    }
}
