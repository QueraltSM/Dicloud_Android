package es.disoft.disoft.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

import es.disoft.disoft.R;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.User;
import es.disoft.disoft.user.WebViewActivity;

public class NotificationUtils extends ContextWrapper {

    private final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private final String NOTIFICATION_ID = "NOTIFICATION_ID";

    //use constant ID for notification used as group summary
    private final int SUMMARY_ID = 0;
    private final String CHANNEL_NAME = getString(R.string.channel_name);
    private String CHANNEL_ID = getString(R.string.channel_ID);
    private final String TAG = getString(R.string.app_name);
    private int COLOR;
    private int IMPORTANCE;
    private boolean create = true;
    private NotificationManager mManager;

    private String title;
    private String text;
    private int id;

    private int singleIcon = R.drawable.ic_chat;
    private int groupIcon = R.drawable.ic_chat;

    public NotificationUtils(Context base) {
        super(base);

        COLOR = translateColor(PreferenceManager.getDefaultSharedPreferences(this).getString("notification_led", ""));


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
            CHANNEL_ID = checkingSharedNotif();

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE);
            channel.enableLights(true);
            channel.setLightColor(getColor());
            channel.enableVibration(true);
            getManager().createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String checkingSharedNotif() {
        String TAG = "ledDeColores";
        SharedPreferences prefs = getSharedPreferences("es.disoft.disoft_preferences", MODE_PRIVATE);
        Integer prefsCheck = prefs.getInt("idChannel", 0);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i(TAG, "checkingSharedNotif: "  + prefsCheck);

        if (prefsCheck != 0) {
            NotificationChannel notificationChannel = getManager().getNotificationChannel(getString(R.string.channel_ID) + prefsCheck);
            if (translateColor(settings.getString("notification_led", "")) != notificationChannel.getLightColor()) {

                Log.i(TAG, "checkingSharedNotif:  nueva notif");
                getManager().deleteNotificationChannel(CHANNEL_ID + prefsCheck);
                prefsCheck += 1;

            }

        }else{
            prefsCheck = 1;
        }
        prefs.edit().putInt("idChannel", prefsCheck).apply();
        return CHANNEL_ID + prefsCheck;
    }

    private int translateColor(String notification_led) {
        String TAG = "ledDeColores";
        Log.i(TAG, "translateColor: " + notification_led);
        switch (notification_led) {

            case "Blue":
                return Color.BLUE;

            case "Yellow":
                return Color.YELLOW;

            case "Green":
                Log.i(TAG, "translateColor: estoy dentro de verde");
                return Color.GREEN;

            case "Red":
                return Color.RED;

            default:
                return Color.WHITE;
        }

    }

    private int getColor() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String color1 = settings.getString("notification_led", "");
        return translateColor(color1);
    }

    //    @RequiresApi(api = Build.VERSION_CODES.M)
    private NotificationManager getManager() {
        if (mManager == null)
//            mManager = getSystemService(NotificationManager.class);
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return mManager;
    }

    public void createNotification(int id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
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
        } else if (messages.size() == 1) {
            String txt = messages.get(0).messages_count + " " + getString(R.string.new_message) + " " + messages.get(0).from;
            title = ObjectUtils.firstNonNull(title, User.currentUser.getDbAlias());
            text = ObjectUtils.firstNonNull(text, txt);
            id = ObjectUtils.firstNonNull(id, messages.get(0).from_id);
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
        Log.d(TAG, "standardNotification: type: " + type);

        Intent resultIntent = new Intent(this, WebViewActivity.class);
        resultIntent.putExtra(NOTIFICATION_ID, id);
        resultIntent.putExtra(NOTIFICATION_TYPE, type);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(IMPORTANCE)
                .setGroup(CHANNEL_NAME)
                .setLights(COLOR, 1, 1)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            if (create) notification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            else if (create) notification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        return notification;
    }

    public void clearAll() {
        getManager().cancelAll();
    }
}
