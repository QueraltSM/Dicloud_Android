package es.disoft.dicloud;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.User;

public class HttpConnections {

    public static JSONObject execute(String... params) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        JSONObject jsonObject = null;

        try {

            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

            String data = params[1];
            outputStream.writeBytes(data);
            outputStream.flush();
            outputStream.close();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) buffer.append(line);
            jsonObject = new JSONObject(buffer.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();

            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
            }
        }

        try {
            Log.i("JSON", "execute: " + jsonObject.toString());
        } catch (Exception ignored) { }
        return jsonObject;
    }


    public static String getData(URL url, Context mContext) {

        Map<String, String> userUID = new HashMap<>();
        if(User.currentUser == null)
            User.currentUser = DisoftRoomDatabase.getDatabase(mContext).userDao().getUserLoggedIn();

        JSONObject data = null;

        if (User.currentUser != null) {
            userUID.put("appSource", mContext.getString(R.string.app_name));
            userUID.put("token", User.currentUser.getToken());
            data = execute(url.toString(), new JSONObject(userUID).toString());
        }

        if (data == null) return null;
        else return data.toString();
    }
}
