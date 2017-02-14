package potapeyko.rss.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Дмитрий on 25.01.2017.
 */
public class ParserDateUtils {
    private final static SimpleDateFormat dateFormats[] = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
            new SimpleDateFormat("EEE, d MMM yy HH:mm:ss z", Locale.US),
            new SimpleDateFormat("EEE, d MMM yy HH:mm z", Locale.US),
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US),
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm z", Locale.US),
            new SimpleDateFormat("EEE d MMM yy HH:mm:ss z", Locale.US),
            new SimpleDateFormat("EEE d MMM yy HH:mm z", Locale.US),
            new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss z", Locale.US),
            new SimpleDateFormat("EEE d MMM yyyy HH:mm z", Locale.US),
            new SimpleDateFormat("d MMM yy HH:mm z", Locale.US),
            new SimpleDateFormat("d MMM yy HH:mm:ss z", Locale.US),
            new SimpleDateFormat("d MMM yyyy HH:mm z", Locale.US),
            new SimpleDateFormat("d MMM yyyy HH:mm:ss z", Locale.US),

            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()),
            new SimpleDateFormat("EEE, d MMM yy HH:mm:ss z", Locale.getDefault()),
            new SimpleDateFormat("EEE, d MMM yy HH:mm z", Locale.getDefault()),
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.getDefault()),
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm z", Locale.getDefault()),
            new SimpleDateFormat("EEE d MMM yy HH:mm:ss z", Locale.getDefault()),
            new SimpleDateFormat("EEE d MMM yy HH:mm z", Locale.getDefault()),
            new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss z", Locale.getDefault()),
            new SimpleDateFormat("EEE d MMM yyyy HH:mm z", Locale.getDefault()),
            new SimpleDateFormat("d MMM yy HH:mm z", Locale.getDefault()),
            new SimpleDateFormat("d MMM yy HH:mm:ss z", Locale.getDefault()),
            new SimpleDateFormat("d MMM yyyy HH:mm z", Locale.getDefault()),
            new SimpleDateFormat("d MMM yyyy HH:mm:ss z", Locale.getDefault()),
    };

    static Date parseDate(String date) {
        for (SimpleDateFormat format : dateFormats) {
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                return format.parse(date);
            } catch (ParseException e) {
            }
        }
        return null;
    }
}
