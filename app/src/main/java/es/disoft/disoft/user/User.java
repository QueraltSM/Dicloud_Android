package es.disoft.disoft.user;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import es.disoft.disoft.db.DbHelper;

public class User {

    public static boolean logged(Context context) {
        DbHelper checkUser = new DbHelper(context);
        SQLiteDatabase db = checkUser.getReadableDatabase();

        return 1 == DatabaseUtils.queryNumEntries(db, "users", "loggedIn=1");
    }
}
