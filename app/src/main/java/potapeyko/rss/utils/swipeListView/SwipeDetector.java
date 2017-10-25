package potapeyko.rss.utils.swipeListView;

/**
 * Created by Дмитрий on 21.10.2017.
 */

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Класс для обнаружения свайпа на View
 */
public class SwipeDetector implements View.OnTouchListener {
    Context context;
    SwipedListView mListView;
    View mDownView;
    private int downPosition;
    private SwipedListView.OnItemSwipeListener swipeListener=null;




    public static enum Action {
        LR, // Слева направо
        RL, // Справа налево
        TB, // Сверху вниз
        BT, // Снизу вверх
        None // не обнаружено действий
    }

    private static final int HORIZONTAL_MIN_DISTANCE = 100; // Минимальное расстояние для свайпа по горизонтали
    private static final int VERTICAL_MIN_DISTANCE = 80; // Минимальное расстояние для свайпа по вертикали

    public float getDownX() {
        return downX;
    }

    private float downX;
    private float downY;

    public float getUpX() {
        return upX;
    }

    private float upX;
    private float upY; // Координаты
    private float deltaX, deltaY;
    private Action mSwipeDetected = Action.None; // Последнее дейтсвие
    View child = null;

    public SwipeDetector(Context context) {
        super();
        this.context = context;

    }

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }

    public Action getAction() {
        return mSwipeDetected;
    }

    void setOnItemSwipeListener(SwipedListView.OnItemSwipeListener onItemSwipeListener){
        this.swipeListener = onItemSwipeListener;
    }
    /**
     * Определение свайпа
     */
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (mListView == null) {
            mListView = (SwipedListView) view;
        }



        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mSwipeDetected = Action.None;
                // TODO: ensure this is a finger, and set a flag
                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = mListView.getChildCount();
                int[] listViewCoords = new int[2];
                mListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = mListView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child;
                        break;
                    }
                }

                if (mDownView != null) {
                    downX = motionEvent.getRawX();
                    downY = motionEvent.getRawY();
                    downPosition = mListView.getPositionForView(mDownView);
                }

                //всегда
                break;
            }

            case MotionEvent.ACTION_CANCEL & MotionEvent.ACTION_UP: {
                if (mDownView != null) {
                    boolean result = false;//если не свайп влево/право
                    // cancel
                    float deltaX = motionEvent.getRawX() - downX;

                    if(Math.abs(deltaX)>mDownView.getWidth()/2 && swipeListener!=null ){
                        result = true;
                        int sign = (int) (Math.abs(deltaX)/deltaX);
                        final View animatedView = mDownView;
                        Log.e("mDownView","if "+mDownView.toString());
                        animatedView.animate()
                                .translationX(animatedView.getWidth()*sign)
                                .alpha(1)
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {
                                    }
                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        swipeListener.onItemSwipe(mListView,animatedView,downPosition,mSwipeDetected);
                                    }
                                    @Override
                                    public void onAnimationCancel(Animator animator) {
                                    }
                                    @Override
                                    public void onAnimationRepeat(Animator animator) {
                                    }
                                })
                                .setDuration(500);
                    }
                    else
                        {
                        Log.e("mDownView","else "+mDownView.toString());
                        mDownView.animate()
                                .translationX(0)
                                .alpha(1)
                                .setListener(null)
                                .setDuration(500);
                    }
                    mDownView = null;
                    return result;
                }
                return false;
            }

            case MotionEvent.ACTION_MOVE: {
                float deltaX = motionEvent.getRawX() - downX;
                if (Math.abs(deltaX) > HORIZONTAL_MIN_DISTANCE) {
                    if (downPosition != ListView.INVALID_POSITION && mDownView!=null && mListView!=null) {
                        // Слева направо
                        if (deltaX < 0) {
                              mSwipeDetected = Action.LR;
                            mDownView.setTranslationX(deltaX);

                        }
                        // Справа налево
                        if (deltaX > 0) {
                            mSwipeDetected = Action.RL;
                            mDownView.setTranslationX(deltaX);

                        }
                    }
                }
                break;
            }
        }
       return false;
    }
}
