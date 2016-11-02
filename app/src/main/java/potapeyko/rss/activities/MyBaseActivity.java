package potapeyko.rss.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import potapeyko.rss.interfaces.IActivityListener;


import java.util.ArrayList;


public  class MyBaseActivity extends AppCompatActivity {
    private final ArrayList<IActivityListener> onCreateListeners = new ArrayList<>();
    private final ArrayList<IActivityListener> onSaveInstanceStateListeners = new ArrayList<>();
    void onCreateSubscribe(IActivityListener listener){
        onCreateListeners.add(listener);
    }
    void onSaveInstanceStateSubscribe(IActivityListener listener){
        onSaveInstanceStateListeners.add(listener);
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for(IActivityListener listener : onCreateListeners ){
            listener.onCreateActivity(savedInstanceState);
        }
    }

    @Override
    protected final void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        for(IActivityListener listener : onSaveInstanceStateListeners ){
            listener.onSaveInstanceStateActivity(outState);
        }
    }
}
