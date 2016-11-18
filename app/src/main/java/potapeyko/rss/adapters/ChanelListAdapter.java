package potapeyko.rss.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import potapeyko.rss.R;
import potapeyko.rss.models.Channel;

import java.util.ArrayList;

public class ChanelListAdapter extends BaseAdapter {
      private final ArrayList<Channel> chanels;
    private final LayoutInflater layoutInflater;

    public ChanelListAdapter(Context context, ArrayList<Channel> news) {
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
        final Channel chanel = (Channel) getItem(position);
        title.setText(chanel.getTitle());
        return contentView;
    }
}
