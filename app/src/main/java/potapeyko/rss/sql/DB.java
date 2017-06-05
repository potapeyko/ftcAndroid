package potapeyko.rss.sql;


import android.content.Context;
import lombok.NonNull;

public final class DB {



    public static DbReader getReader(final @NonNull Context context){
        return new DbReader(context);
    }
    public static DbWriter getWriter(final @NonNull Context context){
        return new DbWriter(context);
    }

}