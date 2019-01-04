package es.disoft.disoft.workers;

import android.content.Context;
import android.support.annotation.NonNull;
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
            return Result.success();
        } catch (Exception e) {
            Log.e("WORKER", "doWork: ", e);
            return Result.retry();
        }
    }

    public static class checkMessagesEvery5sc {

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
                            // TODO cambiar tiempo a 15 segundos?
                            Thread.sleep(5 * 1000);
                            checkMessages(context);
//                            test();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
        }

        private static void test() {
            NotificationUtils mNotififacionUtils = new NotificationUtils(context);
            mNotififacionUtils.createNotification(999, "superTaitol", "megatexta");
            mNotififacionUtils.show();
        }
    }

    private static void checkMessages(Context context) {
        Log.i("vivo", "checkMessages: ");
        if (User.currentUser != null)
            if (Messages.update(context)) notificateMessages(context, Messages.getUpdated());
    }

    public static void notificateMessages(Context context, List<?> messages) {
        NotificationUtils mNotififacionUtils = new NotificationUtils(context);

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

            String text = messagesCount > 1 ? context.getString(R.string.new_messages_from) : context.getString(R.string.new_message_from);
            String title = User.currentUser.getDbAlias();
            text = messagesCount + " " + text + " " + from;

            mNotififacionUtils.createNotification(id, title, text);
            mNotififacionUtils.show();
        }

        ArrayList<Message> deletedMessages = Messages.getDeleted();
        if (deletedMessages != null) {
            for (Message message : deletedMessages) {
                Log.w("mensajeee", "clear: " + message.toString());
                mNotififacionUtils.clear(message.getFrom_id());
            }
        }
    }
}
