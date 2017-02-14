package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.ActivityListenerAdapter;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbConvention;
import potapeyko.rss.sql.DbReader;


public final class ChannelChangeActivity extends MyBaseActivity {
    private final DB db;
    static final int CHANEL_CHANGE_CODE = 1235;
    private Cursor channelsListCursor = null;
    private static final String CHANEL_ID_KEY= "CHANEL_ID";

    public ChannelChangeActivity() {
        this.db = new DB(this);
        this.onCreateSubscribe(new ActivityListenerAdapter() {
            @Override
            public void onCreateActivity(@Nullable Bundle savedInstanceState) {
                setContentView(R.layout.activity_feed_change);
                channelsListInit();
            }
        });
    }

    static void start(@NonNull final Activity other) {
        final Intent intent = new Intent(other, ChannelChangeActivity.class);
        other.startActivityForResult(intent, CHANEL_CHANGE_CODE);
    }


    static long getResultChanelId(@NonNull final Intent intent) {
        return intent.getLongExtra(CHANEL_ID_KEY, -1);
    }

    private void channelsListInit() {
        final ListView channelsList = (ListView) findViewById(R.id.feed_change_list);
        TextView title = (TextView) findViewById(R.id.feed_change_text);
        if (channelsList != null) {
            DbReader dbReader = db.getReader();
            try {
                dbReader.open();
                channelsListCursor = dbReader.getAllChannelsCursor();
                if(!channelsListCursor.moveToFirst()){
                    if (title != null) {
                        title.setText(R.string.activity_channel_change_empty_list);
                    }
                    return;
                }


                final String[] from = {DbConvention.FEED_TITLE,
                        DbConvention.FEED_DESCRIPTION};
                final int[] to = {R.id.feeds_list_item_title, R.id.feeds_list_item_description};

                final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.feeds_list_item,
                        channelsListCursor, from, to);

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
            } catch (Throwable th) {
                th.printStackTrace();
                Toast.makeText(this, R.string.db_exception, Toast.LENGTH_SHORT).show();
            } finally {
                dbReader.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (channelsListCursor != null) {
            channelsListCursor.close();
        }
    }
}



