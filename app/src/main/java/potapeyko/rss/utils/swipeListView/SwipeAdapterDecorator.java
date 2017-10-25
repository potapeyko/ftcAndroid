package potapeyko.rss.utils.swipeListView;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import potapeyko.rss.R;

/**
 * Created by Дмитрий on 21.10.2017.
 */
public class SwipeAdapterDecorator implements ListAdapter {
    private final ListAdapter baseAdapter;
    private final Context context;
    private final SwipeDetector swipeDetector;
    SwipeAdapterDecorator(ListAdapter baseAdapter, Context context) {
        this.baseAdapter = baseAdapter;
        this.context = context;
        swipeDetector = new SwipeDetector(context);
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View resView =  baseAdapter.getView(i,  view,  viewGroup);
        resView.setTranslationX(0);
        return resView;
    }
    @Override
    public boolean hasStableIds() {
        return baseAdapter.hasStableIds();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        baseAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        baseAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return baseAdapter.getCount();
    }

    @Override
    public Object getItem(int i) {
        return baseAdapter.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return baseAdapter.getItemId(i);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return baseAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return baseAdapter.isEnabled(position);
    }

    @Override
    public int getItemViewType(int position) {
        return baseAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return baseAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return baseAdapter.isEmpty();
    }
}
