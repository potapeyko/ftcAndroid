package potapeyko.rss.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import lombok.NonNull;

public final class NetworkHelper {
    private final Context context;

    public NetworkHelper(@NonNull Context context) {
        this.context = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connMgr==null)return false;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


}
