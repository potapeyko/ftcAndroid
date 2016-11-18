package potapeyko.rss.activities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Patterns;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import potapeyko.rss.parser.ParsHelper;
import potapeyko.rss.sql.DB;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class AddIntentService extends IntentService {

    private static final String ACTION_ADD = "potapeyko.rss.activities.action.ADD";



    private static final String EXTRA_PARAM1 = "potapeyko.rss.activities.extra.PARAM1";


    public AddIntentService() {
        super("AddIntentService");
    }

     static void startActionADD(Context context, String uri) {
        Intent intent = new Intent(context, AddIntentService.class);
        intent.setAction(ACTION_ADD);
        intent.putExtra(EXTRA_PARAM1, uri);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD.equals(action)) {
                final String uri = intent.getStringExtra(EXTRA_PARAM1);
                handleActionAdd(uri);
            }
        }
    }

    private void handleActionAdd(String uri)  {
        if(Patterns.WEB_URL.matcher(uri).matches()){
            //не приконектились
            return;
        }
        URL url;
        InputStream is;
        HttpURLConnection urlConnection;
        DB db = new DB(this);
        try {
            url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            is = urlConnection.getInputStream();
            XmlPullParser xpp = ParsHelper.prepareXpp(is, null);
            ParsHelper helper = new ParsHelper(xpp,db);

            long channelId = helper.addChannel(url);
            if(channelId==-1){
                //не добавили почему-то
                return;
            }
            helper.addNews(channelId);

            //закончили

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
}
