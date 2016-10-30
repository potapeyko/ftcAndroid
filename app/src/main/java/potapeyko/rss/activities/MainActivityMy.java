package potapeyko.rss.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.adapters.ItemsListAdapter;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.models.News;

import java.util.ArrayList;


public class MainActivityMy extends MyBaseActivity implements IActivityListener {
    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private TextView text;
    private ListView mNewsList;

    public MainActivityMy() {
        this.onCreateSubscribe(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.textView);
        text.setText("Измененный текст");
        leftDrawerLayoutInit();
        newsListInit();
    }

    private void leftDrawerLayoutInit() {
        mDrawerTitles = getResources().getStringArray(R.array.drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList, this));
    }
    private void  newsListInit(){
        mNewsList = (ListView) findViewById(R.id.newsList);
        ItemsListAdapter adapter = new ItemsListAdapter(this, getItemsList());
        mNewsList.setAdapter(adapter);
        mNewsList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivityMy.this, FullNewsActivity.class);
                intent.putExtra("id",id);
                startActivity(intent);
            }
        });
    }
    //todo времянка замена на базу
    private ArrayList<News> getItemsList() {
        ArrayList<News> news = new ArrayList<>();
        news.add(new News("Новость 1", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio")) ;
        news.add(new News("Новость 1", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio"));
        news.add(new News("Новость 2", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio"
                ));
        news.add(new News("Новость3 ", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio"
                ));
        news.add(new News("Новость 4", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio"
                ));
        news.add(new News("Новость 5", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio"
                ));
        news.add(new News("Новость 6", "Описание новости Описание новости Описание новости Описание новости Описание новости",
                "http://ru.stackoverflow.com/questions/417548/%D0%A0%D0%B0%D0%B7%D0%BC%D0%B5%D1%80-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9-%D0%BD%D0%B0-%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B5-android-studio"
                ));
        return news;
    }

}
