//package potapeyko.rss.parser;
//
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//import potapeyko.rss.model.FeedItem;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//
//
//public class NewParser {
//
//    public static final ArrayList<FeedItem> getNews(String feedUrl) throws IOException, ParserConfigurationException, SAXException {
//        ArrayList<FeedItem> rssItems = null;
//
//        URL url = new URL(feedUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//            InputStream is = conn.getInputStream();
//            //DocumentBuilderFactory, DocumentBuilder are used for
//            //xml parsing
//            DocumentBuilderFactory dbf = DocumentBuilderFactory
//                    .newInstance();
//            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
//            //using document Builder parse xml data and assign
//            //it to Element
//
//            Document document = documentBuilder.parse(is);
//            Element element = document.getDocumentElement();
//
//            //rss try
//            NodeList nodeList = element.getElementsByTagName("item");
//            if (nodeList.getLength() > 0) {
//                rssItems = prepareNewsFromRss(nodeList);
//                return rssItems;
//            }
//
//        }
//        return null;
//    }
//
//    private static ArrayList<FeedItem> prepareNewsFromRss(NodeList nodeList) {
//        ArrayList<FeedItem> rssItems= new ArrayList<>();
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            Element entry = (Element) nodeList.item(i);
//
//            Element _titleE = (Element) entry.getElementsByTagName(
//                    "title").item(0);
//            Element _descriptionE = (Element) entry
//                    .getElementsByTagName("description").item(0);
//            Element _pubDateE = (Element) entry
//                    .getElementsByTagName("pubDate").item(0);
//            Element _linkE = (Element) entry.getElementsByTagName(
//                    "link").item(0);
//
//            String _title = _titleE.getFirstChild().getNodeValue();
//            String _description = _descriptionE.getFirstChild().getNodeValue();
//            Date _pubDate = new Date(_pubDateE.getFirstChild().getNodeValue());
//            String _link = _linkE.getFirstChild().getNodeValue();
//
//            //create RssItemObject and add it to the ArrayList
//            FeedItem rssItem = new FeedItem(0,_title,_description,_link,_pubDate);
//            rssItems.add(rssItem);
//        }
//        return rssItems;
//    }
//
//
//}
