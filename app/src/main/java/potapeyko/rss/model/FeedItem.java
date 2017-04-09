package potapeyko.rss.model;


import android.content.ContentValues;
import android.util.Log;
import lombok.*;

import java.util.Date;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FeedItem {
    @Getter
    private long id;
    @Getter @Setter
    private String title;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private String link;
    @Getter @Setter
    private Date pubDate;
    @Getter @Setter
    private String mediaURL;
    @Getter @Setter
    private Long mediaSize;
    @Getter @Setter private int checkedFlag=0;
    @Getter @Setter private int favoriteFlag=0;


    public FeedItem(){
        Log.d("wtf","constr");
    }
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (getTitle() != null)
            values.put("title", getTitle());
        if (getLink() != null)
            values.put("link", getLink());
        if (getDescription() != null)
            values.put("description", getDescription());
        if (getPubDate() != null)
            values.put("pubDate", getPubDate().getTime());
        if (getMediaURL() != null)
            values.put("mediaURL", getMediaURL());
        if (getMediaSize() != null)
            values.put("mediaSize", getMediaSize());
        return values;
    }

}