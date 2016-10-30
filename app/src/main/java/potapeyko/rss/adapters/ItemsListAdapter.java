package potapeyko.rss.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import potapeyko.rss.R;

import potapeyko.rss.activities.FullNewsActivity;
import potapeyko.rss.activities.MainActivityMy;
import potapeyko.rss.models.News;


import java.util.ArrayList;

public class ItemsListAdapter extends BaseAdapter {

    private ArrayList<News> news;
    private Context context;
    private LayoutInflater layoutInflater;

    public ItemsListAdapter(Context context, ArrayList<News> news) {
        this.news = news;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public Object getItem(int i) {
        return news.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View contentView = view;
        if (contentView == null) {
            contentView = layoutInflater.inflate(R.layout.news_list_item, viewGroup, false);
        }
        TextView title = (TextView) contentView.findViewById(R.id.news_list_title);
        ImageView image = (ImageView) contentView.findViewById(R.id.news_list_image);
        final News news = (News) getItem(position);
        title.setText(news.getTitle());

//        image.setImageResource(news.getImageResource()); // TODO: 28.10.2016
//        contentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(view.getContext(),FullNewsActivity.class);//что сюда пихать, чтоб работало????
//                context.startActivity(intent);
//
////                Uri addres = Uri.parse(news.getFullNewsUri());
////                Intent uriOpen = new Intent(Intent.ACTION_VIEW, addres);
////                view.getContext().startActivity(uriOpen);
//            }
//        });
        return contentView;
    }
}
