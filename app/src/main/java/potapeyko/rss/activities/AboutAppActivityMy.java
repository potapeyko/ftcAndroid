package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.ActivityListenerAdapter;
import potapeyko.rss.interfaces.IActivityListener;


public final class AboutAppActivityMy extends MyBaseActivity {

    public AboutAppActivityMy() {
        this.onCreateSubscribe(new ActivityListenerAdapter()
        {
            @Override
            public void onCreateActivity(@Nullable Bundle savedInstanceState) {
                setContentView(R.layout.activity_about);
            }
        });
    }

    static void start(Activity other) {
        Intent intent = new Intent(other, AboutAppActivityMy.class);
        other.startActivity(intent);
    }
}
