package potapeyko.rss.model;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Channel {
    @Getter private  final long id;
    @Getter  private final String title;
    @Getter  private final String link;
    @Getter @Setter private  String description;

}
