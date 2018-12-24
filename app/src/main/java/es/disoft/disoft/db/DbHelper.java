package es.disoft.disoft.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final int    VERSION                = 1;
    private static final String DATABASE_NAME          = "disoft.db";
    private static final String USERS_TABLE            = "users";
    private static final String MENUS_TABLE            = "menus";
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + USERS_TABLE     +
            "("                               +
                "pkCode TEXT PRIMARY KEY,"    +
                "user_id INTEGER,"            +
                "user TEXT,"                  +
                "fullName TEXT,"              +
                "dbAlias TEXT,"               +
                "loggedIn INTEGER DEFAULT 0," +
                "token TEXT"                  +
            ")";
    private static final String SQL_CREATE_MENUS_TABLE =
            "CREATE TABLE " + MENUS_TABLE     +
            "("                               +
                "id TEXT PRIMARY KEY,"        +
                "user_id INTEGER,"            +
                "menu TEXT,"                  +
                "submenu TEXT,"               +
                "shortname TEXT"              +
            ")";
    private static final String TAG = "MENU";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {      // la primera vez, cuando no existe ese ficherodb
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MENUS_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_MENUS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Cuando vas a actualizar la bd con nuevos campos o algo cuando actualizas en google play
    }


    public void upsertUser(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();

        if (db != null) {
            String[] args = new String[]{values.getAsString("pkCode")};
            try {
                db.insertOrThrow("users", null, values);
            } catch (SQLiteConstraintException e) {
                db.update(USERS_TABLE, values, "Pkcode=?", args);
            }

            values.clear();

            values.put("loggedIn", 1);
            db.update(USERS_TABLE, values, "Pkcode=?", args);
            values.put("loggedIn", 0);
            db.update(USERS_TABLE, values, "Pkcode!=?", args);

            db.close();
        }
    }


    public ArrayAdapter<String> getSuggestions(Context context, String suggestionsType) {
        String query      = "SELECT DISTINCT " + suggestionsType + " FROM " + USERS_TABLE;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c          = db.rawQuery(query, null);

        ArrayList<String> suggestionsAL = new ArrayList<>();
        while(c.moveToNext()) suggestionsAL.add(c.getString(c.getColumnIndex(suggestionsType)).toLowerCase());
        c.close();

        String[] suggestions = suggestionsAL.toArray(new String[0]);
        return new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, suggestions);
    }


    public boolean userIsLogged() {
        SQLiteDatabase db = getReadableDatabase();
        return 1 == DatabaseUtils.queryNumEntries(db, USERS_TABLE, "loggedIn=1");
    }


    public void updateCurrentUserData(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(USERS_TABLE, values, "loggedIn=?", new String[]{"1"});
    }


    public ContentValues getCurrentUserData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE loggedIn=?", new String[]{"1"});

        ContentValues values = new ContentValues();
        if (c.moveToFirst()) {
            values.put("user_id",  c.getString(c.getColumnIndex("user_id")));
            values.put("dbAlias",  c.getString(c.getColumnIndex("dbAlias")));
            values.put("fullName", c.getString(c.getColumnIndex("fullName")));
            values.put("token",    c.getString(c.getColumnIndex("token")));
        }
        db.close();

        return values;
    }

    public void userLogout() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("loggedIn", 0);
        db.update(USERS_TABLE, values, "loggedIn=?", new String[]{"1"});
        db.close();
    }

//    TODO esto que es?
    public void getCurrentUserMenu() {
        SQLiteDatabase db = getWritableDatabase();

        String query = "" +
                "SELECT " + MENUS_TABLE + ".* " +
                "FROM   " + MENUS_TABLE + " INNER JOIN " + USERS_TABLE + " " +
                "ON "     + MENUS_TABLE + ".agent_id = " + USERS_TABLE + ".agent_id " +
                "WHERE "  + USERS_TABLE + ".loggedIn=1 ";
    }

    JSONArray jArray = null;

    public void setCurrentUserMenu(String menuAsJsonString) throws JSONException {
        SQLiteDatabase db = getWritableDatabase();

        if (db != null) {

            jArray = new JSONObject(menuAsJsonString).getJSONArray("usermenu");


            Cursor c = db.rawQuery("SELECT user_id FROM " + USERS_TABLE + " WHERE loggedIn=?", new String[]{"1"});
            if (c.moveToFirst()) {
                String uid = c.getString(c.getColumnIndex("user_id"));
                db.delete(MENUS_TABLE, "user_id=?", new String[]{uid});
            }


            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                ContentValues values = new ContentValues();
                values.put("id",        json_data.getString("id"));
                values.put("user_id",   json_data.getString("agent_id"));
                values.put("menu",      json_data.getString("menu"));
                values.put("submenu",   json_data.getString("submenu"));
                values.put("shortname", json_data.getString("shortname"));

                db.insert(MENUS_TABLE, null, values);
                Log.i(TAG, "Introducido el id '" + json_data.getString("submenu") + "'");

            }
            db.close();
        }
    }
}