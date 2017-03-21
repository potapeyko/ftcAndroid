package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import lombok.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.model.Feed;
import potapeyko.rss.model.FeedItem;
import potapeyko.rss.parser.FeedParser;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbReader;
import potapeyko.rss.sql.DbWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static potapeyko.rss.utils.BroadcastSender.*;


public class AddChannelIntentService extends IntentService implements FeedParser.FeedHandler, FeedParser.FeedItemHandler {

    private static final String ACTION_ADD = "potapeyko.rss.activities.action.ADD";
    private static final String ACTION_ADD_STOP = "potapeyko.rss.activities.action.ADD.STOP";///todo как?

    private static final String EXTRA_URI = "potapeyko.rss.activities.extra.URL";

    private static final Long NOTHING_ID = -23L;

    private static final int ADD_INTENT_CONNECT_TIMEOUT = 1000;

    public AddChannelIntentService() {
        super("AddChannelIntentService");
    }

    private DB db;

    private long feedID = NOTHING_ID;

    static void startActionADD(@NonNull Context context, @NonNull String uri) {
        final Intent intent = new Intent(context, AddChannelIntentService.class);
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
            } else if (ACTION_ADD_STOP.equals(action)) {
                //todo // STOPSHIP: 04.02.2017
            }
        }
    }

    private void handleActionAdd(@NonNull String uri) {

        if (!Patterns.WEB_URL.matcher(uri).matches()) {
            sendMyBroadcast(this, CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            return;
        }

        //проверка, м.б. канал уже есть
        db = new DB(this);
        DbReader dbReader = null;
        try {
            dbReader = db.getReader();
            dbReader.open();
            boolean isInDb = dbReader.isFeedInDb(uri);
            if (isInDb) {
                sendMyBroadcast(this, CHANNEL_ALREADY_WAS_IN_DB_BROADCAST_MESS, 0);
                return;
            }
        } catch (/*DbException*/Throwable e) {
            e.printStackTrace();
            sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
            return;
        } finally {
            if (dbReader != null) {
                dbReader.close();
            }
        }

        //убедились в отсутствии канала, начинаем парсить
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(ADD_INTENT_CONNECT_TIMEOUT);
            urlConnection.connect();
            InputStream is = urlConnection.getInputStream();

            XmlPullParser xpp = FeedParser.prepareXpp(is);

            FeedParser parser = new FeedParser();
            //регистрация класса в качестве получателя сообщений
            parser.setFeedHandler(this);
            parser.setFeedItemHandler(this);

            parser.parseFeed(xpp, uri); //запуск парсинга
            sendMyBroadcast(this, CHANNEL_NEWS_ADD_BROADCAST_MESS, feedID);
        } catch (IOException | XmlPullParserException | FeedParser.UnknownFeedException e) {
            sendMyBroadcast(this, CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            //todo логирование
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    public void OnFeedInfo(FeedParser feedParser, Feed feed) {//получение от парсера инфы о канале
        //запись в бд. ответное сообщение.
        DbWriter dbWriter = null;
        Log.d("wtf","FEED  "+feed.getTitle()+" "+feed.getLink()+"\n");
        try {
            dbWriter = db.getWriter();
            dbWriter.open();
            long result = dbWriter.addFeedToDB(feed.getTitle(), feed.getLink(), feed.getSiteLink(), feed.getDescription(),
                    feed.getLastBuildDate(), feed.getPubDate(),0);
            if (result == -1) {
                sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
                //todo логирование
            } else {
                Log.d("wtf","FEED res");
                feedID = result;
                sendMyBroadcast(this, CHANNEL_ADD_BROADCAST_MESS, feedID);
            }
        } catch (Throwable th) {
            sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
            //todo логирование
        } finally {
            if (dbWriter != null) {
                dbWriter.close();
            }
        }
    }

    @Override
    public void OnFeedItem(FeedParser feedParser, FeedItem feedItem) {
        //получение от парсера инфы о новости канала

        if (feedID == NOTHING_ID) {
            Log.d("wtf","item  \n");
            return;
        }
        Log.d("wtf","item  "+feedItem.getTitle()+" "+feedItem.getLink()+"\n"+feedItem.getDescription());
        DbWriter dbWriter = null;
        try {

            dbWriter = db.getWriter();
            dbWriter.open();

            if (!dbWriter.isFeedItemInDb(feedItem)) {
                dbWriter.addFeedItemToDB(feedID, feedItem.getTitle(), feedItem.getLink(), feedItem.getDescription(),
                        feedItem.getPubDate(), feedItem.getMediaURL(), feedItem.getMediaSize(),0);

            }

        } catch (Throwable th) {
            sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
            //todo логирование
        } finally {
            if (dbWriter != null) {
                dbWriter.close();
            }
        }
    }
}
