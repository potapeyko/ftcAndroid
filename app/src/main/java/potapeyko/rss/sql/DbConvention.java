package potapeyko.rss.sql;

public  final class DbConvention {
    private DbConvention() throws Exception {
        throw new Exception();
    }

    static final String DB_NAME = "rssReaderDb";
    static final int DB_VERSION = 1;

    static final String DB_CHANEL_TABLE = "chanel";
    public static final String CHANEL_TABLE_TITLE = "title";
    static final String CHANEL_TABLE_LINK = "link";
    public static final String CHANEL_TABLE_DESCRIPTION = "description";
    static final String CHANEL_ID = "_id";


    static final String DB_NEWS_TABLE = "news";
    static final String NEWS_TABLE_DESCRIPTION = "description";
    public static final String NEWS_TABLE_TITLE = "title";
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