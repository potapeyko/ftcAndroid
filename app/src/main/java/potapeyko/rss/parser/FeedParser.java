package potapeyko.rss.parser;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import potapeyko.rss.model.Feed;
import potapeyko.rss.model.FeedItem;

import java.io.IOException;
import java.io.InputStream;


public class    FeedParser {
    public interface FeedHandler {
        public void OnFeed(FeedParser feedParser, Feed feed);
    }

    public interface FeedItemHandler {
        public void OnFeedItem(FeedParser feedParser, FeedItem item);
    }

    public class UnknownFeedException extends Exception {
        private static final long serialVersionUID = -4953090101978301549L;

        public UnknownFeedException() {
            super("This is not a RSS or Atom feed.");
        }

        public UnknownFeedException(Throwable throwable) {
            super("This is not a RSS or Atom feed and.", throwable);
        }
    }

    public static XmlPullParser prepareXpp(@NonNull InputStream is) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(is, null);
        return xpp;
    }


    @Getter
    @Setter
    private FeedHandler feedHandler;
    @Getter
    @Setter
    private FeedItemHandler feedItemHandler;
    private boolean stopProcessing = false;
    public boolean shouldStopProcessing() {
        synchronized (this) {return stopProcessing;}
    }
    public void stopProcessing() {
        synchronized (this) { stopProcessing = true;}
    }
    @Getter private String FeedUrl;


    public FeedParser() {
    }



    public void parseFeed(@NonNull XmlPullParser parser, @NonNull String FeedUrl)
            throws XmlPullParserException, IOException, UnknownFeedException {

        this.FeedUrl = FeedUrl;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next();
            if(eventType==XmlPullParser.END_DOCUMENT){
                throw new UnknownFeedException();
            }
        }
        if (parser.getName().equals("rss")) {
            RSSParser.process(parser, this);
        } else if (parser.getName().equals("feed")) {
            AtomParser.process(parser, this);
        } else {
            throw new UnknownFeedException();
        }
    }
}
