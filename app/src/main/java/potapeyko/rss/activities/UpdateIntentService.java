package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
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


public class UpdateIntentService extends IntentService {

    private static final String ACTION_UPDATE = "potapeyko.rss.activities.action.UPDATE";

    private static final String CONNECTION_EXCEPTION_BROADCAST_MESS = "ConnectionException";
    private static final String DB_EXCEPTION_BROADCAST_MESS = "DBException";
    private static final String CHANNEL_UPDATE_BROADCAST_MESS = "updateChannel";
    private static final String ALL_CHANNELS_UPFATE_BROADCAST_MESS = "updateAllChannels";


    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    static void startActionUpdate(@NonNull Context context) {
        Intent intent = new Intent(context, UpdateIntentService.class);
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
        HttpURLConnection urlConnection;
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
                urlConnection.connect();
                is = urlConnection.getInputStream();
                xpp = ParsHelper.prepareXpp(is, null);
                helper = new ParsHelper(xpp, db);
                boolean areNewNews = helper.checkNews(channel.getId());//проверка внутри
                if (areNewNews) sendMyBroadcast(CHANNEL_UPDATE_BROADCAST_MESS, channel.getId());
            }
            sendMyBroadcast(ALL_CHANNELS_UPFATE_BROADCAST_MESS, 0);
        } catch (DbException e) {
            sendMyBroadcast(DB_EXCEPTION_BROADCAST_MESS, 0);
            e.printStackTrace();
        } catch (IOException | XmlPullParserException | ConnectionException e) {
            sendMyBroadcast(CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void sendMyBroadcast(String message, long data) {
        Intent serviceStartedIntent = new Intent("potapeyko.rss.activities");
        serviceStartedIntent.putExtra("message", message);
        serviceStartedIntent.putExtra("data", data);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(serviceStartedIntent);
    }


}
