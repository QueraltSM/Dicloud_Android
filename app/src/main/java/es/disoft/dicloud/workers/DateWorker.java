package es.disoft.dicloud.workers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import es.disoft.dicloud.R;
import es.disoft.dicloud.db.DisoftRoomDatabase;
import es.disoft.dicloud.model.Date;
import es.disoft.dicloud.model.User;
import es.disoft.dicloud.notification.NotificationUtils;
import es.disoft.dicloud.user.Dates;

public class DateWorker { //extends Worker {

    /*public DateWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        checkDatesEvery5sc.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            User.currentUser = DisoftRoomDatabase.getDatabase(getApplicationContext()).userDao().getUserLoggedIn();
            checkDates(getApplicationContext());
            Log.wtf("WORKER", "doWork: HE ENTRADO SEEEEEEEEEEEEEEEH");
            return Result.success();
        } catch (Exception e) {
            Log.wtf("WORKER", "doWork: ", e);
            return Result.retry();
        }
    }

    public static void runDateWork(String UID, int repeatInterval) {

//        int syncFrequency = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sync_frequency", "15"));
        if (repeatInterval != -1) {
            PeriodicWorkRequest.Builder logCheckBuilder =
                    new PeriodicWorkRequest.Builder(
                            ChatWorker.class,
                            repeatInterval,
                            TimeUnit.MINUTES);

            PeriodicWorkRequest chatWork = logCheckBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                    UID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    chatWork);
//            WorkManager.getInstance().enqueue(chatWork);
        }else{
            cancelWork(UID);
        }
    }

    public static void cancelWork(String UID) {
        WorkManager.getInstance().cancelAllWorkByTag(UID);
    }

    public static class checkDatesEvery5sc {

        @SuppressLint("StaticFieldLeak")
        public static Context context;
        private static Thread thread;

        public void setContext(Context cnt) {
            context = cnt;
        }

        public static void start() {
            createThread();
            thread.start();
        }

        public static void stop() {
            thread.interrupt();
        }

        private static void createThread() {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!thread.isInterrupted()) {
                        try {
                            // TODO cambiar tiempo a 15 segundos?
                            Thread.sleep(5 * 1000);
                            checkDates(context);
//                            test();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
        }
    }

    private static void checkDates(Context context) {
        Log.i("vivo", "checkDates: ");
        if (User.currentUser != null)
            if (Dates.update(context)) notificateDates(context, Dates.getUpdated());
    }

    public static void notificateDates(Context context, List<?> Dates) {
        NotificationUtils mNotififacionUtils = new NotificationUtils(context);

        for (Object Date : Dates) {
            int DatesCount, id;
            String from;

            if (Date instanceof Date) {
                DatesCount = ((Date) Date).getDates_count();
                from          = ((Date) Date).getFrom();
                id            = ((Date) Date).getFrom_id();
            } else {
                DatesCount = ((Date.EssentialInfo) Date).getDates_count();
                from          = ((Date.EssentialInfo) Date).getFrom();
                id            = ((Date.EssentialInfo) Date).getFrom_id();
            }

            String text = DatesCount > 1 ? context.getString(R.string.new_date_with) : context.getString(R.string.new_date_with);
            String title = User.currentUser.getDbAlias();
            text = DatesCount + " " + text + " " + from;

            mNotififacionUtils.createNotification(id, title, text);
            mNotififacionUtils.show();
        }

        ArrayList<Date> deletedDates = Dates.getDeleted();
        if (deletedDates != null) {
            for (Date date : deletedDates)
                mNotififacionUtils.clear(date.getFrom_id());
        }
    }*/
}
