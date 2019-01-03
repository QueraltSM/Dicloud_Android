package es.disoft.disoft;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

//        animacion de inicio de app?
        setContentView(R.layout.activity_launcher);
        runChatWork();
        login();
    }

    private void runChatWork() {
        PeriodicWorkRequest.Builder logCheckBuilder =
                new PeriodicWorkRequest.Builder(ChatWorker.class, 15, TimeUnit.MINUTES);

        PeriodicWorkRequest chatWork = logCheckBuilder.build();
        WorkManager.getInstance().enqueue(chatWork);
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
