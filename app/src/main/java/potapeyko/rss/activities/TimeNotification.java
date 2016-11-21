package potapeyko.rss.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class TimeNotification extends BroadcastReceiver {

    private SharedPreferences sPref;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Log.d("UpdateIntentService", "UpdateIntentService");
        UpdateIntentService.startActionUpdate(context);
        if(sPref.getBoolean("auto_update",true)) {
            setNextUpdateTime();
            Long nextUpdate = sPref.getLong("nextUpdate", 0);
            if (nextUpdate == 0) {
                nextUpdate = System.currentTimeMillis();
                Log.d("UpdateIntentService", "WTF");
            }
            am.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
        }
    }


    private void setNextUpdateTime() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Long periodInMinute = Long.parseLong(sp.getString("period", "20"));
        SharedPreferences.Editor ed = sPref.edit();
        ed.putLong("nextUpdate", System.currentTimeMillis() + periodInMinute * 1000 * 60);
        ed.apply();
    }
}
