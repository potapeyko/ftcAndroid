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
                                Long mediaSize, int checkedFlag,int favoriteFlag) throws DbException {
        if (dB == null) return;
        final ContentValues cv = new ContentValues();
        cv.put(DbConvention.FEED_ITEM_FEED_ID, feedId);
        cv.put(DbConvention.FEED_ITEM_TITLE, title);
        cv.put(DbConvention.FEED_ITEM_LINK, link);
        cv.put(DbConvention.FEED_ITEM_DESCRIPTION, description);
        cv.put(DbConvention.FEED_ITEM_PUBLICATION_DATE, pubDate.getTime());
        cv.put(DbConvention.FEED_ITEM_MEDIA_URL, mediaUrl);
        cv.put(DbConvention.FEED_ITEM_MEDIA_SIZE, mediaSize);
        cv.put(DbConvention.FEED_ITEM_FLAGS_CHECKED, checkedFlag);
        cv.put(DbConvention.FEED_ITEM_FLAGS_FAVORITE, favoriteFlag);
        long result = -1;

        dB.beginTransaction();
        try {
            result = dB.insert(DbConvention.FEED_ITEM_TABLE_NAME, null, cv);
            if(result!=-1){
                String sql = "UPDATE `" + DbConvention.FEED_TABLE_NAME +
                        "` SET `"+DbConvention.FEED_COUNT+"` = `"+
                        DbConvention.FEED_COUNT+"` + 1 WHERE _id = " + feedId;
                dB.execSQL(sql);
            }
            dB.setTransactionSuccessful();
        } finally {
            dB.endTransaction();
        }
        if (result == -1) throw new DbException();
    }


    public long addFeedToDB(final String title, final String link, final String siteLink, final String description,
                            Date lastBuildDate, Date pubDate, int feedItemCount) {
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
        cv.put(DbConvention.FEED_COUNT, feedItemCount);

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

    public void changeFeedItemFlags(final long feedItemId,final long feedId, int newCheckedFlag,int lastCheckedFlag,
                                    int newFavoriteFlag,int lastFavoriteFlag) {
        if (dB == null) return;
        final ContentValues cv = new ContentValues();
        if(newCheckedFlag!=lastCheckedFlag){
        cv.put(DbConvention.FEED_ITEM_FLAGS_CHECKED, newCheckedFlag);}
        if(newFavoriteFlag!=lastFavoriteFlag){
        cv.put(DbConvention.FEED_ITEM_FLAGS_FAVORITE, newFavoriteFlag);}
        if(cv.size()==0)return;
        dB.beginTransaction();
        try {
            dB.update(DbConvention.FEED_ITEM_TABLE_NAME, cv, "_id = ?",
                    new String[]{String.valueOf(feedItemId)});
            if(newCheckedFlag!=lastCheckedFlag){
                String actionSign;
                if(newCheckedFlag>lastCheckedFlag){actionSign=" - ";}
                else{actionSign=" + ";}
                String sql = "UPDATE `" + DbConvention.FEED_TABLE_NAME +
                        "` SET `"+DbConvention.FEED_COUNT+"` = `"
                        +DbConvention.FEED_COUNT+"` "+actionSign+" 1 WHERE _id = " + feedId;
                dB.execSQL(sql);
            }
            dB.setTransactionSuccessful();
        } finally {
            dB.endTransaction();
        }
    }
    public void changeFeedCount(final long feedId, int newCount) {
        if (dB == null) return;
        final ContentValues cv = new ContentValues();
        cv.put(DbConvention.FEED_COUNT, newCount);
        dB.beginTransaction();
        try {
            dB.update(DbConvention.FEED_TABLE_NAME, cv, "_id = ?",
                    new String[]{String.valueOf(feedId)});
            dB.setTransactionSuccessful();
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
