package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Intent;


public class UpdateChannelIntentServiceStopper extends IntentService {
    public UpdateChannelIntentServiceStopper() {
        super("UpdateChannelIntentServiceStopper");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        UpdateChannelIntentService.stop();
    }
}
