package com.rzm.commonrecyclerview.recyclerview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.rzm.commonrecyclerview.recyclerview.creator.RefreshViewCreator;


/**
 * Created by renzhenming on 2018/3/16.
 * 1.添加头部和尾部
 * 2.设置多种item
 * 3.下拉刷新
 */

public class RefreshRecyclerView extends WrapRecyclerView {
    private RefreshViewCreator mRefreshViewCreator;

    //刷新头布局
    private View mRefreshView;

    //头部具高度
    private int mRefreshViewHeight = 0;

    //手指按下的位置y坐标
    private float mStartDonwY;

    //拖拽阻力系数，为了达到更强的拖拽视觉效果而设置,越小拖动越困难
    public float mDragResistance = 0.3f;

    // 默认状态
    public int REFRESH_STATUS_NORMAL = 0x0011;

    // 下拉刷新状态,当前刷新view还没有完全拉出来
    public int REFRESH_STATUS_PULL_DOWN = 0x0022;

    // 松开刷新状态，当前刷新view已经完全拉出来并且超过了view的高度
    public int REFRESH_STATUS_LOOSEN_REFRESHING = 0x0033;

    // 正在刷新状态，当前view被释放，高度等于view高度
    public int REFRESH_STATUS_REFRESHING = 0x0044;

    //当前状态
    private int mCurrentRefreshStatus = REFRESH_STATUS_NORMAL;

    //是否正在拖动
    private boolean mCurrentDrag;

    //下拉刷新是否可用
    private boolean mPullRefreshEnabled;

    public RefreshRecyclerView(Context context) {
        super(context);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        addRefreshView();
    }

    /**
     * 传入的RefreshViewCreator中包含有设置的刷新布局，通过添加不同的creator，
     * 实现对刷新布局的插拔式替换
     * RefreshViewCreator必须在所有的头部局之前设置才能有效，没有做位置的设定，默认按添加顺序显示
     * @param creator
     */
    public void addRefreshViewCreator(RefreshViewCreator creator){
        this.mRefreshViewCreator = creator;
        addRefreshView();
    }

    /**
     * 最终的刷新头部局是从RefreshViewCreator中获取到，然后调用父类WrapRecyclerView的添加头部的方法
     * 设置刷新布局
     */
    private void addRefreshView() {
        Adapter adapter = getAdapter();
        if (adapter != null && mRefreshViewCreator!= null){
            View refreshView = mRefreshViewCreator.getRefreshView(getContext(), this);
            if (refreshView != null){
                addHeaderView(refreshView);
                this.mRefreshView = refreshView;
            }
        }
    }

    /**
     * 添加refreshView之后,这个view默认是作为一个view添加的，所以需要默认设置这个view隐藏，
     * 按照view的测量高度，缩进去相同的高度，只在下拉的时候显示
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed){
            if (mRefreshView != null && mRefreshViewHeight <= 0){
                mRefreshViewHeight = mRefreshView.getMeasuredHeight();
                if (mRefreshViewHeight > 0){
                    // 隐藏头部刷新的View  marginTop  多留出1px防止无法判断是不是滚动到头部问题
                    setRefreshViewMarginTop(-mRefreshViewHeight+1);
                    //setRefreshViewMarginTop(-mRefreshViewHeight);
                }
            }
        }
    }

    /**
     * 设置顶部刷新view的marginTop
     * @param marginTop
     */
    private void setRefreshViewMarginTop(int marginTop) {
        if (mRefreshView == null) return;

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mRefreshView.getLayoutParams();

        if (marginTop < -mRefreshViewHeight+1){
            marginTop = -mRefreshViewHeight+1;
        }
        /*if (marginTop < -mRefreshViewHeight){
            marginTop = -mRefreshViewHeight;
        }*/
        layoutParams.topMargin = marginTop;

        mRefreshView.setLayoutParams(layoutParams);
    }

    /**
     * 在dispatch中处理按下和抬起的事件，因为recyclerView如果已经设置了条目点击事件，那么在onTouchEvent中，按下的事件
     * 不会被处理
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mPullRefreshEnabled) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //相对屏幕的位置
                    mStartDonwY = ev.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    if (mCurrentDrag) {
                        restoreRefreshView();
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 重置当前刷新状态状态
     */
    private void restoreRefreshView() {
        if (mRefreshView == null) return;
        //松开拖拽之后，获取到当前recyclerView距离顶部的margin
        int currentTopMargin = ((ViewGroup.MarginLayoutParams) (mRefreshView.getLayoutParams())).topMargin;

        //这个距离是将刷新view完全隐藏的距离
        //int finalTopMargin = -mRefreshViewHeight+1;
        int finalTopMargin = -mRefreshViewHeight;

        if (mCurrentRefreshStatus == REFRESH_STATUS_LOOSEN_REFRESHING){
            //这个状态下，松开之后的finalTopMargin应该是0，也就是当刷新view的高度正好完全出现的高度
            finalTopMargin = 0;
            //设置状态为正在刷新
            mCurrentRefreshStatus = REFRESH_STATUS_REFRESHING;

            //这个回调用于处理刷新布局的显示
            if(mRefreshViewCreator != null){
                mRefreshViewCreator.onRefreshing();
            }

            //这个回调用于刷新数据
            if (mListener != null){
                mListener.onRefresh();
            }

        }

        //得到了起始位置和最终位置，使用值动画平滑移动view到最终位置
        ValueAnimator animator = ValueAnimator.ofFloat(currentTopMargin, finalTopMargin);
        //动画的时长不能写死，应该根据初始位置和最终位置间的距离动态设置，以保证动画的视觉效果
        int duration = Math.abs(currentTopMargin-finalTopMargin);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                setRefreshViewMarginTop((int) animatedValue);
            }
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        mCurrentDrag = false;
    }

    /**
     * 处理手指按下的move事件
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mPullRefreshEnabled) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    // 如果是在最顶部才处理，否则不需要处理，如果正在刷新中，也不再处理
                    if (canScrollUp() || mCurrentRefreshStatus == REFRESH_STATUS_REFRESHING) {
                        // 如果没有到达最顶端，也就是说还可以向上滚动就什么都不处理
                        return super.onTouchEvent(e);
                    }

                    // 解决下拉刷新自动滚动问题
                    if (mCurrentDrag) {
                        scrollToPosition(0);
                    }

                    //获取手指触摸拖拽的距离
                    int dragDistance = (int) (e.getRawY() - mStartDonwY);

                    //设置拖拽阻力系数，即实际刷新view的移动距离是拖拽系数和拖拽距离的乘积
                    dragDistance = (int) (dragDistance * mDragResistance);
                    if (dragDistance > 0) {
                        //拖拽过程中不断的设置refresh view的marginTop值
                        int marginTop = dragDistance - mRefreshViewHeight;

                        setRefreshViewMarginTop(marginTop);

                        //更新当前状态
                        updateRefreshStatus(marginTop);

                        mCurrentDrag = true;

                        return false;
                    }
                    break;
            }
        }
        return super.onTouchEvent(e);
    }

    /**
     * 更新下拉过程中的状态，并回调
     * @param marginTop
     */
    private void updateRefreshStatus(int marginTop) {
        //当前是没有进行下拉
        if (marginTop <= -mRefreshViewHeight){
            mCurrentRefreshStatus = REFRESH_STATUS_NORMAL;
        }else if (marginTop < 0){
            //marginTop小于0大于-mRefreshViewHeight的过程中，是view被不断拉出的过程
            mCurrentRefreshStatus = REFRESH_STATUS_PULL_DOWN;
        }else{
            mCurrentRefreshStatus = REFRESH_STATUS_LOOSEN_REFRESHING;
        }
        //这是下拉过程中三个状态，正在刷新状态将在onMoveUp中回调
        if (mRefreshViewCreator != null){
            mRefreshViewCreator.onPull(marginTop,mRefreshViewHeight,mCurrentRefreshStatus);
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     * 判断是不是滚动到了最顶部，这个是从SwipeRefreshLayout里面copy过来的源代码
     *
     * 如下方法已过时:
     *
     * if (android.os.Build.VERSION.SDK_INT < 14) {
     *    return ViewCompat.canScrollVertically(this, -1) || this.getScrollY() > 0;
     * } else {
     *    return ViewCompat.canScrollVertically(this, -1);
     * }
     *
     * 方法 canScrollVertically(int direction):
     *
     * Check if this view can be scrolled vertically in a certain direction.
     * params Negative to check scrolling up, positive to check scrolling down.
     * @return true if this view can be scrolled in the specified direction, false otherwise.
     */
    private boolean canScrollUp() {
        //int i = computeVerticalScrollOffset();
        //return i != 0;
        //System.out.println("computeVerticalScrollOffset:"+i);
        //RecyclerView.canScrollVertically(1)的值表示是否能向上滚动，false表示已经滚动到底部
        //RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return canScrollVertically(-1) || this.getScrollY() > 0;
        } else {
            return canScrollVertically(-1);
        }

    }

    /**
     * 设置下拉刷新是否可用
     * @param enabled
     */
    public void setRefreshEnabled(boolean enabled) {
        if(mWrapRecyclerAdapter == null){
            throw new NullPointerException("you have not set adapter right now");
        }
        mPullRefreshEnabled = enabled;
        if (!enabled) {
            removeRefreshView();
        }
    }

    /**
     * 停止刷新,刷新完成后调用此方法
     */
    public void stopRefresh() {
         if (!(this instanceof RefreshRecyclerView)){
             throw new IllegalStateException("you should use onRefreshComplete to finish refresh in CommonRecyclerView");
         }
         mCurrentRefreshStatus = REFRESH_STATUS_NORMAL;
         restoreRefreshView();
         if (mRefreshViewCreator != null) {
             mRefreshViewCreator.onStopRefresh();
         }
     }


    // 处理刷新回调监听
    private OnRefreshListener mListener;
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }
    public interface OnRefreshListener {
        void onRefresh();
    }

}
