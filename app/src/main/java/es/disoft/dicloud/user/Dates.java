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
import es.disoft.dicloud.model.Date;
import es.disoft.dicloud.model.DateDao;
import es.disoft.dicloud.model.Date_tmp;
import es.disoft.dicloud.model.User;

public class Dates {

    /*@SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static ArrayList<Date> updatedDates;
    private static ArrayList<Date> deletedDates;

    public static synchronized boolean update(Context context) {
        mContext = context;

        try {
            String url = mContext.getString(R.string.URL_GET_DATES);
            String jsonResponse = jsonRequest(new URL(url));
            updateDates(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();

        }
        return updatedDates != null && (!updatedDates.isEmpty() || !deletedDates.isEmpty());
    }

    private static String jsonRequest(URL url) {
        return HttpConnections.getData(url,mContext);
    }

    private static void updateDates(String DatesAsJsonString) throws JSONException {

        if (User.currentUser != null) {
            if (DatesAsJsonString != null) storeNewDates(DatesAsJsonString);

            updatedDates = new ArrayList<>();
            deletedDates = new ArrayList<>();
            DateDao DateDao = DisoftRoomDatabase.getDatabase(mContext).dateDao();
            List<Date.Fetch> fetch = DateDao.fetch(User.currentUser.getId());
            Log.i("mensajeee", "fetched: " + fetch.toString());
            for (Date.Fetch Date : fetch) {
                switch (Date.getStatus()) {
                    case "deleted":
                        Log.e("mensajeee", "deleted: " + Date.toString());
                        DateDao.delete(Date.getFrom_id());
                        deletedDates.add(Date);
                        break;
                    case "updated":
                        Log.e("mensajeee", "updated: " + Date.toString());
                        DateDao.insert(Date);
                        updatedDates.add(Date);
                        break;
                    default:
                }
            }
            DisoftRoomDatabase.getDatabase(mContext).dateDao_tmp().deleteAll();
        }

    }

    private static void storeNewDates(String DatesAsJsonString) throws JSONException {
        JSONArray jArray = new JSONObject(DatesAsJsonString).getJSONArray("Dates");

        List<Date_tmp> newDates = new ArrayList<>();
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject json_data = jArray.getJSONObject(i);

            int from_id                   = json_data.getInt("from_id");
            String from                   = json_data.getString("from");
            String last_Date_timestamp = json_data.getString("last_Date_timestamp");
            int Dates_count            = json_data.getInt("Dates_count");

            newDates.add(new Date_tmp(from_id, from, last_Date_timestamp, Dates_count));
        }

        Log.w("mensajes", newDates.toString());
        DisoftRoomDatabase.getDatabase(mContext).dateDao_tmp().insert(newDates);

        Log.i("mensaje", "updateDates: " + DatesAsJsonString);
    }

    public static ArrayList<Date> getUpdated() { return updatedDates; }
    public static ArrayList<Date> getDeleted() {
        return deletedDates;
    }*/
}
