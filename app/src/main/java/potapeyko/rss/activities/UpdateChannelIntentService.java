package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.exceptions.ConnectionException;
import potapeyko.rss.exceptions.DbException;
import potapeyko.rss.model.Channel;
import potapeyko.rss.parser.ParsHelper;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.sql.DbWriter;

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

        DB db = new DB(this);
        DbReader dbReader = db.getReader();
        HttpURLConnection urlConnection = null;
        try {
            ArrayList<Channel> channels;
            try {
                dbReader.open();
                channels = dbReader.getAllChannelsList();
            } catch (Throwable th) {
                throw new DbException(th);
            }
            finally {
                dbReader.close();
            }

            for (Channel channel : channels) {
                URL url = new URL(channel.getLink());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(UPDATE_INTENT_CONNECT_TIMEOUT);
                urlConnection.connect();
                InputStream is = urlConnection.getInputStream();
                XmlPullParser xpp = ParsHelper.prepareXpp(is);
                ParsHelper helper = new ParsHelper(xpp, db);

                boolean areNewNews = helper.checkNews(channel.getId());

                if (areNewNews) sendMyBroadcast(this, CHANNEL_UPDATE_BROADCAST_MESS,
                        channel.getId());
            }

        } catch (DbException e) {
            sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
            e.printStackTrace();
        } catch (IOException | XmlPullParserException | ConnectionException e) {
            sendMyBroadcast(this, CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
