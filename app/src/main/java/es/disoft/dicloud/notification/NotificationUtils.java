package es.disoft.dicloud.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import es.disoft.dicloud.R;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Message;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.user.WebViewActivity;

public class NotificationUtils extends ContextWrapper {

    private final String NOTIFICATION_GROUP_TYPE = "group";
    private final String NOTIFICATION_NOTIFICATION_TYPE = "notification";

    private final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private final String NOTIFICATION_ID = "NOTIFICATION_ID";

    //use constant ID for notification used as group summary
    private final int SUMMARY_ID = 0;
    private final String CHANNEL_NAME = getString(R.string.channel_name);
    private String CHANNEL_ID = getString(R.string.channel_ID);
    private final String TAG = getString(R.string.app_name);
    private int COLOR;
    private int IMPORTANCE;
    private NotificationManager mManager;

    private String title;
    private String text;
    private int id;

    private int icon = R.drawable.ic_chat;

    public NotificationUtils(Context base) {
        super(base);

        if (notificationEnabled()) {
            COLOR = translateColor(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(this).getString("notification_led", "Red")));

            setImportance(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel();
        }
    }

    private void setImportance(boolean notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            IMPORTANCE = notification ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT;
        else
            IMPORTANCE = notification ? NotificationManager.IMPORTANCE_MAX : NotificationManager.IMPORTANCE_MIN;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CHANNEL_ID = checkingSharedNotif();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE);
        channel.enableLights(true);
        channel.setLightColor(getPreferenceColor());
        channel.setSound(getPreferenceSound(), audioAttributes);
        channel.setVibrationPattern(getPreferenceVibration());
        getManager().createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String checkingSharedNotif() {
        String TAG = "ledDeColores";
        SharedPreferences prefs = getSharedPreferences("es.disoft.dicloud_preferences", MODE_PRIVATE);
        Integer prefsCheck = prefs.getInt("idChannel", 0);
        Log.i(TAG, "checkingSharedNotif: "  + prefsCheck);

        if (prefsCheck != 0) {
            NotificationChannel notificationChannel = getManager().getNotificationChannel(getString(R.string.channel_ID) + prefsCheck);

            if (notificationChannel != null) {

                int preferenceColor      = getPreferenceColor();
                String preferenceSound   = ObjectUtils.firstNonNull(getPreferenceSound(), "").toString();
                long[] preferenceVibrate = getPreferenceVibration();
                Log.i(TAG, "preferences v: " + ArrayUtils.toString(preferenceVibrate));

                int channelColor      = notificationChannel.getLightColor();
                String channelSound   = ObjectUtils.firstNonNull(notificationChannel.getSound(), "").toString();
                long[] channelVibrate = notificationChannel.getVibrationPattern();
                Log.i(TAG, "channel v: " + ArrayUtils.toString(channelVibrate));

                if (preferenceColor != channelColor
                        || !preferenceSound.equals(channelSound)
                        || !Arrays.equals(preferenceVibrate, channelVibrate)) {

                    Log.i(TAG, "checkingSharedNotif:  nueva notif");
                    getManager().deleteNotificationChannel(CHANNEL_ID + prefsCheck);
                    prefsCheck += 1;
                }
            }

        }else{
            prefsCheck = 1;
        }
        prefs.edit().putInt("idChannel", prefsCheck).apply();
        return CHANNEL_ID + prefsCheck;
    }

    private boolean notificationEnabled() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return settings.getBoolean("notifications_new_message", true);
    }

    private Uri getPreferenceSound() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tone = settings.getString("notifications_new_message_ringtone", getString(R.string.pref_ringtone));
        Uri sound = null;
        if (!Objects.requireNonNull(tone).isEmpty()) sound = Uri.parse(tone);
        return sound;
    }

    private long[] getPreferenceVibration() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean vibration = settings.getBoolean("notifications_new_message_vibrate", true);
        // 0   : Start without a delay
        // 400 : Vibrate for 400 milliseconds
        // 200 : Pause for 200 milliseconds
        // 400 : Vibrate for 400 milliseconds
        return vibration ? new long[]{0, 400, 200, 400} : new long[0];
    }

    private int getPreferenceColor() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String color = settings.getString("notification_led", "");
        return translateColor(Objects.requireNonNull(color));
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
                return Color.GREEN;

            case "Red":
                return Color.RED;

            default:
                return Color.WHITE;
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
        if (notificationEnabled()) {
    //        messages = DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().getAllMessagesEssentialInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                showCuteNotifications();
            else
                showPrehistoricAndUglyNotifications();
        }
    }

    public void clear(int id) {
        DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().delete(id);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            clearNewNotifications(id);
        else
            clearOldNotifications();
    }

    private void clearNewNotifications(int id) {
        List<Message.EssentialInfo> messages = DisoftRoomDatabase.getDatabase(getApplicationContext()).messageDao().getAllMessagesEssentialInfo();
        if (messages.size() >= 1)
            getManager().cancel(TAG, id);
        else
            getManager().cancelAll();

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
            text  = ObjectUtils.firstNonNull(text, txt);
            id    = ObjectUtils.firstNonNull(id, messages.get(0).from_id);
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
        return standardNotification(title, icon)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .build();
    }

    private Notification notificationOldStyle() {

        return standardNotification(title, icon)
                .build();
    }

    private Notification summaryNotificationNewStyle() {

        return standardNotification(getString(R.string.app_name), icon)
                .setContentIntent(getPendingIntent(NOTIFICATION_GROUP_TYPE))
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

        return standardNotification(getString(R.string.app_name), icon)
                .setGroupSummary(true)
                .setContentText(messagesCount + " " + getString(R.string.new_messages))
                .setStyle(inboxStyle)
                .setContentIntent(getPendingIntent(NOTIFICATION_GROUP_TYPE))
                .build();
    }

    private NotificationCompat.Builder standardNotification(String title, int icon) {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(IMPORTANCE)
                .setGroup(CHANNEL_NAME)
                .setLights(COLOR, 2000, 3000)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent(NOTIFICATION_NOTIFICATION_TYPE));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                notification
                        .setSound(getPreferenceSound())
                        .setVibrate(getPreferenceVibration());

        return notification;
    }

    private PendingIntent getPendingIntent(String type) {
        Intent resultIntent = new Intent(this, WebViewActivity.class);
        resultIntent.putExtra(NOTIFICATION_ID, id);
        resultIntent.putExtra(NOTIFICATION_TYPE, type);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        return PendingIntent.getActivity(this, uniqueInt, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void clearAll() {
        getManager().cancelAll();
    }
}
