package es.disoft.disoft.user;

import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.MainActivity;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

public class Menu {

    private MainActivity activity;
    private String mUID;
    private Map<String, Map<String, String>> menu;

    private ExpandableListView                  expandableListView;
    private ExpandableListAdapter               expandableListAdapter;
    private List<MenuModel>                     headerList;
    private Map<MenuModel, List<MenuModel>>     childList;
    private WebView webView;

    public Menu(MainActivity activity, String mUID, ExpandableListView expandableListView) {
        this.menu               = new LinkedHashMap<>();
        this.mUID               = mUID;
        this.expandableListView = expandableListView;
        this.webView            = activity.findViewById(R.id.webView);
        this.activity = activity;
    }

    public void loadMenu() {
        new JsonTask().execute(activity.getString(R.string.URL_SYNC_MENU));
    }

    private void generateSkeleton() {
        ArrayList<String> menuItems = User.getMenuItems(activity, mUID);

        for (String menuItem : menuItems) {
            menu.put(menuItem, User.getSubmenuItems(activity, mUID, menuItem));
        }

        setMenu();
    }

    private void setMenu() {
        headerList = new ArrayList<>();
        childList  = new LinkedHashMap<>();

        MenuModel menuModel;
        menuModel = new MenuModel("Página principal", true, false, activity.getString(R.string.URL_ROOT), R.drawable.ic_menu_home);
        headerList.add(menuModel);
        childList.put(menuModel, null);

        for (Map.Entry<String, Map<String, String>> headerEntry : menu.entrySet()) {
            String menuHeader                    = headerEntry.getKey();

            if (menuHeader.equals("Desconectar")) {
                menuModel = new MenuModel("Ajustes", true, false, "", R.drawable.ic_menu_manage);
                headerList.add(menuModel);
                childList.put(menuModel, null);
                String urlLogoutString = activity.getString(R.string.URL_ROOT) + headerEntry.getValue().get("Desconectar");
                menuModel = new MenuModel(menuHeader, true, false, urlLogoutString, R.drawable.ic_power_settings);
            } else {
                menuModel = new MenuModel(menuHeader, true, true, "", null);
            }
            headerList.add(menuModel);

            if (menuModel.hasChildren) {
                List<MenuModel> childModelsList = new ArrayList<>();
                MenuModel childModel;
                for (Map.Entry<String, String> childEntry : headerEntry.getValue().entrySet()) {
                    String menuChild = childEntry.getKey();
                    String menulink  = childEntry.getValue();
                    childModel       = new MenuModel(menuChild, false, false, activity.getString(R.string.URL_ROOT) + menulink, null);
                    childModelsList.add(childModel);
                }
                childList.put(menuModel, childModelsList);
            } else {
                childList.put(menuModel, null);
            }
        }


        expandableListAdapter = new CustomExpandableListAdapter(activity, headerList, childList);

        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) { }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) { }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup && !headerList.get(groupPosition).hasChildren) {

                    switch (headerList.get(groupPosition).menuName) {
                        case "Ajustes":
                            break;
                        case "Desconectar":
                            User.logoutWithConfirmation(activity);
                            break;
                        default:
                            webView.loadUrl(headerList.get(groupPosition).url);
                    }

                    activity.onBackPressed();
                }

                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    if (model.url.length() > 0) {
                        webView.loadUrl(model.url);
                        activity.onBackPressed();
                    }
                }

                return false;
            }
        });
    }

    private String jsonRequest(URL url) throws IOException {
        return HttpConnections.getUserMenu(activity, url);
    }

    private void jsonResponse(String menuAsJsonString) throws JSONException {
        DbHelper myDb = new DbHelper(activity);
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
