package potapeyko.rss.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.ActivityListenerAdapter;
import potapeyko.rss.network.NetworkHelper;

import static potapeyko.rss.utils.BroadcastSender.*;


public final class NewChanelActivityMy extends MyBaseActivity {

    private Button btnNewChanel;
    private EditText etUri;
    private TextView tvNewChanel;
    private final NetworkHelper nwHelper;
    private ProgressBar progressBar;

    final private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcastMessage = intent.getStringExtra(STRING_BROADCAST_MESSAGE);
            if(CHANNEL_NEWS_ADD_BROADCAST_MESS.equals(broadcastMessage)){
                MainActivity.start(NewChanelActivityMy.this,intent.getLongExtra(LONG_BROADCAST_DATA,-1));
                NewChanelActivityMy.this.finish();
            }
            else if(CHANNEL_ADD_BROADCAST_MESS.equals(broadcastMessage)){
                showCauseToast(broadcastMessage);
            }
            else
            {
                btnNewChanel.setEnabled(true);
                etUri.setEnabled(true);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                showCauseToast(broadcastMessage);
            }

        }
    };

    private void showCauseToast(String broadcastMessage) {
        if(CHANNEL_ALREADY_WAS_IN_DB_BROADCAST_MESS.equals(broadcastMessage)){
            Toast.makeText(this,R.string.channel_already_was_in_db,Toast.LENGTH_LONG).show();
        }
        else if(CONNECTION_EXCEPTION_BROADCAST_MESS.equals(broadcastMessage)){
            Toast.makeText(this,R.string.connection_exception,Toast.LENGTH_LONG).show();
        }
        else if(DB_EXCEPTION_BROADCAST_MESS.equals(broadcastMessage)){
            Toast.makeText(this,R.string.db_exception,Toast.LENGTH_LONG).show();
        }
        else if (CHANNEL_ADD_BROADCAST_MESS.equals(broadcastMessage)){
            Toast.makeText(this,R.string.channel_add_broadcast_message,Toast.LENGTH_SHORT).show();
        }
    }


    public NewChanelActivityMy() {

        nwHelper = new NetworkHelper(this);
        this.onCreateSubscribe(new ActivityListenerAdapter(){
            @Override
            public void onCreateActivity(@Nullable Bundle savedInstanceState) {

                LocalBroadcastManager.getInstance(NewChanelActivityMy.this).registerReceiver( br, new IntentFilter( "potapeyko.rss.activities" ) );
                setContentView(R.layout.activity_new_chanel);
                btnNewChanel = (Button) findViewById(R.id.activity_new_chanel_btnNewChanel);
                etUri = (EditText) findViewById(R.id.activity_new_chanel_etNewChanelUri);
                tvNewChanel = (TextView) findViewById(R.id.activity_new_chanel_tvNewChanel);
                progressBar= (ProgressBar)findViewById(R.id.activity_new_chanel_progressBar);
                if (!nwHelper.isNetworkAvailable()) {
                    notConnectionCase();
                }
                else {

                    btnNewChanel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!isLinkCorrect()){
                                Toast.makeText(NewChanelActivityMy.this,
                                        R.string.new_chanel_not_correct_link_toast,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            AddIntentService.startActionADD(getApplicationContext(),etUri.getText().toString());
                            btnNewChanel.setEnabled(false);
                            etUri.setEnabled(false);
                            progressBar.setVisibility(ProgressBar.VISIBLE);
                        }
                    });

                }
            }
        });
    }



    private boolean isLinkCorrect() {
        String uri = etUri.getText().toString();
        return Patterns.WEB_URL.matcher(uri).matches();
    }

    private void notConnectionCase() {
        btnNewChanel.setText(R.string.back);
        btnNewChanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        etUri.setEnabled(false);
        etUri.setVisibility(View.INVISIBLE);
        tvNewChanel.setText(R.string.new_chanel_no_connection);
        Toast.makeText(this,R.string.new_chanel_no_connection_toast,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        super.onDestroy();
    }

    static void start(Activity other) {
        Intent intent = new Intent(other, NewChanelActivityMy.class);
        other.startActivity(intent);
    }

}
