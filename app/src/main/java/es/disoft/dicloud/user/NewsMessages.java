package es.disoft.dicloud.user;

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

import es.disoft.dicloud.HttpConnections;
import es.disoft.dicloud.R;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Message;
import es.disoft.dicloud.model.MessageDao;
import es.disoft.dicloud.model.Message_tmp;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.workers.MessagesWorker;

public class NewsMessages {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static ArrayList<Message> updatedMessages;
    private static ArrayList<Message> deletedMessages;
    private static boolean messageFromNews = false;
    private static ArrayList<Integer> lastCount = new ArrayList<>();

    public static synchronized boolean update(Context context) {
        mContext = context;
        try {
            lastCount.add(0);
            lastCount.add(0);
            String url = mContext.getString(R.string.URL_SYNC_MESSAGES_NEWS);
            String jsonResponse = jsonRequest(new URL(url));
            updateMessages(jsonResponse);
           // url = mContext.getString(R.string.URL_SYNC_MESSAGES);
           // jsonResponse = jsonRequest(new URL(url));
           // updateMessages(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();

        }
        return updatedMessages != null && (!updatedMessages.isEmpty() || !deletedMessages.isEmpty());
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
            Log.i("mensaje en NewsMessages", "fetched: " + fetch.toString());
            for (Message.Fetch message : fetch) {
                System.out.println("antes del switch = " + message.getStatus());
                switch (message.getStatus()) {
                    case "deleted":
                        Log.e("mensajeee", "deleted: " + message.toString());
                        messageDao.delete(message.getFrom_id());
                        deletedMessages.add(message);
                        messageFromNews = false;
                        break;
                    case "updated":
                        Log.e("mensajeee", "updated: " + message.toString());
                        messageDao.insert(message);
                        updatedMessages.add(message);
                        messageFromNews= true;
                        break;
                    default:
                }
            }
            DisoftRoomDatabase.getDatabase(mContext).messageDao_tmp().deleteAll();
        }

    }

    public static boolean getMessageFromNews() {
        return messageFromNews;
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

            System.out.println("i size = " + i);
            System.out.println("last count size = " + lastCount.size());
            if (i<lastCount.size() && messages_count != lastCount.get(i)) {
                System.out.println("DISTINTO = " +
                        messages_count + " : - :" + lastCount.get(i));
                lastCount.set(i, messages_count);
                newMessages.add(new Message_tmp(from_id, from, last_message_timestamp, lastCount.get(i)));
                List<Message> ms = new ArrayList<>();
                for (Message_tmp m : newMessages) {
                    ms.add(new Message(m.getFrom_id(),m.getFrom(),m.getLast_message_timestamp(), m.getMessages_count()));
                }
                messageFromNews = true;
                //MessagesWorker.notificateMessages(mContext,ms);
            }
        }

        System.out.println("CUENTA = " + newMessages.size());
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