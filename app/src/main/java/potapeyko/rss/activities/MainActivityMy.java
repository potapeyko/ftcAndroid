package potapeyko.rss.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;


public class MainActivityMy extends MyBaseActivity implements IActivityListener {
    private TextView text;

    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    public MainActivityMy() {
        this.onCreateSubscribe(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.textView);
        text.setText("Измененный текст");

        mDrawerTitles = getResources().getStringArray(R.array.drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList,this));
    }
}
