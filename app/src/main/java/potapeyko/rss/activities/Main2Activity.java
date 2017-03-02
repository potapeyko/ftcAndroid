package potapeyko.rss.activities;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import lombok.Getter;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.sql.DbWriter;
import potapeyko.rss.utils.BroadcastSender;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
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
    private boolean broadcastRegister = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initToolbar();
        initNavigationView();

        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (chanelId == -1) {
            chanelId = sPref.getLong(CHANEL_ID, chanelId);
            chanelTitle = getString(R.string.activity_main_add_new_chanel);
            chanelTitle = sPref.getString(CHANEL_TITLE, chanelTitle);
        }
        newsTitleAndListInit();
    }

    @Override
    protected void onResume() {
        if (!broadcastRegister) {
            broadcastRegister = true;
            LocalBroadcastManager.getInstance(this).registerReceiver(br,
                    new IntentFilter(BroadcastSender.INTENT_FILTER));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        saveLastChanel();
        if (broadcastRegister) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
            broadcastRegister = false;
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.refreshButton) {
            UpdateChannelIntentService.startActionUpdate(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add) {
            NewChanelActivity.start(this);
        } else if (id == R.id.nav_change) {
            ChannelChangeActivity.start(this);
        } else if (id == R.id.nav_delete) {
            deleteChannel();
        } else if (id == R.id.nav_setting) {
            SettingsActivity.start(this);
        } else if (id == R.id.nav_about) {
            AboutAppActivity.start(this);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
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

    private void deleteChannel() {
        DB db;
        DbWriter dbWriter = null;
        if (this.getChanelId() != -1) {
            try {
                db = new DB(this);
                dbWriter = db.getWriter();
                dbWriter.open();
                dbWriter.deleteFeedById(this.getChanelId());
                final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                final SharedPreferences.Editor ed = sPref.edit();
                ed.putLong(MainActivity.CHANEL_ID, -1);
                ed.apply();
            } catch (Throwable r) {
                r.printStackTrace();
            } finally {
                if (dbWriter != null) {
                    dbWriter.close();
                }
                ChannelChangeActivity.start(this);
            }
        }
    }
    private void saveLastChanel() {
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(CHANEL_TITLE, chanelTitle);
        ed.putLong(CHANEL_ID, chanelId);
        ed.apply();
    }
    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void newsTitleAndListInit() {
        return;
    }

    static void start(@NonNull Activity other, long aLong) {
        Intent intent = new Intent(other, Main2Activity.class);
        intent.putExtra(CHANEL_ID, aLong);
        other.startActivity(intent);
    }
}
