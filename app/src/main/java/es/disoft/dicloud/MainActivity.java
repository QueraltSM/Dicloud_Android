package es.disoft.dicloud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import es.disoft.dicloud.user.LoginActivity;
import es.disoft.dicloud.user.WebViewActivity;

public class MainActivity extends AppCompatActivity {

    private void showMainActivity() {
        new Timer().schedule(new TimerTask(){
            public void run() {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, 5000 );
    }

    private void showHome() {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences("UserLoggedIn", Context.MODE_PRIVATE);
        if (!pref.getBoolean("UserLoggedIn", false)) showMainActivity();
        else showHome();
    }
}
