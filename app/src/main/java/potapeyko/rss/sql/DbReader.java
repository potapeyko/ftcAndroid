package potapeyko.rss.sql;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import lombok.NonNull;
import potapeyko.rss.exceptions.DbException;
import potapeyko.rss.model.Feed;
import potapeyko.rss.model.FeedItem;

import java.util.ArrayList;
import java.util.Date;

public class DbReader {

    final Context context;
    DBHelper dBHelper;

    SQLiteDatabase dB;

    DbReader(final @NonNull Context context) {
        this.context = context;
    }

    public void open() {
        if (dBHelper == null) {
            dBHelper = new DBHelper(context);
        }
        dB = dBHelper.getReadableDatabase();
    }

    public void close() {
        if (dBHelper != null) dBHelper.close();
        dB = null;
    }

    /**
     * @param id - channel id
     * @return channel or null if this channel not found.
     */
    public Feed getFeedById(final long id) throws DbException {
        if (dB == null) throw new DbException();
        final String selection = "_id = " + id;
        Cursor cur = null;
        Feed resFeed = null;
        try {
            cur = dB.query(DbConvention.FEED_TABLE_NAME, null, selection, null, null, null, null);
            if (cur.moveToFirst()) {
                Date lastBuildDate = cur.isNull(5) ? null : new Date(cur.getLong(5));
                Date pubDate = cur.isNull(6) ? null : new Date(cur.getLong(6));
                String title = cur.isNull(1) ? null : cur.getString(1);
                String link = cur.isNull(2) ? null : cur.getString(2);
                String siteLink = cur.isNull(3) ? null : cur.getString(3);
                String description = cur.isNull(4) ? null : cur.getString(4);
                resFeed = new Feed(cur.getLong(0), title, link, siteLink, description,
                        lastBuildDate, pubDate);
            }
            return resFeed;
        } catch (Throwable th) {
            throw new DbException(th);
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    public FeedItem getFeedItemById(final long id) throws DbException {
        if (dB == null) throw new DbException();
        final String[] columns = {DbConvention.FEED_ITEM_ID, DbConvention.FEED_ITEM_TITLE,
                DbConvention.FEED_ITEM_DESCRIPTION, DbConvention.FEED_ITEM_LINK, DbConvention.FEED_ITEM_PUBLICATION_DATE,
                DbConvention.FEED_ITEM_MEDIA_URL, DbConvention.FEED_ITEM_MEDIA_SIZE};
        final String selection = "_id = " + id;
        Cursor cur = null;
        try {
            cur = dB.query(DbConvention.FEED_ITEM_TABLE_NAME, columns, selection, null, null, null, null);
            FeedItem resFeedItem = null;

            if (cur.moveToFirst()) {
                String title = cur.isNull(1) ? null : cur.getString(1);
                String description = cur.isNull(2) ? null : cur.getString(2);
                String link = cur.isNull(3) ? null : cur.getString(3);
                Date pubDate = cur.isNull(4) ? null : new Date(cur.getLong(4));
                String mediaUrl = cur.isNull(5) ? null : cur.getString(5);
                Long mediaSize = cur.isNull(6) ? null : cur.getLong(6);

                resFeedItem = new FeedItem(cur.getLong(0), title, description, link, pubDate, mediaUrl, mediaSize);
            }
            return resFeedItem;
        } catch (Throwable th) {
            throw new DbException(th);
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    public Cursor getAllChannelsCursor() throws DbException {
        if (dB == null) throw new DbException();
        return dB.query(DbConvention.FEED_TABLE_NAME, null, null, null, null, null, null);
    }

    public ArrayList<Feed> getAllFeedsList() throws DbException {
        if (dB == null) throw new DbException();
        final ArrayList<Feed> list = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = dB.query(DbConvention.FEED_TABLE_NAME, null, null, null, null, null, null);
            if (cur.moveToFirst()) {
                Feed feed;
                do {
                    Date lastBuildDate = cur.isNull(5) ? null : new Date(cur.getLong(5));
                    Date pubDate = cur.isNull(6) ? null : new Date(cur.getLong(6));
                    String title = cur.isNull(1) ? null : cur.getString(1);
                    String link = cur.isNull(2) ? null : cur.getString(2);
                    String siteLink = cur.isNull(3) ? null : cur.getString(3);
                    String description = cur.isNull(4) ? null : cur.getString(4);
                    feed = new Feed(cur.getLong(0), title, link, siteLink, description,
                            lastBuildDate, pubDate);
                    list.add(feed);
                }
                while (cur.moveToNext());
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return list;
    }

    public Cursor getAllItemsOfFeedCursor(final long feedId) throws DbException {
        if (dB == null) throw new DbException();
        final String[] columns = new String[]{
                DbConvention.FEED_ITEM_ID,
                DbConvention.FEED_ITEM_TITLE,
                DbConvention.FEED_ITEM_DESCRIPTION,
                DbConvention.FEED_ITEM_LINK,
                DbConvention.FEED_ITEM_PUBLICATION_DATE,
                DbConvention.FEED_ITEM_MEDIA_URL,
                DbConvention.FEED_ITEM_MEDIA_SIZE
        };
        final String selection = DbConvention.FEED_ITEM_FEED_ID + " = " + feedId;
        final String orderBy = DbConvention.FEED_ITEM_ID + DbConvention.SORT_DESCENDING;
        return dB.query(DbConvention.FEED_ITEM_TABLE_NAME, columns, selection, null, null, null, orderBy);
    }

    public boolean isFeedInDb(final String link) throws DbException {
        if (dB == null) throw new DbException();
        if (link == null) return false;

        final String[] columns = {DbConvention.FEED_ID};
        final String selection = DbConvention.FEED_LINK + " = '" + link + "'";

        final Cursor cursor = dB.query(DbConvention.FEED_TABLE_NAME, columns, selection, null, null, null, null);
        final boolean res = cursor.getCount() > 0;
        cursor.close();
        return res;
    }

    public boolean isFeedItemInDb(final FeedItem feedItem) throws DbException {
        if (feedItem == null) return false;
        if (dB == null) throw new DbException();

        final String[] columns = {DbConvention.FEED_ITEM_TITLE};
        final String selection = feedItem.getLink() == null ? DbConvention.FEED_ITEM_LINK + "IS NULL" :
                DbConvention.FEED_ITEM_LINK + " = '" + feedItem.getLink() + "'";

        Cursor cursor = null;
        boolean res;
        try {
            cursor = dB.query(DbConvention.FEED_ITEM_TABLE_NAME, columns, selection, null, null, null, null);
            res = cursor.getCount() > 0;
            return res;
        } catch (Throwable th) {
            throw new DbException(th);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    final class DBHelper extends SQLiteOpenHelper {
        DBHelper(final Context context) {
            super(context, DbConvention.DB_NAME, null, DbConvention.DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(DbConvention.CREATE_FEED_TABLE_COMMAND);
            db.execSQL(DbConvention.DB_CREATE_NEWS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
