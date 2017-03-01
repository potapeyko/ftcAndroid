package potapeyko.rss.activities;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
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

    private static final int NOTIFICATION_ID = 1294124;

    private Long feedId;

    private boolean isNewFeedItem = false;
    private int quantityOfNewFeedItem = 0;
    private int quantityOfFeeds = 0;
    private int numberOfFeed = 0;
    private DB db;
    private NotificationManager notificationManager;
    private static FeedParser parser;
    private static boolean stopProcessing = false;

    public static void stop() {
        parser.stopProcessing();
        stopProcessing = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Toast toast = Toast.makeText(getApplicationContext(),
                "it work ftc onCreate Service", Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * @param progress if process is completed use null.
     * @param quantity - the amount of added news. Use null if this number is unknown.
     *                 ONE of the two options must be NULL
     */

    private Notification getNotification(String title, String text, String subText, Integer progress, Integer quantity) {
        NotificationCompat.Builder
            builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_update_white_24dp)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSubText(subText)
                    .setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }
        Intent notificationIntent;
        PendingIntent contentIntent;
        if (progress != null) {
            builder.setProgress(quantityOfFeeds, progress, false);
            notificationIntent = new Intent(this, UpdateChannelIntentServiceStopper.class);
            notificationIntent.setAction(ACTION_UPDATE_STOP);

            contentIntent = PendingIntent.getService(this,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            notificationIntent = new Intent(this, ChannelChangeActivity.class);
            contentIntent = PendingIntent.getActivity(this,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
        if (quantity != null) {
            builder.setNumber(quantity);
        }
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    private void sendNotification(Notification nf) {
        notificationManager.notify(NOTIFICATION_ID, nf);
    }

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
            quantityOfFeeds = feeds.size();
            for (Feed feed : feeds) {
                if (stopProcessing) {
                    break;
                }
                String uri = feed.getLink();
                URL url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(UPDATE_INTENT_CONNECT_TIMEOUT);
                urlConnection.connect();
                InputStream is = urlConnection.getInputStream();
                XmlPullParser xpp = FeedParser.prepareXpp(is);

                parser = new FeedParser();
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
                } else {
                    Log.d("wtf", "Обнова. Нового нет");
                }
            }
            stopProcessing = false;
            this.sendNotification(
                    this.getNotification(getString(R.string.notification_up),
                            "", getString(R.string.notification_updated_subtext),
                            null, quantityOfNewFeedItem));
            quantityOfNewFeedItem = 0;
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
        numberOfFeed++;
        Notification nf = this.getNotification(
                getString(R.string.notification_up),
                feed.getTitle(),
                getString(R.string.notification_updating_subtext),
                numberOfFeed, null);
        this.sendNotification(nf);
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
                        feedItem.getPubDate(), feedItem.getMediaURL(), feedItem.getMediaSize());//todo добавить поля
                isNewFeedItem = true;
                quantityOfNewFeedItem++;
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
