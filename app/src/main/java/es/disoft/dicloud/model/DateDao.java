package es.disoft.dicloud.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

//@Dao
public interface DateDao extends BaseDao<Date> {

   /* @Query("SELECT * FROM Dates")
    List<Date> getAllDates();

    @Query("SELECT COUNT(DISTINCT from_id) FROM Dates")
    int count();

    @Query("SELECT from_id,`from`,Dates_count FROM Dates ORDER BY last_Date_timestamp DESC")
    List<Date.EssentialInfo> getAllDatesEssentialInfo();

    @Query("DELETE FROM Dates WHERE from_id = :id")
    void delete(int id);

    @Query("DELETE FROM Dates")
    void deleteAll();

    @Query("SELECT * FROM" +
            "(" +
            "SELECT Dates.*, " +
            "       'equal' status " +
            "FROM   Dates " +
            "       INNER JOIN Dates_tmp " +
            "               ON Dates.user_id = Dates_tmp.user_id " +
            "               AND Dates.from_id = Dates_tmp.from_id " +
            "               AND Dates.last_Date_timestamp = Dates_tmp.last_Date_timestamp " +
            "UNION " +
            "SELECT Dates.*, " +
            "       'deleted' status " +
            "FROM   Dates " +
            "WHERE  NOT EXISTS (SELECT * " +
            "                   FROM   Dates_tmp " +
            "                   WHERE  Dates.user_id = Dates_tmp.user_id " +
            "                   AND    Dates.from_id = Dates_tmp.from_id) " +
            "UNION " +
            "SELECT Dates_tmp.*, " +
            "       'updated' status " +
            "FROM   Dates_tmp " +
            "WHERE  NOT EXISTS (SELECT * " +
            "                   FROM   Dates " +
            "                   WHERE  Dates.user_id = Dates_tmp.user_id " +
            "                   AND    Dates.from_id = Dates_tmp.from_id " +
            "                   AND    Dates.last_Date_timestamp = Dates_tmp.last_Date_timestamp) " +
            ") " +
            "WHERE user_id = :user_id")
    List<Date.Fetch> fetch(String user_id);*/
}