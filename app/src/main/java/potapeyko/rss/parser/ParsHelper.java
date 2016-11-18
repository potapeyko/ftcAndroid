package potapeyko.rss.parser;


import android.support.annotation.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
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


    @Nullable private final XmlPullParser xpp;
    @Nullable private final DB db;

    public ParsHelper(@Nullable XmlPullParser xpp, @Nullable DB db) {
        this.xpp = xpp;
        this.db = db;
    }

    public long addChannel(URL url) throws XmlPullParserException, IOException {
        Channel channel = null;
        try {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                if (("rss".equals(xpp.getName()))) {
                    channel = parsChannel(url.getQuery());
                }
            }
            if (channel != null) {
                db.open();
                if (db.isChanelInDb(channel.getLink())) {
                    return db.addChanel(channel.getTitle(), channel.getLink(), channel.getDescription());
                }
            }
        } finally {
            db.close();
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

    public void addNews(Long channelId) {
        ArrayList<News> news = null;
        try {
            news = parsNews();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        newsToDB(channelId,news);
    }

    public void checkNews(long channelId) {
        ArrayList<News> news = null;
        try {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                if (("rss".equals(xpp.getName()))) {
                    news = parsNews();
                }
            }
            newsToDB(channelId,news);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
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

    private void newsToDB(long channelId,ArrayList<News> news){
        if (news != null) {
            db.open();
            for (News currentNews : news) {
                if (!db.isNewsInDb(currentNews)) {
                    db.addToNews(channelId, currentNews.getTitle(), currentNews.getFullNewsUri(),
                            currentNews.getDescription());
                }
            }
        }
    }

    public static XmlPullParser prepareXpp(InputStream is, String encoding) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(is, encoding);
        return xpp;
    }
}