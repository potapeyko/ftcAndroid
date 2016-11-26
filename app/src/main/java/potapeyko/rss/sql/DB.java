package potapeyko.rss.sql;


import android.content.Context;
import lombok.NonNull;

public final class DB {

    private final Context context;

    public DB(final @NonNull Context context) {
        this.context = context;
    }

    public DbReader getReader(){
        return new DbReader(context);
    }
    public DbWriter getWriter(){
        return new DbWriter(context);
    }

}