package potapeyko.rss.parser;


import lombok.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import potapeyko.rss.exceptions.ConnectionException;
import potapeyko.rss.exceptions.DbException;
import potapeyko.rss.model.Channel;
import potapeyko.rss.model.News;
import potapeyko.rss.sql.DB;
import potapeyko.rss.sql.DbWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public final class ParsHelper {

    private final static String RSS_TITLE = "title";
    private final static String RSS_DESCRIPTION = "description";
    private final static String RSS_LINK = "link";

    private final static String RSS_ITEM = "item";
    private final static String RSS_ITEM_TITLE = "title";
    private final static String RSS_ITEM_DESCRIPTION = "description";
    private final static String RSS_ITEM_LINK = "link";
    private final static long DEFAULT_ID = -100;

    public final static long OK_RESULT_WITHOUT_ID = -23;


    private final static String EXCEPTION_CHANNEL = "The channel is not found";


    @NonNull
    private final XmlPullParser xpp;
    @NonNull
    private final DB db;

    public ParsHelper(@NonNull XmlPullParser xpp, @NonNull DB db) {
        this.xpp = xpp;
        this.db = db;
    }

    /**
     * @return id of new channel in db or OK_RESULT_WITHOUT_ID if this channel already was IN db
     * @throws ConnectionException - if can't pars channel info
     * @throws DbException         - if  can't keep a channel in the db
     */
    public long addChannel() throws ConnectionException, DbException {

        Channel channel = getChannel();
        if (channel == null) throw new ConnectionException(EXCEPTION_CHANNEL);
        DbWriter dbWriter = null;
        try {
            dbWriter = db.getWriter();
            dbWriter.open();
            boolean isInDb = dbWriter.isChanelInDb(channel.getLink());
            if (!isInDb) {
                long result = dbWriter.addChanel(channel.getTitle(), channel.getLink(), channel.getDescription());
                if (result == -1) {
                    throw new DbException();
                } else {
                    return result;
                }
            } else {
                return OK_RESULT_WITHOUT_ID;
            }

        } catch (Throwable th) {
            throw new DbException(th);
        } finally {
            if (dbWriter != null) {
                dbWriter.close();
            }
        }
    }

    private Channel getChannel() throws ConnectionException {
        try {

            while (xpp.getEventType() != XmlPullParser.START_TAG || xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (("rss".equals(xpp.getName()))) {
                        return parsChannel();
                    }
                }
                xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            throw new ConnectionException(EXCEPTION_CHANNEL, e);
        }
        throw new ConnectionException(EXCEPTION_CHANNEL);
    }


    private Channel parsChannel() throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (xpp.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (RSS_TITLE.equals(xpp.getName())) {
                        xpp.next();
                        title = xpp.getText();
                        if (title == null) return null;
                    }
                    if (RSS_DESCRIPTION.equals(xpp.getName())) {
                        xpp.next();
                        description = xpp.getText();
                    }
                    if (RSS_LINK.equals(xpp.getName())) {
                        xpp.next();
                        link = xpp.getText();
                    }
                    if (RSS_ITEM.equals(xpp.getName())) {
                        if (link != null && title != null && description != null) {
                            return new Channel(DEFAULT_ID, title, link, description);
                        } else {
                            return null;
                        }
                    }
                    break;
                default:
                    break;
            }
            xpp.next();
        }
        return null;
    }


    /**
     * It does not check the channel format
     */
    public void addNews(long channelId) throws ConnectionException, DbException {
        ArrayList<News> news;
        try {
            news = parsNews();
        } catch (XmlPullParserException | IOException e) {
            throw new ConnectionException(e);
        }
        newsToDB(channelId, news);
    }


    /**
     * @return true - if some news added to db.
     * false - if nothing added to db.
     * <p>
     * Check channel format and add new news in db
     */
    public boolean checkNews(long channelId) throws ConnectionException, DbException {
        boolean areNewNews;
        ArrayList<News> news = null;
        try {
            while (xpp.getEventType() != XmlPullParser.START_TAG || xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (("rss".equals(xpp.getName()))) {
                        news = parsNews();
                        break;
                    }
                }
                xpp.next();
            }
            areNewNews = newsToDB(channelId, news);
        } catch (XmlPullParserException | IOException e) {
            throw new ConnectionException(e);
        }
        return areNewNews;
    }

    private ArrayList<News> parsNews() throws XmlPullParserException, IOException {

        ArrayList<News> news = new ArrayList<>();
        News currentNews = null;
        boolean inItem = false;
        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (xpp.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (RSS_ITEM.equals(xpp.getName())) {
                        inItem = true;
                        currentNews = new News();
                        break;
                    } else if (inItem && RSS_ITEM_TITLE.equals(xpp.getName())) {
                        xpp.next();
                        String title = xpp.getText();
                        currentNews.setTitle(title);
                    } else if (inItem && RSS_ITEM_DESCRIPTION.equals(xpp.getName())) {
                        xpp.next();
                        String description = xpp.getText();
                        currentNews.setDescription(description);
                    } else if (inItem && RSS_ITEM_LINK.equals(xpp.getName())) {
                        xpp.next();
                        String link = xpp.getText();
                        currentNews.setFullNewsUri(link);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (RSS_ITEM.equals(xpp.getName())) {
                        inItem = false;
                        if (currentNews != null) {
                            news.add(currentNews);
                        }
                        break;
                    }

                default:
                    break;
            }
            xpp.next();
        }
        return news;
    }

    /**
     * @return true - if some news added to db
     * false - if nothing add to db
     */
    private boolean newsToDB(long channelId, ArrayList<News> news) throws DbException {
        boolean result = false;

        if (news != null) {
            DbWriter dbWriter = db.getWriter();
            try {
                dbWriter.open();
            } catch (Throwable th) {
                dbWriter.close();
                throw new DbException(th);
            }

            try {
                for (News currentNews : news) {
                    if (!dbWriter.isNewsInDb(currentNews)) {
                        dbWriter.addToNews(channelId, currentNews.getTitle(), currentNews.getFullNewsUri(),
                                currentNews.getDescription());
                        result = true;
                    }
                }
            } finally {
                dbWriter.close();
            }
        }
        return result;
    }

    public static XmlPullParser prepareXpp(@NonNull InputStream is) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(is, null);
        return xpp;
    }
}