package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import android.widget.TextView;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.model.FeedItem;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.sql.DbWriter;

public class FullFeedItemActivity extends MyBaseActivity {
    private long feedItemId;
    private long feedId;
    private final DB db;
    private FeedItem feedItem;
    private int favoriteFlag = 0;///ВРЕМЕННО!!!
    private static final String idFeedItemKey = "FULL_NEWS_ID";
    private static final String idFeedKey = "FEED_ID";
    private static final int UNKNOWN_ID = -10;
    private ImageView favoriteBtn;

    public FullFeedItemActivity() {
        db = new DB(FullFeedItemActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.share, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        if(item!=null){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, feedItem.getLink());
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(shareActionProvider!=null) {

            shareActionProvider.setShareIntent(shareIntent);}
        }
//            shareActionProvider = new ShareActionProvider(FullFeedItemActivity.this);
//            shareActionProvider.setShareIntent(shareIntent);
//            MenuItemCompat.setActionProvider(item, shareActionProvider);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
        favoriteBtn = (ImageView) findViewById(R.id.activity_full_feeditem_favoriteBtn);
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
            if (favoriteBtn != null) {
                if (feedItem.getFavoriteFlag() == MyTag.trueValue) {
                    favoriteBtn.setImageResource(R.drawable.ic_item_bookmark_white);
                } else {
                    favoriteBtn.setImageResource(R.drawable.ic_item_bookmark_not_white);
                }
                favoriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean result = changeFavoriteFlagInDb();
                        if(result) {
                            if (feedItem.getFavoriteFlag() == MyTag.trueValue) {
                                favoriteBtn.setImageResource(R.drawable.ic_item_bookmark_not_white);
                                feedItem.setFavoriteFlag(MyTag.falseValue);
                            } else {
                                favoriteBtn.setImageResource(R.drawable.ic_item_bookmark_white);
                                feedItem.setFavoriteFlag(MyTag.trueValue);
                            }
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

    private boolean changeFavoriteFlagInDb() {
        if (feedItem != null) {
            int newFlag;
            DbWriter dbWriter = null;
            try {
                if (feedItem.getFavoriteFlag() == MyTag.trueValue) {
                    newFlag = MyTag.falseValue;
                } else {
                    newFlag = MyTag.trueValue;
                }
                dbWriter = db.getWriter();
                dbWriter.open();
                dbWriter.changeFeedItemFlags(feedItemId, feedId, feedItem.getCheckedFlag(), feedItem.getCheckedFlag(),
                        newFlag, feedItem.getFavoriteFlag());
                return true;
            }catch (Throwable th){
                th.printStackTrace();
                return false;
            }
            finally {
                if(dbWriter!=null){dbWriter.close();}
            }
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(idFeedItemKey, feedItemId);
        outState.putLong(idFeedKey, feedId);
    }

    static void start(@NonNull Activity other, long feedItemId, long feedId) {
        Intent intent = new Intent(other, FullFeedItemActivity.class);
        intent.putExtra(idFeedItemKey, feedItemId);
        intent.putExtra(idFeedKey, feedId);
        other.startActivity(intent);
    }
}


