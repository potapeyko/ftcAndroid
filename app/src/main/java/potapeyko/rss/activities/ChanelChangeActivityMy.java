package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import potapeyko.rss.R;
import potapeyko.rss.adapters.ChanelListAdapter;
import potapeyko.rss.constants.LogCodes;
import potapeyko.rss.interfaces.ActivityListenerAdapter;
import potapeyko.rss.models.Chanel;
import potapeyko.rss.sql.DB;

import java.util.ArrayList;

import static potapeyko.rss.constants.OtherCodes.CHANEL_CHANGE_CODE;
import static potapeyko.rss.constants.OtherCodes.CHANEL_ID_KEY;


public final class ChanelChangeActivityMy extends MyBaseActivity {
    private ListView chanelsList;
    private final DB db;
    ArrayList<Long> chanelsId;


    public ChanelChangeActivityMy() {
        this.db = new DB(this);
        this.onCreateSubscribe(new ActivityListenerAdapter() {
            @Override
            public void onCreateActivity(@Nullable Bundle savedInstanceState) {
                setContentView(R.layout.activity_chanel_change);
                chanelsListInit();
            }
        });
    }

    static void start(Activity other) {
        Intent intent = new Intent(other, ChanelChangeActivityMy.class);
        other.startActivityForResult(intent, CHANEL_CHANGE_CODE);
    }


    static long getResultChanelId(Intent intent) {
        return intent.getLongExtra(CHANEL_ID_KEY, -1);
    }

    private void chanelsListInit() {
        chanelsList = (ListView) findViewById(R.id.chanel_change_list);
        if (chanelsList != null) {
            db.open();
            try {
                ArrayList<Chanel> chanels = db.getAllChanelsList();
                ChanelListAdapter adapter = new ChanelListAdapter(this, chanels);
                chanelsList.setAdapter(adapter);
                chanelsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent();
                        intent.putExtra(CHANEL_ID_KEY, id);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            } finally {
                db.close();
            }
        } else {
            Log.e(LogCodes.MAIN_ACTIVITY, "не найден ListView activity_main_newsList");
        }
    }
}



