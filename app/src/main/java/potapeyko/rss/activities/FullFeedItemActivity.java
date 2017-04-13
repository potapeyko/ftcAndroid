package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.model.FeedItem;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbReader;

public class FullFeedItemActivity extends AppCompatActivity {
    private long feedItemId;
    private long feedId;
    private final DB db;
    private FeedItem feedItem;
    private int favoriteFlag = 0;///ВРЕМЕННО!!!
    private static final String idFeedItemKey = "FULL_NEWS_ID";
    private static final String idFeedKey = "FEED_ID";
    private static final int UNKNOWN_ID = -10;

    public FullFeedItemActivity() {
        db = new DB(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_feeditem);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(idFeedItemKey)) {
            feedItemId = savedInstanceState.getLong(idFeedItemKey);
            feedId = savedInstanceState.getLong(idFeedKey);
        } else {
            feedItemId = getIntent().getLongExtra(idFeedItemKey, UNKNOWN_ID);
            feedId = getIntent().getLongExtra(idFeedKey, UNKNOWN_ID);
        }

        TextView title = (TextView) findViewById(R.id.activity_full_feedItem_title);
        WebView description = (WebView) findViewById(R.id.activity_full_feedItem_description);
        TextView link = (TextView) findViewById(R.id.activity_full_feedItem_link);
        if (feedItemId == UNKNOWN_ID || feedId == UNKNOWN_ID) {
            if (title != null) {
                title.setText(R.string.activity_full_news_unknown_news_title);
            }
            if (description != null) {
                description.setVisibility(View.INVISIBLE);
            }
            if (link != null) {
                link.setVisibility(View.INVISIBLE);
            }
            return;
        }

        DbReader dbReader = null;
        try {
            dbReader = db.getReader();
            dbReader.open();
            feedItem = dbReader.getFeedItemById(feedItemId);
            if (title != null) {
                title.setText(feedItem.getTitle());
            }

            if (description != null) {
                description.loadDataWithBaseURL(null, feedItem.getDescription(), "text/html", "utf-8", null);
            }
            if (link != null) {
                link.setText(R.string.activity_full_news_link_text);
                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri address = Uri.parse(feedItem.getLink());
                        Intent uriOpen = new Intent(Intent.ACTION_VIEW, address);
                        view.getContext().startActivity(uriOpen);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(idFeedItemKey, feedItemId);
        outState.putLong(idFeedKey, feedId);
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

    static void start(@NonNull Activity other, long feedItemId, long feedId) {
        Intent intent = new Intent(other, FullFeedItemActivity.class);
        intent.putExtra(idFeedItemKey, feedItemId);
        intent.putExtra(idFeedKey, feedId);
        other.startActivity(intent);
    }
}


