package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import potapeyko.rss.R;
import potapeyko.rss.interfaces.IActivityListener;
import potapeyko.rss.network.NetworkHelper;

import java.net.URI;

public final class NewChanelActivityMy extends MyBaseActivity implements IActivityListener {

    private Button btnNewChanel;
    private EditText etUri;
    private TextView tvNewChanel;
    private final NetworkHelper nwHelper;

    public NewChanelActivityMy() {
        this.onCreateSubscribe(this);
        nwHelper = new NetworkHelper(this);
    }

    @Override
    public void onCreateActivity(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_chanel);
        btnNewChanel = (Button) findViewById(R.id.btnNewChanel);
        etUri = (EditText) findViewById(R.id.etNewChanelUri);
        tvNewChanel = (TextView) findViewById(R.id.tvNewChanel);
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
                    //иедем в сервис грузим парсим
                }
            });
        }
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
    public void onSaveInstanceStateActivity(Bundle outState) {

    }

    static void start(Activity other) {
        Intent intent = new Intent(other, NewChanelActivityMy.class);
        other.startActivity(intent);
    }

}
