package potapeyko.rss.sql;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public final class DB {


    private class DbConvention {

        private static final String DB_NAME = "rssReaderDb";
        private static final int DB_VERSION = 6;

        private static final String DB_CHANEL_TABLE = "chanel";
        private static final String CHANEL_TABLE_TITLE = "title";
        private static final String CHANEL_TABLE_LINK = "link";
        private static final String CHANEL_TABLE_DESCRIPTION = "description";

        private static final String DB_NEWS_TABLE = "news";
        private static final String NEWS_TABLE_DESCRIPTION = "description";
        private static final String NEWS_TABLE_TITLE = "title";
        private static final String NEWS_TABLE_LINK = "link";
        private static final String NEWS_TABLE_CHANEL_ID = "chanel_id";


        private static final String DB_CREATE_CHANEL =
                "create table" + DB_CHANEL_TABLE +
                        "(_id integer primary key autoincrement," +
                        CHANEL_TABLE_TITLE + " text NOT NULL, " +
                        CHANEL_TABLE_LINK + "text NOT NULL," +
                        CHANEL_TABLE_DESCRIPTION + " text);";

        private static final String DB_CREATE_NEWS =
                "create table" + DB_NEWS_TABLE +
                        "(_id integer primary key autoincrement," +
                        NEWS_TABLE_TITLE + "text," +
                        NEWS_TABLE_LINK + "text," +
                        NEWS_TABLE_DESCRIPTION + "text," +
                        NEWS_TABLE_CHANEL_ID + "INTEGER," +
                        "foreign key (" + NEWS_TABLE_CHANEL_ID + ") references" +
                        DB_CHANEL_TABLE + "(_id) on delete cascade);";
    }


    private final Context context;


    private DBHelper dBHelper;
    private SQLiteDatabase dB;

    public DB(Context ctx) {
        context = ctx;
    }


    public void open() {
        dBHelper = new DBHelper(context, DbConvention.DB_NAME, null, DbConvention.DB_VERSION);
        dB = dBHelper.getWritableDatabase();
    }


    public void close() {
        if (dBHelper != null) dBHelper.close();
    }


    public Cursor getAllChanels() {
        return dB.query(DbConvention.DB_CHANEL_TABLE, null, null, null, null, null, null);
    }


    public Cursor getAllNewsOfChanel(long chanelId) {
        //// TODO: 02.11.2016 картинки
        String[] columns = new String[]{"_id", "title"};
        String selection = "chanel_id = " + chanelId;
        return dB.query(DbConvention.DB_NEWS_TABLE, columns, selection, null, null, null, null);
    }


    public void addToNews(Long chanelId, String title, String link, String description) {
        ContentValues cv = new ContentValues();
        cv.put("chanel_id", title);
        cv.put("title", title);
        cv.put("link", link);
        cv.put("description", description);
        long result = dB.insert(DbConvention.DB_NEWS_TABLE, null, cv);
        if (result == -1) Log.e("myLog", "Ошибка записи в бд");
    }


    public void deleteChanel(long id) {
        dB.delete(DbConvention.DB_CHANEL_TABLE, "_id = " + id, null);
    }


    private final class DBHelper extends SQLiteOpenHelper {


        DBHelper(Context context, String name, CursorFactory factory,
                 int version) {
            super(context, name, factory, version);

        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DbConvention.DB_CREATE_CHANEL);
            //у rss нет обязательный полей в новости, поэтому ограничения not null нет
            db.execSQL(DbConvention.DB_CREATE_NEWS);
            test(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            test(db);
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