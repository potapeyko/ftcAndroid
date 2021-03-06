package potapeyko.rss.sql;

public final class DbConvention {
    private DbConvention() throws Exception {
        throw new Exception();
    }

    static final String DB_NAME = "rssReaderDb";
    static final int DB_VERSION = 1;

    //FEED
    static final String FEED_TABLE_NAME = "feed";
    static final String FEED_ID = "_id";
    public static final String FEED_TITLE = "title";
    static final String FEED_SITE_LINK = "siteLink";
    static final String FEED_LINK = "link";
    public static final String FEED_DESCRIPTION = "description";
    static final String FEED_BUILD_DATE = "lastBuildDate";
    static final String FEED_PUBLICATION_DATE = "pubDate";
    public static final String FEED_COUNT = "count";

    static final String CREATE_FEED_TABLE_COMMAND =
            "create table " + FEED_TABLE_NAME +
                    "(_id integer primary key autoincrement, " +
                    FEED_TITLE + " text NOT NULL, " +
                    FEED_LINK + " text NOT NULL, " +
                    FEED_SITE_LINK + " text, " +
                    FEED_DESCRIPTION + " text, " +
                    FEED_BUILD_DATE + " integer, " +
                    FEED_PUBLICATION_DATE + " integer, " +
                    FEED_COUNT +" integer );";

    //FEED_ITEM
    static final String FEED_ITEM_TABLE_NAME = "feedItem";
    public static final String FEED_ITEM_ID = "_id";
    public static final String FEED_ITEM_TITLE = "title";
    static final String FEED_ITEM_DESCRIPTION = "description";
    static final String FEED_ITEM_LINK = "link";
    static final String FEED_ITEM_FEED_ID = "chanel_id";
    public static final String FEED_ITEM_PUBLICATION_DATE = "pubDate";
    static final String FEED_ITEM_MEDIA_URL = "mediaURL";
    static final String FEED_ITEM_MEDIA_SIZE = "mediaSize";
    public static final String FEED_ITEM_FLAGS_FAVORITE = "favorite";
    public static final String FEED_ITEM_FLAGS_CHECKED = "checked";
    public static final String FEED_ITEM_FLAGS_DELETE = "deleteFlag";

    static final String DB_CREATE_NEWS_TABLE =
            "create table " + FEED_ITEM_TABLE_NAME +
                    "(_id integer primary key autoincrement, " +
                    FEED_ITEM_TITLE + " text, " +
                    FEED_ITEM_LINK + " text, " +
                    FEED_ITEM_DESCRIPTION + " text, " +
                    FEED_ITEM_PUBLICATION_DATE + " INTEGER, " +
                    FEED_ITEM_MEDIA_URL + " text, " +
                    FEED_ITEM_MEDIA_SIZE + " INTEGER, " +
                    FEED_ITEM_FLAGS_FAVORITE + " INTEGER, "+
                    FEED_ITEM_FLAGS_CHECKED  + " INTEGER, "+
                    FEED_ITEM_FLAGS_DELETE  + " INTEGER, "+
                    FEED_ITEM_FEED_ID + " INTEGER, " +
                    " foreign key (" + FEED_ITEM_FEED_ID + ") references " +
                    FEED_TABLE_NAME + " (_id) on delete cascade);";

    static final String SORT_DESCENDING = " DESC ";//ПО УБЫВАНИЮ

}