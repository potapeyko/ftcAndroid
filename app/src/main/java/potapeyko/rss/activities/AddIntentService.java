package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Patterns;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.Exeptions.ConnectionException;
import potapeyko.rss.Exeptions.DbException;
import potapeyko.rss.parser.ParsHelper;
import potapeyko.rss.sql.DB;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class AddIntentService extends IntentService {

    private static final String ACTION_ADD = "potapeyko.rss.activities.action.ADD";

    private static final String EXTRA_URI = "potapeyko.rss.activities.extra.URL";

    private static final String CONNECTION_EXCEPTION_BROADCAST_MESS = "ConnectionException";
    private static final String DB_EXCEPTION_BROADCAST_MESS = "DBException";
    private static final String CHANNEL_ADD_BROADCAST_MESS = "ChannelAdded";
    private static final String CHANNEL_NEWS_ADD_BROADCAST_MESS = "ChannelNewsAdded";


    public AddIntentService() {
        super("AddIntentService");
    }

    static void startActionADD(@NonNull Context context, @NonNull String uri) {
        Intent intent = new Intent(context, AddIntentService.class);
        intent.setAction(ACTION_ADD);
        intent.putExtra(EXTRA_URI, uri);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD.equals(action)) {
                final String uri = intent.getStringExtra(EXTRA_URI);

                handleActionAdd(uri);

            }
        }
    }

    private void handleActionAdd(String uri) {

        if (!Patterns.WEB_URL.matcher(uri).matches()) {
            sendMyBroadcast(CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            return;
        }

        URL url;
        InputStream is;
        HttpURLConnection urlConnection = null;
        DB db = new DB(this);

        try {
            url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            // urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.connect();
            is = urlConnection.getInputStream();

            XmlPullParser xpp = ParsHelper.prepareXpp(is, null);

            ParsHelper helper = new ParsHelper(xpp, db);
            long channelId = helper.addChannel(url);
            if (channelId == -1) {
                throw new DbException();
            }
            sendMyBroadcast(CHANNEL_ADD_BROADCAST_MESS, channelId);
            helper.addNews(channelId);
            sendMyBroadcast(CHANNEL_NEWS_ADD_BROADCAST_MESS, channelId);
        } catch (DbException ex){
            sendMyBroadcast(DB_EXCEPTION_BROADCAST_MESS,0);
            ex.printStackTrace();
        }catch ( IOException | ConnectionException | XmlPullParserException e) {
            sendMyBroadcast(CONNECTION_EXCEPTION_BROADCAST_MESS,0);
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
    }


    private void sendMyBroadcast(String message, long data) {
        Intent serviceStartedIntent = new Intent("potapeyko.rss.activities");
        serviceStartedIntent.putExtra("message", message);
        serviceStartedIntent.putExtra("data", data);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(serviceStartedIntent);
    }
}
