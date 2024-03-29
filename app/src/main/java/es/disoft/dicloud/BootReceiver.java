package es.disoft.dicloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Message;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.user.NewsMessages;
import es.disoft.dicloud.user.ChatMessages;
import es.disoft.dicloud.workers.ChatWorker;
import es.disoft.dicloud.workers.NewsWorker;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {

        new Thread() {
            public void run() {
                User.currentUser = DisoftRoomDatabase.getDatabase(context).userDao().getUserLoggedIn();
                if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && User.currentUser != null) {
                    NewsMessages.update(context);
                    ChatMessages.update(context);
                    List<Message.EssentialInfo> messages = DisoftRoomDatabase.getDatabase(context).messageDao().getAllMessagesEssentialInfo();
                    ChatWorker.notificateMessages(context, messages);
                    NewsWorker.notificateMessages(context, messages);
                }
            }
        }.start();
    }
}