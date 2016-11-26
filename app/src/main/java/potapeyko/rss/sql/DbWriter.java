package potapeyko.rss.sql;

import android.content.ContentValues;
import android.content.Context;
import lombok.NonNull;
import potapeyko.rss.exceptions.DbException;

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
    public void addToNews(final long chanelId, final String title, final String link, final String description) throws DbException {
        if (dB == null) return;
        final ContentValues cv = new ContentValues();
        cv.put(DbConvention.NEWS_TABLE_CHANEL_ID, chanelId);
        cv.put(DbConvention.NEWS_TABLE_TITLE, title);
        cv.put(DbConvention.NEWS_TABLE_LINK, link);
        cv.put(DbConvention.NEWS_TABLE_DESCRIPTION, description);
        long result = -1;

        dB.beginTransaction();
        try {
            result = dB.insert(DbConvention.DB_NEWS_TABLE, null, cv);
            dB.setTransactionSuccessful();
        } finally {
            dB.endTransaction();
        }
        if (result == -1) throw new DbException();
    }

    public long addChanel(final String title, final String link, final String description) {
        if (dB == null) return -1;
        final ContentValues cv = new ContentValues();
        cv.put(DbConvention.CHANEL_TABLE_TITLE, title);
        cv.put(DbConvention.CHANEL_TABLE_LINK, link);
        cv.put(DbConvention.CHANEL_TABLE_DESCRIPTION, description);
        long resultId;

        dB.beginTransaction();
        try {
            resultId = dB.insert(DbConvention.DB_CHANEL_TABLE, null, cv);
            dB.setTransactionSuccessful();
            return resultId;
        } finally {
            dB.endTransaction();
        }
    }

    public void deleteChanelById(final long id) {
        if (dB == null) return;
        final String newsSelection = DbConvention.NEWS_TABLE_CHANEL_ID + " = " + id;
        final String chanelSelection = DbConvention.CHANEL_ID + " = " + id;

        dB.beginTransaction();
        try {
            dB.delete(DbConvention.DB_NEWS_TABLE, newsSelection, null);
            dB.delete(DbConvention.DB_CHANEL_TABLE, chanelSelection, null);
            dB.setTransactionSuccessful();
        } finally {
            dB.endTransaction();
        }
    }
}
