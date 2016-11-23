package potapeyko.rss.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import potapeyko.rss.R;

public class SettingsActivity extends PreferenceActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }

    static void start(Context other){
        Intent  intent = new Intent(other,SettingsActivity.class);
        other.startActivity(intent);
    }


}
