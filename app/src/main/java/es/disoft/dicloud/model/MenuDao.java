package es.disoft.dicloud.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MenuDao extends BaseDao<Menu> {

    @Query("SELECT * FROM menus")
    List<Menu> getAllMenus();

    @Query("SELECT DISTINCT menu FROM menus ORDER BY id")
    List<Menu.MenuItem> getMenuItems();

    @Query("SELECT submenu, url FROM menus WHERE menu = :menuItem ORDER BY id")
    List<Menu.SubmenuItem> getSubmenuItems(String menuItem);

    @Query("DELETE FROM menus")
    void deleteAll();
}
