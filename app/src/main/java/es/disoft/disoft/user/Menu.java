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
    private android.view.Menu nv_menu;

    public Menu(Context context, String mUID, android.view.Menu nv_menu) {
        this.menu    = new LinkedHashMap<>();
        this.mUID    = mUID;
        this.context = context;
        this.nv_menu = nv_menu;
    }

    public void loadMenu() {
        new JsonTask().execute(context.getString(R.string.URL_SYNC_MENU));
    }

    private void generateSkeleton() {
        ArrayList<String> menuItems = User.getMenuItems(context, mUID);

        for (String menuItem : menuItems) {
            menu.put(menuItem, User.getSubmenuItems(context, mUID, menuItem));
        }

        setMenu();
    }

    private void setMenu() {
        int NONE = android.view.Menu.NONE;
        for (Map.Entry<?, ?> entry : menu.entrySet()) {
            nv_menu.add(0, NONE, NONE, (CharSequence) entry.getKey());
//            System.out.println(entry.getKey() + "/" + entry.getValue());
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
