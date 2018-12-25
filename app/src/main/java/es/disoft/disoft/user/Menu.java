package es.disoft.disoft.user;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.webkit.WebView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import es.disoft.disoft.HttpConnections;
import es.disoft.disoft.MainActivity;
import es.disoft.disoft.R;
import es.disoft.disoft.db.DbHelper;

public class Menu {

    private MainActivity mainActivity;
    private Context context;
    private String mUID;
    private Map<String, TreeMap<String, String>> menu;

    ExpandableListView            expandableListView;
    ExpandableListAdapter         expandableListAdapter;
    List<MenuModel>                  headerList;
    HashMap<MenuModel, List<MenuModel>> childList;
    WebView webView;

    public Menu(MainActivity mainActivity, String mUID, ExpandableListView expandableListView) {
        this.menu               = new LinkedHashMap<>();
        this.mUID               = mUID;
        this.context            = mainActivity;
        this.expandableListView = expandableListView;
        this.webView = mainActivity.findViewById(R.id.webView);
        this.mainActivity = mainActivity;
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
        childList = new HashMap<>();
        headerList = new ArrayList<>();

        MenuModel menuModel = new MenuModel("Android WebView Tutorial", true, false, "https://www.journaldev.com/9333/android-webview-example-tutorial"); //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel);

        if (!menuModel.hasChildren) {
            childList.put(menuModel, null);
        }

        menuModel = new MenuModel("Java Tutorials", true, true, ""); //Menu of Java Tutorials
        headerList.add(menuModel);
        List<MenuModel> childModelsList = new ArrayList<>();
        MenuModel childModel = new MenuModel("Core Java Tutorial", false, false, "https://www.journaldev.com/7153/core-java-tutorial");
        childModelsList.add(childModel);

        childModel = new MenuModel("Java FileInputStream", false, false, "https://www.journaldev.com/19187/java-fileinputstream");
        childModelsList.add(childModel);

        childModel = new MenuModel("Java FileReader", false, false, "https://www.journaldev.com/19115/java-filereader");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            Log.d("API123","here");
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Python Tutorials", true, true, ""); //Menu of Python Tutorials
        headerList.add(menuModel);
        childModel = new MenuModel("Python AST – Abstract Syntax Tree", false, false, "https://www.journaldev.com/19243/python-ast-abstract-syntax-tree");
        childModelsList.add(childModel);

        childModel = new MenuModel("Python Fractions", false, false, "https://www.journaldev.com/19226/python-fractions");
        childModelsList.add(childModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
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

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        webView.loadUrl(headerList.get(groupPosition).url);
                        mainActivity.onBackPressed();
                    }
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
                        mainActivity.onBackPressed();
                    }
                }

                return false;
            }
        });
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
