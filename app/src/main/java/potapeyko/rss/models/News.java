package potapeyko.rss.models;


import lombok.*;


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
    private String fullNewsUri;

//    @Getter    @Setter    int imageResource;

}
