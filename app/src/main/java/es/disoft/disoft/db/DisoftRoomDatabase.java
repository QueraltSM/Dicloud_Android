package es.disoft.disoft.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import es.disoft.disoft.model.Menu;
import es.disoft.disoft.model.MenuDao;
import es.disoft.disoft.model.Message;
import es.disoft.disoft.model.MessageDao;
import es.disoft.disoft.model.MessageDao_tmp;
import es.disoft.disoft.model.Message_tmp;
import es.disoft.disoft.model.User;
import es.disoft.disoft.model.UserDao;

@Database(entities = {User.class, Menu.class, Message.class, Message_tmp.class}, version = 1)
public abstract class DisoftRoomDatabase extends RoomDatabase {

    // marking the instance as volatile to ensure atomic access to the variable
    private static volatile DisoftRoomDatabase INSTANCE;
    private static final    String DATABASE_NAME = "disoft.db";

    public abstract UserDao        userDao();
    public abstract MenuDao        menuDao();
    public abstract MessageDao     messageDao();
    public abstract MessageDao_tmp messageDao_tmp();

    public static DisoftRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DisoftRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DisoftRoomDatabase.class,
                            DATABASE_NAME)
//                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
