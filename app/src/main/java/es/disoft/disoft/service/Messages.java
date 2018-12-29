package es.disoft.disoft.service;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

class Messages {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static ArrayList<ContentValues> updatedMessages;

    static boolean update(Context context) {
        mContext = context;
        Boolean newMessages = false;

        try {
            String url = mContext.getString(R.string.URL_SYNC_MESSAGES);
            String jsonResponse = jsonRequest(new URL(url));
            newMessages = updateMessages(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();

        }

        return newMessages;
    }

    private static String jsonRequest(URL url) throws IOException {
        return HttpConnections.getData(mContext, url);
    }

    private static Boolean updateMessages(String messagesAsJsonString) throws JSONException {
        DbHelper myDb = new DbHelper(mContext);
        updatedMessages = myDb.updateCurrentUserPendingMessages(messagesAsJsonString);
        return !updatedMessages.isEmpty();
    }

    public static Map get(Context context) {
        DbHelper myDb = new DbHelper(context);
        return myDb.getCurrentUserMessages();
    }

    public static ArrayList<ContentValues> getUpdated() {
        return updatedMessages;
    }
}
