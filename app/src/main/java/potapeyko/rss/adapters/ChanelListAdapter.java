package potapeyko.rss.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.models.Chanel;

import java.util.ArrayList;

public class ChanelListAdapter extends BaseAdapter {
      private final ArrayList<Chanel> chanels;
    private final LayoutInflater layoutInflater;

    public ChanelListAdapter(Context context, ArrayList<Chanel> news) {
        this.chanels = news;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return chanels.size();
    }

    @Override
    public Object getItem(int i) {
        return chanels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return chanels.get(i).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View contentView = view;
        if (contentView == null) {
            contentView = layoutInflater.inflate(R.layout.news_list_item, viewGroup, false);
        }
        TextView title = (TextView) contentView.findViewById(R.id.news_list_title);
        ImageView image = (ImageView) contentView.findViewById(R.id.news_list_image);
        final Chanel chanel = (Chanel) getItem(position);
        title.setText(chanel.getTitle());
        //image.setImageResource(news.getImageResource()); // TODO: 28.10.2016
        return contentView;
    }
}
