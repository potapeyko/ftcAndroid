package potapeyko.rss.activities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import potapeyko.rss.constants.LogCodes;
import potapeyko.rss.parser.Parser;
import potapeyko.rss.sql.DB;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class MyService extends Service {
    private static String URI = "uri";
    private ExecutorService threadPoolService;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        threadPoolService = Executors.newFixedThreadPool(5);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        threadPoolService.execute(new AddTask(intent.getStringExtra(URI)));
        return super.onStartCommand(intent, flags, startId);
    }


    static boolean start(Context context, String uri) {
        if (context == null || "".equals(uri)) return false;
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(URI, uri);

        try {
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private class AddTask implements Runnable {
        private final String uri;
        private URL url = null;
        private InputStream is;
        private HttpURLConnection urlConnection = null;
        private DB db;
        AddTask(String uri) {
            this.uri = uri;
        }

        @Override
        public void run() {
            try {
                url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                is = urlConnection.getInputStream();
                XmlPullParser xpp = prepareXpp(is, null);
                db = new DB(MyService.this);
                db.open();
                if(db.isChanelInDb(uri)){
                    Log.e(LogCodes.ADD_CHANEL,"УЖЕ ЕСТЬ");
                    return;
                }
                if(!Parser.parsToDB(xpp,db,uri)){
                    Log.e(LogCodes.ADD_CHANEL,"НЕА");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }  finally {
                db.close();
                urlConnection.disconnect();
                if (is != null) try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }
    }

    private XmlPullParser prepareXpp(InputStream is, String encoding) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);//?
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(is, encoding);
        return xpp;
    }
}

