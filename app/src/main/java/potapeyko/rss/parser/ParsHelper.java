//package potapeyko.rss.parser;
//
//
//import android.util.Log;
//import lombok.NonNull;
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//import org.xmlpull.v1.XmlPullParserFactory;
//import potapeyko.rss.exceptions.ConnectionException;
//import potapeyko.rss.exceptions.DbException;
//import potapeyko.rss.model.Feed;
//import potapeyko.rss.model.FeedItem;
//import potapeyko.rss.sql.DB;
//import potapeyko.rss.sql.DbWriter;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//
//public final class ParsHelper {
//
//    private final static String RSS_TITLE = "title";
//    private final static String RSS_CHANNEL = "channel";
//    private final static String RSS_DESCRIPTION = "description";
//    private final static String RSS_LINK = "link";
//
//    private final static String RSS_ITEM = "item";
//    private final static String RSS_ITEM_TITLE = "title";
//    private final static String RSS_ITEM_DESCRIPTION = "description";
//    private final static String RSS_ITEM_LINK = "link";
//    private final static long DEFAULT_ID = -100;
//
//    public final static long OK_RESULT_WITHOUT_ID = -23;
//
//
//    private final static String EXCEPTION_CHANNEL = "The channel is not found";
//
//
//    @NonNull
//    private final XmlPullParser xpp;
//    @NonNull
//    private final DB db;
//
//    public ParsHelper(@NonNull XmlPullParser xpp, @NonNull DB db) {
//        this.xpp = xpp;
//        this.db = db;
//    }
//
//
//
//    /**
//     * @return id of new channel in db or OK_RESULT_WITHOUT_ID if this channel already was IN db
//     * @throws ConnectionException - if can't pars channel info
//     * @throws DbException         - if  can't keep a channel in the db
//     */
//    public long addChannel() throws ConnectionException, DbException {
//
//        Feed channel = getChannel();
//        if (channel == null) throw new ConnectionException(EXCEPTION_CHANNEL);
//        DbWriter dbWriter = null;
//        try {
//            dbWriter = db.getWriter();
//            dbWriter.open();
//            boolean isInDb = dbWriter.isFeedInDb(channel.getLink());
//            if (!isInDb) {
//                long result = dbWriter.addFeedToDB(channel.getTitle(), channel.getLink(), channel.getDescription());
//                if (result == -1) {
//                    throw new DbException();
//                } else {
//                    return result;
//                }
//            } else {
//                return OK_RESULT_WITHOUT_ID;
//            }
//
//        } catch (Throwable th) {
//            throw new DbException(th);
//        } finally {
//            if (dbWriter != null) {
//                dbWriter.close();
//            }
//        }
//    }
//
//    private Feed getChannel() throws ConnectionException {
//        try {
//            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
//                if (xpp.getEventType() == XmlPullParser.START_TAG) {
//                    if (("rss".equals(xpp.getName()))) {
//                        return parsChannel();
//                    }
//                }
//                xpp.next();
//            }
//        } catch (XmlPullParserException | IOException e) {
//            throw new ConnectionException(EXCEPTION_CHANNEL, e);
//        }
//        throw new ConnectionException(EXCEPTION_CHANNEL);
//    }
//
//
//
//
//    private Feed parsChannel() throws XmlPullParserException, IOException {
//        String title = null;
//        String link = null;
//        String description = null;
//
//        int nestingLevel =0;
//        int currentNestingLevel=0;
//
//        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
//            switch (xpp.getEventType()) {
//                case XmlPullParser.START_TAG:
//                    currentNestingLevel++;
//                    if(RSS_CHANNEL.equals(xpp.getName())){
//                        nestingLevel=currentNestingLevel+1;
//                        break;
//                    }
//                    if (RSS_TITLE.equals(xpp.getName())&&currentNestingLevel==nestingLevel) {
//                        xpp.next();
//                        title = xpp.getText();
//                        break;
//                    }
//                    if (RSS_DESCRIPTION.equals(xpp.getName())&&currentNestingLevel==nestingLevel) {
//                        xpp.next();
//                        description = xpp.getText();
//                        break;
//                    }
//                    if (RSS_LINK.equals(xpp.getName())&&currentNestingLevel==nestingLevel) {
//                        xpp.next();
//                        link = xpp.getText();
//                        break;
//                    }
//                    if (RSS_ITEM.equals(xpp.getName())) {
//                        return new Feed(DEFAULT_ID, title, link, description);
//                    }
//                    break;
//                case XmlPullParser.END_TAG:
//                    currentNestingLevel--;
//                    break;
//                default:
//                    break;
//            }
//            xpp.next();
//        }
//        return null;
//    }
//
//
//    /**
//     * It does not check the channel format
//     */
//    public void addNews(long channelId) throws ConnectionException, DbException {
//        ArrayList<FeedItem> aNews;
//        try {
//            aNews = parsNews();
//        } catch (XmlPullParserException | IOException e) {
//            throw new ConnectionException(e);
//        }
//        newsToDB(channelId, aNews);
//    }
//
//
//    /**
//     * @return true - if some news added to db.
//     * false - if nothing added to db.
//     * <p>
//     * Check channel format and add new news in db
//     */
//    public boolean checkNews(long channelId) throws ConnectionException, DbException {
//        boolean areNewNews;
//        ArrayList<FeedItem> aNews = null;
//        try {
//            while (xpp.getEventType() != XmlPullParser.START_TAG || xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
//                if (xpp.getEventType() == XmlPullParser.START_TAG) {
//                    if (("rss".equals(xpp.getName()))) {
//                        aNews = parsNews();
//                        break;
//                    }
//                }
//                xpp.next();
//            }
//            areNewNews = newsToDB(channelId, aNews);
//        } catch (XmlPullParserException | IOException e) {
//            throw new ConnectionException(e);
//        }
//        return areNewNews;
//    }
//
//    private ArrayList<FeedItem> parsNews() throws XmlPullParserException, IOException {
//
//        ArrayList<FeedItem> aNews = new ArrayList<>();
//        FeedItem currentFeedItem = null;
//        boolean inItem = false;
//        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
//            switch (xpp.getEventType()) {
//                case XmlPullParser.START_TAG:
//                    if (RSS_ITEM.equals(xpp.getName())) {
//                        inItem = true;
//                        currentFeedItem = new FeedItem();
//                        break;
//                    } else if (inItem && RSS_ITEM_TITLE.equals(xpp.getName())) {
//                        xpp.next();
//                        String title = xpp.getText();
//                        currentFeedItem.setTitle(title);
//                    } else if (inItem && RSS_ITEM_DESCRIPTION.equals(xpp.getName())) {
//                        xpp.next();
//                        String description = xpp.getText();
//                        currentFeedItem.setDescription(description);
//                    } else if (inItem && RSS_ITEM_LINK.equals(xpp.getName())) {
//                        xpp.next();
//                        String link = xpp.getText();
//                        currentFeedItem.setLink(link);
//                    }
//                    break;
//
//                case XmlPullParser.END_TAG:
//                    if (RSS_ITEM.equals(xpp.getName())) {
//                        inItem = false;
//                        if (currentFeedItem != null) {
//                            aNews.add(currentFeedItem);
//                        }
//                        break;
//                    }
//
//                default:
//                    break;
//            }
//            xpp.next();
//        }
//        return aNews;
//    }
//
//    /**
//     * @return true - if some aNews added to db
//     * false - if nothing add to db
//     */
//    private boolean newsToDB(long channelId, ArrayList<FeedItem> aNews) throws DbException {
//        boolean result = false;
//
//        if (aNews != null) {
//            DbWriter dbWriter = db.getWriter();
//            try {
//                dbWriter.open();
//            } catch (Throwable th) {
//                dbWriter.close();
//                throw new DbException(th);
//            }
//            int debugId=0;
//            try {
//                for (FeedItem currentFeedItem : aNews) {
//                    if (!dbWriter.isFeedItemInDb(currentFeedItem)) {
//
//                        dbWriter.addFeedItemToDB(channelId, currentFeedItem.getTitle(), currentFeedItem.getLink(),
//                                currentFeedItem.getDescription());
//
//                        result = true;
//                        Log.d("1111",String.valueOf(debugId++));
//                    }
//                }
//            } finally {
//                dbWriter.close();
//            }
//        }
//        return result;
//    }
//
//
//}