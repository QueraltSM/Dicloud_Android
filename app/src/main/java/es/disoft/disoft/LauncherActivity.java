package es.disoft.disoft;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.User;
import es.disoft.disoft.user.LoginActivity;
import es.disoft.disoft.user.WebViewActivity;
import es.disoft.disoft.workers.ChatWorker;

public class LauncherActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("servicio", "App starts");
        context = getApplicationContext();

        setContentView(R.layout.activity_launcher);
        runChatWork(context,this);
        login();
    }

    public static void runChatWork(Context context, Activity activ) {

        int syncFrequency = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sync_frequency", "15"));
        if (syncFrequency != -1) {
            PeriodicWorkRequest.Builder logCheckBuilder =
                    new PeriodicWorkRequest.Builder(ChatWorker.class, syncFrequency, TimeUnit.MINUTES);
            PeriodicWorkRequest chatWork = logCheckBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(activ.getString(R.string.app_name), ExistingPeriodicWorkPolicy.REPLACE, chatWork);
//            WorkManager.getInstance().enqueue(chatWork);
        }else{
            WorkManager.getInstance().cancelAllWorkByTag(activ.getString(R.string.app_name));
        }
    }

    private void login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (User.currentUser == null)
                    User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();

                Intent activityIntent;
                activityIntent = User.currentUser != null ?
                        new Intent(context, WebViewActivity.class) :
                        new Intent(context, LoginActivity.class);


                startActivity(activityIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.nothing);

                finish();
            }
        }).start();
    }
}
