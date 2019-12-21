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
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Message;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.notification.NotificationUtils;
import es.disoft.dicloud.user.ChatMessages;
import es.disoft.dicloud.user.NewsMessages;

public class ChatWorker extends Worker {

    public ChatWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        checkMessagesEvery5sc.context = context;
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

    public static void runChatWork(String UID, int repeatInterval) {
        if (repeatInterval != -1) {
            PeriodicWorkRequest.Builder logCheckBuilder =
                    new PeriodicWorkRequest.Builder(
                            ChatWorker.class,
                            repeatInterval,
                            TimeUnit.MINUTES);
            PeriodicWorkRequest chatWork = logCheckBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                    UID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    chatWork);
        } else {
            cancelWork(UID);
        }
    }

    public static void cancelWork(String UID) {
            WorkManager.getInstance().cancelAllWorkByTag(UID);
    }

    public static class checkMessagesEvery5sc {

        @SuppressLint("StaticFieldLeak")
        public static Context context;
        private static Thread thread;

        public void setContext(Context cnt) {
            context = cnt;
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
                            checkMessages(context);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
        }
    }

    private static void checkMessages(Context context) {
        Log.i("vivo", "checkMessages: ");
        if (User.currentUser != null)
            if (ChatMessages.update(context)) notificateMessages(context, ChatMessages.getUpdated());
    }

    public static void notificateMessages(Context context, List<?> messages) {
        NotificationUtils mNotififacionUtils = new NotificationUtils(context);
        for (Object message : messages) {
            int id;
            String from;
            if (message instanceof Message) {
                from          = ((Message) message).getFrom();
                id            = ((Message) message).getFrom_id();
            } else {
                from          = ((Message.EssentialInfo) message).getFrom();
                id            = ((Message.EssentialInfo) message).getFrom_id();
            }
            String title = User.currentUser.getDbAlias();
            String text = "Tienes un chat pendiente con " + from;
            mNotififacionUtils.createNotification(id, title, text);
            mNotififacionUtils.show();
            NewsMessages.setMessageFromNews(false);
        }

        ArrayList<Message> deletedMessages = ChatMessages.getDeleted();
        if (deletedMessages != null) {
            for (Message message : deletedMessages)
                mNotififacionUtils.clear(message.getFrom_id());
        }
    }
}
