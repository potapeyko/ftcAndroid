package potapeyko.rss.models;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public class News {
    @Getter
    private final long id;
    @Getter @Setter
    private String title;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private String fullNewsUri;


    public News(long id, String title, String description, String fullNewsUri) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fullNewsUri = fullNewsUri;
    }


//    @Getter    @Setter    int imageResource;


}
