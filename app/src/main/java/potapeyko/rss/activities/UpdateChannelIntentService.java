package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.R;
import potapeyko.rss.exceptions.DbException;
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
import java.util.ArrayList;

import static potapeyko.rss.utils.BroadcastSender.*;


public class UpdateChannelIntentService extends IntentService implements FeedParser.FeedHandler,
        FeedParser.FeedItemHandler {

    private static final String ACTION_UPDATE = "potapeyko.rss.activities.action.UPDATE";
    private static final String ACTION_UPDATE_STOP = "potapeyko.rss.activities.action.UPDATE_STOP";


    private static final int UPDATE_INTENT_CONNECT_TIMEOUT = 1000;

    private Long feedId;
    private boolean isNewFeedItem = false;
    private DB db;

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
            } else if (ACTION_UPDATE_STOP.equals(action)) {
                //todo // STOPSHIP: 04.02.2017  
            }
        }
    }

    private void handleActionUpdate() {

        db = new DB(this);
        DbReader dbReader = db.getReader();
        HttpURLConnection urlConnection = null;
        try {
            ArrayList<Feed> feeds;
            try {
                dbReader.open();
                feeds = dbReader.getAllFeedsList();
            } catch (Throwable th) {
                throw new DbException(th);
            } finally {
                dbReader.close();
            }

            for (Feed feed : feeds) {
                String uri = feed.getLink();
                URL url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(UPDATE_INTENT_CONNECT_TIMEOUT);
                urlConnection.connect();
                InputStream is = urlConnection.getInputStream();
                XmlPullParser xpp = FeedParser.prepareXpp(is);

                FeedParser parser = new FeedParser();
                //регистрация класса в качестве получателя сообщений
                parser.setFeedHandler(this);
                parser.setFeedItemHandler(this);
                feedId = feed.getId();
                isNewFeedItem = false;
                parser.parseFeed(xpp, uri); //запуск парсинга
                //отправка собщения об обновлении канала.
                if (isNewFeedItem) {
                    sendMyBroadcast(this, CHANNEL_UPDATE_BROADCAST_MESS,
                            feed.getId());
                }
                else {
                    Log.d("wtf","Обнова. Нового нет");
                }
            }
        } catch (IOException | XmlPullParserException | FeedParser.UnknownFeedException e) {
            sendMyBroadcast(this, CONNECTION_EXCEPTION_BROADCAST_MESS, 0);
            //todo логирование
            e.printStackTrace();
        } catch (DbException e) {
            sendMyBroadcast(this, DB_EXCEPTION_BROADCAST_MESS, 0);
            //todo логирование
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    public void OnFeedInfo(FeedParser feedParser, Feed feed) {
        //do nothing
    }

    @Override
    public void OnFeedItem(FeedParser feedParser, FeedItem feedItem) {
        //получение от парсера инфы о новости канала
        DbWriter dbWriter = null;
        try {
            dbWriter = db.getWriter();
            dbWriter.open();
            if (!dbWriter.isFeedItemInDb(feedItem)) {
                dbWriter.addFeedItemToDB(feedId, feedItem.getTitle(), feedItem.getLink(), feedItem.getDescription(),
                        feedItem.getPubDate(),feedItem.getMediaURL(),feedItem.getMediaSize());//todo добавить поля
                isNewFeedItem = true;
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
