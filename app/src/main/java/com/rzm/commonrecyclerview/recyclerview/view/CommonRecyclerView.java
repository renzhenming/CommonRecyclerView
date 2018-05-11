package com.rzm.commonrecyclerview.recyclerview.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.rzm.commonrecyclerview.recyclerview.creator.LoadViewCreator;


/**
 * Created by renzhenming on 2018/3/17.
 * 1.添加头部和尾部
 * 2.设置多种item
 * 3.下拉刷新
 * 4.上拉加载下一页
 */

public class CommonRecyclerView extends RefreshRecyclerView {
    //上拉加载更多辅助类
    private LoadViewCreator mLoadViewCreator;

    //加载更多的view布局
    private View mLoadView;

    private float mDownY;

    //当前RecyclerView的LayoutManager类型
    protected LayoutManagerType layoutManagerType;

    //加载下一页的方式，默认滑动到底部自动加载
    private LoadMoreType mLoadMoreType = LoadMoreType.Auto;

    //加载更多布局的高度
    private int mLoadViewHeight;

    //当前的状态
    private int mCurrentLoadStatus;

    // 默认状态
    public int LOAD_STATUS_NORMAL = 0x0055;

    // 上拉加载更多状态
    public static int LOAD_STATUS_PULL_DOWN = 0x0066;

    // 松开加载更多状态
    public static int LOAD_STATUS_LOOSEN_LOADING = 0x0077;

    // 正在加载更多状态
    public int LOAD_STATUS_LOADING = 0x0088;

    //当前是否在拖拽
    private boolean mCurrentDrag = false;

    //当前最后一个可见条目的位置
    private int mLastVisibleItemPosition;

    //当LayoutManager是StaggeredLayoutManager的时候，最后一行可见条目数组
    private int[] mLastPositions;

    //是否全部数据都已经加载完成，默认为false
    private boolean mComplete = false;

    //是否可以加载下一页，脚布局是否存在
    private boolean mLoadMoreEnabled;

    public CommonRecyclerView(Context context) {
        super(context);
    }

    public CommonRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 利用辅助类接口把加载更多的脚布局样式设置提供出去
     * LoadViewCreator必须在所有的脚布局之后设置才能有效，没有做位置的设定，默认按添加顺序显示
     *
     * @param creator
     */
    public void addLoadViewCreator(LoadViewCreator creator) {
        this.mLoadViewCreator = creator;
        addLoadView();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        addLoadView();
    }

    /**
     * 添加加载更多的view
     */
    private void addLoadView() {
        Adapter adapter = getAdapter();
        if (adapter != null && mLoadViewCreator != null) {
            View loadView = mLoadViewCreator.getLoadView(getContext(), this);
            if (loadView != null) {
                addFooterView(loadView);
                this.mLoadView = loadView;
                mLoadViewCreator.onInit();
            }
        }
    }

    public enum LayoutManagerType {
        LinearLayout,
        StaggeredGridLayout,
        GridLayout
    }

    /**
     * 设置加载下一页的方式
     * Auto滑动到底部自动加载
     * Pull滑动到底部手动上拉加载
     */
    public enum LoadMoreType {
        Auto, Pull
    }

    /**
     * 设置加载下一页的方式，默认滑动到底部自动加载
     * @param type
     */
    public void setLoadMoreType(LoadMoreType type) {
        this.mLoadMoreType = type;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        //如果不是自动加载下一页，下边的逻辑目前由于不涉及其他功能就不再走了
        if (mLoadMoreType != LoadMoreType.Auto || !mLoadMoreEnabled) return;

        RecyclerView.LayoutManager layoutManager = getLayoutManager();

        if (layoutManagerType == null) {
            if (layoutManager instanceof LinearLayoutManager) {
                layoutManagerType = LayoutManagerType.LinearLayout;
            } else if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LayoutManagerType.GridLayout;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManagerType = LayoutManagerType.StaggeredGridLayout;
            } else {
                throw new RuntimeException("LayoutManager type unknown");
            }
        }

        //根据LayoutManager类型获取最后一个可见条目位置
        switch (layoutManagerType) {
            case LinearLayout:
                mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case GridLayout:
                mLastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case StaggeredGridLayout:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (mLastPositions == null) {
                    mLastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(mLastPositions);
                mLastVisibleItemPosition = findMax(mLastPositions);
                break;
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        //如果不是自动加载下一页，下边的逻辑目前由于不涉及其他功能就不再走了
        if (mLoadMoreType != LoadMoreType.Auto || !mLoadMoreEnabled) return;

        if (mListener != null && state == RecyclerView.SCROLL_STATE_IDLE) {
            RecyclerView.LayoutManager layoutManager = getLayoutManager();

            //获取当前可见的item个数
            int visibleItemCount = layoutManager.getChildCount();

            //获取setAdapter之后所有的item个数包括未显示到屏幕上的
            int totalItemCount = layoutManager.getItemCount();

            //当滑动到底部的时候，仍需要满足两个条件才能进行下一页的加载，1.数据还没有全部加载完成 2.当前状态不是正在加载
            if (visibleItemCount > 0 && mLastVisibleItemPosition >= totalItemCount - 1
                    && totalItemCount > visibleItemCount && !mComplete
                    && mCurrentLoadStatus != LOAD_STATUS_LOADING) {

                mCurrentLoadStatus = LOAD_STATUS_LOADING;

                if (mLoadViewCreator != null) {
                    mLoadViewCreator.onLoading();
                }

                if (mListener != null) {
                    mListener.onLoad();
                }
            }
        }
    }

    /**
     * 重置当前加载更多状态
     */
    private void restoreLoadView() {
        //获取当前位置loadView的marginBottom值
        if (mLoadView == null) return;

        int currentBottomMargin = ((ViewGroup.MarginLayoutParams) (mLoadView.getLayoutParams())).bottomMargin;

        //最终要滑动到marginBottom为0的位置
        int finalBottomMargin = 0;

        if (mCurrentLoadStatus == LOAD_STATUS_LOOSEN_LOADING && !mComplete) {
            mCurrentLoadStatus = LOAD_STATUS_LOADING;

            if (mLoadViewCreator != null) {
                mLoadViewCreator.onLoading();
            }

            if (mListener != null) {
                mListener.onLoad();
            }
        }

        //distance用作动画执行的时间
        int distance = currentBottomMargin - finalBottomMargin;

        // 回弹到指定位置
        ValueAnimator animator = ObjectAnimator.ofFloat(currentBottomMargin, finalBottomMargin).setDuration(distance);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float currentTopMargin = (float) animation.getAnimatedValue();
                setLoadViewMarginBottom((int) currentTopMargin);

            }
        });

        animator.start();

        mCurrentDrag = false;
    }


    /**
     * 在dispatch中处理按下和抬起的事件，因为recyclerView如果已经设置了条目点击事件，那么在onTouchEvent中，按下的事件
     * 不会被处理
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //自动加载下一页状态下不需要处理触摸事件，因为就目前来说，触摸事件只跟下一页加载方式有关
        if (mLoadMoreType != LoadMoreType.Auto && mLoadMoreEnabled) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentDrag) {
                    restoreLoadView();
                }
                break;
        }
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //如果不是自动加载下一页，下边的逻辑目前由于不涉及其他功能就不再走了
        if (mLoadMoreType != LoadMoreType.Auto && mLoadMoreEnabled) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    // 如果是在最底部才处理，否则不需要处理
                    if (canScrollDown() || mCurrentLoadStatus == LOAD_STATUS_LOADING) {
                        // 如果没有到达最底端，也就是说还可以向下滚动就什么都不处理
                        return super.onTouchEvent(e);
                    }

                    if (mLoadView != null) {
                        mLoadViewHeight = mLoadView.getMeasuredHeight();
                    }

                    // 解决上拉加载更多自动滚动问题
                    if (mCurrentDrag) {
                        scrollToPosition(getAdapter().getItemCount() - 1);
                    }

                    //获取手指触摸拖拽的距离
                    int distanceY = (int) (e.getRawY() - mDownY);
                    distanceY = (int) (distanceY * mDragResistance);

                    // 如果是已经到达底部，并且不断的拉动，那么不断的改变LoadView的marginBottom的值

                    //注意上拉得到的distanceY是负值
                    if (distanceY < 0) {

                        //通过设置loadView的marginBottom值不断的移动loadView的位置
                        setLoadViewMarginBottom(-distanceY);

                        //更新状态
                        updateLoadStatus(-distanceY);

                        //当前是否在拖拽
                        mCurrentDrag = true;

                        return true;
                    }
                    break;
            }
        }
        return super.onTouchEvent(e);
    }

    /**
     * 回调状态
     *
     * @param dragHeight
     */
    private void updateLoadStatus(int dragHeight) {
        //如果加载已经完成，那么最后的拉动不需要更新状态
        if (mComplete) return;
        if (dragHeight <= 0) {
            mCurrentLoadStatus = LOAD_STATUS_NORMAL;
        } else if (dragHeight < mLoadViewHeight) {
            //当拖动距离小于loadView的高度的时候
            mCurrentLoadStatus = LOAD_STATUS_PULL_DOWN;
        } else {
            mCurrentLoadStatus = LOAD_STATUS_LOOSEN_LOADING;
        }

        if (mLoadViewCreator != null) {
            mLoadViewCreator.onPull(dragHeight, mLoadViewHeight, mCurrentLoadStatus);
        }
    }

    /**
     * 设置loadView的marginBottom值
     *
     * @param marginBottom
     */
    private void setLoadViewMarginBottom(int marginBottom) {
        if (mLoadView == null) return;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mLoadView.getLayoutParams();
        if (marginBottom < 0) {
            marginBottom = 0;
        }
        layoutParams.bottomMargin = marginBottom;
        mLoadView.setLayoutParams(layoutParams);
    }

    /**
     * @return true if this view can be scrolled in the specified direction, false otherwise.
     */
    private boolean canScrollDown() {
        return canScrollVertically(1);
    }

    /**
     * 到底加载是否可用
     */
    public void setLoadMoreEnabled(boolean enabled) {
        if(mWrapRecyclerAdapter == null){
            throw new NullPointerException("you have not set adapter right now");
        }
        mLoadMoreEnabled = enabled;
        if (!enabled) {
            removeLoadMoreView();
        }
    }

    /**
     * 获取头布局总个数
     * @return
     */
    public int getHeaderViewsCount(){
        if(mWrapRecyclerAdapter == null){
            throw new NullPointerException("you have not set adapter right now");
        }
        return mWrapRecyclerAdapter.getHeaderViewsCount();
    }
    /**
     * 获取脚布局总个数
     * @return
     */
    public int getFooterViewsCount(){
        if(mWrapRecyclerAdapter == null){
            throw new NullPointerException("you have not set adapter right now");
        }
        return mWrapRecyclerAdapter.getFooterViewsCount();
    }
    /**
     * 全部数据加载完成的时候调用这个方法，
     */
    public void onLoadComplete(boolean isFullComplete) {
        setLoadComplete(isFullComplete);
        mCurrentLoadStatus = LOAD_STATUS_NORMAL;
        restoreLoadView();
        if (mLoadViewCreator != null) {
            mLoadViewCreator.onStopLoad(isFullComplete);
        }
    }

    /**
     * 是否全部数据加载完成，这个方法不提供外界调用，只用于stopLoad，onRefreshComplete
     *
     * @param complete
     */
    protected void setLoadComplete(boolean complete) {
        this.mComplete = complete;
    }

    /**
     * 刷新的时候调用
     */
    public void onRefreshComplete() {
        //刷新之后重新设置mLoadComplete为false
        setLoadComplete(false);
        //重新初始化加载更多布局
        if (mLoadViewCreator != null)
            mLoadViewCreator.onInit();
        stopRefresh();
    }


    // 处理加载更多回调监听
    private OnLoadMoreListener mListener;

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mListener = listener;
    }

    public interface OnLoadMoreListener {
        void onLoad();
    }
}


























