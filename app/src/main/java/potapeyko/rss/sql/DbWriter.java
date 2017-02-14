package potapeyko.rss.sql;

import android.content.ContentValues;
import android.content.Context;
import lombok.NonNull;
import potapeyko.rss.exceptions.DbException;

import java.util.Date;

public class DbWriter extends DbReader {
    @Override
    public void open() {
        if (dBHelper == null) {
            dBHelper = new DBHelper(context);
        }
        dB = dBHelper.getWritableDatabase();
    }

    DbWriter(@NonNull Context context) {
        super(context);
    }

    public void addFeedItemToDB(final long feedId, final String title, final String link,
                                final String description, final Date pubDate, final String mediaUrl,
                                Long mediaSize) throws DbException {
        if (dB == null) return;
        final ContentValues cv = new ContentValues();
        cv.put(DbConvention.FEED_ITEM_FEED_ID, feedId);
        cv.put(DbConvention.FEED_ITEM_TITLE, title);
        cv.put(DbConvention.FEED_ITEM_LINK, link);
        cv.put(DbConvention.FEED_ITEM_DESCRIPTION, description);
        cv.put(DbConvention.FEED_ITEM_PUBLICATION_DATE, pubDate.getTime());
        cv.put(DbConvention.FEED_ITEM_MEDIA_URL, mediaUrl);
        cv.put(DbConvention.FEED_ITEM_MEDIA_SIZE, mediaSize);
        long result = -1;

        dB.beginTransaction();
        try {
            result = dB.insert(DbConvention.FEED_ITEM_TABLE_NAME, null, cv);
            dB.setTransactionSuccessful();
        } finally {
            dB.endTransaction();
        }
        if (result == -1) throw new DbException();
    }

    public long addFeedToDB(final String title, final String link, final String siteLink, final String description,
                            Date lastBuildDate, Date pubDate) {
        if (dB == null) return -1;
        final ContentValues cv = new ContentValues();
        cv.put(DbConvention.FEED_TITLE, title);
        cv.put(DbConvention.FEED_LINK, link);
        cv.put(DbConvention.FEED_SITE_LINK, siteLink);
        cv.put(DbConvention.FEED_DESCRIPTION, description);
        Long lastBuildD = lastBuildDate == null ? 1 : lastBuildDate.getTime();
        cv.put(DbConvention.FEED_BUILD_DATE, lastBuildD);
        Long pubD = pubDate == null ? 1 : pubDate.getTime();
        cv.put(DbConvention.FEED_PUBLICATION_DATE, pubD);




        long resultId;

        dB.beginTransaction();
        try {
            resultId = dB.insert(DbConvention.FEED_TABLE_NAME, null, cv);
            dB.setTransactionSuccessful();
            return resultId;
        } finally {
            dB.endTransaction();
        }
    }

    public void deleteFeedById(final long id) {
        if (dB == null) return;
        final String newsSelection = DbConvention.FEED_ITEM_FEED_ID + " = " + id;
        final String chanelSelection = DbConvention.FEED_ID + " = " + id;

        dB.beginTransaction();
        try {
            dB.delete(DbConvention.FEED_ITEM_TABLE_NAME, newsSelection, null);
            dB.delete(DbConvention.FEED_TABLE_NAME, chanelSelection, null);
            dB.setTransactionSuccessful();
        } finally {
            dB.endTransaction();
        }
    }
}
