package potapeyko.rss.models;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class News {
    String title;
    String description;
    String fullNewsUri;

    public News(String title, String description, String fullNewsUri){
        this.title=title;
        this.description=description;
        this.fullNewsUri=fullNewsUri;
    }


//    @Getter    @Setter    int imageResource;


}
