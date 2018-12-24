package es.disoft.disoft.user;


import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

public class Menu {

    private final Context context;

    public Menu(Context context) {
        this.context = context;
    }

    public void setMenu() {
        new JsonTask().execute(context.getString(R.string.URL_SYNC_MENU));
    }

    private String jsonRequest(URL url) throws IOException {
        return HttpConnections.getUserMenu(url);
    }

    private void jsonResponse(String menuAsJsonString) throws JSONException {
        DbHelper myDb = new DbHelper(context);
        myDb.setCurrentUserMenu(menuAsJsonString);
    }

    private class JsonTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                String json = jsonRequest(new URL(params[0]));
                jsonResponse(json);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
