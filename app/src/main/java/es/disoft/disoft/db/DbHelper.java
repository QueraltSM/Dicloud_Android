package es.disoft.disoft.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final int    VERSION                = 1;
    private static final String DATABASE_NAME          = "disoft.db";
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE USERS "         +
            "(" +
            "pkCode TEXT PRIMARY KEY,"    +
            "user_id INTEGER,"            +
            "user TEXT,"                  +
            "name TEXT,"                  +
            "lastName TEXT,"              +
            "dbAlias TEXT,"               +
            "loggedIn INTEGER DEFAULT 0," +
            "token TEXT"                  +
            ")";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {      // la primera vez, cuando no existe ese ficherodb
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL(SQL_CREATE_USERS_TABLE);        // si no existe la bd la crea
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Cuando vas a actualizar la bd con nuevos campos o algo cuando actualizas en google play
    }

    public static void addUser() {

    }
}
