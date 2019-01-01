package es.disoft.disoft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.User;
import es.disoft.disoft.user.Messages;
import es.disoft.disoft.workers.ChatWorker;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {

        new Thread() {
            public void run() {
                User.currentUser = DisoftRoomDatabase.getDatabase(context).userDao().getUserLoggedIn();

                if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && User.currentUser != null) {
                    Messages.update(context);
                    List<Message.EssentialInfo> messages = DisoftRoomDatabase.getDatabase(context).messageDao().getAllMessagesEssentialInfo();
                    ChatWorker.notificateMessages(context, messages);
                }
            }
        }.start();
    }
}