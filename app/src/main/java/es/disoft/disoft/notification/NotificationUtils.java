package es.disoft.disoft.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

import es.disoft.disoft.R;

public class NotificationUtils extends ContextWrapper {
    //use constant ID for notification used as group summary
    private final int SUMMARY_ID      = 0;
    private final String CHANNEL_NAME = getString(R.string.channel_name);
    private final String CHANNEL_ID   = getString(R.string.channel_ID);
    private final String TAG          = getString(R.string.app_name);
    private final int COLOR           = Color.BLUE;
    private  int IMPORTANCE;
    private NotificationManager mManager;

    private String title;
    private String text;
    private int    id;

    private int singleIcon = R.drawable.ic_menu_gallery;
    private int groupIcon  = R.drawable.ic_menu_slideshow;

    public NotificationUtils(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            IMPORTANCE = NotificationManager.IMPORTANCE_MAX;
        createNotificationChannel();
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    private NotificationManager getManager() {
        if (mManager == null)
            mManager = getSystemService(NotificationManager.class);
        return mManager;
    }


    public void createNotification(int id, String title, String text) {
        this.id    = id;
        this.title = title;
        this.text  = text;
    }


    public void show() {
        showGroupNotification();
    }


    private void showSingleNotification() {
        NotificationManagerCompat.from(this).notify(TAG, id, getAndroidChannelNotification());
    }


    private void showGroupNotification() {
        showSingleNotification();
        NotificationManagerCompat.from(this).notify(TAG, SUMMARY_ID, getSummaryNotification());
    }


    private Notification getAndroidChannelNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(singleIcon)
                .setGroup(CHANNEL_NAME)
                .setPriority(IMPORTANCE)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .build();
    }


    private Notification getSummaryNotification() {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
//           TODO recoger las notificaciones de la base de datos y mostrarlas todas
            int notificationsCount = 1;
            ArrayList<StatusBarNotification> myOtherNotifications = getMyOtherNotifications();
            boolean isGroup = false;
            for (StatusBarNotification notification : myOtherNotifications)
                if (notification.getId() == 0) isGroup = true;

            if (isGroup) {
                String s = myOtherNotifications.get(0).getNotification().extras.getString(Notification.EXTRA_TEXT);
                    notificationsCount = s != null ? Integer.parseInt(s.split(" ")[0]) + 1 : 1;
            }

            String mText = notificationsCount == 1 ? getString(R.string.new_chat) : getString(R.string.new_chats);
            //noinspection deprecation
            text = WordUtils.capitalize(mText, ".".toCharArray());
            text = notificationsCount + " " + text;
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(groupIcon)
                .setGroup(CHANNEL_NAME)
                .setPriority(IMPORTANCE)
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
