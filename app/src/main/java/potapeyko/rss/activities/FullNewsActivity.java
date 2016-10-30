package potapeyko.rss.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;

public class FullNewsActivity extends MyBaseActivity implements IActivityListener {

    public FullNewsActivity() {
        this.onCreateSubscribe(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_full_news);
    }
}
