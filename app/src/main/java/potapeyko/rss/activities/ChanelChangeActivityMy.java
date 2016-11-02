package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import potapeyko.rss.interfaces.IActivityListener;


public final class ChanelChangeActivityMy extends MyBaseActivity implements IActivityListener {
    public ChanelChangeActivityMy() {
        this.onCreateSubscribe(this);
    }

    static void start(Activity other) {
        Intent intent = new Intent(other, ChanelChangeActivityMy.class);
        other.startActivity(intent);
    }
    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onSaveInstanceStateActivity(Bundle outState) {

    }
}


