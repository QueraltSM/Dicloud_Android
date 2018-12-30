package es.disoft.disoft.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    void insert(Message message);

    @Insert
    void insert(List<Message> messages);

    @Query("SELECT * FROM messages")
    List<Message> getAllMessages();

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    @Query("DELETE FROM messages")
    void deleteAll();

    @Query("SELECT * FROM"                                                                                              +
                    "("                                                                                                 +
                    "SELECT messages.*, "                                                                               +
                    "       'equal' status "                                                                            +
                    "FROM   messages "                                                                                  +
                    "       INNER JOIN messages_tmp "                                                                   +
                    "               ON messages.user_id = messages_tmp.user_id "                                        +
                    "               AND messages.from_id = messages_tmp.from_id "                                       +
                    "               AND messages.last_message_timestamp = messages_tmp.last_message_timestamp "         +
                    "UNION "                                                                                            +
                    "SELECT messages.*, "                                                                               +
                    "       'deleted' status "                                                                          +
                    "FROM   messages "                                                                                  +
                    "WHERE  NOT EXISTS (SELECT * "                                                                      +
                    "                   FROM   messages_tmp "                                                           +
                    "                   WHERE  messages.user_id = messages_tmp.user_id "                                +
                    "                   AND    messages.from_id = messages_tmp.from_id) "                               +
                    "UNION "                                                                                            +
                    "SELECT messages_tmp.*, "                                                                           +
                    "       'updated' status "                                                                          +
                    "FROM   messages_tmp "                                                                              +
                    "WHERE  NOT EXISTS (SELECT * "                                                                      +
                    "                   FROM   messages "                                                               +
                    "                   WHERE  messages.user_id = messages_tmp.user_id "                                +
                    "                   AND    messages.from_id = messages_tmp.from_id "                                +
                    "                   AND    messages.last_message_timestamp = messages_tmp.last_message_timestamp) " +
                    ") "                                                                                                +
                    "WHERE user_id = :user_id")
    List<Message.Fetch> fetch(String user_id);
}