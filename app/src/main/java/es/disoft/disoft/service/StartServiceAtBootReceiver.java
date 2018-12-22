package es.disoft.disoft.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.disoft.disoft.user.User;

public class StartServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && User.logged(context)) {
            StartService.setAlarmManager(context, ChatService.class);
        }
    }
}