package es.disoft.disoft;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import es.disoft.disoft.user.LoginActivity;
import es.disoft.disoft.user.User;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("servicio", "App starts");

//        animacion de inicio de app?
        setContentView(R.layout.activity_launcher);

        Intent activityIntent;
        activityIntent = User.logged(this) ?
                new Intent(this, MainActivity.class) :
                new Intent(this, LoginActivity.class);


        startActivity(activityIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.nothing);

        finish();

    }
}
