package es.disoft.disoft.user;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

public class Menu {

    private Context context;
    private String mUID;
    private Map<String, TreeMap<String, String>> menu;

    public Menu(Context context, String mUID) {
        this.context = context;
        this.mUID    = mUID;
        menu         = new LinkedHashMap<>();
    }

    public void loadMenu() {
        new JsonTask().execute(context.getString(R.string.URL_SYNC_MENU));
    }

    private void generateSkeleton() {
        ArrayList<String> menuItems = User.getMenuItems(context, mUID);

        for (String menuItem : menuItems) {
            menu.put(menuItem, User.getSubmenuItems(context, mUID, menuItem));
        }
    }

    private String jsonRequest(URL url) throws IOException {
        return HttpConnections.getUserMenu(context, url);
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

        @Override
        protected void onPostExecute(Void aVoid) {
            generateSkeleton();
        }
    }
}
