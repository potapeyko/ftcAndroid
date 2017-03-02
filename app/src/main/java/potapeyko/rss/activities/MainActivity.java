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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import lombok.Getter;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.model.Feed;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbConvention;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.utils.BroadcastSender;
import potapeyko.rss.utils.UpdateAlarmListener;


public final class MainActivity extends MyBaseActivity implements IActivityListener, SharedPreferences.OnSharedPreferenceChangeListener {

    static final String CHANEL_ID = "chanel_id";
    private static final String CHANEL_TITLE = "chanel_title";


    @Getter
    @NonNull
    private long chanelId;
    private String chanelTitle = "";
    private DB db;
    private Cursor newsCursor;
    private SharedPreferences sPref;
    private SimpleCursorAdapter adapter;
    private boolean broadcastRegister =false;


    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(getString(R.string.LOG_KEY), "onReceive intent == null");
                return;
            }

            if (BroadcastSender.CHANNEL_UPDATE_BROADCAST_MESS.equals(intent.getStringExtra(
                    BroadcastSender.STRING_BROADCAST_MESSAGE)) &&
                    (intent.getLongExtra(BroadcastSender.LONG_BROADCAST_DATA, -1)) == chanelId) {
                DbReader dbReader = null;
                try {
                    dbReader = db.getReader();
                    dbReader.open();
                    newsCursor = dbReader.getAllItemsOfFeedCursor(chanelId);
                    adapter.changeCursor(newsCursor);
                    adapter.notifyDataSetChanged();
                } catch (Throwable th) {
                    th.printStackTrace();
                } finally {
                    if (dbReader != null) {
                        dbReader.close();
                    }
                }
            }
        }
    };

    public MainActivity() {
        this.onCreateSubscribe(this);
    }

    @Override
    protected void onResume() {
        if(!broadcastRegister){
            broadcastRegister = true;
            LocalBroadcastManager.getInstance(this).registerReceiver(br,
                    new IntentFilter(BroadcastSender.INTENT_FILTER));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        saveLastChanel();
        if (br != null && broadcastRegister) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
            broadcastRegister = false;
        }
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
            long nextUpdate = nextPlanedUpdate > nextUpdateForPeriod ? nextUpdateForPeriod : nextPlanedUpdate;
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
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout2);
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
        ListView newsList = (ListView) findViewById(R.id.activity_main_feedsList);
        TextView title = (TextView) findViewById(R.id.activity_main_txtTitle);

        if (chanelId == -1) {
            prepareEmptyActivity(newsList, title);
            return;
        }
        DbReader dbReader = null;
        try {
            db = new DB(this);
            dbReader = db.getReader();
            dbReader.open();
            if ("".equals(chanelTitle)) {
                Feed currentChannel = dbReader.getFeedById(this.chanelId);
                if (currentChannel != null) {
                    chanelTitle = currentChannel.getTitle();
                } else {
                    chanelId = -1;
                    prepareEmptyActivity(newsList, title);
                    return;
                }
            }

            if (title != null) {
                title.setText(chanelTitle);
            }

            if (newsList != null) {
                newsList.setVisibility(View.VISIBLE);
                newsCursor = dbReader.getAllItemsOfFeedCursor(chanelId);

                String[] from = {DbConvention.FEED_ITEM_TITLE};
                int[] to = {R.id.feedItem_list_title};

                adapter =
                        new SimpleCursorAdapter(this, R.layout.feeditem_list_item, newsCursor, from, to);

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
            if (dbReader != null) {
                dbReader.close();
            }
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
        super.onDestroy();
        if (newsCursor != null) {
            newsCursor.close();
        }

    }

    static void start(@NonNull Activity other, long aLong) {
        Intent intent = new Intent(other, MainActivity.class);
        intent.putExtra(CHANEL_ID, aLong);
        other.startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(getString(R.string.settings_period_key).equals(key)||
                getString(R.string.settings_auto_update_key).equals(key)) {
            alarmSettings();
        }
    }
}
