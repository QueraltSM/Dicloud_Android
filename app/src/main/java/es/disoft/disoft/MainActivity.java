package es.disoft.disoft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import es.disoft.disoft.db.DbHelper;
import es.disoft.disoft.service.ChatService;
import es.disoft.disoft.service.StartService;
import es.disoft.disoft.user.LoginActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String dbAlias, name, lastName, token;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        activity = this;


        try {
            getUserData();
            runAlarmManager();
            setTextActionBar();
            setPage();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void runAlarmManager() {
        StartService.setAlarmManager(this, ChatService.class);
    }

    private void setTextActionBar() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        TextView navCompany = header.findViewById(R.id.nav_company);
        TextView navName    = header.findViewById(R.id.nav_name);

        navCompany.setText(WordUtils.capitalizeFully(dbAlias));
        String fullName = name + " " + lastName;
        navName.setText(WordUtils.capitalizeFully(fullName));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView mWebView;
        mWebView = findViewById(R.id.webView);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) mWebView.goBack();
                    else finish();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_manage:
                break;
            case R.id.nav_home:
                String home = getString(R.string.URL_INDEX);
                ((WebView) findViewById(R.id.webView)).loadUrl(home);
                break;
            default:
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void getUserData() {
        DbHelper checkUser = new DbHelper(this);
        SQLiteDatabase db = checkUser.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT dbAlias,name,lastname,token FROM users WHERE loggedIn=?", new String[]{"1"});

        if (c.moveToFirst()) {
            dbAlias  = c.getString(c.getColumnIndex("dbAlias"));
            name     = c.getString(c.getColumnIndex("name"));
            lastName = c.getString(c.getColumnIndex("lastName"));
            token    = c.getString(c.getColumnIndex("token"));
            db.close();
        }
    }


    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void setPage() throws UnsupportedEncodingException {
        final WebView myWebView = findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());

        final String url = getString(R.string.URL_INDEX);

        String postData  = "token=" + URLEncoder.encode(token,"UTF-8");

        myWebView.postUrl(url, postData.getBytes());

        // This is to show alerts
        myWebView.setWebChromeClient(new WebChromeClient());

        // This is to handle events
        myWebView.setWebViewClient(new WebViewClient() {
            // In the 1st load, go back throws err_cache_miss
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode == -1) myWebView.loadUrl(url);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            // Avoid an infinite loop
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.endsWith("/index.asp")) myWebView.clearHistory();
                super.onPageFinished(view, url);
            }

            // Logout confirmation
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {

                Log.i("url_", "shouldOverrideUrlLoading: " + url);
                if (url.endsWith("/disconect")) {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.logout_title)
                            .setMessage(R.string.logout_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    logout(myWebView, url);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                    return true;
                } else if (url.endsWith("/pass_changed")) {
                    Toast.makeText(getApplicationContext(), R.string.error_pass_changed, Toast.LENGTH_LONG).show();
                    logout();
                    return true;
                } else if (url.endsWith("/disabled")) {
                    Toast.makeText(getApplicationContext(), R.string.error_user_disabled, Toast.LENGTH_LONG).show();
                    logout();
                    return true;
//                } else if (url.contains("agententer.asp")) {
//                    Toast.makeText(getApplicationContext(), R.string.error_unexpected, Toast.LENGTH_LONG).show();
//                    logout();
//                    return true;
                }
                return false;
            }
        });
    }


    private void logout() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE USERS SET loggedIn=0, token='' WHERE loggedIn=1");

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


    private void logout(WebView myWebView, String url) {
        myWebView.loadUrl(url);
        logout();
    }
}
