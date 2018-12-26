package es.disoft.disoft.user;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.TreeMap;

import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

public class User {

    public static boolean isLogged(Context context) {
        DbHelper myDb = new DbHelper(context);
        return myDb.userIsLogged();
    }

    public static ContentValues getData(Context context) {
        DbHelper myDb = new DbHelper(context);
        return myDb.getCurrentUserData();
    }

    public static void updateData(Context context, ContentValues values) {
        DbHelper myDb = new DbHelper(context);
        myDb.updateCurrentUserData(values);
    }

    public static void logout(Context context) {
        clearSession(context);
        DbHelper myDb = new DbHelper(context);
        myDb.userLogout();
        context.startActivity(new Intent(context, LoginActivity.class));
        ((Activity) context).finish();
    }

    public static void logoutWithConfirmation(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        logout(context);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    static ArrayList<String> getMenuItems(Context context, String uid) {
        DbHelper myDb = new DbHelper(context);
        return myDb.getCurrentUserMenuItems(uid);
    }

    /**
     * @return TreeMap<submenu name, submane url as string>
     */
    static TreeMap<String, String> getSubmenuItems(Context context, String uid, String menu) {
        DbHelper myDb = new DbHelper(context);
        return myDb.getCurrentUserSubmenuItems(uid, menu);
    }

    @SuppressWarnings("deprecation")
    private static void clearSession(Context context) {

        WebView mWebView = ((Activity) context).findViewById(R.id.webView);

        mWebView.clearCache(true);
        mWebView.clearHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
