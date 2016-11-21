package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.ActivityListenerAdapter;
import potapeyko.rss.sql.DB;


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

    public void del(View view){
        DB db = new DB(this);
        try {
            db.open();
            db.clearAll();
        }finally {
            db.close();
        }

    }
    public void add(View view){
        UpdateIntentService.startActionUpdate(this);
    }

}
