package es.disoft.dicloud.menu;

public class MenuModel {

    public String menuName;
    public String url;
    public Integer iconPathId;
    public boolean hasChildren, isGroup;

    public MenuModel(String menuName, boolean isGroup, boolean hasChildren, String url, Integer iconPathId) {

        this.menuName    = menuName;
        this.url         = url;
        this.isGroup     = isGroup;
        this.hasChildren = hasChildren;
        this.iconPathId  = iconPathId;
    }
}