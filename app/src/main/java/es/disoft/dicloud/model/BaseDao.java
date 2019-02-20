package es.disoft.dicloud.model;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import java.util.List;

public interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(T obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<T> objs);

    @Update
    void update(T obj);

    @Delete
    void delete(T obj);
}
