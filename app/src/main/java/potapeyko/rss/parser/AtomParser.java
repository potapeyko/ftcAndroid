package potapeyko.rss.parser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.model.Feed;
import potapeyko.rss.model.FeedItem;

import java.io.IOException;

/**
 * Created by Дмитрий on 25.01.2017.
 */
public class    AtomParser {
    final static String NS_ATOM = "http://www.w3.org/2005/Atom";

    public static void process(XmlPullParser parser, FeedParser feedParser) throws XmlPullParserException, IOException {

        Feed feed = new Feed();
        feed.setLink(feedParser.getFeedUrl());

        for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (isAtomElement(parser, "entry")) {
                break;
            } else if (isAtomElement(parser, "title")) {
                feed.setTitle(parser.nextText());
            } else if (isAtomElement(parser, "updated")) {
                feed.setLastBuildDate(ParserDateUtils.parseDate(parser.nextText()));
            } else if (isAtomElement(parser, "link")) {
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    String relType = parser.getAttributeValue(null, "rel");
                    if ("self".equals(relType)) {
                        feed.setSiteLink(parser.getAttributeValue(null, "href"));
                    }
                }
            } else if (isAtomElement(parser, "subtitle")) {
                feed.setDescription(parser.nextText());
            }
        }

        if (feedParser.getFeedHandler() != null)
            feedParser.getFeedHandler().OnFeed(feedParser, feed);
        if (feedParser.shouldStopProcessing())
            return;

        parseEntries(parser, feedParser);
    }

    private static void parseEntries(XmlPullParser parser, FeedParser feedParser) throws XmlPullParserException, IOException {
        FeedItem item = null;

        for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
            if (eventType == XmlPullParser.START_TAG) {
                if (isAtomElement(parser, "entry")) {
                    item = new FeedItem();
                } else if (item != null && isAtomElement(parser, "title")) {
                    item.setTitle(parser.nextText());
                } else if (item != null && isAtomElement(parser, "link")) {
                    String rel = parser.getAttributeValue(null, "rel");
                    if (rel == null || rel.equals("alternate"))
                        item.setLink(parser.getAttributeValue(null, "href"));
                    else if (rel.equals("enclosure")) {
                        if (parser.getAttributeValue(null, "length") != null)
                            item.setMediaSize(Long.valueOf(parser.getAttributeValue(null, "length")));
                        item.setMediaURL(parser.getAttributeValue(null, "href"));
                    }
                } else if (item != null && isAtomElement(parser, "summary") && item.getDescription() == null)
                    item.setDescription(parser.nextText());
                else if (item != null && isAtomElement(parser, "content"))
                    item.setDescription(parser.nextText());
                else if (item != null && isAtomElement(parser, "published")&& item.getPubDate() == null)
                    item.setPubDate(ParserDateUtils.parseDate(parser.nextText()));
                else if (item != null && isAtomElement(parser, "updated"))
                    item.setPubDate(ParserDateUtils.parseDate(parser.nextText()));
            } else if (eventType == XmlPullParser.END_TAG) {
                if (isAtomElement(parser, "entry")) {
                    if (feedParser.getFeedItemHandler() != null)
                        feedParser.getFeedItemHandler().OnFeedItem(feedParser, item);
                    if (feedParser.shouldStopProcessing())
                        return;
                    item = null;
                }
            }
        }
    }

    private static boolean isAtomElement(XmlPullParser parser, String name) {
        return parser.getName().equals(name) && parser.getNamespace().equals(NS_ATOM);
    }
}
