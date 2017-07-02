package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbConvention;
import potapeyko.rss.sql.DbReader;


public final class FeedChangeActivity extends MyBaseActivity {
    static final int CHANEL_CHANGE_CODE = 1235;
    private Cursor channelsListCursor = null;
    private static final String CHANEL_ID_KEY = "CHANEL_ID";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_change);
        Toolbar toolbar = initToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        channelsListInit();
    }

    static void start(@NonNull final Activity other) {
        final Intent intent = new Intent(other, FeedChangeActivity.class);
        other.startActivityForResult(intent, CHANEL_CHANGE_CODE);
    }


    static long getResultChanelId(@NonNull final Intent intent) {
        return intent.getLongExtra(CHANEL_ID_KEY, -1);
    }

    private void channelsListInit() {
        final ListView channelsList = (ListView) findViewById(R.id.feed_change_list); //получаем список с разметки
        TextView title = (TextView) findViewById(R.id.feed_change_text);
        if (channelsList != null) {
            DbReader dbReader = DB.getReader(this);
            try {
                dbReader.open();
                channelsListCursor = dbReader.getAllChannelsCursor();
                if (!channelsListCursor.moveToFirst()) {
                    if (title != null) {
                        title.setText(R.string.activity_channel_change_empty_list);
                        title.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NewFeedActivity.start(FeedChangeActivity.this);
                            }
                        });
                    }
                    return;
                }


                final String[] from = {DbConvention.FEED_TITLE, //откуда брать данные
                        DbConvention.FEED_DESCRIPTION, DbConvention.FEED_COUNT};
                final int[] to = {R.id.feeds_list_item_title, //в какие поля записывать
                        R.id.feeds_list_item_description, R.id.feeds_list_item_flag};

                final SimpleCursorAdapter adapter =
                        new SimpleCursorAdapter(this, R.layout.feeds_list_item,
                        channelsListCursor, from, to);

                channelsList.setAdapter(adapter);
                channelsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//устанавливаем обработчик нажатия
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



