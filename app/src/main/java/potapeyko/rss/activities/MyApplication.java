package potapeyko.rss.activities;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import potapeyko.rss.R;
import potapeyko.rss.utils.UpdateAlarmListener;

import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MyApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences preferences;
    private Locale locale;
    private String lang;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.settings_lang_key).equals(key)) {
            lang = preferences.getString("lang", "default");
            if (lang.equals("default")) {
                lang = getResources().getConfiguration().locale.getCountry();
            }
            locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);

            Intent mainActivityIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            Intent settingsActivityIntent  = new Intent(getApplicationContext(),SettingsActivity.class);

            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settingsActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settingsActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivityIntent);
            startActivity(settingsActivityIntent);

        } else if (getString(R.string.settings_period_key).equals(key) ||
                getString(R.string.settings_auto_update_key).equals(key)) {
            alarmSettings();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lang = preferences.getString("lang", "default");
        if (lang.equals("default")) {
            lang = getResources().getConfiguration().locale.getCountry();
        }
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
        preferences.registerOnSharedPreferenceChangeListener(this);
        alarmSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    private void alarmSettings() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            return;
        }
        Intent intent = new Intent(this, UpdateAlarmListener.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);//флаг - отмена такого-же интента

        boolean isAutoUpdate = preferences.getBoolean(getString(R.string.settings_auto_update_key), true);
        if (isAutoUpdate) {

            long nextPlanedUpdate = preferences.getLong(UpdateAlarmListener.NEXT_UPDATE_TIME_KEY,
                    System.currentTimeMillis());//если обновление не запл. то обновиться сразу
            long nextUpdateForPeriod = 1000 * 60 * Long.parseLong(
                    preferences.getString(getString(R.string.settings_period_key), "0"))
                    + System.currentTimeMillis();
            long nextUpdate = nextPlanedUpdate > nextUpdateForPeriod ? nextUpdateForPeriod : nextPlanedUpdate;
            am.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
        } else {
            am.cancel(pendingIntent);
        }
    }
}
