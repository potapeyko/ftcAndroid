package potapeyko.rss.sql;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import potapeyko.rss.models.Chanel;
import potapeyko.rss.models.News;

import java.util.ArrayList;

import static potapeyko.rss.constants.LogCodes.DB_LOG;


public final class DB {


    private final class DbConvention {
        private DbConvention() throws Exception {
            throw new Exception();
        }

        private static final String DB_NAME = "rssReaderDb";
        private static final int DB_VERSION = 6;

        private static final String DB_CHANEL_TABLE = "chanel";
        private static final String CHANEL_TABLE_TITLE = "title";
        private static final String CHANEL_TABLE_LINK = "link";
        private static final String CHANEL_TABLE_DESCRIPTION = "description";
        private static final String CHANEL_ID = "_id";


        private static final String DB_NEWS_TABLE = "news";
        private static final String NEWS_TABLE_DESCRIPTION = "description";
        private static final String NEWS_TABLE_TITLE = "title";
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

    public Chanel getChanelById(long id) {
        String selection = "_id = " + id;
        Cursor cur = dB.query(DbConvention.DB_CHANEL_TABLE, null, selection, null, null, null, null);
        Chanel resChanel = null;
        try {
            if (cur.moveToFirst()) {
                resChanel = new Chanel(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3));
            }
            return resChanel;
        } finally {
            cur.close();
        }
    }
    public News getNewsById(long id) {
        String [] columns = {DbConvention.NEWS_ID,DbConvention.NEWS_TABLE_TITLE,
                DbConvention.NEWS_TABLE_DESCRIPTION,DbConvention.NEWS_TABLE_LINK};
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

    public Cursor getAllChanelsCursor() {
        return dB.query(DbConvention.DB_CHANEL_TABLE, null, null, null, null, null, null);
    }

    public ArrayList<Chanel> getAllChanelsList() {
        ArrayList<Chanel> list = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = dB.query(DbConvention.DB_CHANEL_TABLE, null, null, null, null, null, null);
            if (cur.moveToFirst()) {
                do {
                    list.add(new Chanel(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3)));
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
        //// TODO: 02.11.2016 картинки
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

    public long addChanel(String title, String link) {
        return addChanel(title, link, null);
    }

    private long addChanel(String title, String link, String description) {
        ContentValues cv = new ContentValues();
        cv.put(DbConvention.CHANEL_TABLE_TITLE, title);
        cv.put(DbConvention.CHANEL_TABLE_LINK, link);
        cv.put(DbConvention.CHANEL_TABLE_DESCRIPTION, description);
        return dB.insert(DbConvention.DB_CHANEL_TABLE, null, cv);
    }

    public void addDescriptionToChanel(Long chanelId, String description) {
        if (description == null || "".equals(description)) return;
        String sql = "update " + DbConvention.DB_CHANEL_TABLE +
                " set " + DbConvention.CHANEL_TABLE_DESCRIPTION + " = '" +
                description + "' where _id = " + chanelId + ";";

        dB.execSQL(sql);
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

    public boolean isNewsInDb(final String link) {
        if (link == null) return false;

        String[] columns = {DbConvention.NEWS_TABLE_TITLE};
        String selection = DbConvention.NEWS_TABLE_LINK + " = '" + link + "'";
        Cursor cursor = dB.query(DbConvention.DB_CHANEL_TABLE, columns, selection, null, null, null, null);
        boolean res = cursor.getCount() > 0;
        cursor.close();
        return res;
    }

    public void addToNews(Long chanelId, String title, String link, String description) {
        ContentValues cv = new ContentValues();
        cv.put(DbConvention.NEWS_TABLE_CHANEL_ID, chanelId);
        cv.put(DbConvention.NEWS_TABLE_TITLE, title);
        cv.put(DbConvention.NEWS_TABLE_LINK, link);
        cv.put(DbConvention.NEWS_TABLE_DESCRIPTION, description);
        long result = dB.insert(DbConvention.DB_NEWS_TABLE, null, cv);
        if (result == -1) Log.e(DB_LOG, "Ошибка addToNews записи в бд");
    }

    public void deleteChanel(long id) {
        dB.delete(DbConvention.DB_CHANEL_TABLE, "_id = " + id, null);
    }

    public void clearAll() {
        if (dB == null) return;
        dB.delete(DbConvention.DB_CHANEL_TABLE, null, null);
        dB.delete(DbConvention.DB_NEWS_TABLE, null, null);//почему то каскадное не работает?
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

        private void test(SQLiteDatabase db) {
            String[] chanel_title = {"Канал 1", "Канал 2", "Канал 3", "Канал 4", "Канал 5"};
            String[] chanel_link = {"https://habrahabr.ru/rss/hub/programming/", "https://habrahabr.ru/rss/hub/programming/",
                    "Программер", "Программер", "Бухгалтер"};
            String[] chanel_description = {"Социальное СМИ. Новости о блогах, поиске работы. Обзоры и рейтинги компаний.",
                    "Программер", "Программер", "Бухгалтер", ""};

            String[] news_title = {"Новость 1", "Новость 1 2", "Новость 1 3", "Новость 1 4", "Новость 1 5"};
            long[] news_channel_id = {1, 1, 1, 2, 3};
            String[] news_description = {"Социальное СМИ. Новости о блогах, поиске работы. Обзоры и рейтинги компаний.",
                    "Программер", "Программер", "", "Бухгалтер"};

            ContentValues cv = new ContentValues();


            for (int i = 0; i < chanel_title.length; i++) {
                cv.clear();
                cv.put("title", chanel_title[i]);
                cv.put("link", chanel_link[i]);
                cv.put("description", chanel_description[i]);
                db.insert("chanel", null, cv);
            }

            for (int i = 0; i < news_title.length; i++) {
                cv.clear();
                cv.put("title", news_title[i]);
                cv.put("link", chanel_link[i]);
                cv.put("description", news_description[i]);
                cv.put("chanel_id", news_channel_id[i]);
                db.insert("news", null, cv);
            }
        }
    }
}