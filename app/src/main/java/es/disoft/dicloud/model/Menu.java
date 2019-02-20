package es.disoft.dicloud.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName   = "menus",
        indices     = @Index("user_id"),
        foreignKeys = @ForeignKey(entity        = User.class,
                                  parentColumns = "id",
                                  childColumns  = "user_id",
                                  onDelete      = ForeignKey.CASCADE))
public class Menu {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    @ColumnInfo(name = "user_id")
    @NonNull
    private String user_id;
    @NonNull
    private String menu;
    @NonNull
    private String submenu;
    @NonNull
    private String url;

    public Menu(@NonNull String menu,
                @NonNull String submenu,
                @NonNull String url) {
        user_id      = User.currentUser.getId();
        this.menu    = menu;
        this.submenu = submenu;
        this.url     = url;
    }

    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    @NonNull
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(@NonNull String user_id) {
        this.user_id = user_id;
    }

    @NonNull
    public String getMenu() {
        return menu;
    }

    public void setMenu(@NonNull String menu) {
        this.menu = menu;
    }

    @NonNull
    public String getSubmenu() {
        return submenu;
    }

    public void setSubmenu(@NonNull String submenu) {
        this.submenu = submenu;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public static class MenuItem {
        public String menu;
    }

    public static class SubmenuItem {
        public String submenu;
        public String url;
    }
}

