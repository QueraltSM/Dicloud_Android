package es.disoft.disoft.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import es.disoft.disoft.db.DbHelper;

public class User {

    public static boolean logged(Context context) {
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
        DbHelper myDb = new DbHelper(context);
        myDb.userLogout();
    }
}
