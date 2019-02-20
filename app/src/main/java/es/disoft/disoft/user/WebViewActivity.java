package es.disoft.disoft.user;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.disoft.disoft.ConnectionAvailable;
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

    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    private NestedWebView webView;
    private NestedScrollView scrollView;
    private ProgressBar progressBar;
    private Bundle state;

    private String urlBeforeFail;

    private String asw_cam_message;
    private ValueCallback<Uri> asw_file_message;
    private ValueCallback<Uri[]> asw_file_path;
    private final static int asw_file_req = 1;

    private final static int file_perm = 2;

    static boolean FUPLOAD   = true;
    static boolean CAMUPLOAD = true;
    static boolean ONLYCAM   = false;
    static boolean MULFILE   = false;
    private String TYPE      = "*/*";

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

        activity   = this;
        scrollView = findViewById(R.id.scrollview);

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
        inflater.inflate(R.menu.buttons, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == asw_file_req) {
                    if (null == asw_file_path) return;
                    if (data == null || data.getData() == null) {
                        if (asw_cam_message != null)
                            results = new Uri[]{Uri.parse(asw_cam_message)};

                    } else {
                        String dataString = data.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{ Uri.parse(dataString) };
                        } else {
                            if(MULFILE) {
                                if (data.getClipData() != null) {
                                    final int numSelectedFiles = data.getClipData().getItemCount();
                                    results = new Uri[numSelectedFiles];
                                    for (int i = 0; i < numSelectedFiles; i++)
                                        results[i] = data.getClipData().getItemAt(i).getUri();
                                }
                            }
                        }
                    }
                }
            }
            asw_file_path.onReceiveValue(results);
            asw_file_path = null;
        } else {
            if (requestCode == asw_file_req) {
                if (null == asw_file_message) return;
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                asw_file_message.onReceiveValue(result);
                asw_file_message = null;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.home_button) webView.loadUrl(getString(R.string.URL_INDEX));
        if (itemId == R.id.resfresh_button) webView.reload();
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
        try { ChatWorker.checkMessagesEvery5sc.stop(); }
        catch (NullPointerException ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (User.currentUser == null)
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();

        //TODO limpiar las notificaciones!!!
        new NotificationUtils(getApplicationContext()).clearAll();

        Log.e("URL_", "onResume: " + webView.getUrl());

        if (loadedFromNotification())
            openChat();
        else if (state != null)
            webView.saveState(state);
        else if (webView.getUrl() == null || webView.getUrl() != null && !webView.getUrl().contains("chat.asp"))
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

    // TODO no inicia sesion, creo que es aqui
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
        @JavascriptInterface
        public void printLog() {
            if (scrollView != null) scrollView.scrollTo(0, 0);
            Log.i("log", "printLog: ");
        }

//        This in html -> Allow send data to android through webview
//        Android.sendData("<%=session("name")%>");
    }

    //Checking if particular permission is given or not
    public boolean checkPermission(int permission){
        switch(permission){
            case 1:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            case 2:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            case 3:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        }
        return false;
    }

    //Creating image file for upload
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
        String new_name = "disoft_" + file_name + "_";
        File sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".jpg", sd_directory);
    }


    //Checking permission for storage and camera for writing and uploading images
    public void requestPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        //Checking for storage permission to write images for upload
        if (FUPLOAD && CAMUPLOAD && !checkPermission(2) && !checkPermission(3)) {
            ActivityCompat.requestPermissions(this, perms, file_perm);

            //Checking for WRITE_EXTERNAL_STORAGE permission
        } else if (FUPLOAD && !checkPermission(2)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, file_perm);

            //Checking for CAMERA permissions
        } else if (CAMUPLOAD && !checkPermission(3)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, file_perm);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void createWebview() {

        webView    = findViewById(R.id.webView);
        scrollView = findViewById(R.id.scrollview);

        Log.e("webview!!!", "setPage: ");

        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);

        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
        }

        webView.setWebViewClient(new WebViewClient());

//        webView.setView(findViewById(R.id.appBarLayout), findViewById(R.id.progressBarLayout));

        // Allow send data to android through webview
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                Toast.setText(getApplicationContext(), R.string.download_start).show();

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
//                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Name of your downloadble file goes here, example: Mathematics II ");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        });

        // This is to show alerts
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                    progressBar.setVisibility(newProgress == 100
                            ? View.INVISIBLE
                            : View.VISIBLE);
            }

            @Override

            /**
             * Thanks to
             * https://github.com/mgks/Android-SmartWebView
             */
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if (checkPermission(2) && checkPermission(3)) {
                    if (FUPLOAD) {
                        if (asw_file_path != null)
                            asw_file_path.onReceiveValue(null);

                        asw_file_path = filePathCallback;
                        Intent takePictureIntent = null;
                        if (CAMUPLOAD) {
                            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = create_image();
                                    takePictureIntent.putExtra("PhotoPath", asw_cam_message);
                                } catch (IOException ex) {
                                    Log.e("aaaah", "Image file creation failed", ex);
                                }
                                if (photoFile != null) {
                                    asw_cam_message = "file:" + photoFile.getAbsolutePath();
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                } else {
                                    takePictureIntent = null;
                                }
                            }
                        }
                        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        if (!ONLYCAM) {
                            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                            contentSelectionIntent.setType(TYPE);
                            if (MULFILE) {
                                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            }
                        }
                        Intent[] intentArray;
                        if (takePictureIntent != null)
                            intentArray = new Intent[]{takePictureIntent};
                        else
                            intentArray = new Intent[0];


                        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.fileChooser));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                        startActivityForResult(chooserIntent, asw_file_req);
                    }
                    return true;
                } else {
                    requestPermissions();
                    return false;
                }
            }
        });

        // This is to handle events
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                switch (errorCode) {
                    case WebViewClient.ERROR_HOST_LOOKUP:
                        view.loadUrl(getString(R.string.URL_ERROR));
                        urlBeforeFail = failingUrl == null
                                ? getString(R.string.URL_INDEX)
                                : failingUrl;
                        break;
                    case WebViewClient.ERROR_UNKNOWN:
                        if (description.equals(getString(R.string.ERR_CACHE_MISS))) {
                            Log.i("url_", "onReceivedError: " + description);
                            view.loadUrl(getString(R.string.URL_INDEX));
                        }
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                Log.i("start", "onPageStarted: ");

                super.onPageStarted(view, url, favicon);
                if (!isNetworkAvailable(getApplicationContext()) && !url.equals(getString(R.string.URL_ERROR))) {
                    view.loadUrl(getString(R.string.URL_ERROR));
                    urlBeforeFail = url;
                    Toast.setText(getApplicationContext(), "No hay internet").show();
                    loadURLWhenNetworkAvailable(view);
                }
            }

            // Avoid an infinite loop
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

//                webView.loadUrl(renderHTMLinjection());
//                if (scrollView != null) scrollView.scrollTo(0, 0);

                ObjectAnimator anim = ObjectAnimator.ofInt(scrollView, "scrollY", 0);
                anim.setDuration(400);
                anim.start();


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

                Log.i("new url", "shouldOverrideUrlLoading: " + url);

                if (url.endsWith("/pass_changed")) {
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
                        view.postUrl(url, postData.getBytes());
                        closeSession();
                    } catch (UnsupportedEncodingException e) {
                        closeSession();
                    }
                    return true;
                } else if (url.contains("maps.google.com")) {
                    Uri IntentUri    = Uri.parse(url);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, IntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    if (mapIntent.resolveActivity(getPackageManager()) != null)
                        startActivity(mapIntent);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadURLWhenNetworkAvailable(final WebView view) {

        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){

                while (!ConnectionAvailable.isNetworkAvailable(getApplicationContext())) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    if ((new ConnectionAvailable(getString(R.string.URL_INDEX)).execute().get())) {
                        view.loadUrl(urlBeforeFail);
                    } else {
                        handler.postDelayed(this, delay);
                    }
                } catch (Exception ignored) { }

            }
        }, delay);

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

                "                   $('button').each(function() {" +
                "                       if($(this).attr('id') == " + notificationId + ") {" +
                "                           button = $(this);" +
                "                           clearInterval(checkExist);" +

                "                           setTimeout(" +
                "                               function(){ " +
                "                                   button.click();" +
                "                               }," + ms + ");" +

                "                       }" +
                "                   }); " +

                "               }, 100);" +

                "           })()";
    }

    private String renderHTMLinjection() {
        return  "javascript:(function() {" +
                "               Android.printLog(); " +
                "           })()";
    }
}