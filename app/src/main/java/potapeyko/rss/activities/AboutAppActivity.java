package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import lombok.NonNull;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.ActivityListenerAdapter;


public final class AboutAppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    static void start(@NonNull Activity other) {
        Intent intent = new Intent(other, AboutAppActivity.class);
        other.startActivity(intent);
    }

}
