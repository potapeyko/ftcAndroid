package potapeyko.rss.parser;


import android.support.annotation.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import potapeyko.rss.Exeptions.ConnectionException;
import potapeyko.rss.Exeptions.DbException;
import potapeyko.rss.models.Channel;
import potapeyko.rss.models.News;
import potapeyko.rss.sql.DB;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public final class ParsHelper {

    private final static String RSS_TITLE = "title";
    private final static String RSS_DESCR = "description";
    private final static String RSS_CHANEL = "chanel";

    private final static String RSS_ITEM = "item";
    private final static String RSS_ITEM_TITLE = "title";
    private final static String RSS_ITEM_DESCR = "description";
    private final static String RSS_ITEM_LINK = "link";
    private final static long DEFAULT_ID = -1;

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
     * @param url - url of channel in Internet
     * @return id of channel in db or -1 if this channel already was IN db
     * @throws ConnectionException - if can't pars channel info
     * @throws DbException         - if  can't keep a channel in the db
     */
    public long addChannel(URL url) throws ConnectionException, DbException {
        Channel channel = null;
        try {
            while (xpp.getEventType() != XmlPullParser.START_TAG || xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (("rss".equals(xpp.getName()))) {
                        channel = parsChannel(url.toString());
                        break;
                    }
                }
                xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            throw new ConnectionException(EXCEPTION_CHANNEL, e);
        }

        if (channel != null) {
            try {
                db.open();
                boolean isInDb = db.isChanelInDb(channel.getLink());
                if (!isInDb) {
                    return db.addChanel(channel.getTitle(), channel.getLink(), channel.getDescription());
                }

            } catch (Throwable th) {
                throw new DbException(th);
            } finally {
                db.close();
            }
        }

        return -1;
    }

    private Channel parsChannel(String url) throws XmlPullParserException, IOException {
        Channel channel = null;
        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (xpp.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (RSS_TITLE.equals(xpp.getName())) {
                        xpp.next();
                        String title = xpp.getText();
                        if (title == null) return null;
                        channel = new Channel(DEFAULT_ID, title, url, null);//id присваивается в бд
                        break;
                    }
                    if (RSS_DESCR.equals(xpp.getName()) && channel != null) {
                        xpp.next();
                        String description = xpp.getText();
                        channel.setDescription(description);
                        return channel;
                    }
                    if (RSS_ITEM.equals(xpp.getName())) {
                        return channel;
                    }
                    break;
                default:
                    break;
            }
            xpp.next();
        }
        return channel;
    }

    public void addNews(Long channelId) throws ConnectionException, DbException {
        ArrayList<News> news = null;
        try {
            news = parsNews();
        } catch (XmlPullParserException | IOException e) {
            throw new ConnectionException(e);
        }
        newsToDB(channelId, news);
    }


    /**
     * @return  true - if some news added to db
                false - if nothing add to db
     */
    public boolean checkNews(long channelId) throws ConnectionException, DbException {
        boolean areNewNews = false;
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
                    } else if (inItem && RSS_ITEM_DESCR.equals(xpp.getName())) {
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
     * @return  true - if some news added to db
                false - if nothing add to db
     */
    private boolean newsToDB(long channelId, ArrayList<News> news) throws DbException {
        boolean result=false;
        if (news != null) {
            for (News currentNews : news) {
                if (!db.isNewsInDb(currentNews)) {
                    db.addToNews(channelId, currentNews.getTitle(), currentNews.getFullNewsUri(),
                            currentNews.getDescription());
                    result=true;
                }
            }
        }
        return result;
    }

    public static XmlPullParser prepareXpp(InputStream is, String encoding) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(is, encoding);
        return xpp;
    }
}