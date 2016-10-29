package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;

public class NewChanelActivityMy extends MyBaseActivity implements IActivityListener {

    public NewChanelActivityMy(){
        this.onCreateSubscribe(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_chanel);
    }
    static void start(Activity other){
        Intent intent = new Intent(other,NewChanelActivityMy.class);
        other.startActivity(intent);
    }
}
