package potapeyko.rss.utils.swipeListView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Дмитрий on 19.10.2017.
 */


public class SwipedListView extends ListView {

    private OnItemSwipeListener onItemSwipeListener;
    private OnItemClickListener onItemClickListener;
    private SwipeDetector swipeDetector = new SwipeDetector(getContext());

    public SwipedListView(Context context) {
        super(context);
    }

    public SwipedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(swipeDetector);
    }

    public SwipedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void setAdapter(ListAdapter adapter) {
        SwipeAdapterDecorator adapterDecorator = new SwipeAdapterDecorator(adapter,this.getContext());
        super.setAdapter(adapterDecorator);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
        super.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!swipeDetector.swipeDetected()) {
                    onItemClickListener.onItemClick(adapterView, view, i, l);
                }
            }
        });
    }

    public void setOnItemSwipeListener(OnItemSwipeListener onItemSwipeListener) {
        this.onItemSwipeListener = onItemSwipeListener;
        swipeDetector.setOnItemSwipeListener(onItemSwipeListener);
    }


    public Animation getLeftRightAnimation(float fromX, float toX) {
        Animation animation = new TranslateAnimation(fromX, toX, 0, 0);
        animation.setStartOffset(100);
        animation.setDuration(800);
        //animation.setAnimationListener(this);
        animation.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                android.R.anim.anticipate_overshoot_interpolator));
        return animation;
    }

    public static interface OnItemSwipeListener {
        boolean onItemSwipe(AdapterView<?> parent, View swipedView, int swipedItemPosition,  SwipeDetector.Action action);
    }
}
