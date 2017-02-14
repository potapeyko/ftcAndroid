package potapeyko.rss.model;


import android.content.ContentValues;
import lombok.*;

import java.util.Date;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class Feed {
    @Getter private  long id;
    @Getter @Setter private String title;
    @Getter @Setter private String link;
    @Getter @Setter private String siteLink;

    @Getter @Setter private  String description;
    @Getter @Setter private Date lastBuildDate;
    @Getter @Setter private Date pubDate;

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (getTitle() != null)
            values.put("title", getTitle());
        if (getLink() != null)
            values.put("link", getLink());
        if (getLastBuildDate() != null)
            values.put("lastBuildDate", getLastBuildDate().getTime());
        if (getPubDate() != null)
            values.put("pubDate", getPubDate().getTime());
        if(getDescription()!=null){
            values.put("description",getDescription());
        }
        return values;
    }

}
