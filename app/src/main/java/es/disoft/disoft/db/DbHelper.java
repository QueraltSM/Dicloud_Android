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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class DbHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "disoft.db";
    private static final String USERS_TABLE = "users";
    private static final String MENUS_TABLE = "menus";
    private static final String MESSAGES_TABLE = "messages";
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + USERS_TABLE +
                    "(" +
                    "pkCode TEXT PRIMARY KEY," +
                    "user_id INTEGER," +
                    "user TEXT," +
                    "fullName TEXT," +
                    "dbAlias TEXT," +
                    "loggedIn INTEGER DEFAULT 0," +
                    "token TEXT" +
                    ")";
    private static final String SQL_CREATE_MENUS_TABLE =
            "CREATE TABLE " + MENUS_TABLE +
                    "(" +
                    "id TEXT PRIMARY KEY," +
                    "user_id INTEGER," +
                    "menu TEXT," +
                    "submenu TEXT," +
                    "shortname TEXT" +
                    ")";
    private static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + MESSAGES_TABLE +
                    "(" +
                    "id TEXT PRIMARY KEY," +
                    "user_to_id INTEGER," +
                    "user_from_id INTEGER," +
                    "user_from TEXT," +
                    "last_message_timestamp TEXT," +
                    "messages_count INTEGER" +
                    ")";
    private static final String TAG = "MENU";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {      // la primera vez, cuando no existe ese ficherodb
        dropTables(db, USERS_TABLE, MENUS_TABLE, MESSAGES_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_MENUS_TABLE);
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }


    private void dropTables(SQLiteDatabase db, String... tables) {
        for (String table : tables) db.execSQL("DROP TABLE IF EXISTS " + table);
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
        String query = "SELECT DISTINCT " + suggestionsType + " FROM " + USERS_TABLE;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        ArrayList<String> suggestionsAL = new ArrayList<>();
        while (c.moveToNext())
            suggestionsAL.add(c.getString(c.getColumnIndex(suggestionsType)).toLowerCase());
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
        String sql = "" +
                "SELECT user_id,          " +
                       "dbAlias,          " +
                       "fullName,         " +
                       "token             " +
                "FROM " + USERS_TABLE + " " +
                "WHERE loggedIn = ?       ";
        Cursor c = db.rawQuery(sql, new String[]{"1"});

        ContentValues values = new ContentValues();
        if (c.moveToFirst())
            for (int i = 0; i < c.getColumnCount(); i++)
                values.put(c.getColumnName(i), c.getString(i));
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

    public ArrayList<String> getCurrentUserMenuItems(String uid) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "" +
                "SELECT DISTINCT menu " +
                "FROM " + MENUS_TABLE + " " +
                "WHERE user_id = ?" +
                "ORDER BY id ASC";

        Cursor c = db.rawQuery(sql, new String[]{uid});

        ArrayList<String> menu = new ArrayList<>();
        while (c.moveToNext()) {
            menu.add(c.getString(c.getColumnIndex("menu")));
        }
        db.close();
        return menu;
    }

    public TreeMap<String, String> getCurrentUserSubmenuItems(String uid, String menu) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "" +
                "SELECT DISTINCT submenu,shortname " +
                "FROM " + MENUS_TABLE + " " +
                "WHERE user_id = ? AND menu = ? " +
                "ORDER BY id ASC";

        Cursor c = db.rawQuery(sql, new String[]{uid, menu});
        TreeMap<String, String> submenuItems = new TreeMap<>();
        while (c.moveToNext()) {
            String submenuItem = c.getString(c.getColumnIndex("submenu"));
            String shortname = c.getString(c.getColumnIndex("shortname"));
            submenuItems.put(submenuItem, shortname);
        }
        db.close();
        return submenuItems;
    }

    public void setCurrentUserMenu(String menuAsJsonString) throws JSONException {
        SQLiteDatabase db = getWritableDatabase();

        if (db != null) {

            if (userIsLogged()) db.delete(MENUS_TABLE, "user_id=?", new String[]{getCurrentUserID(db)});

            JSONArray jArray = new JSONObject(menuAsJsonString).getJSONArray("usermenu");
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

    public Boolean updateCurrentUserPendingMessages(String messagesAsJsonString) throws JSONException {
        SQLiteDatabase db = getWritableDatabase();
        Boolean updated   = false;

        if (db != null) {
            String tempTableName = "my" + MESSAGES_TABLE;

            if (userIsLogged()) {
                createTempMessagesTable(db, tempTableName, messagesAsJsonString);
                updated = updateMessagesTable(db, tempTableName, getCurrentUserID(db));
                Log.i(TAG, "updateCurrentUserPendingMessages: TERMINÉEEEEEE!!!!");
            }
        }

        db.close();
        return updated;
    }


    private void createTempMessagesTable(SQLiteDatabase db, String tempTableName, String messagesAsJsonString) throws JSONException {
        String tempTable = SQL_CREATE_MESSAGES_TABLE.replace("TABLE " + MESSAGES_TABLE, "TEMPORARY TABLE " + tempTableName);
        db.execSQL(tempTable);

        JSONArray jArray = new JSONObject(messagesAsJsonString).getJSONArray("messages");
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject json_data = jArray.getJSONObject(i);

            ContentValues values = new ContentValues();
            values.put("id",                     json_data.getString("id"));
            values.put("user_to_id",             json_data.getInt("to_id"));
            values.put("user_from_id",           json_data.getInt("from_id"));
            values.put("user_from",              json_data.getString("from"));
            values.put("last_message_timestamp", json_data.getString("last_message_timestamp"));
            values.put("messages_count",         json_data.getInt("messages_count"));

            db.insert(tempTableName, null, values);
        }
    }


    private Boolean updateMessagesTable(SQLiteDatabase db, String tempTableName, String uid) {

        boolean updated = false;

        String sql_select_diff_messages =
                "SELECT * FROM"                                                                                                                     +
                        "("                                                                                                                         +
                        "SELECT " + MESSAGES_TABLE + ".*, "                                                                                         +
                        "       'equal' status "                                                                                                    +
                        "FROM   " + MESSAGES_TABLE + " "                                                                                            +
                        "       INNER JOIN " + tempTableName + " "                                                                                  +
                        "               ON " + MESSAGES_TABLE + ".id = " + tempTableName + ".id "                                                   +
                        "               AND " + MESSAGES_TABLE + ".last_message_timestamp = " + tempTableName + ".last_message_timestamp "          +
                        "UNION "                                                                                                                    +
                        "SELECT " + MESSAGES_TABLE + ".*, "                                                                                         +
                        "       'deleted' status "                                                                                                  +
                        "FROM   " + MESSAGES_TABLE + " "                                                                                            +
                        "WHERE  NOT EXISTS (SELECT * "                                                                                              +
                        "                   FROM   " + tempTableName + " "                                                                          +
                        "                   WHERE  " + MESSAGES_TABLE + ".id = " + tempTableName + ".id) "                                          +
                        "UNION "                                                                                                                    +
                        "SELECT " + tempTableName + ".*, "                                                                                          +
                        "       'updated' status "                                                                                                  +
                        "FROM   " + tempTableName + " "                                                                                             +
                        "WHERE  NOT EXISTS (SELECT * "                                                                                              +
                        "                   FROM   " + MESSAGES_TABLE + " "                                                                         +
                        "                   WHERE  " + MESSAGES_TABLE + ".id = " + tempTableName + ".id "                                           +
                        "                   AND    " + MESSAGES_TABLE + ".last_message_timestamp = " + tempTableName + ".last_message_timestamp) "  +
                        ") "                                                                                                                        +
                        "WHERE user_to_id = ?;";

        Cursor c = db.rawQuery(sql_select_diff_messages, new String[]{uid});

        while (c.moveToNext()) {
            String status = c.getString(c.getColumnIndex("status"));
            String id     = c.getString(c.getColumnIndex("id"));

            switch (status) {
                case "deleted":
                    db.delete(MESSAGES_TABLE, "id=?", new String[]{id});
                    break;
                case "updated":
                    ContentValues values = new ContentValues();
                    for (int i = 0; i < c.getColumnCount() - 1; i++)
                        values.put(c.getColumnName(i), c.getString(i));
                    try {
                        db.insertOrThrow(MESSAGES_TABLE, null, values);
                    } catch (SQLiteConstraintException e) {
                        db.update(MESSAGES_TABLE, values, "id=?", new String[]{id});
                    }

                    updated = true;
                    break;
                default:
            }
        }
        return updated;
    }

    public Map getCurrentUserMessages() {

        Cursor c = null;
        Map<String,ContentValues> messages = new LinkedHashMap<>();

        if (userIsLogged()) {
            String sql = "SELECT * FROM " + MESSAGES_TABLE + " WHERE user_to_id=?";
            SQLiteDatabase db = getReadableDatabase();
            c = db.rawQuery(sql, new String[]{getCurrentUserID(db)});

            while (c.moveToNext()) {
                ContentValues values = new ContentValues();
                for (int i = 0; i < c.getColumnCount(); i++) values.put(c.getColumnName(i), c.getString(i));
                messages.put(c.getString(c.getColumnIndex("id")), values);
            }
            db.close();
        }
        return messages;
    }

    private String getCurrentUserID(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT user_id FROM " + USERS_TABLE + " WHERE loggedIn=?", new String[]{"1"});
        if (c.moveToFirst()) return c.getString(c.getColumnIndex("user_id"));
        return null;
    }
}