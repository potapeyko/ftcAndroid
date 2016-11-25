package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.Exeptions.ConnectionException;
import potapeyko.rss.Exeptions.DbException;
import potapeyko.rss.models.Channel;
import potapeyko.rss.parser.ParsHelper;
import potapeyko.rss.sql.DB;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static potapeyko.rss.utils.BroadcastSender.*;


public class UpdateChannelIntentService extends IntentService {

    private static final String ACTION_UPDATE = "potapeyko.rss.activities.action.UPDATE";


    private static final int UPDATE_INTENT_CONNECT_TIMEOUT = 1000;


    public UpdateChannelIntentService() {
        super("UpdateChannelIntentService");
    }

    public static void startActionUpdate(@NonNull Context context) {
        Intent intent = new Intent(context, UpdateChannelIntentService.class);
        intent.setAction(ACTION_UPDATE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {

                handleActionUpdate();

            }
        }
    }

    private void handleActionUpdate() {
        URL url;
        InputStream is;
        HttpURLConnection urlConnection = null;
        DB db = new DB(this);
        XmlPullParser xpp;
        ParsHelper helper;
        ArrayList<Channel> channels;

        try {
            try {
                db.open();
                channels = db.getAllChannelsList();
            }catch (Throwable th){
                throw new DbException(th);
            }

            for (Channel channel : channels) {
                url = new URL(channel.getLink());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(UPDATE_INTENT_CONNECT_TIMEOUT);
                urlConnection.connect();
                is = urlConnection.getInputStream();
                xpp = ParsHelper.prepareXpp(is);
                helper = new ParsHelper(xpp, db);

                boolean areNewNews = helper.checkNews(channel.getId());

                if (areNewNews) sendMyBroadcast(this, CHANNEL_UPDATE_BROADCAST_MESS,
                        channel.getId());
            }

        } catch (DbException e) {
            sendMyBroadcast(this,DB_EXCEPTION_BROADCAST_MESS, 0);
            e.printStackTrace();
        } catch (IOException | XmlPullParserException | ConnectionException e) {
            sendMyBroadcast(this,CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            db.close();
        }
    }
}
