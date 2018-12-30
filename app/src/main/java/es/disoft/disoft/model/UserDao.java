package es.disoft.disoft.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("SELECT * FROM users WHERE loggedIn = 1")
    User getUserLoggedIn();

    @Query("SELECT DISTINCT userAlias FROM users")
    List<User.UserAlias> getAllUserAlias();

    @Query("SELECT DISTINCT dbAlias FROM users")
    List<User.DbAlias> getAllDbAlias();

    @Query("UPDATE users SET loggedIn = 0 WHERE id = :user_id")
    void logout(String user_id);
}