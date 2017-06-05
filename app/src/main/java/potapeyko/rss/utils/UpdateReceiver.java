package potapeyko.rss.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.activities.UpdateChannelIntentService;


public class UpdateReceiver extends BroadcastReceiver {

    private SharedPreferences sPref;
    private Context context;
    private boolean isAutoUpdate;
    private static final String DEFAULT_PERIOD = "1440";
    public static final String NEXT_UPDATE_TIME_KEY = "nextUpdate";
    private static final String UPDATE_RECEIVER_TIME_CHANGED="UPDATE_RECEIVER_TIME_CHANGED";

    public static void onUpdatePeriodChanged(@NonNull Context other){
        Intent in = new Intent(other, UpdateReceiver.class);
        in.putExtra(UPDATE_RECEIVER_TIME_CHANGED,true);
        other.sendBroadcast(in);
    }
    public static void onUpdatePeriodCheck(@NonNull Context other){
        Intent in = new Intent(other, UpdateReceiver.class);
        in.putExtra(UPDATE_RECEIVER_TIME_CHANGED,false);
        other.sendBroadcast(in);
    }

    private void planedNextUpdate(boolean updatePeriodChanged) {
        Intent in = new Intent(context, UpdateReceiver.class);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                in, PendingIntent.FLAG_CANCEL_CURRENT);
        if(!isAutoUpdate){
            am.cancel(pendingIntent);//отменяем запланированные обновления
            return;
        }

        //запланированное время
        long nextPlanedUpdate = sPref.getLong(UpdateReceiver.NEXT_UPDATE_TIME_KEY,
                System.currentTimeMillis());//если обновление не запл. то обновиться сразу
        if(nextPlanedUpdate<=System.currentTimeMillis()){//если в прошлом, то должны были уже обновится
            nextPlanedUpdate  = 1000 * 60 * Long.parseLong(
                    sPref.getString(context.getString(R.string.settings_period_key), DEFAULT_PERIOD))
                    + System.currentTimeMillis();
        }
        //следующее обновление, исходя из настроек (используется при смене периода обновления в настр.)
        if (updatePeriodChanged) {
            nextPlanedUpdate  = 1000 * 60 * Long.parseLong(
                    sPref.getString(context.getString(R.string.settings_period_key), DEFAULT_PERIOD))
                    + System.currentTimeMillis();
            Toast.makeText(context,"Ok",Toast.LENGTH_SHORT).show();
        }
        final SharedPreferences.Editor ed = sPref.edit();
        ed.putLong(NEXT_UPDATE_TIME_KEY, nextPlanedUpdate);//запоминаем время,когда обновиться
        ed.apply();
        am.set(AlarmManager.RTC_WAKEUP, nextPlanedUpdate, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        this.context = context;
        //уже обновляться?
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        long nextPlanedUpdate = sPref.getLong(UpdateReceiver.NEXT_UPDATE_TIME_KEY,
                System.currentTimeMillis());//если обновление не запл. но автообновление, то обновиться сразу
        isAutoUpdate = sPref.getBoolean(context.getString(R.string.settings_auto_update_key), true);
        if(isAutoUpdate && nextPlanedUpdate<= System.currentTimeMillis()) {
            UpdateChannelIntentService.startActionUpdate(context);//обновляем
        }
        planedNextUpdate(intent.getBooleanExtra(UPDATE_RECEIVER_TIME_CHANGED,false));
    }
}
