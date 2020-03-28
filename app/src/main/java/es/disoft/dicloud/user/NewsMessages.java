package es.disoft.dicloud.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.disoft.dicloud.HttpConnections;
import es.disoft.dicloud.R;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Message;
import es.disoft.dicloud.model.MessageDao;
import es.disoft.dicloud.model.Message_tmp;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.workers.NewsWorker;

public class NewsMessages {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static ArrayList<Message> updatedMessages;
    private static ArrayList<Message> deletedMessages;
    private static boolean messageFromNews;
    private static ArrayList<Integer> lastCount = new ArrayList<>();
    private static boolean showUpdate = true;
    private static boolean deleted = false;

    public static synchronized boolean update(Context context) {
        mContext = context;
        try {
            lastCount.add(0);
            lastCount.add(0);
            String url = mContext.getString(R.string.URL_SYNC_MESSAGES_NEWS);
            System.out.println("chat messages = " + url);
            String jsonResponse = jsonRequest(new URL(url));
            updateMessages(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return updatedMessages != null && showUpdate && (!updatedMessages.isEmpty() || !deletedMessages.isEmpty());
    }

    private static String jsonRequest(URL url) {
        return HttpConnections.getData(url,mContext);
    }

    private static void updateMessages(String messagesAsJsonString) throws JSONException {
        if (User.currentUser != null) {
            if (messagesAsJsonString != null) storeNewMessages(messagesAsJsonString);
            updatedMessages = new ArrayList<>();
            deletedMessages = new ArrayList<>();
            MessageDao messageDao = DisoftRoomDatabase.getDatabase(mContext).messageDao();
            List<Message.Fetch> fetch = messageDao.fetch(User.currentUser.getId());
            Log.i("mensajeee", "fetched: " + fetch.toString());
            for (Message.Fetch message : fetch) {
                switch (message.getStatus()) {
                    case "deleted":
                        Log.e("mensajeee", "deleted: " + message.toString());
                        messageDao.delete(message.getFrom_id());
                        deletedMessages.add(message);
                        showUpdate = false;
                        break;
                    case "updated":
                        Log.e("mensajeee", "updated: " + message.toString());
                        messageDao.insert(message);
                        updatedMessages.add(message);
                        break;
                    default:
                }
            }
        }
    }

    public static boolean getMessageFromNews() {
        return messageFromNews;
    }

    public static void setMessageFromNews(boolean messageFromNews) {
        NewsMessages.messageFromNews = messageFromNews;
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

            if (i<lastCount.size() && messages_count > lastCount.get(i)) {
                lastCount.set(i, messages_count);
                newMessages.add(new Message_tmp(from_id, from, last_message_timestamp, lastCount.get(i)));
                List<Message> ms = new ArrayList<>();
                for (Message_tmp m : newMessages) {
                    ms.add(new Message(m.getFrom_id(),m.getFrom(),m.getLast_message_timestamp(), m.getMessages_count()));
                }
                NewsWorker.notificateMessages(mContext,ms);
                messageFromNews = true;
                showUpdate = true;
            } else if (deleted && messages_count==1 && lastCount.get(i)==1) {
                showUpdate = true;
                deleted = false;
            } else {
                lastCount.set(i, messages_count);
                showUpdate = false;
            }
        }
        Log.w("mensajes", newMessages.toString());
        DisoftRoomDatabase.getDatabase(mContext).messageDao_tmp().insert(newMessages);
        Log.i("mensaje", "updateMessages: " + messagesAsJsonString);
    }

    public static ArrayList<Message> getUpdated() {
        return updatedMessages;
    }

    public static ArrayList<Message> getDeleted() {
        return deletedMessages;
    }
}