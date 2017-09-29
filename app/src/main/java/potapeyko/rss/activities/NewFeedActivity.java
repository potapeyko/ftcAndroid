package potapeyko.rss.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import lombok.NonNull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import potapeyko.rss.R;
import potapeyko.rss.model.Feed;
import potapeyko.rss.network.NetworkHelper;
import potapeyko.rss.utils.BroadcastSender;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import static potapeyko.rss.utils.BroadcastSender.*;

//todo: не влазит на экран при горизонтальной ориентации экрана. + сделать поход в интернет.
public final class NewFeedActivity extends MyBaseActivity {

    private Button btnConnectNewChanel;
    private Button btnFindNewChanel;
    private EditText etUri;
    private EditText etKeywords;
    private ListView lvFeedsList;
    private TextView tvNewChanel;
    private Spinner spUrlProtocol;
    private final NetworkHelper nwHelper;
    private LinearLayout progressBarLayout; //progressBarLayout inside this linearLayout
    ArrayList<Feed> feedsList;
//    int o=0;

    private GetListOfFeedsAsynkTask getListOfFeedsAsynkTask;

    /// сохранение асинктаска при повороте экрана.
    @Override
    public Object onRetainCustomNonConfigurationInstance() {

        if (getListOfFeedsAsynkTask != null) {
            getListOfFeedsAsynkTask.unlink();
        }
        return getListOfFeedsAsynkTask;

    }

    static private class GetListOfFeedsAsynkTask extends AsyncTask<String, Void, Void> {
        NewFeedActivity newFeedActivity;

        public ArrayList<Feed> getFeeds() {
            return feeds;
        }

        ArrayList<Feed> feeds;

        public final void link(NewFeedActivity activity) {
            newFeedActivity = activity;
        }

        public final void unlink() {
            newFeedActivity = null;
        }

        @Override
        protected Void doInBackground(String... uri) {
            //сходить на сайт, распарсить, отдать
            if (uri == null || uri[0] == null || uri[0].compareTo("") == 0) return null;
            try {
                HttpURLConnection urlConnection = null;


                URL url = new URL("http://cloud.feedly.com/v3/search/feeds?count=50&q=" + URLEncoder.encode(uri[0], "UTF-8"));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(1000);
                urlConnection.connect();
                InputStream is = urlConnection.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(is));
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(reader);
                JSONObject jsonObj = (JSONObject) obj;
                JSONArray jsonResults = (JSONArray) jsonObj.get("results");

                int sizeOfJsonRes = jsonResults.size();
                feeds = new ArrayList<>(sizeOfJsonRes);

                String link;
                String description;
                String title = null;
                String website;
                Object[] temp = new Object[4];

                for (int i = 0; i < sizeOfJsonRes; i++) {
                    JSONObject f = (JSONObject) jsonResults.get(i);
                    temp[0] = f.get("feedId");
                    temp[1] = f.get("title");
                    temp[2] = f.get("website");
                    temp[3] = f.get("description");
                    if (temp[0] == null) continue;
                    link = temp[0].toString().substring(5);
                    if (temp[1] == null) {
                        title = null;
                    } else {
                        title = temp[1].toString();
                    }
                    if (temp[2] == null) {
                        website = null;
                    } else {
                        website = temp[2].toString();
                    }
                    if (temp[3] == null) {
                        description = null;
                    } else {
                        description = temp[3].toString();
                    }

                    feeds.add(
                            new Feed(1, title, link, website,
                                    description, null,
                                    null, 0)
                    );
                }
                //http://cloud.feedly.com/v3/search/feeds?count=50&q=sport


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (newFeedActivity != null) {
                newFeedActivity.createFeedsList(feeds);
            }
        }
    }


    private void createFeedsList(ArrayList<Feed> s) {
        if (lvFeedsList != null && s != null) {
            feedsList = s;
            ArrayAdapter arrayAdapter = createArrayAdapter(s);
            lvFeedsList.setAdapter(arrayAdapter);
            lvFeedsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView urlView = (TextView) view.findViewById(R.id.newfeeds_list_item_url);
                    if (urlView != null) {
                        AddChannelIntentService.startActionADD(getApplicationContext(), urlView.getText().toString());
                        progressBarLayout.setVisibility(ProgressBar.VISIBLE);
                    }
                }
            });
            //http://cloud.feedly.com/v3/search/feeds?count=50&q=sport
        }
        progressBarLayout.setVisibility(ProgressBar.INVISIBLE);//убираем прогресс
        activateControls();
    }

    final private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) {
                Log.e(getString(R.string.LOG_KEY), "onReceive intent == null");
                return;
            }

            String broadcastMessage = intent.getStringExtra(STRING_BROADCAST_MESSAGE);
            if (CHANNEL_NEWS_ADD_BROADCAST_MESS.equals(broadcastMessage)) {//все успешно добавленно
                MainActivity.start(NewFeedActivity.this,
                        intent.getLongExtra(LONG_BROADCAST_DATA, -1));//открываем добавленный канал
                NewFeedActivity.this.finish();
            } else if (CHANNEL_ADD_BROADCAST_MESS.equals(broadcastMessage)) {
                showCauseToast(broadcastMessage);
            } else {//какая то ошибка
                NewFeedActivity.this.activateControls();
                progressBarLayout.setVisibility(ProgressBar.INVISIBLE);//убираем прогресс
                showCauseToast(broadcastMessage); // сообщения о причинах ошибки
            }

        }
    };

    private void showCauseToast(String broadcastMessage) {
        if (CHANNEL_ALREADY_WAS_IN_DB_BROADCAST_MESS.equals(broadcastMessage)) {
            Toast.makeText(this, R.string.channel_already_was_in_db, Toast.LENGTH_LONG).show();
        } else if (CONNECTION_EXCEPTION_BROADCAST_MESS.equals(broadcastMessage)) {
            Toast.makeText(this, R.string.connection_exception, Toast.LENGTH_LONG).show();
        } else if (DB_EXCEPTION_BROADCAST_MESS.equals(broadcastMessage)) {
            Toast.makeText(this, R.string.db_exception, Toast.LENGTH_LONG).show();
        } else if (CHANNEL_ADD_BROADCAST_MESS.equals(broadcastMessage)) {
            Toast.makeText(this, R.string.channel_add_broadcast_message, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(getString(R.string.LOG_KEY), "showCauseToast unknown broadcastMessage");
        }
    }

    public NewFeedActivity() {
        nwHelper = new NetworkHelper(this);
    }//а че он не Singleton?

    private void deactivateControls() {
        btnConnectNewChanel.setEnabled(false);
        btnFindNewChanel.setEnabled(false);
        etUri.setEnabled(false);
        etKeywords.setEnabled(false);
        spUrlProtocol.setEnabled(false);
    }

    private void activateControls() {
        btnConnectNewChanel.setEnabled(true);
        btnFindNewChanel.setEnabled(true);
        etUri.setEnabled(true);
        etKeywords.setEnabled(true);
        spUrlProtocol.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //задача работает, но не держит активость (не мешает удалиться)
        if (getListOfFeedsAsynkTask != null) {
            getListOfFeedsAsynkTask.unlink();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //если активность восстановили, а в ней была запущена задача, тогда линкуем обратно
        checkAsynkTask();

    }

    private void checkAsynkTask() {
        if (getListOfFeedsAsynkTask != null) {
            //задача могла закончиться пока активность была скрыта.
            if (getListOfFeedsAsynkTask.getStatus() != AsyncTask.Status.FINISHED) {
                getListOfFeedsAsynkTask.link(this);
                deactivateControls();
                progressBarLayout.setVisibility(ProgressBar.VISIBLE);
            } else {
                createFeedsList(getListOfFeedsAsynkTask.getFeeds());
                getListOfFeedsAsynkTask = null;//задача закончилась, результаты достали. больше она не нужна
            }
        }//если равна нулю, то будет создана при нажатии кнопки
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(NewFeedActivity.this).registerReceiver(br, new IntentFilter(BroadcastSender.INTENT_FILTER));
        setContentView(R.layout.activity_new_feed);
        android.support.v7.widget.Toolbar toolbar = initToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnConnectNewChanel = (Button) findViewById(R.id.activity_new_feed_btnConnectNewChanel);
        btnFindNewChanel = (Button) findViewById(R.id.activity_new_feed_btnFindNewChanel);
        etUri = (EditText) findViewById(R.id.activity_new_feed_etNewChanelUri);
        etKeywords = (EditText) findViewById(R.id.activity_new_feed_etFindNewChanelKeywords);


        tvNewChanel = (TextView) findViewById(R.id.activity_new_feed_tvNewChanel);
        spUrlProtocol = (Spinner) findViewById(R.id.activity_new_feed_spUrlProtocol);
        lvFeedsList = (ListView) findViewById(R.id.activity_new_feed_list);
        progressBarLayout = (LinearLayout) findViewById(R.id.activity_new_feed_progressBar_layout);

        if (!nwHelper.isNetworkAvailable()) {
            notConnectionCase();
        } else {
            btnConnectNewChanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = getUri();
                    if (!isLinkCorrect(uri)) {
                        Toast.makeText(NewFeedActivity.this,
                                R.string.new_chanel_not_correct_link_toast,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AddChannelIntentService.startActionADD(getApplicationContext(), uri);
                    deactivateControls();
                    progressBarLayout.setVisibility(ProgressBar.VISIBLE);
                }
            });

            btnFindNewChanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deactivateControls();
                    progressBarLayout.setVisibility(ProgressBar.VISIBLE);
                    getListOfFeedsAsynkTask = new GetListOfFeedsAsynkTask();
                    getListOfFeedsAsynkTask.link(NewFeedActivity.this);
                    getListOfFeedsAsynkTask.
                            execute(etKeywords.getText().toString());
                }
            });
            btnFindNewChanel.requestFocus();
        }


        //получаем сохраненную при пересоздании активности задачу
        getListOfFeedsAsynkTask = (GetListOfFeedsAsynkTask) getLastCustomNonConfigurationInstance();
        checkAsynkTask();

        //может скрывали активность, тогда задача могла убиться вместе с активностью а список сохранился
        if (savedInstanceState != null) {
            ArrayList<Feed> feedsFromBandle = (ArrayList<Feed>) savedInstanceState.getSerializable("feedsList");
            if (feedsFromBandle != null) {
                createFeedsList(feedsFromBandle);
            }
        }
    }

    private ArrayAdapter createArrayAdapter(final ArrayList a) {
        return new ArrayAdapter<Feed>(getApplicationContext(),
                R.layout.newfeeds_list_item, a) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Feed feed = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.newfeeds_list_item, null);
                }
                ((TextView) convertView.findViewById(R.id.newfeeds_list_item_title))
                        .setText(feed.getTitle());
                ((TextView) convertView.findViewById(R.id.newfeeds_list_item_url))
                        .setText(feed.getLink());
                ((TextView) convertView.findViewById(R.id.newfeeds_list_item_description))
                        .setText(feed.getDescription());
                return convertView;
            }
        };
    }

    private String getUri() {
        String uri = etUri.getText().toString();
        String http_https[] = getResources().getStringArray(R.array.http_https);
        if (!uri.contains("://")) {//no protocol in url => берем из spinner
            return getResources().getStringArray(R.array.http_https)[spUrlProtocol.getSelectedItemPosition()] + uri;
        } else {
            String[] s = uri.split("://", 2);

            String protocol = s[0] + "://";
            for (int i = 0; i < http_https.length; i++) {

                if (http_https[i].equals(protocol)) {
                    spUrlProtocol.setSelection(i, false);
                    etUri.setText(s[1]);
                    break;
                }
            }
            return uri;//есть :// но не протокол. обработается при попытке обращения
        }
    }

    private boolean isLinkCorrect(String uri) {
        return Patterns.WEB_URL.matcher(uri).matches();
    }

    private void notConnectionCase() {//todo доделать для остальных полей
        btnConnectNewChanel.setText(R.string.back);
        btnConnectNewChanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        etUri.setEnabled(false);
        etUri.setVisibility(View.INVISIBLE);
        tvNewChanel.setText(R.string.new_chanel_no_connection);
        Toast.makeText(this, R.string.new_chanel_no_connection_toast, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        super.onDestroy();
    }

    static void start(@NonNull Activity other) {
        Intent intent = new Intent(other, NewFeedActivity.class);
        other.startActivity(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feedsList", feedsList);
    }
}
