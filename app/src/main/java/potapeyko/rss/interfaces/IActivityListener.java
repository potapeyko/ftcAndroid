package potapeyko.rss.interfaces;

import android.os.Bundle;
import android.support.annotation.Nullable;


public interface IActivityListener {
    void onCreateActivity(@Nullable Bundle savedInstanceState);
    void onSaveInstanceStateActivity (Bundle outState);
}
