package potapeyko.rss.activities;


import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.sql.DB;

import java.util.Calendar;
import java.util.Date;

class mySimpleCursorAdapter extends android.support.v4.widget.SimpleCursorAdapter {


    private View.OnClickListener itemIconsClickListener;
    private long feedId;

    mySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, long feedId, View.OnClickListener itemIconsClickListener) {
        super(context, layout, c, from, to);
        this.feedId = feedId;
        this.itemIconsClickListener = itemIconsClickListener;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        final View v = view.findViewById(mTo[1]);
        Long date = cursor.getLong(mFrom[1]);
        Date d = new Date(date);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        String dateText = String.format("%te.%tm.%ty%n", c, c, c);
        setViewText((TextView) v, dateText);

        ImageView im = (ImageView) view.findViewById(R.id.feedItem_list_checkedBtn);
        ImageView im2 = (ImageView) view.findViewById(R.id.feedItem_list_favoriteBtn);

        im.setTag(new MyTag(cursor.getLong(0), cursor.getInt(7), cursor.getInt(8), R.id.feedItem_list_checkedBtn));
        im2.setTag(new MyTag(cursor.getLong(0), cursor.getInt(7), cursor.getInt(8), R.id.feedItem_list_favoriteBtn));

        if (((MyTag) im.getTag()).checkedFlag == 0) {
            im.setImageResource(R.drawable.ic_item_check_not);
            view.setBackgroundColor(this.mContext.getResources().getColor(R.color.colorWhite));
        } else {
            im.setImageResource(R.drawable.ic_item_check);
            view.setBackgroundColor(this.mContext.getResources().getColor(R.color.colorGray));
        }

        if (((MyTag) im2.getTag()).favoriteFlag == 0) {
            im2.setImageResource(R.drawable.ic_item_bookmark_not_black);
        } else {
            im2.setImageResource(R.drawable.ic_item_bookmark_black);
        }

        im.setOnClickListener(itemIconsClickListener);
        im2.setOnClickListener(itemIconsClickListener);

    }
}

