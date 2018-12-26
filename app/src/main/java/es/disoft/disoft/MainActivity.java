package es.disoft.disoft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import es.disoft.disoft.service.ChatService;
import es.disoft.disoft.service.StartService;
import es.disoft.disoft.user.Menu;
import es.disoft.disoft.user.User;

public class MainActivity extends AppCompatActivity {

    private String mDbAlias, mToken, mFullName, mUID;

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

        activity = this;

        try {
            getUserData();
            setMenu();
            setTextActionBar();
            setPage();
            runAlarmManager();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //TODO eliminar esta linea cuando se pongan los mensajes en segundo plano
        startService(new Intent(this, ChatService.class));
    }


    private void setMenu() {
        ExpandableListView a = findViewById(R.id.expandableListView);
        Menu myMenu = new Menu(this, mUID, a);
        myMenu.loadMenu();
    }


    private void runAlarmManager() {
        StartService.setAlarmManager(this, ChatService.class);
    }


    private void setTextActionBar() {
        setTextActionBar(null, null);
    }


    private void setTextActionBar(String dbAlias, String fullName) {

        dbAlias  = ObjectUtils.firstNonNull(dbAlias, mDbAlias);
        fullName = ObjectUtils.firstNonNull(fullName, mFullName);

        final String finalDbAlias = dbAlias;
        final String finalFullName = fullName;
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        View header = navigationView.getHeaderView(0);
                        TextView navCompany  = header.findViewById(R.id.nav_company);
                        TextView navFullName = header.findViewById(R.id.nav_full_name);

                        navCompany.setText(WordUtils.capitalizeFully(finalDbAlias));
                        navFullName.setText(WordUtils.capitalizeFully(finalFullName));
                    }
                });
            }
        }.start();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            WebView mWebView = findViewById(R.id.webView);
            if (mWebView.canGoBack()) mWebView.goBack();
            else finish();
        }
    }


    private void getUserData() {
        ContentValues userData = User.getData(this);

        mDbAlias  = userData.getAsString("dbAlias");
        mFullName = userData.getAsString("fullName");
        mToken    = userData.getAsString("token");
        mUID      = userData.getAsString("user_id");
    }


    class WebAppInterface {
        @JavascriptInterface
        public void sendData(String data) {
            if (!data.equalsIgnoreCase(mFullName)) {
                mFullName = data;
                setTextActionBar(null, mFullName);
                ContentValues values = new ContentValues();
                values.put("fullName", mFullName);
                User.updateData(activity, values);
            Log.i("NAME", "distintoooo: " + mFullName);
            }
            Log.i("NAME", "sendData: " + mFullName);
        }

//        This in html -> Allow send data to android through webview
//        Android.sendData("<%=session("name")%>");
    }


    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void setPage() throws UnsupportedEncodingException {
        final WebView myWebView = findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        // TODO para poder rellenar forumularios (servirá para el focus en los mensajes)
//        webSettings.setDomStorageEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());

        final String url = getString(R.string.URL_INDEX);

        String postData  = "token=" + URLEncoder.encode(mToken,"UTF-8");

        // Allow send data to android through webview
        myWebView.addJavascriptInterface(new WebAppInterface(), "Android");
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
                /* TODO esto es para rellenar forumularios a través del webview -> tiene que estar esto activado
                   TODO webSettings.setDomStorageEnabled(true);
                   TODO (servirá para el focus en los mensajes) */
//                myWebView.loadUrl("javascript:var x = document.getElementById('advanced').value = 'aaa';");
                // TODO Esto no deberia estar aqui, deberia hacerlo la propia web
                myWebView.loadUrl("javascript:(function() { $('.navbar-header button').remove(); })()");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {

                Log.i("url_", "shouldOverrideUrlLoading: " + url);
                if (url.endsWith("/disconect")) {
                    User.logoutWithConfirmation(activity);
                    return true;
                } else if (url.endsWith("/pass_changed")) {
                    Toast.makeText(getApplicationContext(), R.string.error_pass_changed, Toast.LENGTH_LONG).show();
                    User.logout(activity);
                    return true;
                } else if (url.endsWith("/disabled")) {
                    Toast.makeText(getApplicationContext(), R.string.error_user_disabled, Toast.LENGTH_LONG).show();
                    User.logout(activity);
                    return true;
                } else if (url.contains("hibernar.asp")) {
                    return true;
                } else if (url.contains("agententer.asp")) {
                    // Creo que cuando llevas mucho sin usar la app redirige, sin haber cerrado sesion, al login
                    Toast.makeText(getApplicationContext(), R.string.error_unexpected, Toast.LENGTH_LONG).show();
                    try {
                        String postData = "token=" + URLEncoder.encode(mToken,"UTF-8");
                        myWebView.postUrl(url, postData.getBytes());
                    } catch (UnsupportedEncodingException e) {
                        User.logout(activity);
                    }
                    return true;
                }
                return false;
            }
        });
    }
}
