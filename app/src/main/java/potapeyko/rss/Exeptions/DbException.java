package potapeyko.rss.Exeptions;
public class DbException extends Exception{


    public DbException(Throwable throwable) {
        super(throwable);
    }

    public DbException() {
        super();
    }
}