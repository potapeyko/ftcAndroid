package potapeyko.rss.sql;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import lombok.NonNull;
import potapeyko.rss.exceptions.DbException;
import potapeyko.rss.model.Channel;
import potapeyko.rss.model.News;

import java.util.ArrayList;

public class DbReader {
    public static final class DbConvention {
        private DbConvention() throws Exception {
            throw new Exception();
        }

        static final String DB_NAME = "rssReaderDb";
        static final int DB_VERSION = 1;

        static final String DB_CHANEL_TABLE = "chanel";
        static final String CHANEL_TABLE_TITLE = "title";
        static final String CHANEL_TABLE_LINK = "link";
        static final String CHANEL_TABLE_DESCRIPTION = "description";
        static final String CHANEL_ID = "_id";


        static final String DB_NEWS_TABLE = "news";
        static final String NEWS_TABLE_DESCRIPTION = "description";
        static final String NEWS_TABLE_TITLE = "title";
        static final String NEWS_TABLE_LINK = "link";
        static final String NEWS_TABLE_CHANEL_ID = "chanel_id";
        static final String NEWS_ID = "_id";

        static final String DB_CREATE_CHANEL_TABLE =
                "create table " + DB_CHANEL_TABLE +
                        "(_id integer primary key autoincrement, " +
                        CHANEL_TABLE_TITLE + " text NOT NULL, " +
                        CHANEL_TABLE_LINK + " text NOT NULL, " +
                        CHANEL_TABLE_DESCRIPTION + " text);";


        //у rss нет обязательный полей в новости, поэтому ограничения not null нет
        static final String DB_CREATE_NEWS_TABLE =
                "create table " + DB_NEWS_TABLE +
                        "(_id integer primary key autoincrement, " +
                        NEWS_TABLE_TITLE + " text, " +
                        NEWS_TABLE_LINK + " text, " +
                        NEWS_TABLE_DESCRIPTION + " text, " +
                        NEWS_TABLE_CHANEL_ID + " INTEGER, " +
                        " foreign key ( " + NEWS_TABLE_CHANEL_ID + " ) references " +
                        DB_CHANEL_TABLE + " (_id) on delete cascade);";

        static final String SORT_DESCENDING = " DESC ";

    }

    final Context context;
    DBHelper dBHelper;

    SQLiteDatabase dB;

    public DbReader(final @NonNull Context context) {
        this.context = context;
    }

    public void open() {
        if (dBHelper == null) {
            dBHelper = new DBHelper(context, DbConvention.DB_NAME, null, DbConvention.DB_VERSION);
        }
        dB = dBHelper.getReadableDatabase();
    }

    public void close() {
        if (dBHelper != null) dBHelper.close();
        dB = null;
    }


    public Channel getChanelById(final long id) throws DbException {
        if (dB == null) throw new DbException();
        final String selection = "_id = " + id;
        final Cursor cur = dB.query(DbConvention.DB_CHANEL_TABLE, null, selection, null, null, null, null);
        Channel resChanel = null;
        try {
            if (cur.moveToFirst()) {
                resChanel = new Channel(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3));
            }
            return resChanel;
        } finally {
            cur.close();
        }
    }

    public News getNewsById(final long id) throws DbException {
        if (dB == null) throw new DbException();
        final String[] columns = {DbConvention.NEWS_ID, DB.DbConvention.NEWS_TABLE_TITLE,
                DbConvention.NEWS_TABLE_DESCRIPTION, DbConvention.NEWS_TABLE_LINK};
        final String selection = "_id = " + id;
        final Cursor cur = dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, null);
        News resNews = null;
        try {
            if (cur.moveToFirst()) {
                resNews = new News(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3));
            }
            return resNews;
        } finally {
            cur.close();
        }
    }

    public Cursor getAllChannelsCursor() throws DbException {
        if (dB == null) throw new DbException();
        return dB.query(DbConvention.DB_CHANEL_TABLE, null, null, null, null, null, null);
    }


    public ArrayList<Channel> getAllChannelsList() throws DbException {
        if (dB == null) throw new DbException();
        final ArrayList<Channel> list = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = dB.query(DbConvention.DB_CHANEL_TABLE, null, null, null, null, null, null);
            if (cur.moveToFirst()) {
                do {
                    list.add(new Channel(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3)));
                }
                while (cur.moveToNext());
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return list;
    }

    public Cursor getAllNewsOfChanelCursor(final long chanelId) throws DbException {
        if (dB == null) throw new DbException();
        final String[] columns = new String[]{
                DbConvention.NEWS_ID,
                DbConvention.NEWS_TABLE_TITLE,
                DbConvention.NEWS_TABLE_DESCRIPTION,
                DbConvention.NEWS_TABLE_LINK
        };
        final String selection = DbConvention.NEWS_TABLE_CHANEL_ID + " = " + chanelId;
        final String orderBy = DbConvention.NEWS_ID + DbConvention.SORT_DESCENDING;
        return dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, orderBy);
    }

    public boolean isChanelInDb(final String link) throws DbException {
        if (dB == null) throw new DbException();
        if (link == null) return false;

        final String[] columns = {DbConvention.CHANEL_TABLE_TITLE};
        final String selection = DbConvention.CHANEL_TABLE_LINK + " = '" + link + "'";

        final Cursor cursor = dB.query(DbConvention.DB_CHANEL_TABLE, columns, selection, null, null, null, null);
        final boolean res = cursor.getCount() > 0;
        cursor.close();
        return res;
    }

    public boolean isNewsInDb(final News news) throws DbException {
        if (news == null) return false;
        if (dB == null) throw new DbException();
        final String[] columns = {DB.DbConvention.NEWS_TABLE_TITLE};

        final String whereLink = news.getFullNewsUri() == null ? DbConvention.NEWS_TABLE_LINK + "IS NULL" :
                DbConvention.NEWS_TABLE_LINK + " = '" + news.getFullNewsUri() + "'";
        final String whereDescription = news.getDescription() == null ? DbConvention.NEWS_TABLE_DESCRIPTION + "IS NULL" :
                DbConvention.NEWS_TABLE_DESCRIPTION + " = '" + news.getDescription() + "'";
        final String whereTitle = news.getTitle() == null ? DB.DbConvention.NEWS_TABLE_TITLE + "IS NULL" :
                DB.DbConvention.NEWS_TABLE_TITLE + " = '" + news.getTitle() + "'";

        final String selection = whereLink + " AND " + whereTitle + " AND " + whereDescription;
        Cursor cursor = null;
        boolean res;
        try {
            cursor = dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, null);
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
        DBHelper(final Context context, final String name, final SQLiteDatabase.CursorFactory factory, final int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(DbConvention.DB_CREATE_CHANEL_TABLE);
            db.execSQL(DbConvention.DB_CREATE_NEWS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
