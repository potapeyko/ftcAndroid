package potapeyko.rss.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import lombok.NonNull;

public final class BroadcastSender {

    public static final String STRING_BROADCAST_MESSAGE =  "STRING_BROADCAST_MESSAGE";
    public static final String LONG_BROADCAST_DATA =  "LONG_BROADCAST_DATA";
    public static final String CONNECTION_EXCEPTION_BROADCAST_MESS = "ConnectionException";
    public static final String CHANNEL_UPDATE_BROADCAST_MESS = "UpdateChannel";
    public static final String DB_EXCEPTION_BROADCAST_MESS = "DBException";
    public static final String  CHANNEL_ADD_BROADCAST_MESS = "ChannelAdded";
    public static final String CHANNEL_NEWS_ADD_BROADCAST_MESS = "ChannelNewsAdded";
    public static final String CHANNEL_ALREADY_WAS_IN_DB_BROADCAST_MESS = "ChannelAlreadyWasInDb";
    public static final String INTENT_FILTER = "potapeyko.rss.utils";

    private BroadcastSender() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static void sendMyBroadcast(@NonNull Context context, @NonNull  String message, long data) {
        Intent serviceStartedIntent = new Intent(INTENT_FILTER);
        serviceStartedIntent.putExtra(STRING_BROADCAST_MESSAGE, message);
        serviceStartedIntent.putExtra(LONG_BROADCAST_DATA, data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(serviceStartedIntent);
    }
}

