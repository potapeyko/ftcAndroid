package potapeyko.rss.activities;

@lombok.AllArgsConstructor
class MyTag {
    long feedItemId;
    int checkedFlag;
    int favoriteFlag;
    int idOfClickedIcon;
    static final int trueValue=1;
    static final int falseValue =0;
}
