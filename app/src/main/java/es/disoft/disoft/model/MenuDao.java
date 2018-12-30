package es.disoft.disoft.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MenuDao {

    @Insert
    void insert(List<Menu> menus);

    @Query("SELECT * FROM menus")
    List<Menu> getAllMenus();

    @Query("SELECT DISTINCT menu FROM menus WHERE user_id = :user_id ORDER BY id")
    List<Menu.MenuItem> getMenuItems(String user_id);

    @Query("SELECT submenu, url FROM menus WHERE user_id = :user_id AND menu = :menuItem ORDER BY id")
    List<Menu.SubmenuItem> getSubmenuItems(String user_id, String menuItem);

    @Query("DELETE FROM menus WHERE user_id = :user_id")
    void deleteUserMenu(String user_id);
}
