package potapeyko.rss.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.adapters.ItemsListAdapter;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.models.News;
import potapeyko.rss.sql.DB;


import java.util.ArrayList;


public final class MainActivity extends MyBaseActivity implements IActivityListener {
    private String[] drawerTitles;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private TextView text;
    private ListView mNewsList;
    private DB db;
    private long chanelId = 1; //todo сделать сохранение в настройках приложения, чтобы открывался нужный канал
    private static final String CHANEL_ID = "chanel_id";


    public MainActivity() {
        this.onCreateSubscribe(this);
        this.onSaveInstanceStateSubscribe(this);
    }

    @Override
    public void onSaveInstanceStateActivity(Bundle outState) {
        outState.clear();
        outState.putLong(CHANEL_ID, chanelId);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null && savedInstanceState.containsKey(CHANEL_ID))
            chanelId = savedInstanceState.getLong(CHANEL_ID);

        text = (TextView) findViewById(R.id.textView);
        if (text != null)
            text.setText(R.string.main_chanel_title);
        leftDrawerLayoutInit();
        db = new DB(this);
        newsListInit();
    }

    private void leftDrawerLayoutInit() {
        drawerTitles = getResources().getStringArray(R.array.drawer_items_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, drawerTitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener(drawerLayout, drawerList, this));
    }

    private void newsListInit() {
        mNewsList = (ListView) findViewById(R.id.newsList);
        ItemsListAdapter adapter = new ItemsListAdapter(this, getItemsList(chanelId));
        mNewsList.setAdapter(adapter);
        mNewsList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FullNewsActivity.start(MainActivity.this, id);
            }
        });
    }

    //todo времянка замена отдельный поток
    private ArrayList<News> getItemsList(long chanelId) {
        ArrayList<News> news = new ArrayList<>();
        db.open();
        Cursor cur = db.getAllNewsOfChanel(chanelId);
        if (cur.moveToFirst()) {
            do {
                news.add(new News(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3)));
            }
            while (cur.moveToNext());
        }
        db.close();
        return news;
    }

}
