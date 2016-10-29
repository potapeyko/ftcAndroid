package potapeyko.rss.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import potapeyko.rss.interfaces.IActivityListener;


import java.util.ArrayList;


public  class MyBaseActivity extends Activity{
    private final ArrayList<IActivityListener> onCreateListeners = new ArrayList<>();
    void onCreateSubscribe(IActivityListener listener){
        onCreateListeners.add(listener);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for(IActivityListener listener : onCreateListeners ){
            listener.onCreateActivity(savedInstanceState);
        }
    }

}
