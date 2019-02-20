package es.disoft.dicloud;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;


public class ConnectionAvailable extends AsyncTask<Void, Void, Boolean> {

    private final String URL;
    public ConnectionAvailable(String url) {
        URL = url;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        try {
            Socket sock = new Socket();
            // Check the connection to the web
//            String ip = "8.8.8.8";
            String ip = InetAddress.getByName(new URL(URL).getHost()).getHostAddress();
            if (ip.equals("::1")) return true;
            sock.connect(new InetSocketAddress(ip, 80), 5000);
            sock.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}