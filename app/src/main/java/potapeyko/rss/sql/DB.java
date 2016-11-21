package potapeyko.rss.sql;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import potapeyko.rss.Exeptions.DbException;
import potapeyko.rss.models.Channel;
import potapeyko.rss.models.News;

import java.util.ArrayList;

import static potapeyko.rss.constants.LogCodes.DB_LOG;


public final class DB {


    public final class DbConvention {
        private DbConvention() throws Exception {
            throw new Exception();
        }

        private static final String DB_NAME = "rssReaderDb";
        private static final int DB_VERSION = 6;

        private static final String DB_CHANEL_TABLE = "chanel";
        public static final String CHANEL_TABLE_TITLE = "title";
        private static final String CHANEL_TABLE_LINK = "link";
        public static final String CHANEL_TABLE_DESCRIPTION = "description";
        private static final String CHANEL_ID = "_id";


        private static final String DB_NEWS_TABLE = "news";
        private static final String NEWS_TABLE_DESCRIPTION = "description";
        public static final String NEWS_TABLE_TITLE = "title";
        private static final String NEWS_TABLE_LINK = "link";
        private static final String NEWS_TABLE_CHANEL_ID = "chanel_id";
        private static final String NEWS_ID = "_id";

        private static final String DB_CREATE_CHANEL_TABLE =
                "create table " + DB_CHANEL_TABLE +
                        "(_id integer primary key autoincrement, " +
                        CHANEL_TABLE_TITLE + " text NOT NULL, " +
                        CHANEL_TABLE_LINK + " text NOT NULL, " +
                        CHANEL_TABLE_DESCRIPTION + " text);";

        private static final String DB_CREATE_NEWS_TABLE =
                "create table " + DB_NEWS_TABLE +
                        "(_id integer primary key autoincrement, " +
                        NEWS_TABLE_TITLE + " text, " +
                        NEWS_TABLE_LINK + " text, " +
                        NEWS_TABLE_DESCRIPTION + " text, " +
                        NEWS_TABLE_CHANEL_ID + " INTEGER, " +
                        " foreign key ( " + NEWS_TABLE_CHANEL_ID + " ) references " +
                        DB_CHANEL_TABLE + " (_id) on delete cascade);";
    }


    private final Context context;
    private DBHelper dBHelper;
    private SQLiteDatabase dB;

    public DB(Context context) {
        this.context = context;
    }


    public void open() {
        dBHelper = new DBHelper(context, DbConvention.DB_NAME, null, DbConvention.DB_VERSION);
        try {
            dB = dBHelper.getWritableDatabase();
        } catch (Exception e) {
            dB = dBHelper.getReadableDatabase();
            Log.e("DB_LOG", "БД НЕ МОЖЕТ БЫТЬ ОТКРЫТА ДЛЯ ПИСЬМА");
        }

    }


    public void close() {
        if (dBHelper != null) dBHelper.close();
        dB = null;
    }

    public Channel getChanelById(long id) {
        String selection = "_id = " + id;
        Cursor cur = dB.query(DbConvention.DB_CHANEL_TABLE, null, selection, null, null, null, null);
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

    public News getNewsById(long id) {
        String[] columns = {DbConvention.NEWS_ID, DbConvention.NEWS_TABLE_TITLE,
                DbConvention.NEWS_TABLE_DESCRIPTION, DbConvention.NEWS_TABLE_LINK};
        String selection = "_id = " + id;
        Cursor cur = dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, null);
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

    public Cursor getAllChannelsCursor() {
        return dB.query(DbConvention.DB_CHANEL_TABLE, null, null, null, null, null, null);
    }

    public ArrayList<Channel> getAllChannelsList() {
        ArrayList<Channel> list = new ArrayList<>();
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

    public Cursor getAllNewsOfChanelCursor(long chanelId) {
        String[] columns = new String[]{
                DbConvention.NEWS_ID,
                DbConvention.NEWS_TABLE_TITLE,
                DbConvention.NEWS_TABLE_DESCRIPTION,
                DbConvention.NEWS_TABLE_LINK
        };
        String selection = DbConvention.NEWS_TABLE_CHANEL_ID + " = " + chanelId;
        return dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, null);//когда закрывать??
    }

    public ArrayList<News> getAllNewsOfChanelList(long chanelId) {
        ArrayList<News> list = new ArrayList<>();
        String[] columns = new String[]{
                DbConvention.NEWS_ID,
                DbConvention.NEWS_TABLE_TITLE,
                DbConvention.NEWS_TABLE_DESCRIPTION,
                DbConvention.NEWS_TABLE_LINK
        };
        String selection = DbConvention.NEWS_TABLE_CHANEL_ID + " = " + chanelId;
        Cursor cur = null;
        try {
            cur = dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, null);
            if (cur.moveToFirst()) {
                do {
                    list.add(new News(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3)));
                }
                while (cur.moveToNext());
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return list;
    }


    public long addChanel(String title, String link, String description) {
        ContentValues cv = new ContentValues();
        cv.put(DbConvention.CHANEL_TABLE_TITLE, title);
        cv.put(DbConvention.CHANEL_TABLE_LINK, link);
        cv.put(DbConvention.CHANEL_TABLE_DESCRIPTION, description);
        return dB.insert(DbConvention.DB_CHANEL_TABLE, null, cv);
    }

    public boolean isChanelInDb(final String link) {
        if (link == null) return false;

        String[] columns = {DbConvention.CHANEL_TABLE_TITLE};
        String selection = DbConvention.CHANEL_TABLE_LINK + " = '" + link + "'";

        Cursor cursor = dB.query(DbConvention.DB_CHANEL_TABLE, columns, selection, null, null, null, null);
        boolean res = cursor.getCount() > 0;
        cursor.close();
        return res;
    }

    public boolean isNewsInDb(final News news) throws DbException {
        if (news == null) return false;
        String[] columns = {DbConvention.NEWS_TABLE_TITLE};

        String whereLink = news.getFullNewsUri() == null ? DbConvention.NEWS_TABLE_LINK + "IS NULL" :
                DbConvention.NEWS_TABLE_LINK + " = '" + news.getFullNewsUri() + "'";
        String whereDescription = news.getDescription() == null ? DbConvention.NEWS_TABLE_DESCRIPTION + "IS NULL" :
                DbConvention.NEWS_TABLE_DESCRIPTION + " = '" + news.getDescription() + "'";
        String whereTitle = news.getTitle() == null ? DbConvention.NEWS_TABLE_TITLE + "IS NULL" :
                DbConvention.NEWS_TABLE_TITLE + " = '" + news.getTitle() + "'";

        String selection = whereLink + " AND " + whereTitle + " AND " + whereDescription;
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

    public void addToNews(Long chanelId, String title, String link, String description) throws DbException {
        ContentValues cv = new ContentValues();
        cv.put(DbConvention.NEWS_TABLE_CHANEL_ID, chanelId);
        cv.put(DbConvention.NEWS_TABLE_TITLE, title);
        cv.put(DbConvention.NEWS_TABLE_LINK, link);
        cv.put(DbConvention.NEWS_TABLE_DESCRIPTION, description);
        long result = dB.insert(DbConvention.DB_NEWS_TABLE, null, cv);
        if (result == -1) throw  new DbException();
    }

    public void clearAll() {
        if (dB == null) return;
        dB.delete(DbConvention.DB_CHANEL_TABLE, null, null);
        dB.delete(DbConvention.DB_NEWS_TABLE, null, null);//почему то каскадное не работает?
    }

    public void deleteChanelById(long id) {
        if (dB == null) return;
        String newsSelection = DbConvention.NEWS_TABLE_CHANEL_ID + " = " + id;
        String chanelSelection = DbConvention.CHANEL_ID + " = " + id;

        int a = dB.delete(DbConvention.DB_NEWS_TABLE, newsSelection, null);
        int b = dB.delete(DbConvention.DB_CHANEL_TABLE, chanelSelection, null);

    }

    private final class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DbConvention.DB_CREATE_CHANEL_TABLE);
            //у rss нет обязательный полей в новости, поэтому ограничения not null нет
            db.execSQL(DbConvention.DB_CREATE_NEWS_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}