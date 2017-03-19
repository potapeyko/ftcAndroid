package potapeyko.rss.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import potapeyko.rss.R;
import potapeyko.rss.utils.UpdateAlarmListener;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            return;
        }
        Intent in = new Intent(context, UpdateAlarmListener.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                in, PendingIntent.FLAG_CANCEL_CURRENT);//флаг - отмена такого-же интента
        SharedPreferences sPref;
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoUpdate = sPref.getBoolean(context.getString(R.string.settings_auto_update_key), true);
        if (isAutoUpdate) {
            Toast.makeText(context.getApplicationContext(),
                    "it work ftc return isAutoUpdate", Toast.LENGTH_LONG).show();
            long nextPlanedUpdate = sPref.getLong(UpdateAlarmListener.NEXT_UPDATE_TIME_KEY,
                    System.currentTimeMillis());//если обновление не запл. то обновиться сразу
            long nextUpdateForPeriod = 1000 * 60 * Long.parseLong(
                    sPref.getString(context.getString(R.string.settings_period_key), "0"))
                    + System.currentTimeMillis();
            long nextUpdate = nextPlanedUpdate > nextUpdateForPeriod ? nextUpdateForPeriod : nextPlanedUpdate;
            am.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
        } else {
            Toast.makeText(context.getApplicationContext(),
                    "it work ftc return not isAutoUpdate", Toast.LENGTH_LONG).show();
            am.cancel(pendingIntent);
        }
    }
}
