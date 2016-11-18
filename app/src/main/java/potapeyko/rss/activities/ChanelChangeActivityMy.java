package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import potapeyko.rss.R;
import potapeyko.rss.constants.LogCodes;
import potapeyko.rss.interfaces.ActivityListenerAdapter;
import potapeyko.rss.sql.DB;

import static potapeyko.rss.constants.OtherCodes.CHANEL_CHANGE_CODE;
import static potapeyko.rss.constants.OtherCodes.CHANEL_ID_KEY;


public final class ChanelChangeActivityMy extends MyBaseActivity {
    private ListView channelsList;
    private final DB db;
    
    private Cursor channelsListCursor =null;
    

    public ChanelChangeActivityMy() {
        this.db = new DB(this);
        this.onCreateSubscribe(new ActivityListenerAdapter() {
            @Override
            public void onCreateActivity(@Nullable Bundle savedInstanceState) {
                setContentView(R.layout.activity_chanel_change);
                channelsListInit();
            }
        });
    }

    static void start(Activity other) {
        Intent intent = new Intent(other, ChanelChangeActivityMy.class);
        other.startActivityForResult(intent, CHANEL_CHANGE_CODE);
    }


    static long getResultChanelId(Intent intent) {
        return intent.getLongExtra(CHANEL_ID_KEY, -1);
    }

    private void channelsListInit() {
        channelsList = (ListView) findViewById(R.id.chanel_change_list);
        if (channelsList != null) {
            db.open();
            try {
                channelsListCursor = db.getAllChannelsCursor();
                String [] from = {DB.DbConvention.CHANEL_TABLE_TITLE,
                        DB.DbConvention.CHANEL_TABLE_DESCRIPTION};
                int [] to = {R.id.channels_list_item_title, R.id.channels_list_item_description};
                SimpleCursorAdapter adapter =new SimpleCursorAdapter(this, R.layout.channels_list_item,
                        channelsListCursor,from,to);
                channelsList.setAdapter(adapter);
                channelsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent();
                        intent.putExtra(CHANEL_ID_KEY, id);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            } finally {
                db.close();
            }
        } else {
            Log.e(LogCodes.MAIN_ACTIVITY, "не найден ListView activity_main_newsList");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(channelsListCursor!=null){
            channelsListCursor.close();
        }
    }
}



