package potapeyko.rss.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.*;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.*;

import lombok.Getter;
import potapeyko.rss.R;
import potapeyko.rss.constants.LogCodes;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.sql.DB;


public final class MainActivity extends MyBaseActivity implements IActivityListener {

    private static final String CHANEL_ID = "chanel_id";
    private static final String CHANEL_TITLE = "chanel_title";

    @Getter private long chanelId ;
    private String chanelTitle = "";
    private DB db;
    private Cursor newsCursor;
    private SharedPreferences sPref;

    private BroadcastReceiver br;

    public MainActivity() {
        this.onCreateSubscribe(this);
    }



    @Override
    protected void onPause() {
        saveLastChanel();
        super.onPause();
    }

    private void saveLastChanel() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(CHANEL_TITLE, chanelTitle);
        ed.putLong(CHANEL_ID, chanelId);
        ed.apply();
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        setContentView(R.layout.activity_main);
        chanelId = getIntent().getLongExtra("chanelId", -1);
        chanelTitle = "";
        if (chanelId == -1) {
            sPref = getPreferences(MODE_PRIVATE);
            chanelTitle = getString(R.string.activity_main_add_new_chanel);
            chanelTitle = sPref.getString(CHANEL_TITLE, chanelTitle);
            chanelId = sPref.getLong(CHANEL_ID,chanelId);

        }
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
            }
        };

        leftDrawerLayoutInit();
        newsTitleAndListInit();
    }

    @Override
    public void onSaveInstanceStateActivity(Bundle outState) {

    }

    private void leftDrawerLayoutInit() {
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_items_array);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.activity_main_left_drawer);
        if (drawerList != null) {
            drawerList.setAdapter(new ArrayAdapter<>(this,
                    R.layout.drawer_list_item, drawerTitles));
            drawerList.setOnItemClickListener(new DrawerItemClickListener(drawerLayout, drawerList, this));
        } else {
            Log.e(LogCodes.MAIN_ACTIVITY, "не найден ListView activity_main_left_drawer");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        long id = ChanelChangeActivityMy.getResultChanelId(data);
        if (id != -1) {
            chanelId = id;
            chanelTitle = "";
            newsTitleAndListInit();
        }
    }

    private void newsTitleAndListInit() {
        ListView newsList = (ListView) findViewById(R.id.activity_main_newsList);
        TextView title = (TextView) findViewById(R.id.activity_main_txtTitle);

        if (chanelId == -1) {
            if (title != null) {
                title.setText(chanelTitle);
            }
            return;
        }

        try {
            db = new DB(this);
            db.open();

            if ("".equals(chanelTitle)) {
                chanelTitle = db.getChanelById(this.chanelId).getTitle();
            }
            if (title != null) {
                title.setText(chanelTitle);
            }

            if (newsList != null) {
                newsCursor = db.getAllNewsOfChanelCursor(chanelId);
                String[] from = {DB.DbConvention.NEWS_TABLE_TITLE};
                int[] to = {R.id.news_list_title};

                SimpleCursorAdapter adapter =
                        new SimpleCursorAdapter(this, R.layout.news_list_item, newsCursor, from, to);
                newsList.setAdapter(adapter);
                newsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FullNewsActivity.start(MainActivity.this, id);
                    }
                });
            } else {
                Log.e(LogCodes.MAIN_ACTIVITY, "не найден ListView activity_main_newsList");
            }
        } finally {
            db.close();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newsCursor != null) {
            newsCursor.close();
        }
    }

    static void start(Activity other, Long aLong) {
        Intent intent = new Intent(other, MainActivity.class);
        intent.putExtra("chanelId", aLong);
        other.startActivity(intent);
    }
}
