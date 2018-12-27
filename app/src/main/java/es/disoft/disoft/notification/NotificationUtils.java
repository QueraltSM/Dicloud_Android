package es.disoft.disoft.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import es.disoft.disoft.R;

public class NotificationUtils extends ContextWrapper {
    //use constant ID for notification used as group summary
    private final int SUMMARY_ID    = 0;
    private final String GROUP_NAME = getString(R.string.channel_name);
    private final String CHANNEL_ID = getString(R.string.channel_ID);
    private final String TAG        = GROUP_NAME;
    private NotificationManager mManager;
    private String title;
    private String text;

    private int singleIcon = R.drawable.ic_menu_gallery;
    private int groupIcon  = R.drawable.ic_menu_slideshow;

    public NotificationUtils(Context base) {
        super(base);
        createNotificationChannel();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name           = getString(R.string.channel_name);
            int importance              = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            getManager().createNotificationChannel(channel);
        }
    }

    private NotificationManager getManager() {
        if (mManager == null)
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return mManager;
    }

    private Notification getAndroidChannelNotification() {
        String CHANNEL_ID = getString(R.string.channel_ID);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(singleIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setGroup(GROUP_NAME)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .build();
    }


    public void createNotification(String title, String text) {
        this.title = title;
        this.text  = text;
    }


    public void show() {
        if (otherNotificationExists()) showGroupNotification();
        else showSingleNotification();
    }


    private void showSingleNotification() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 1000);
        NotificationManagerCompat.from(this).notify(TAG, randomNum, getAndroidChannelNotification());
    }


    private void showGroupNotification() {
        showSingleNotification();
        NotificationManagerCompat.from(this).notify(TAG, SUMMARY_ID, getSummaryNotification());
    }


    private Notification getSummaryNotification() {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            int notificationsCount = 2;
            ArrayList<StatusBarNotification> myOtherNotifications = getMyOtherNotifications();
            boolean isGroup = false;
            for (StatusBarNotification notification : myOtherNotifications)
                if (notification.getId() == 0) isGroup = true;

            if (isGroup) {
                String s = myOtherNotifications.get(0).getNotification().extras.getString(Notification.EXTRA_TEXT);
                notificationsCount = Integer.parseInt(s.split(" ")[0]) + 1;
            }

            text = WordUtils.capitalize(getString(R.string.new_messages), ".".toCharArray());
            text = notificationsCount + " " + text;
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(groupIcon)
                .setGroup(GROUP_NAME)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroupSummary(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .build();
    }


    private boolean otherNotificationExists() {
        return !getMyOtherNotifications().isEmpty();
    }


    private ArrayList<StatusBarNotification> getMyOtherNotifications() {
        ArrayList<StatusBarNotification> myOtherNotifications = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();

            for (StatusBarNotification notification : barNotifications) {
                Log.i("run", "getMyOtherNotifications: " + notification.getId());
                Log.i("run", "getMyOtherNotifications: " + notification.getTag());
                if (notification.getTag().equals(TAG))
                    myOtherNotifications.add(notification);
            }
        }
        return myOtherNotifications;
    }
}
