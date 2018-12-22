package es.disoft.disoft.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class StartService {

    private static final int INTERVAL_IN_MILLIS = 60 *1000;

    public static void setAlarmManager(Context context, Class<?> cls) {
        Intent mIntent        = new Intent(context, cls);
        PendingIntent pIntent = PendingIntent.getService(context, 0, mIntent, 0);
        AlarmManager alarm    = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), INTERVAL_IN_MILLIS, pIntent);
    }
}