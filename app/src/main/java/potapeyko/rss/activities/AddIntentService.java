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

import static potapeyko.rss.utils.BroadcastSender.*;


public class AddIntentService extends IntentService {

    private static final String ACTION_ADD = "potapeyko.rss.activities.action.ADD";

    private static final String EXTRA_URI = "potapeyko.rss.activities.extra.URL";


    private static final int ADD_INTENT_CONNECT_TIMEOUT = 1000;

    public AddIntentService() {
        super("AddIntentService");
    }

    static void startActionADD(@NonNull Context context, @NonNull String uri) {
        final Intent intent = new Intent(context, AddIntentService.class);
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
            sendMyBroadcast(this,CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            return;
        }

        URL url;
        InputStream is;
        HttpURLConnection urlConnection = null;
        DB db = new DB(this);

        try {
            url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(ADD_INTENT_CONNECT_TIMEOUT);
            urlConnection.connect();
            is = urlConnection.getInputStream();

            XmlPullParser xpp = ParsHelper.prepareXpp(is, null);

            final ParsHelper helper = new ParsHelper(xpp, db);
            final long channelId = helper.addChannel(url);
            if (channelId == ParsHelper.OK_RESULT_WITHOUT_ID) {
                sendMyBroadcast(this,CHANNEL_ALREADY_WAS_IN_DB_BROADCAST_MESS, channelId);
                return;
            }

            sendMyBroadcast(this,CHANNEL_ADD_BROADCAST_MESS, channelId);
            helper.addNews(channelId);
            sendMyBroadcast(this,CHANNEL_NEWS_ADD_BROADCAST_MESS, channelId);

        } catch (DbException e){
            sendMyBroadcast(this,DB_EXCEPTION_BROADCAST_MESS,0);
            e.printStackTrace();
        }catch ( IOException | ConnectionException | XmlPullParserException e) {
            sendMyBroadcast(this,CONNECTION_EXCEPTION_BROADCAST_MESS,0);
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
