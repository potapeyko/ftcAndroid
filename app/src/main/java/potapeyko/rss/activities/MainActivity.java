package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.*;
import lombok.Getter;
import potapeyko.rss.R;
import potapeyko.rss.adapters.NewsListAdapter;
import potapeyko.rss.constants.LogCodes;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.models.Chanel;
import potapeyko.rss.models.News;
import potapeyko.rss.sql.DB;


import java.util.ArrayList;


public final class MainActivity extends MyBaseActivity implements IActivityListener {


    private DB db;
    @Getter private long chanelId = 8; //todo сделать сохранение в настройках приложения, чтобы открывался нужный канал
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
        if (savedInstanceState != null && savedInstanceState.containsKey(CHANEL_ID)) {
            chanelId = savedInstanceState.getLong(CHANEL_ID);
        } else chanelId = getIntent().getLongExtra("chanelId", 3);


        leftDrawerLayoutInit();

        newsTitleAndListInit();
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
        if(data==null)return;
        long id = ChanelChangeActivityMy.getResultChanelId(data);
        if(id != -1){
            chanelId =id;
            newsTitleAndListInit();
        }
    }

    private void newsTitleAndListInit() {
        ListView newsList = (ListView) findViewById(R.id.activity_main_newsList);
        NewsListAdapter adapter;
        TextView title = (TextView) findViewById(R.id.activity_main_txtTitle);
        db = new DB(this);

        db.open();
        Chanel chanel = db.getChanelById(this.chanelId);
        if (title != null&&chanel!=null) {
            title.setText(chanel.getTitle());
        }
        if (newsList != null) {

            ArrayList<News> news = db.getAllNewsOfChanelList(this.chanelId);
//            ArrayList<News> news = db.getAllNews();
            adapter = new NewsListAdapter(this, news);
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
        db.close();
    }

    static void start(Activity other, Long aLong) {
        Intent intent = new Intent(other, MainActivity.class);
        intent.putExtra("chanelId", aLong);
        other.startActivity(intent);
    }
}
