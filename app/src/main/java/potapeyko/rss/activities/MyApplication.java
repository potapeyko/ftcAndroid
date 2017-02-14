package potapeyko.rss.activities;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import potapeyko.rss.R;

import java.util.Locale;

/**
 * Created by Дмитрий on 14.02.2017.
 */
public class MyApplication extends Application  implements SharedPreferences.OnSharedPreferenceChangeListener{
    private SharedPreferences preferences;
    private Locale locale;
    private String lang;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.settings_lang_key).equals(key)) {
            lang = preferences.getString("lang", "default");
            if (lang.equals("default")) {lang=getResources().getConfiguration().locale.getCountry();}
            locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);

            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    @Override
    public void onCreate() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lang = preferences.getString("lang", "default");
        if (lang.equals("default")) {lang=getResources().getConfiguration().locale.getCountry();}
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
        SharedPreferences sPref  = PreferenceManager.getDefaultSharedPreferences(this);
        sPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }
}
