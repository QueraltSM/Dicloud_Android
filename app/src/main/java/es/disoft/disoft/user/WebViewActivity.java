package es.disoft.disoft.user;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import es.disoft.disoft.ConnectionAvailable;
import es.disoft.disoft.R;
import es.disoft.disoft.Toast;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.menu.MenuFactory;
import es.disoft.disoft.model.User;
import es.disoft.disoft.notification.NotificationUtils;
import es.disoft.disoft.workers.ChatWorker;

public class WebViewActivity extends AppCompatActivity {

    private String notificationType;
    private int notificationId;
    private final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private final String NOTIFICATION_ID   = "NOTIFICATION_ID";

    private static Activity activity;
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        activity = this;

        setMenu();
        createWebview();
        setTextActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String TAG = "aaaaaaaa";
        int itemId = item.getItemId();
        if (itemId == R.id.home_button) webView.loadUrl(getString(R.string.URL_INDEX));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getExtras() != null) {
            Log.d("notificacion!!!", "\n\nbundle:\nt: " + intent.getStringExtra(NOTIFICATION_TYPE) + "\nid: " + intent.getIntExtra(NOTIFICATION_ID, -999));
        }
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        ChatWorker.checkMessagesEvery5sc.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {

        if (User.currentUser == null)
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();

        //TODO limpiar las notificaciones!!!
//        new NotificationUtils(getApplicationContext()).clearAll();

        Log.e("URL_", "onResume: ");

        if (loadedFromNotification())
            openChat();
        else
            openIndex();

        ChatWorker.checkMessagesEvery5sc.context = this;
        ChatWorker.checkMessagesEvery5sc.start();
        super.onResume();
    }


    private boolean loadedFromNotification() {
        if (getIntent() != null) {
            notificationType = getIntent().getStringExtra(NOTIFICATION_TYPE) ;
            notificationId   = getIntent().getIntExtra(NOTIFICATION_ID, 0);
        }
        return notificationType != null;
    }

    private void openChat() {
        if (webView.getUrl() != null && webView.getUrl().endsWith("chat.asp"))
            webView.loadUrl(pushButton(100));
        else
            webView.loadUrl(getString(R.string.URL_CHAT));

        Log.d("mensajeee", "\n\nbundle:\nt: " + notificationType + "\nid: " + notificationId);
    }

    private void openIndex() {
        try {
            final String url = getString(R.string.URL_INDEX);
            String postData = null; //(l)ibreacceso; 0, todos; 1, movil; 2, solo web
            postData = "token=" + URLEncoder.encode(User.currentUser.getToken(), "UTF-8")
                    + "&l=1";
            webView.postUrl(url, postData.getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setMenu() {
        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        new MenuFactory(this, expandableListView).loadMenu(isNetworkAvailable());
    }

    private void setTextActionBar() {
        setTextActionBar(null, null);
    }

    private void setTextActionBar(String dbAlias, String fullName) {

        final String finalDbAlias  = ObjectUtils.firstNonNull(dbAlias,  User.currentUser.getDbAlias());
        final String finalFullName = ObjectUtils.firstNonNull(fullName, User.currentUser.getFullName());

        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NavigationView nv    = findViewById(R.id.nav_view);
                        View header          = nv.getHeaderView(0);
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
            if (webView.canGoBack()) webView.goBack();
            else finish();
        }
    }

    class WebAppInterface {
        @JavascriptInterface
        public void sendData(String data) {
            if (!data.equalsIgnoreCase(User.currentUser.getFullName())) {
                setTextActionBar(null, data);
                User.currentUser.setFullName(data);
                DisoftRoomDatabase.getDatabase(activity).userDao().insert(User.currentUser);
            }
        }

//        This in html -> Allow send data to android through webview
//        Android.sendData("<%=session("name")%>");
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void createWebview() {

        webView = findViewById(R.id.webView);

        Log.e("webview!!!", "setPage: ");

        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        // TODO para poder rellenar forumularios (servirá para el focus en los mensajes)
//        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Allow send data to android through webview
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // This is to show alerts
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100)
                    progressBar.setVisibility(View.INVISIBLE);
                else
                    progressBar.setVisibility(View.VISIBLE);
            }
        });

        // This is to handle events
        webView.setWebViewClient(new WebViewClient() {

            @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                switch (errorCode) {
                    case WebViewClient.ERROR_HOST_LOOKUP:
                        webView.loadUrl(getString(R.string.URL_ERROR));
                        break;
                    case WebViewClient.ERROR_UNKNOWN:
                        webView.loadUrl(getString(R.string.URL_INDEX));
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!isNetworkAvailable() && !url.equals(getString(R.string.URL_ERROR))) {
                    webView.loadUrl(getString(R.string.URL_ERROR));
                }
            }

            // Avoid an infinite loop
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.endsWith("/index.asp")) webView.clearHistory();

                if (loadedFromNotification()) {
                    Log.e("mensajee", "ITS WORKS!!! " + notificationType);

                    webView.loadUrl(pushButton(500));

                    setIntent(null);
                    notificationType = null;
                }

                /* TODO esto es para rellenar forumularios a través del webview -> tiene que estar esto activado
                   TODO webSettings.setDomStorageEnabled(true);
                   TODO (servirá para el focus en los mensajes) */
//                myWebView.loadUrl("javascript:var x = document.getElementById('advanced').value = 'aaa';");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {

                Log.i("url_", "shouldOverrideUrlLoading: " + url);
                if (url.endsWith("/disconect")) {
                    WebViewActivity.closeSessionWithConfirmation();
                    return true;
                } else if (url.endsWith("/pass_changed")) {
                    Toast.makeText(getApplicationContext(), R.string.error_pass_changed).show();
                    closeSession();
                    return true;
                } else if (url.endsWith("/disabled")) {
                    Toast.makeText(getApplicationContext(), R.string.error_user_disabled).show();
                    closeSession();
                    return true;
                } else if (url.contains("hibernar.asp")) {
                    return true;
                } else if (url.contains("agententer.asp")) {
                    // Creo que cuando llevas mucho sin usar la app redirige, sin haber cerrado sesion, al login
                    Toast.makeText(getApplicationContext(), R.string.error_unexpected).show();
                    try {
                        String postData = "token=" + URLEncoder.encode(User.currentUser.getToken(),"UTF-8");
                        webView.postUrl(url, postData.getBytes());
                    } catch (UnsupportedEncodingException e) {
                        closeSession();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public static void closeSessionWithConfirmation() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        closeSession();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private static void closeSession() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                DisoftRoomDatabase db = DisoftRoomDatabase.getDatabase(activity);
                db.userDao().logout(User.currentUser.getId());
                db.messageDao().deleteAll();
                db.menuDao().deleteAll();
                User.currentUser = null;
                (new NotificationUtils(activity.getApplicationContext())).clearAll();
            }
        }).start();

        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();

        clearWebViewData();
    }

    private static void clearWebViewData() {
        WebView mWebView = (activity).findViewById(R.id.webView);

        mWebView.clearCache(true);
        mWebView.clearHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(activity);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private String pushButton(int ms) {
        return  "javascript:(function() {" +

                "               var checkExist = setInterval(function() {" +
                "                   var button;" +

                "                   console.log('funcion:');" +

                "                   $('button').each(function() {" +
                "                       if($(this).attr('id') == " + notificationId + ") {" +

                "                           button = $(this);" +

                "                           console.log('if: ' + button.attr('id'));" +

                "                           clearInterval(checkExist);" +
                "                           setTimeout(" +
                "                               function(){ " +

                "                                   console.log('interval: ' + button.attr('id'));" +

                "                                   button.click();" +
                "                               }," + ms + ");" +

                "                       }" +
                "                   }); " +

                "               }, 100);" +

                "           })()";
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
