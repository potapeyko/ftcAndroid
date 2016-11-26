package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.ActivityListenerAdapter;


public final class AboutAppActivity extends MyBaseActivity {
    public AboutAppActivity() {
        this.onCreateSubscribe(new ActivityListenerAdapter()
        {
            @Override
            public void onCreateActivity(@Nullable Bundle savedInstanceState) {
                setContentView(R.layout.activity_about);
            }
        });
    }

    static void start(@NonNull Activity other) {
        Intent intent = new Intent(other, AboutAppActivity.class);
        other.startActivity(intent);
    }

}
