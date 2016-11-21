package potapeyko.rss.activities;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.models.News;
import potapeyko.rss.sql.DB;

public final class FullNewsActivity extends MyBaseActivity implements IActivityListener {
    private long newsId;
    private final DB db;
    private News news;
    private static final String idKey = "FULL_NEWS_ID";

    public FullNewsActivity() {
        this.onSaveInstanceStateSubscribe(this);
        this.onCreateSubscribe(this);
        db = new DB(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_full_news);
        if (savedInstanceState != null && savedInstanceState.containsKey(idKey)) {
            newsId = savedInstanceState.getLong(idKey);
        } else {
            newsId = getIntent().getLongExtra(idKey, 1);
        }

        try {
            db.open();
            news = db.getNewsById(newsId);
            TextView title = (TextView)findViewById(R.id.activity_full_news_title);
            if (title != null) {
                title.setText(news.getTitle());
            }
            WebView description = (WebView)findViewById(R.id.activity_full_news_description);
            if (description != null) {
                description.loadDataWithBaseURL(null,news.getDescription(),"text/html","utf-8",null);
            }
            TextView link = (TextView)findViewById(R.id.activity_full_news_link);
            if (link != null) {
                link.setText("Перейти на сайт с полной версией статьи");
                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri address = Uri.parse(news.getFullNewsUri());
                        Intent uriOpen = new Intent(Intent.ACTION_VIEW, address);
                        view.getContext().startActivity(uriOpen);
                    }
                });
            }
        } finally {
            db.close();
        }


    }

    @Override
    public void onSaveInstanceStateActivity(Bundle outState) {
        outState.putLong(idKey, newsId);
    }

    static void start(Activity other, long newsId) {
        Intent intent = new Intent(other, FullNewsActivity.class);
        intent.putExtra(idKey, newsId);
        other.startActivity(intent);
    }
}


