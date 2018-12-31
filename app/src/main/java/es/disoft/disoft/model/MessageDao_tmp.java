package es.disoft.disoft.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MessageDao_tmp {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Message_tmp> messages);

    @Query("SELECT * FROM messages_tmp")
    List<Message_tmp> getAllMessages();

    @Delete
    void delete(Message_tmp message);

    @Query("DELETE FROM messages_tmp")
    void deleteAll();
}