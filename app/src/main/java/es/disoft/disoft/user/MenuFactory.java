package es.disoft.disoft.user;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DisoftRoomDatabase;
import es.disoft.disoft.model.Menu;
import es.disoft.disoft.model.MenuDao;
import es.disoft.disoft.model.User;

public class MenuFactory {

    private Context context;
    private Map<String, List<Menu.SubmenuItem>> menu;

    private ExpandableListView              expandableListView;
    private ExpandableListAdapter           expandableListAdapter;
    private List<MenuModel>                 headerList;
    private Map<MenuModel, List<MenuModel>> childList;
    private WebView webView;

    public MenuFactory(Context context, ExpandableListView expandableListView) {
        menu                    = new LinkedHashMap<>();
        this.expandableListView = expandableListView;
        this.webView            = ((Activity) context).findViewById(R.id.webView);
        this.context            = context;
    }

    public void loadMenu() {
        new JsonTask().execute(context.getString(R.string.URL_SYNC_MENU));
    }

    private void generateSkeleton() {
        String user_id           = User.currentUser.getId();
        MenuDao menuDao          = DisoftRoomDatabase.getDatabase(context).menuDao();
        List<Menu.MenuItem> menuItems = menuDao.getMenuItems(user_id);

        for (Menu.MenuItem menuItem: menuItems) {
            menu.put(menuItem.menu, menuDao.getSubmenuItems(user_id, menuItem.menu));
        }

    }

    private void setMenu() {
        headerList = new ArrayList<>();
        childList  = new LinkedHashMap<>();

        MenuModel menuModel;

//        menuModel = new MenuModel("Página principal", true, false, context.getString(R.string.URL_ROOT), R.drawable.ic_menu_home);
//        headerList.add(menuModel);
//        childList.put(menuModel, null);

        for (Map.Entry<String, List<Menu.SubmenuItem>> headerEntry : menu.entrySet()) {
            String menuHeader = headerEntry.getKey();

            if (menuHeader.equals("Desconectar")) {
                menuModel = new MenuModel("Ajustes", true, false, "", R.drawable.ic_menu_manage);
                headerList.add(menuModel);
                childList.put(menuModel, null);
                String urlLogoutString = context.getString(R.string.URL_ROOT) + headerEntry.getValue().get(0).url;
                menuModel = new MenuModel(menuHeader, true, false, urlLogoutString, R.drawable.ic_power_settings);
            } else {
                menuModel = new MenuModel(menuHeader, true, true, "", null);
            }
            headerList.add(menuModel);

            if (menuModel.hasChildren) {
                List<MenuModel> childModelsList = new ArrayList<>();
                MenuModel childModel;
                for (Menu.SubmenuItem submenuItem : headerEntry.getValue()) {
                    String url = context.getString(R.string.URL_ROOT) + submenuItem.url;
                    childModel = new MenuModel(submenuItem.submenu, false, false, url, null);
                    childModelsList.add(childModel);
                }
                childList.put(menuModel, childModelsList);
            } else {
                childList.put(menuModel, null);
            }
        }


        expandableListAdapter = new CustomExpandableListAdapter(context, headerList, childList);

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
                            WebViewActivity.closeSessionWithConfirmation();
                            break;
                        default:
                            webView.loadUrl(headerList.get(groupPosition).url);
                    }

                    ((Activity) context).onBackPressed();
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
                        ((Activity) context).onBackPressed();
                    }
                }

                return false;
            }
        });
    }

    private String jsonRequest(URL url) {
        return HttpConnections.getData(url);
    }

    private void jsonResponse(final String menuAsJsonString) {
        try {
            JSONArray jArray = new JSONObject(menuAsJsonString).getJSONArray("usermenu");
            List<es.disoft.disoft.model.Menu> menus = new ArrayList<>();
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);
                es.disoft.disoft.model.Menu menu = new es.disoft.disoft.model.Menu(
                        json_data.getString("menu"),
                        json_data.getString("submenu"),
                        json_data.getString("url"));
                menus.add(menu);
            }
            MenuDao menuDao = DisoftRoomDatabase.getDatabase(context).menuDao();
            menuDao.deleteUserMenu(User.currentUser.getId());
            menuDao.insert(menus);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private class JsonTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                String json = jsonRequest(new URL(params[0]));
                jsonResponse(json);
                generateSkeleton();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setMenu();
                }
            });
        }
    }
}
