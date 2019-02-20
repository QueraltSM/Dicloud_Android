package es.disoft.dicloud.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MessageDao extends BaseDao<Message> {

    @Query("SELECT * FROM messages")
    List<Message> getAllMessages();

    @Query("SELECT COUNT(DISTINCT from_id) FROM messages")
    int count();

    @Query("SELECT from_id,`from`,messages_count FROM messages ORDER BY last_message_timestamp DESC")
    List<Message.EssentialInfo> getAllMessagesEssentialInfo();

    @Query("DELETE FROM messages WHERE from_id = :id")
    void delete(int id);

    @Query("DELETE FROM messages")
    void deleteAll();

    @Query("SELECT * FROM" +
            "(" +
            "SELECT messages.*, " +
            "       'equal' status " +
            "FROM   messages " +
            "       INNER JOIN messages_tmp " +
            "               ON messages.user_id = messages_tmp.user_id " +
            "               AND messages.from_id = messages_tmp.from_id " +
            "               AND messages.last_message_timestamp = messages_tmp.last_message_timestamp " +
            "UNION " +
            "SELECT messages.*, " +
            "       'deleted' status " +
            "FROM   messages " +
            "WHERE  NOT EXISTS (SELECT * " +
            "                   FROM   messages_tmp " +
            "                   WHERE  messages.user_id = messages_tmp.user_id " +
            "                   AND    messages.from_id = messages_tmp.from_id) " +
            "UNION " +
            "SELECT messages_tmp.*, " +
            "       'updated' status " +
            "FROM   messages_tmp " +
            "WHERE  NOT EXISTS (SELECT * " +
            "                   FROM   messages " +
            "                   WHERE  messages.user_id = messages_tmp.user_id " +
            "                   AND    messages.from_id = messages_tmp.from_id " +
            "                   AND    messages.last_message_timestamp = messages_tmp.last_message_timestamp) " +
            ") " +
            "WHERE user_id = :user_id")
    List<Message.Fetch> fetch(String user_id);
}