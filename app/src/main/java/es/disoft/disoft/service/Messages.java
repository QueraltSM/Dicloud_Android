package es.disoft.disoft.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.MessageDao;
import es.disoft.disoft.model.Message_tmp;
import es.disoft.disoft.model.User;

class Messages {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static ArrayList<Message> updatedMessages;

    static boolean update(Context context) {
        mContext = context;

        try {
            String url = mContext.getString(R.string.URL_SYNC_MESSAGES);
            String jsonResponse = jsonRequest(new URL(url));
            updateMessages(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();

        }

        return !updatedMessages.isEmpty();
    }

    private static String jsonRequest(URL url) {
        return HttpConnections.getData(url);
    }

    private static void updateMessages(String messagesAsJsonString) throws JSONException {

        storeNewMessages(messagesAsJsonString);

        updatedMessages = new ArrayList<>();
        MessageDao messageDao = DisoftRoomDatabase.getDatabase(mContext).messageDao();
        List<Message.Fetch> fetch = messageDao.fetch(User.currentUser.getId());

        for (Message.Fetch message : fetch) {
            switch (message.getStatus()) {
                case "deleted":
                    messageDao.delete(message);
                    break;
                case "updated":
                    messageDao.insert(message);
                    updatedMessages.add(message);
                    break;
                default:
            }
        }

        DisoftRoomDatabase.getDatabase(mContext).messageDao_tmp().deleteAll();
    }

    private static void storeNewMessages(String messagesAsJsonString) throws JSONException {
        JSONArray jArray = new JSONObject(messagesAsJsonString).getJSONArray("messages");
        List<Message_tmp> newMessages = new ArrayList<>();
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject json_data = jArray.getJSONObject(i);

            int from_id                   = json_data.getInt("from_id");
            String from                   = json_data.getString("from");
            String last_message_timestamp = json_data.getString("last_message_timestamp");
            int messages_count            = json_data.getInt("messages_count");

            newMessages.add(new Message_tmp(from_id, from, last_message_timestamp, messages_count));
        }

        Log.w("mensajes", newMessages.toString());
        DisoftRoomDatabase.getDatabase(mContext).messageDao_tmp().insert(newMessages);

        Log.i("mensaje", "updateMessages: " + messagesAsJsonString);
    }

    public static ArrayList<Message> getUpdated() {
        return updatedMessages;
    }
}
