//package potapeyko.rss.activities;
//
//import android.app.IntentService;
//import android.content.Context;
//import android.content.Intent;
//import android.support.annotation.NonNull;
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//import potapeyko.rss.exceptions.ConnectionException;
//import potapeyko.rss.exceptions.DbException;
//import potapeyko.rss.model.Feed;
//import potapeyko.rss.sql.DB;
//import potapeyko.rss.sql.DbReader;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//
//import static potapeyko.rss.utils.BroadcastSender.*;
//
//
//public class UpdateChannelIntentService2 extends IntentService {
//
//    private static final String ACTION_UPDATE = "potapeyko.rss.activities.action.UPDATE";
//
//
//    private static final int UPDATE_INTENT_CONNECT_TIMEOUT = 1000;
//
//
//    public UpdateChannelIntentService2() {
//        super("UpdateChannelIntentService");
//    }
//
//    public static void startActionUpdate(@NonNull Context context) {
//        Intent intent = new Intent(context, UpdateChannelIntentService2.class);
//        intent.setAction(ACTION_UPDATE);
//        context.startService(intent);
//    }
//
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_UPDATE.equals(action)) {
//                handleActionUpdate();
//            }
//        }
//    }
//
//    private void handleActionUpdate() {
//
//        DB db = new DB(this);
//        DbReader dbReader = db.getReader();
//        HttpURLConnection urlConnection = null;
//        try {
//            ArrayList<Feed> feeds;
//            try {
//                dbReader.open();
//                feeds = dbReader.getAllFeedsList();
//            } catch (Throwable th) {
//                throw new DbException(th);
//            }
//            finally {
//                dbReader.close();
//            }
//
//            for (Feed channel : feeds) {
//                URL url = new URL(channel.getLink());
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setConnectTimeout(UPDATE_INTENT_CONNECT_TIMEOUT);
//                urlConnection.connect();
//                InputStream is = urlConnection.getInputStream();
//                XmlPullParser xpp = ParsHelper.prepareXpp(is);
//                ParsHelper helper = new ParsHelper(xpp, db);
//
//                boolean areNewNews = helper.checkNews(channel.getId());
//
//                if (areNewNews) sendMyBroadcast(this, CHANNEL_UPDATE_BROADCAST_MESS,
//                        channel.getId());
//            }
//
//        } catch (DbException e) {
//            sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
//            e.printStackTrace();
//        } catch (IOException | XmlPullParserException | ConnectionException e) {
//            sendMyBroadcast(this, CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
//            e.printStackTrace();
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }
//    }
//}
