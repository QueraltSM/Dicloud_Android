package es.disoft.dicloud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Objects;

import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.user.LoginActivity;
import es.disoft.dicloud.user.WebViewActivity;
import es.disoft.dicloud.workers.ChatWorker;
import es.disoft.dicloud.workers.MessagesWorker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class LauncherActivity extends AppCompatActivity {

    private Context context;
    public static String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("servicio", "App starts");

        UID     = getString(R.string.app_name);
        context = getApplicationContext();

        setContentView(R.layout.activity_launcher);

        SharedPreferences sharedPref = getDefaultSharedPreferences(getApplicationContext());
        int repeatInterval = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("sync_frequency", "15")));
        ChatWorker.runChatWork(UID, repeatInterval);
        MessagesWorker.runMessagesWork(UID, repeatInterval);
        login();
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
