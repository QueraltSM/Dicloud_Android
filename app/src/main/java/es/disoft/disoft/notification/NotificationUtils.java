package es.disoft.disoft.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

import es.disoft.disoft.R;
import es.disoft.disoft.user.WebViewActivity;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.User;

public class NotificationUtils extends ContextWrapper {

    private final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private final String NOTIFICATION_ID   = "NOTIFICATION_ID";

    //use constant ID for notification used as group summary
    private final int SUMMARY_ID      = 0;
    private final String CHANNEL_NAME = getString(R.string.channel_name);
    private final String CHANNEL_ID   = getString(R.string.channel_ID);
    private final String TAG          = getString(R.string.app_name);
    private final int COLOR           = Color.BLUE;
    private int IMPORTANCE;
    private boolean create = true;
    private NotificationManager mManager;

    private String title;
    private String text;
    private int    id;

    private int singleIcon = R.drawable.ic_menu_gallery;
    private int groupIcon  = R.drawable.ic_menu_slideshow;

    public NotificationUtils(Context base) {
        super(base);
        setImportance(true);
        createNotificationChannel();
    }

    private void setImportance(boolean importance) {
        create = importance;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            IMPORTANCE = create ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT;
        else
            IMPORTANCE = create ? NotificationManager.IMPORTANCE_MAX : NotificationManager.IMPORTANCE_MIN;
    }

    @SuppressLint("WrongConstant")
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE);
            channel.enableLights(true);
            channel.setLightColor(COLOR);
            channel.enableVibration(true);
            getManager().createNotificationChannel(channel);
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
    private NotificationManager getManager() {
        if (mManager == null)
//            mManager = getSystemService(NotificationManager.class);
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return mManager;
    }

    public void createNotification(int id, String title, String text) {
        this.id    = id;
        this.title = title;
        this.text  = text;
    }

    public void show() {
//        messages = DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().getAllMessagesEssentialInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            showCuteNotifications();
        else
            showPrehistoricAndUglyNotifications();
    }

    public void clear(int id) {
        DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().delete(id);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            getManager().cancel(TAG, id);
        } else {
            clearOldNotifications();
        }
    }

    private void clearOldNotifications() {
        setImportance(false);
        showPrehistoricAndUglyNotifications();
    }

    private void showCuteNotifications() {
        singleNotification();
        NotificationManagerCompat.from(this).notify(TAG, SUMMARY_ID, summaryNotificationNewStyle());
    }

    private void showPrehistoricAndUglyNotifications() {
        List<Message.EssentialInfo> messages = DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().getAllMessagesEssentialInfo();

        if (messages.size() > 1) {
            singleNotification();
            NotificationManagerCompat.from(this).notify(TAG, SUMMARY_ID, summaryNotificationOldStyle(messages));
        } else if (messages.size() == 1){
            String txt = messages.get(0).messages_count + " " + getString(R.string.new_message) + " " + messages.get(0).from;
            title = ObjectUtils.firstNonNull(title, User.currentUser.getDbAlias());
            text  = ObjectUtils.firstNonNull(text,  txt);
            id    = ObjectUtils.firstNonNull(id,    messages.get(0).from_id);
            getManager().cancelAll();
            singleNotificationOld();
        } else {
            getManager().cancelAll();
        }
    }

    private void singleNotification() {
        NotificationManagerCompat.from(this).notify(TAG, id, notificationNewStyle());
    }

    private void singleNotificationOld() {
        NotificationManagerCompat.from(this).notify(TAG, id, notificationOldStyle());
    }

    private Notification notificationNewStyle() {
        return standardNotification(title, singleIcon)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .build();
    }

    private Notification notificationOldStyle() {
        return standardNotification(title, singleIcon)
                .build();
    }

    private Notification summaryNotificationNewStyle() {
        return standardNotification(getString(R.string.app_name), groupIcon)
                .setGroupSummary(true)
                .build();
    }

    private Notification summaryNotificationOldStyle(List<Message.EssentialInfo> messages) {
        int messagesCount = 0;
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(messages.size() + " " + getString(R.string.new_chats));
        for (int i = 0; i < messages.size(); i++) {
            if (i == 5) break;
            Message.EssentialInfo message = messages.get(i);

            messagesCount += message.messages_count;
            String mText = message.messages_count > 1 ? getString(R.string.new_messages_from) : getString(R.string.new_message_from);
            inboxStyle.addLine(message.messages_count + " " + mText + " " + message.from);
        }

        return standardNotification(getString(R.string.app_name), groupIcon)
                .setGroupSummary(true)
                .setContentText(messagesCount + " " + getString(R.string.new_messages))
                .setStyle(inboxStyle).build();
    }

    private NotificationCompat.Builder standardNotification(String title, int icon) {

        String type = DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().count() > 1 ? "group" : "notification";

        Intent resultIntent = new Intent(this, WebViewActivity.class);
        resultIntent.putExtra(NOTIFICATION_ID, id);
        resultIntent.putExtra(NOTIFICATION_TYPE, type);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(IMPORTANCE)
                .setGroup(CHANNEL_NAME)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
//                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            if (create) notification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        else
            if (create) notification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        return notification;
    }

    public void clearAll() {
        getManager().cancelAll();
    }
}
