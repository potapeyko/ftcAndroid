package potapeyko.rss.activities;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import potapeyko.rss.R;
import potapeyko.rss.utils.UpdateReceiver;

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
            Intent settingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);

            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settingsActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settingsActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivityIntent);
            startActivity(settingsActivityIntent);

        } else

        if (getString(R.string.settings_period_key).equals(key) ||
                getString(R.string.settings_auto_update_key).equals(key)) {
            UpdateReceiver.onUpdatePeriodChanged(this);
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
        UpdateReceiver.onUpdatePeriodCheck(this);
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


}
