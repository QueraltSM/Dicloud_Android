package es.disoft.disoft.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

@Dao
public interface MessageDao_tmp extends BaseDao<Message_tmp> {

    @Query("DELETE FROM messages_tmp")
    void deleteAll();
}