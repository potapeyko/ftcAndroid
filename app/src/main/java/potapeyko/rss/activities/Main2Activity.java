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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import lombok.Getter;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.exceptions.DbException;
import potapeyko.rss.model.Feed;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbConvention;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.sql.DbWriter;
import potapeyko.rss.utils.BroadcastSender;

public class Main2Activity extends MyBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    static final String CHANEL_ID = "chanel_id";
    private static final String CHANEL_TITLE = "chanel_title";
    private static final String CHANEL_TITLE_NUMBER = "chanel_title_number";
    private static final String WhERE_FLAG = "where_flag";

    private static final int LAYOUT = R.layout.activity_main2;

    @Getter
    @NonNull
    private long feedId;
    private String chanelTitle = "";
    private int chanelTitleNumber = 0;
    private boolean feedItemViewedChange = false;
    private DB db;
    private Cursor newsCursor;
    private SharedPreferences sPref;
    private SimpleCursorAdapter adapter;
    private boolean broadcastRegister = false;
    private TextView txtNumberTitle;
    private DbReader.WhereFlags currentFilter;
    private LinearLayout dummyPlugView;
    private ListView newsList;

    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(getString(R.string.LOG_KEY), "onReceive intent == null");
                return;
            }

            if (BroadcastSender.CHANNEL_UPDATE_BROADCAST_MESS.equals(intent.getStringExtra(
                    BroadcastSender.STRING_BROADCAST_MESSAGE)) &&
                    (intent.getLongExtra(BroadcastSender.LONG_BROADCAST_DATA, -1)) == feedId) {
                DbReader dbReader = null;
                try {
                    dbReader = db.getReader();
                    dbReader.open();
                    chanelTitleNumber = dbReader.getFeedById(feedId).getItemsCount();
                    if (txtNumberTitle != null) {
                        txtNumberTitle.setText(String.valueOf(chanelTitleNumber));
                    }
                    newsCursor = dbReader.getCursorOfFeedItems(feedId, currentFilter);
                    adapter.changeCursor(newsCursor);
                    adapter.notifyDataSetChanged();
                    checkEmptyList();
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

        db = new DB(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        currentFilter = DbReader.WhereFlags.valueOf(sPref.getString(WhERE_FLAG, "ALL"));

        initToolbar();
        initNavigationView();


        feedId = getIntent().getLongExtra(CHANEL_ID, -1);
        chanelTitle = "";

        if (feedId == -1) {
            feedId = sPref.getLong(CHANEL_ID, feedId);
            chanelTitleNumber = sPref.getInt(CHANEL_TITLE_NUMBER, -1);
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
        if (feedItemViewedChange) {
            feedItemViewedChange = false;
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add) {
            NewFeedActivity.start(this);
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
            feedId = ChannelChangeActivity.getResultChanelId(data);
        }
        chanelTitle = "";
        newsTitleAndListInit();
    }

    private void deleteChannel() {

        DbWriter dbWriter = null;
        if (this.getFeedId() != -1) {
            try {
                dbWriter = db.getWriter();
                dbWriter.open();
                dbWriter.deleteFeedById(this.getFeedId());
                final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                final SharedPreferences.Editor ed = sPref.edit();
                ed.putLong(Main2Activity.CHANEL_ID, -1);
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
        ed.putLong(CHANEL_ID, feedId);
        ed.putInt(CHANEL_TITLE_NUMBER, chanelTitleNumber);
        ed.putString(WhERE_FLAG, currentFilter.name());
        ed.apply();
    }

    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    protected Toolbar initToolbar() {
        Toolbar toolbar = super.initToolbar();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();
        return toolbar;
    }

    private void prepareEmptyActivity(ListView newsList, TextView title) {
        if (title != null) {
            title.setText(R.string.activity_main_add_new_chanel);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChannelChangeActivity.start(Main2Activity.this);
                }
            });
        }
        if (txtNumberTitle != null)
            txtNumberTitle.setVisibility(View.INVISIBLE);
        if (newsCursor != null)
            newsCursor.close();
        if (newsList != null) {
            newsList.setVisibility(View.INVISIBLE);
        }
    }

    private void newsTitleAndListInit() {
        TextView title = (TextView) findViewById(R.id.activity_main2_txtTitle);
        txtNumberTitle = (TextView) findViewById(R.id.activity_main2_txtNumberTitle);

        newsList = (ListView) findViewById(R.id.activity_main2_feedsList);

        dummyPlugView = (LinearLayout) findViewById(R.id.black_empty_option);
        if(dummyPlugView!=null){dummyPlugView.setVisibility(View.INVISIBLE);}
        if(newsList!=null){newsList.setVisibility(View.VISIBLE);}

        if (feedId == -1) {
            prepareEmptyActivity(newsList, title);
            return;
        }
        DbReader dbReader = null;
        try {
            dbReader = db.getReader();
            dbReader.open();
            if ("".equals(chanelTitle)) {
                Feed currentFeed = dbReader.getFeedById(this.feedId);
                if (currentFeed != null) {
                    chanelTitle = currentFeed.getTitle();
                    chanelTitleNumber = currentFeed.getItemsCount();
                } else {
                    feedId = -1;
                    prepareEmptyActivity(newsList, title);
                    return;
                }
            }

            if (title != null) {
                title.setText(chanelTitle);

            }
            if (txtNumberTitle != null) {
                txtNumberTitle.setVisibility(View.VISIBLE);
                txtNumberTitle.setText(String.valueOf(chanelTitleNumber));
            }

            if (newsList != null) {
                newsList.setVisibility(View.VISIBLE);
                newsCursor = dbReader.getCursorOfFeedItems(feedId, currentFilter);

                String[] from = {DbConvention.FEED_ITEM_TITLE,
                        DbConvention.FEED_ITEM_PUBLICATION_DATE};
                int[] to = {R.id.feedItem_list_title, R.id.feedItem_list_date};

                adapter = new mySimpleCursorAdapter(this, R.layout.feeditem_list_item, newsCursor, from, to, feedId, this);

                newsList.setAdapter(adapter);
                if(checkEmptyList()){
                    return;
                }
                newsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long feedItemId) {
                        if (view != null) {
                            ImageView imageView = (ImageView) view.findViewById(R.id.feedItem_list_checkedBtn);
                            MyTag tag = (MyTag) imageView.getTag();
                            if (tag.checkedFlag == MyTag.falseValue) {
                                DbWriter dbWriter = null;
                                try {
                                    dbWriter = db.getWriter();
                                    dbWriter.open();
                                    dbWriter.changeFeedItemFlags(tag.feedItemId, feedId, MyTag.trueValue, MyTag.falseValue, tag.favoriteFlag, tag.favoriteFlag);
                                    dbWriter.close();
                                    chanelTitleNumber--;
                                    txtNumberTitle.setText(String.valueOf(chanelTitleNumber));
                                    tag.checkedFlag = MyTag.trueValue;
                                    imageView.setImageResource(R.drawable.ic_item_check);
                                } catch (Throwable th) {
                                    th.printStackTrace();
                                    if (dbWriter != null) {
                                        dbWriter.close();
                                    }
                                }

                            }
                            feedItemViewedChange = true;
                            FullFeedItemActivity.start(Main2Activity.this, feedItemId, feedId);
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


    @Override
    public void onClick(View v) {
        MyTag tag = (MyTag) v.getTag();
        ImageView im = (ImageView) v;
        DbWriter dbWriter = null;

        try {
            dbWriter = db.getWriter();
            dbWriter.open();
        } catch (Throwable th) {
            th.printStackTrace();
            if (dbWriter != null) {
                dbWriter.close();
            }
            return;
        }

        if (tag.idOfClickedIcon == R.id.feedItem_list_checkedBtn) {
            if (tag.checkedFlag == MyTag.trueValue) {
                try {
                    dbWriter.changeFeedItemFlags(tag.feedItemId, feedId, MyTag.falseValue, MyTag.trueValue, tag.favoriteFlag, tag.favoriteFlag);
                    chanelTitleNumber++;
                    txtNumberTitle.setText(String.valueOf(chanelTitleNumber));
                    tag.checkedFlag = MyTag.falseValue;
                    im.setImageResource(R.drawable.ic_item_check_not);
                } catch (Throwable th) {
                    th.printStackTrace();
                    if (dbWriter != null) {
                        dbWriter.close();
                    }
                    return;
                }
            } else {
                try {
                    dbWriter.changeFeedItemFlags(tag.feedItemId, feedId, MyTag.trueValue, MyTag.falseValue, tag.favoriteFlag, tag.favoriteFlag);
                    chanelTitleNumber--;
                    txtNumberTitle.setText(String.valueOf(chanelTitleNumber));
                    tag.checkedFlag = MyTag.trueValue;
                    im.setImageResource(R.drawable.ic_item_check);
                } catch (Throwable th) {
                    th.printStackTrace();
                    if (dbWriter != null) {
                        dbWriter.close();
                    }
                    return;
                }
            }

        } else if (tag.idOfClickedIcon == R.id.feedItem_list_favoriteBtn) {
            if (tag.favoriteFlag == MyTag.falseValue) {
                try {
                    dbWriter.changeFeedItemFlags(tag.feedItemId, feedId, tag.checkedFlag, tag.checkedFlag,
                            MyTag.trueValue, MyTag.falseValue);

                    tag.favoriteFlag = MyTag.trueValue;
                    im.setImageResource(R.drawable.ic_item_bookmark_black);
                } catch (Throwable th) {
                    th.printStackTrace();
                    if (dbWriter != null) {
                        dbWriter.close();
                    }
                    return;
                }
            } else {
                try {
                    dbWriter.changeFeedItemFlags(tag.feedItemId, feedId, tag.checkedFlag, tag.checkedFlag,
                            MyTag.falseValue, MyTag.trueValue);
                    tag.favoriteFlag = MyTag.falseValue;
                    im.setImageResource(R.drawable.ic_item_bookmark_not_black);
                } catch (Throwable th) {
                    th.printStackTrace();
                    if (dbWriter != null) {
                        dbWriter.close();
                    }
                    return;
                }
            }
        }
        try {
            newsCursor = dbWriter.getCursorOfFeedItems(feedId, currentFilter);
            adapter.swapCursor(newsCursor);
            adapter.notifyDataSetChanged();
            checkEmptyList();
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            if (dbWriter != null) dbWriter.close();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        DbReader dbReader = null;

        try {
            switch (id) {
                case R.id.allButton:
                    currentFilter = DbReader.WhereFlags.ALL;
                    break;
                case R.id.checkedButton:
                    currentFilter = DbReader.WhereFlags.CHECKED;
                    break;
                case R.id.favoriteButton:
                    currentFilter = DbReader.WhereFlags.FAVORITE;
                    break;
                case R.id.notCheckedButton:
                    currentFilter = DbReader.WhereFlags.NOT_CHECKED;
                    break;
                case R.id.checkAllButton:
                    setAllAsChecked(newsCursor);
                    if (txtNumberTitle != null) {
                        txtNumberTitle.setText(String.valueOf(chanelTitleNumber));
                    }
                    break;
                case R.id.refreshButton:
                    UpdateChannelIntentService.startActionUpdate(this);
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }

            dbReader = db.getReader();
            dbReader.open();
            newsCursor = dbReader.getCursorOfFeedItems(feedId, currentFilter);
            adapter.changeCursor(newsCursor);
            adapter.notifyDataSetChanged();
            checkEmptyList();
            return true;
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            if (dbReader != null)
                dbReader.close();
        }
        return true;
    }

    /**
     * check list and set dummy Plug View
     * @return true - if list is empty
     * false - if list is not empty
     */
    private boolean checkEmptyList() {
        if(newsCursor.getCount()==0){
            newsList.setVisibility(View.INVISIBLE);
            dummyPlugView.setVisibility(View.VISIBLE);
            return true;
        }else{
            newsList.setVisibility(View.VISIBLE);
            dummyPlugView.setVisibility(View.INVISIBLE);
        }
        return false;
    }

    private void setAllAsChecked(Cursor newsCursor) {
        DbWriter dbWriter = null;
        if (newsCursor != null && !newsCursor.isClosed()) {
            try {
                dbWriter = db.getWriter();
                dbWriter.open();
                newsCursor.moveToFirst();
                int itemIdIndex = newsCursor.getColumnIndex(DbConvention.FEED_ITEM_ID);
                int itemCheckedFlagIndex = newsCursor.getColumnIndex(DbConvention.FEED_ITEM_FLAGS_CHECKED);
                int itemFavoriteFlagIndex = newsCursor.getColumnIndex(DbConvention.FEED_ITEM_FLAGS_FAVORITE);

                while (!newsCursor.isAfterLast()) {
                    int lastCheckedFlag = newsCursor.getInt(itemCheckedFlagIndex);
                    if (lastCheckedFlag != MyTag.trueValue) {
                        chanelTitleNumber--;
                    }
                    dbWriter.changeFeedItemFlags(newsCursor.getLong(itemIdIndex), feedId, MyTag.trueValue,
                            lastCheckedFlag,
                            newsCursor.getInt(itemFavoriteFlagIndex),
                            newsCursor.getInt(itemFavoriteFlagIndex));
                    newsCursor.moveToNext();
                }
                dbWriter.close();
            } catch (Throwable th) {
                th.printStackTrace();
            } finally {
                if (dbWriter != null) {
                    dbWriter.close();
                }
            }
        }
    }
}
