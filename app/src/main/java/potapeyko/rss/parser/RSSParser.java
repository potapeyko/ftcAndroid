package potapeyko.rss.parser;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.model.Feed;
import potapeyko.rss.model.FeedItem;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Дмитрий on 25.01.2017.
 */
class RSSParser {
    private RSSParser() {
    }

    static void process(XmlPullParser parser, FeedParser feedParser) throws XmlPullParserException, IOException {
        Feed feed = new Feed();
        feed.setLink(feedParser.getFeedUrl());
        boolean in_image = false;

        // look for subscription details, stop at item tag
        int eventType = parser.getEventType();
        for (; eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
            // check for an ending image tag
            if (in_image && eventType == XmlPullParser.END_TAG && parser.getName().equals("image")) {
                in_image = false;
                continue;
            }
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (in_image) {
                //image?
                continue;
            }
            String name = parser.getName();
            // if we're starting an item, move past the subscription details section
            if (name.equals("item")) {
                break;
            } else if (name.equals("image")) {
                in_image = true;
                continue;
            } else if (parser.getDepth() != 3) {//todo почему 3?
                continue;
            } else if (name.equalsIgnoreCase("pubDate")) {
                Date date = ParserDateUtils.parseDate(parser.nextText());
                if (date != null) {
                    feed.setPubDate(date);
                }
            } else if (name.equalsIgnoreCase("lastBuildDate")) {
                Date date = ParserDateUtils.parseDate(parser.nextText());
                if (date != null)
                    feed.setLastBuildDate(date);
            } else if (name.equalsIgnoreCase("title") && parser.getNamespace().equals("")) {
                feed.setTitle(parser.nextText());
            } else if (name.equalsIgnoreCase("link") && parser.getNamespace().equals("")) {
                feed.setSiteLink(parser.nextText());
            } else if (name.equalsIgnoreCase("description") && parser.getNamespace().equals("")) {
                feed.setDescription(parser.nextText());
            }
        }

        if (feedParser.getFeedHandler() != null) {
            feedParser.getFeedHandler().OnFeedInfo(feedParser, feed);
        }

        if (feedParser.shouldStopProcessing()) {
            return;
        }

        parseRSSItems(parser, feedParser);
    }

    private static void parseRSSItems(XmlPullParser parser, FeedParser feedParser) throws XmlPullParserException, IOException {
        FeedItem item = null;

        for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName();
                String namespace = parser.getNamespace();
                if (name.equalsIgnoreCase("item")) {
                    item = new FeedItem();
                } else if (parser.getNamespace().equals("") && name.equalsIgnoreCase("title") && item != null) {
                    item.setTitle(parser.nextText());
                } else if (parser.getNamespace().equals("") && name.equalsIgnoreCase("link") && item != null) {
                    item.setLink(parser.nextText());
                } else if (item != null && namespace.equals("") && name.equalsIgnoreCase("description")) {
                    item.setDescription(parser.nextText());
                } else if (item != null && name.equalsIgnoreCase("pubDate")) {
                    item.setPubDate(ParserDateUtils.parseDate(parser.nextText()));
                } else if (item != null && name.equalsIgnoreCase("enclosure")) {
                    item.setMediaURL(parser.getAttributeValue(null, "url"));
                    try {
                        item.setMediaSize(Long.valueOf(parser.getAttributeValue(null, "length")));
                    } catch (Exception e) {
                        item.setMediaSize(0L);
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                String name = parser.getName();
                if (name.equalsIgnoreCase("item")) {
                    if (feedParser.getFeedItemHandler() != null)
                        feedParser.getFeedItemHandler().OnFeedItem(feedParser, item);
                    if (feedParser.shouldStopProcessing()) {
                        return;
                    }
                    item = null;
                }
            }
        }
    }
}
