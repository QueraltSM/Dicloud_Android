package es.disoft.disoft;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.User;
import es.disoft.disoft.user.LoginActivity;

public class LauncherActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("servicio", "App starts");
        context = getApplicationContext();

//        animacion de inicio de app?
        setContentView(R.layout.activity_launcher);

        new Thread(new Runnable() {
            @Override
            public void run() {
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
