package potapeyko.rss.parser;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.constants.LogCodes;
import potapeyko.rss.models.News;
import potapeyko.rss.sql.DB;

import java.io.IOException;


public final class Parser {

    private final static String RSS_TITLE = "title";
    private final static String RSS_DESCR = "description";
    private final static String RSS_CHANEL = "chanel";

    private final static String RSS_ITEM = "item";
    private final static String RSS_ITEM_TITLE = "title";
    private final static String RSS_ITEM_DESCR = "description";
    private final static String RSS_ITEM_LINK = "link";


    public static boolean parsToDB(XmlPullParser xpp, DB db, final String uri) throws XmlPullParserException, IOException {
        if (db == null) return false;
        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (xpp.getEventType()) {
                case XmlPullParser.START_TAG:
                    if ("rss".equals(xpp.getName())) {
                        xpp.next();
                        return parsChanel(xpp, db, uri);
                    } else return false;

                default:
                    break;
            }
            xpp.next();
        }
        return true;
    }

    private static boolean parsChanel(XmlPullParser xpp, DB db, String uri) throws XmlPullParserException, IOException {
        //оперделили что rss. читаем канал
        long chanelId = -1;
        boolean isTitle=false;

        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (xpp.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (!isTitle&&RSS_TITLE.equals(xpp.getName())) {
                        xpp.next();
                        String title = xpp.getText();
                        isTitle=true;
                        if (title == null) return false;

                        if(db.isChanelInDb(uri)){
                            Log.e(LogCodes.PARS,"УЖЕ ЕСТЬ");
                            return false;
                        }
                        chanelId = db.addChanel(title, uri);

                    } else if (RSS_DESCR.equals(xpp.getName()) && chanelId != -1) {
                        xpp.next();
                        String description = xpp.getText();
                        if (description == null) return false;
                        db.addDescriptionToChanel(chanelId, description);
                    } else if (chanelId != -1 && RSS_ITEM.equals(xpp.getName())) {
                        parseItem(xpp, db, chanelId);
                    }
                    break;
            }
            xpp.next();
        }
        return true;
    }

    private static void parseItem(XmlPullParser xpp, DB db, long chanelId) throws XmlPullParserException, IOException {
        News news = new News();
        while (true) {
            switch (xpp.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (RSS_ITEM.equals(xpp.getName())) {
                        xpp.next();
                        continue;
                    }
                    if (RSS_ITEM_TITLE.equals(xpp.getName())) {
                        xpp.next();
                        String itemTitle = xpp.getText();
                        if (itemTitle == null) return;
                        news.setTitle(itemTitle);
                    } else if (RSS_ITEM_DESCR.equals(xpp.getName())) {
                        xpp.next();
                        String itemDescr = xpp.getText();
                        if (itemDescr == null) return;
                        news.setDescription(itemDescr);
                    } else if (RSS_ITEM_LINK.equals(xpp.getName())) {
                        xpp.next();
                        String itemLink = xpp.getText();
                        if (itemLink == null) return;
                        news.setFullNewsUri(itemLink);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (RSS_ITEM.equals(xpp.getName())) {
                        if (! db.isNewsInDb(news.getFullNewsUri())) {
                            db.addToNews(chanelId, news.getTitle(), news.getFullNewsUri(), news.getDescription());
                        }
                        xpp.next();
                        return;
                    }
                    default:break;
            }
            xpp.next();
        }
    }
}