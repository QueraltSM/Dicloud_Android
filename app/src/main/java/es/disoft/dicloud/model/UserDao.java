package es.disoft.dicloud.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface UserDao extends BaseDao<User> {

    @Query("SELECT * FROM users WHERE loggedIn = 1")
    User getUserLoggedIn();

    @Query("SELECT DISTINCT userAlias FROM users")
    List<User.UserAlias> getAllUserAlias();

    @Query("SELECT DISTINCT dbAlias FROM users")
    List<User.DbAlias> getAllDbAlias();

    @Query("UPDATE users SET loggedIn = 0 WHERE id = :user_id")
    void logout(String user_id);
}