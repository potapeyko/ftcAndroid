package potapeyko.rss.Exeptions;
public class DbException extends Exception{

    public DbException(String detailMessage) {
        super(detailMessage);
    }

    public DbException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DbException(Throwable throwable) {
        super(throwable);
    }

    public DbException() {
        super();
    }
}