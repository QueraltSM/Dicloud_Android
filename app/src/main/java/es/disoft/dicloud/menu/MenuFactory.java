package es.disoft.dicloud.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

import es.disoft.dicloud.HttpConnections;
import es.disoft.dicloud.R;
import es.disoft.dicloud.settings.SettingsActivity;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Menu;
import es.disoft.dicloud.model.MenuDao;
import es.disoft.dicloud.user.LoginActivity;
import es.disoft.dicloud.user.WebViewActivity;

import static android.provider.Settings.System.getString;
import static es.disoft.dicloud.R.string.URL_INDEX;
import static es.disoft.dicloud.user.WebViewActivity.closeSession;

public class MenuFactory {

    private Context context;
    private Map<String, List<Menu.SubmenuItem>> menu;

    private ExpandableListView              expandableListView;
    private ExpandableListAdapter           expandableListAdapter;
    private List<MenuModel>                 headerList;
    private Map<MenuModel, List<MenuModel>> childList;
    private WebView webView;

    private String URL_INDEX;

    public MenuFactory(Context context) {
        this.context = context;
    }

    public MenuFactory(Context context, ExpandableListView expandableListView) {
        menu                    = new LinkedHashMap<>();
        this.expandableListView = expandableListView;
        this.webView            = ((Activity) context).findViewById(R.id.webView);
        this.context            = context;

        URL_INDEX = WebViewActivity.URL_INDEX;
    }

    public void loadMenu(boolean networkAvailable) {

        if (networkAvailable) {
           new JsonTask().execute(context.getString(R.string.URL_SYNC_MENU));
        } else {
            Log.i("asdasd", "loadMenu: ");
            new Thread(){
                @Override
                public void run() {
                    generateSkeleton();
                    setMenu();
                }
            }.start();
        }
    }

    private void generateSkeleton() {
        MenuDao menuDao               = DisoftRoomDatabase.getDatabase(context).menuDao();
        List<Menu.MenuItem> menuItems = menuDao.getMenuItems();

        for (Menu.MenuItem menuItem: menuItems)
            menu.put(menuItem.menu, menuDao.getSubmenuItems(menuItem.menu));
    }

    private void setMenu() {
        headerList = new ArrayList<>();
        childList  = new LinkedHashMap<>();

        MenuModel menuModel = null;

//        menuModel = new MenuModel("Página principal", true, false, context.getString(R.string.URL_ROOT), R.drawable.ic_menu_home);
//        headerList.add(menuModel);
//        childList.put(menuModel, null);

        for (Map.Entry<String, List<Menu.SubmenuItem>> headerEntry : menu.entrySet()) {
            String menuHeader = headerEntry.getKey();

            if (!menuHeader.equals("Desconectar")) {
                menuModel = new MenuModel(menuHeader, true, true, "", null);
                headerList.add(menuModel);
            }

            if (menuModel != null && menuModel.hasChildren) {
                List<MenuModel> childModelsList = new ArrayList<>();
                MenuModel childModel;
                for (Menu.SubmenuItem submenuItem : headerEntry.getValue()) {
                    String url = URL_INDEX + submenuItem.url;
                    childModel = new MenuModel(submenuItem.submenu, false, false, url, null);
                    childModelsList.add(childModel);
                }
                childList.put(menuModel, childModelsList);
            } else {
                childList.put(menuModel, null);
            }
        }

        menuModel = new MenuModel(context.getString(R.string.menu_manage), true, false, "", R.drawable.ic_menu_manage);
        headerList.add(menuModel);
        childList.put(menuModel, null);
        String urlLogoutString = URL_INDEX + "disconect";
        menuModel = new MenuModel(context.getString(R.string.menu_disconect), true, false, urlLogoutString, R.drawable.ic_power_settings);
        headerList.add(menuModel);


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
                        case "Configuración":
                            context.startActivity(new Intent(context,SettingsActivity.class));
                            ((Activity) context).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                            break;
                        case "Desconectar":
                            closeSessionWithConfirmation();
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
        return HttpConnections.getData(url,context);
    }

    private void jsonResponse(final String menuAsJsonString) {
        try {
            JSONArray jArray = new JSONObject(menuAsJsonString).getJSONArray("usermenu");
            List<es.disoft.dicloud.model.Menu> menus = new ArrayList<>();
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);
                es.disoft.dicloud.model.Menu menu = new es.disoft.dicloud.model.Menu(
                        json_data.getString("menu"),
                        json_data.getString("submenu"),
                        json_data.getString("url"));
                menus.add(menu);
            }
            MenuDao menuDao = DisoftRoomDatabase.getDatabase(context).menuDao();
            menuDao.deleteAll();
            menuDao.insert(menus);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class JsonTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            Log.i("hilo", "start: ");

            try {
                String json = jsonRequest(new URL(params[0]));
                jsonResponse(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("hilo", "end: ");
            generateSkeleton();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setMenu();
        }
    }

    public void closeSessionWithConfirmation() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
//                TODO poner en espñaol
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        closeSession();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
}
