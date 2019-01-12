package es.disoft.disoft.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import es.disoft.disoft.R;
import es.disoft.disoft.Toast;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.menu.MenuFactory;
import es.disoft.disoft.model.User;
import es.disoft.disoft.notification.NotificationUtils;
import es.disoft.disoft.workers.ChatWorker;

import static es.disoft.disoft.ConnectionAvailable.isNetworkAvailable;

public class WebViewActivity extends AppCompatActivity {

    private String notificationType;
    private int notificationId;
    private final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private final String NOTIFICATION_ID   = "NOTIFICATION_ID";

    private static Activity activity;
    private WebView webView;
    private ProgressBar progressBar;
    private Bundle state;

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

        Log.i("qweasd", "onCreate: " + savedInstanceState);
        setMenu();
        createWebview();
        setTextActionBar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i("qwe", "onSaveInstanceState: ");
        state = outState;
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i("qwe", "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.home_button) webView.loadUrl(getString(R.string.URL_INDEX));
        return super.onOptionsItemSelected(item);
    }

    private boolean closeNav() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getExtras() != null)
            Log.d("notificacion!!!", "\n\nbundle:\nt: " + intent.getStringExtra(NOTIFICATION_TYPE) + "\nid: " + intent.getIntExtra(NOTIFICATION_ID, -999));

        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatWorker.checkMessagesEvery5sc.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (User.currentUser == null)
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();

        //TODO limpiar las notificaciones!!!
//        new NotificationUtils(getApplicationContext()).clearAll();

        Log.e("URL_", "onResume: ");

        if (loadedFromNotification())
            openChat();
        else if (state != null)
            webView.saveState(state);
        else
            openIndex();

        ChatWorker.checkMessagesEvery5sc.context = this;
        ChatWorker.checkMessagesEvery5sc.start();
    }


    private boolean loadedFromNotification() {
        if (getIntent() != null) {
            notificationType = getIntent().getStringExtra(NOTIFICATION_TYPE) ;
            notificationId   = getIntent().getIntExtra(NOTIFICATION_ID, 0);
        }
        return notificationType != null;
    }

    private void openChat() {
        closeNav();
        if (webView.getUrl() != null && webView.getUrl().endsWith("chat.asp")) {
            if (notificationType.equals("notification"))
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    webView.loadUrl(pushButton(100));
        } else {
            webView.loadUrl(getString(R.string.URL_CHAT));
        }
        Log.d("mensajeee", "\n\nbundle:\nt: " + notificationType + "\nid: " + notificationId);
    }

    private void openIndex() {
        try {
            final String url = getString(R.string.URL_INDEX);
            String postData; //(l)ibreacceso; 0, todos; 1, movil; 2, solo web
            postData = "token=" + URLEncoder.encode(User.currentUser.getToken(), "UTF-8")
                    + "&l=1";
            webView.postUrl(url, postData.getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setMenu() {
        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        new MenuFactory(this, expandableListView).loadMenu(isNetworkAvailable(getApplicationContext()));
    }

    private void setTextActionBar() {
        setTextActionBar(null);
    }

    private void setTextActionBar(String fullName) {
        if(User.currentUser == null)
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();

        final String finalDbAlias  = User.currentUser.getDbAlias();
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
        if (!closeNav()) {
            if (webView.canGoBack()) webView.goBack();
            else finish();
        }
    }

    class WebAppInterface {
        @JavascriptInterface
        public void sendData(String data) {
            if (!data.equalsIgnoreCase(User.currentUser.getFullName())) {
                setTextActionBar(data);
                User.currentUser.setFullName(data);
                DisoftRoomDatabase.getDatabase(activity.getApplicationContext()).userDao().insert(User.currentUser);
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
                if (!isNetworkAvailable(getApplicationContext()) && !url.equals(getString(R.string.URL_ERROR))) {
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

                    if (notificationType.equals("notification"))
//                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                            webView.loadUrl(pushButton(500));

                    setIntent(null);
                    notificationType = null;
                }

                /* esto es para rellenar forumularios a través del webview -> tiene que estar esto activado
                   webView.getSettings().setDomStorageEnabled(true); */
//                webView.loadUrl("javascript:var x = $('#advanced').text() = 'aaa';");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {

                Log.i("url_", "shouldOverrideUrlLoading: " + url);
                if (url.endsWith("/disconect")) {
//                    closeSessionWithConfirmation();
                    return true;
                } else if (url.endsWith("/pass_changed")) {
                    Toast.setText(getApplicationContext(), R.string.error_pass_changed).show();
                    closeSession();
                    return true;
                } else if (url.endsWith("/disabled")) {
                    Toast.setText(getApplicationContext(), R.string.error_user_disabled).show();
                    closeSession();
                    return true;
                } else if (url.contains("hibernar.asp")) {
                    return true;
                } else if (url.contains("agententer.asp")) {
                    // Creo que cuando llevas mucho sin usar la app redirige, sin haber cerrado sesion, al login
                    Toast.setText(getApplicationContext(), R.string.error_unexpected).show();
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

    public static void closeSession() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                DisoftRoomDatabase db = DisoftRoomDatabase.getDatabase(activity.getApplicationContext());
                db.userDao().logout(User.currentUser.getId());
                db.messageDao().deleteAll();
                db.menuDao().deleteAll();
                User.currentUser = null;
                (new NotificationUtils(activity.getApplicationContext())).clearAll();
            }
        }).start();

        activity.startActivity(new Intent(activity.getApplicationContext(), LoginActivity.class));
        activity.finish();

        clearWebViewData();
    }

    private static void clearWebViewData() {
        WebView mWebView = activity.findViewById(R.id.webView);

        mWebView.clearCache(true);
        mWebView.clearHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(activity.getApplicationContext());
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
}
