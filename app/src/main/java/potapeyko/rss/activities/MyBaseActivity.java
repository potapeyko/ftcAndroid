package potapeyko.rss.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;


import java.util.ArrayList;


public class MyBaseActivity extends AppCompatActivity {

    protected Toolbar initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        return toolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.refreshButton) {
            UpdateChannelIntentService.startActionUpdate(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
