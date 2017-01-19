package potapeyko.rss.model;


import lombok.*;

import java.util.Date;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class News {
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

}
