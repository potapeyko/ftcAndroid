package potapeyko.rss.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;

public final class FullNewsActivity extends MyBaseActivity implements IActivityListener {
    private long newsId;
    private static final String idKey = "FULL_NEWS_ID";
    public FullNewsActivity() {
        this.onSaveInstanceStateSubscribe(this);
        this.onCreateSubscribe(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_full_news);
        if (savedInstanceState != null && savedInstanceState.containsKey(idKey)) {
            newsId = savedInstanceState.getLong(idKey);
        } else {
            newsId = getIntent().getLongExtra(idKey, 1);
        }
    }

    @Override
    public void onSaveInstanceStateActivity(Bundle outState) {
        outState.putLong(idKey,newsId);
    }

    static void start(Activity other, long newsId) {
        Intent intent = new Intent(other, FullNewsActivity.class);
        intent.putExtra(idKey, newsId);
        other.startActivity(intent);
    }
}


//    public FullNewsActivity() {
//        this.onCreateSubscribe(new ActivityListenerAdapter() {
//            @Override
//            public void onCreateActivity(@Nullable Bundle savedInstanceState) {
//                setContentView(R.layout.activity_full_news);
//                if(savedInstanceState!=null&&savedInstanceState.containsKey("newsId")){
//                    newsId = savedInstanceState.getLong("newsId");
//                }
//                else{
//                    newsId = getIntent().getLongExtra("newsId",1);
//                }
//            }
//        });
//    }