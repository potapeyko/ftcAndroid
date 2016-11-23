package potapeyko.rss.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import lombok.Getter;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.models.Channel;
import potapeyko.rss.sql.DB;
import potapeyko.rss.utils.BroadcastSender;
import potapeyko.rss.utils.UpdateAlarmListener;


public final class MainActivity extends MyBaseActivity implements IActivityListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String CHANEL_ID = "chanel_id";
    private static final String CHANEL_TITLE = "chanel_title";

    @Getter
    @NonNull
    private long chanelId;
    private String chanelTitle = "";
    private DB db;
    private Cursor newsCursor;
    private SharedPreferences sPref;
    private SimpleCursorAdapter adapter;

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(
                    BroadcastSender.STRING_BROADCAST_MESSAGE)
                    .equals(BroadcastSender.CHANNEL_UPDATE_BROADCAST_MESS) &&
                    (intent.getLongExtra(BroadcastSender.LONG_BROADCAST_DATA, -1)) == chanelId) {

                try {
                    db.open();
                    newsCursor = db.getAllNewsOfChanelCursor(chanelId);
                    adapter.changeCursor(newsCursor);
                    adapter.notifyDataSetChanged();
                    db.close();
                } catch (Throwable th) {
                    th.printStackTrace();
                } finally {
                    db.close();
                }
            }
        }
    };

    public MainActivity() {
        this.onCreateSubscribe(this);
    }


    @Override
    protected void onPause() {

        saveLastChanel();
        super.onPause();
    }

    private void saveLastChanel() {
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(CHANEL_TITLE, chanelTitle);
        ed.putLong(CHANEL_ID, chanelId);
        ed.apply();
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        setContentView(R.layout.activity_main);

        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        sPref.registerOnSharedPreferenceChangeListener(this);

        chanelId = getIntent().getLongExtra(CHANEL_ID, -1);
        chanelTitle = "";

        if (chanelId == -1) {
            chanelId = sPref.getLong(CHANEL_ID, chanelId);
            chanelTitle = getString(R.string.activity_main_add_new_chanel);
            chanelTitle = sPref.getString(CHANEL_TITLE, chanelTitle);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("potapeyko.rss.activities"));

        leftDrawerLayoutInit();
        newsTitleAndListInit();

        alarmSettings();
    }

    private void alarmSettings() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            return;
        }
        Intent intent = new Intent(this, UpdateAlarmListener.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);//флаг - отмена такого-же интента

        boolean isAutoUpdate = sPref.getBoolean(getString(R.string.settings_auto_update_key), true);
        if (isAutoUpdate) {

            long nextPlanedUpdate = sPref.getLong(UpdateAlarmListener.NEXT_UPDATE_TIME_KEY,
                    System.currentTimeMillis());//если обновление не запл. то обновиться сразу
            long nextUpdateForPeriod = 1000 * 60 * Long.parseLong(
                    sPref.getString(getString(R.string.settings_period_key), "0"))
                    + System.currentTimeMillis();
            Long nextUpdate = nextPlanedUpdate > nextUpdateForPeriod ? nextUpdateForPeriod : nextPlanedUpdate;
            am.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
        } else {
            am.cancel(pendingIntent);
        }
    }

    @Override
    public void onSaveInstanceStateActivity(Bundle outState) {
        outState.putLong(CHANEL_ID, chanelId);
        outState.putString(CHANEL_TITLE, chanelTitle);
    }

    private void leftDrawerLayoutInit() {
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_items_array);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.activity_main_left_drawer);
        if (drawerList != null) {
            drawerList.setAdapter(new ArrayAdapter<>(this,
                    R.layout.drawer_list_item, drawerTitles));
            drawerList.setOnItemClickListener(new DrawerItemClickListener(drawerLayout, drawerList, this));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != ChannelChangeActivity.CHANEL_CHANGE_CODE) {
            return;
        }
        if (data != null) {
            chanelId = ChannelChangeActivity.getResultChanelId(data);
        }
        chanelTitle = "";
        newsTitleAndListInit();
    }

    private void newsTitleAndListInit() {
        ListView newsList = (ListView) findViewById(R.id.activity_main_newsList);
        TextView title = (TextView) findViewById(R.id.activity_main_txtTitle);

        if (chanelId == -1) {
            prepareEmptyActivity(newsList, title);
            return;
        }

        try {
            db = new DB(this);
            db.open();

            if ("".equals(chanelTitle)) {
                Channel currentChannel = db.getChanelById(this.chanelId);
                if (currentChannel != null) {
                    chanelTitle = currentChannel.getTitle();
                } else {
                    prepareEmptyActivity(newsList, title);
                    return;
                }
            }

            if (title != null) {
                title.setText(chanelTitle);
            }

            if (newsList != null) {
                newsList.setVisibility(View.VISIBLE);
                newsCursor = db.getAllNewsOfChanelCursor(chanelId);

                String[] from = {DB.DbConvention.NEWS_TABLE_TITLE};
                int[] to = {R.id.news_list_title};

                adapter =
                        new SimpleCursorAdapter(this, R.layout.news_list_item, newsCursor, from, to);

                newsList.setAdapter(adapter);
                newsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FullNewsActivity.start(MainActivity.this, id);
                    }
                });
            }
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            db.close();
        }

    }

    private void prepareEmptyActivity(ListView newsList, TextView title) {
        if (title != null) {
            title.setText(R.string.activity_main_add_new_chanel);
        }
        if (newsCursor != null)
            newsCursor.close();
        if (newsList != null) {
            newsList.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        super.onDestroy();
        if (newsCursor != null) {
            newsCursor.close();
        }

    }

    static void start(Activity other, Long aLong) {
        Intent intent = new Intent(other, MainActivity.class);
        intent.putExtra(CHANEL_ID, aLong);
        other.startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        alarmSettings();
    }
}
