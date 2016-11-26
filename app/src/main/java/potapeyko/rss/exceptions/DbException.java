package potapeyko.rss.exceptions;
public class DbException extends Exception{


    public DbException(Throwable throwable) {
        super(throwable);
    }

    public DbException() {
        super();
    }
}