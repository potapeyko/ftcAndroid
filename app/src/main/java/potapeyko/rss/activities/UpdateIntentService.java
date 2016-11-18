package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.models.Channel;
import potapeyko.rss.parser.ParsHelper;
import potapeyko.rss.sql.DB;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class UpdateIntentService extends IntentService {

    private static final String ACTION_UPDATE = "potapeyko.rss.activities.action.UPDATE";


    public UpdateIntentService() {
        super("UpdateIntentService");
    }


     static void startActionUpdate(Context context) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_UPDATE);
        context.startService(intent);
         Log.e("AAAA","AAA1");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {
                Log.e("AAAA","AAA2");
                handleActionUpdate();
            }
        }
    }

    private void handleActionUpdate() {
        URL url;
        InputStream is;
        HttpURLConnection urlConnection;
        DB db = new DB(this);

        try {
            Log.e("AAAA","AAA3");
            db.open();
            ArrayList<Channel> channels = db.getAllChannelsList();
            for (Channel channel : channels) {
                Log.e("AAAA","AAA4");
                url = new URL(channel.getLink());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                is = urlConnection.getInputStream();
                XmlPullParser xpp = ParsHelper.prepareXpp(is, null);
                ParsHelper helper = new ParsHelper(xpp, db);

                helper.checkNews(channel.getId());
                //закончили с новостями канала
            }
            Log.e("AAAA","AAA5");
            //звкончили со всеми каналами
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            Log.e("AAAA","AAA6");
        } finally {
            db.close();
            Log.e("AAAA","AAA7");
        }
    }
}
