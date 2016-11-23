package potapeyko.rss.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import potapeyko.rss.R;
import potapeyko.rss.activities.UpdateIntentService;


public class UpdateAlarmListener extends BroadcastReceiver {

    private SharedPreferences sPref;
    private Context context;
    private static final String DEFAULT_PERIOD = "1440";
    public static final String NEXT_UPDATE_TIME_KEY = "nextUpdate";




    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }


        this.context = context;
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        UpdateIntentService.startActionUpdate(context);
        if (sPref.getBoolean(context.getString(R.string.settings_auto_update_key), true)) {

            final Long nextUpdateTime = setNextUpdateTime();
            am.set(AlarmManager.RTC_WAKEUP, nextUpdateTime, pendingIntent);

        }
    }

    /**
     * @return time of next update in milliseconds
     */
    private long setNextUpdateTime() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final Long period = Long.parseLong(sp.getString(
                context.getString(R.string.settings_period_key),
                DEFAULT_PERIOD));
        final SharedPreferences.Editor ed = sPref.edit();
        final Long nextUpdateTime = System.currentTimeMillis() + period * 1000 * 60;
        ed.putLong(NEXT_UPDATE_TIME_KEY, nextUpdateTime);
        ed.apply();
        return nextUpdateTime;
    }

}
