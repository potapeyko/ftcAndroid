package potapeyko.rss.activities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//сервис проверяющий обновления.
public class CheckNewsService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Log.d("myLogs", "Вход в change() выполнен");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
