package es.disoft.disoft;

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

            Log.i("LOGIN_", "execute: ");
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

        Log.i("JSON", "execute: " + jsonObject.toString());
        return jsonObject;
    }

    public static String getUserMenu(URL url) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader        = null;

        connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        InputStream stream = connection.getInputStream();
        reader             = new BufferedReader(new InputStreamReader(stream));

        StringBuilder buffer = new StringBuilder();
        String line          = "";

        while ((line = reader.readLine()) != null) buffer.append(line).append("\n");

        connection.disconnect();
        reader.close();

        return buffer.toString();
    }
}
