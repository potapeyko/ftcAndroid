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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import lombok.Getter;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.model.Feed;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbConvention;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.sql.DbWriter;
import potapeyko.rss.utils.BroadcastSender;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static final String CHANEL_ID = "chanel_id";
    private static final String CHANEL_TITLE = "chanel_title";
    private static final String FLAG_VIEWED_ITEM ="1";
    private static final String FLAG_NOT_VIEWED_ITEM ="0";

    private static final int LAYOUT =R.layout.activity_main2;

    @Getter
    @NonNull
    private long chanelId;
    private String chanelTitle = "";
    private int chanelTitleNumber=0;
    private boolean feedItemViewedChange =false;
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
        setContentView(LAYOUT);

        initToolbar();
        initNavigationView();

        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);

        chanelId = getIntent().getLongExtra(CHANEL_ID, -1);
        chanelTitle = "";

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

        //если смотерли активность, не просмотренную ранее, то перегружаем все.
        if(feedItemViewedChange){
            newsTitleAndListInit();
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
    protected void onDestroy() {
        super.onDestroy();
        if (newsCursor != null) {
            newsCursor.close();
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CHANEL_ID, chanelId);
        outState.putString(CHANEL_TITLE, chanelTitle);
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

                String[] from = {DbConvention.FEED_ITEM_TITLE, DbConvention.FEED_ITEM_FLAGS};
                int[] to = {R.id.feedItem_list_title, R.id.feedItem_list_flag};

                adapter =
                        new SimpleCursorAdapter(this, R.layout.feeditem_list_item, newsCursor, from, to) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View row = super.getView(position, convertView, parent);

                                TextView tv = (TextView) row.findViewById(R.id.feedItem_list_flag);
                                if (tv != null && tv.getText() != null) {
                                    if (tv.getText().toString().equals(FLAG_VIEWED_ITEM)) {
                                        row.setBackgroundColor(getResources().getColor(R.color.colorGray));
                                    }
                                    else{
                                        row.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                                    }
                                }
                                return row;
                            }
                        };

                newsList.setAdapter(adapter);
                newsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        boolean isItemViewed=true;
                        if(view!=null) {
                            TextView tv = (TextView)view.findViewById(R.id.feedItem_list_flag);
                            if(tv.getText().toString().equals(FLAG_NOT_VIEWED_ITEM))
                            {
                                Main2Activity.this.chanelTitleNumber--;//// TODO: закончить логику
                                feedItemViewedChange=true;
                                isItemViewed=false;
                                Log.d("wtf","111");
                            }
                            FullNewsActivity.start(Main2Activity.this, id,isItemViewed);
                        }
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

    static void start(@NonNull Activity other, long aLong) {
        Intent intent = new Intent(other, Main2Activity.class);
        intent.putExtra(CHANEL_ID, aLong);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        other.startActivity(intent);
    }
}
