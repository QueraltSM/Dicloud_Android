package es.disoft.disoft.service;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

class Messages {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

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
        return myDb.updateCurrentUserPendingMessages(messagesAsJsonString);
    }

    public static Map get(Context context) {
        DbHelper myDb = new DbHelper(context);
        return myDb.getCurrentUserMessages();
    }
}
