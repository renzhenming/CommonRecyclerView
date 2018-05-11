package com.rzm.commonrecyclerview.recyclerview.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.rzm.commonrecyclerview.recyclerview.adpter.WrapRecyclerAdapter;


/**
 * Created by renzhenming on 2018/3/16.
 *
 * 1.添加头部和尾部
 * 2.设置多种item
 */

public class WrapRecyclerView extends RecyclerView {

    //列表数据adapter
    protected Adapter mAdapter;

    //包装类adapter
    protected WrapRecyclerAdapter mWrapRecyclerAdapter;

    //空页面
    private View mEmptyView;

    //加载中页面
    private View mLoadingView;

    //加载失败页面
    private View mFailureView;

    public WrapRecyclerView(Context context) {
        super(context);
    }

    public WrapRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {

        if (mAdapter != null){
            //防止重复设置adapter
            mAdapter.unregisterAdapterDataObserver(mDataObserver);
            mAdapter = null;
        }

        this.mAdapter = adapter;

        if (mAdapter instanceof WrapRecyclerAdapter){
            mWrapRecyclerAdapter = (WrapRecyclerAdapter)mAdapter;
        }else {
            mWrapRecyclerAdapter = new WrapRecyclerAdapter(mAdapter);
        }
        //将使用WrapRecyclerAdapter包裹一层之后的adapter设置
        super.setAdapter(mWrapRecyclerAdapter);

        //注册观察者(注意这里是列表数据的adapter设置的观察者，如果用包装类adapter设置是无效的，当列表数据adapter刷新的时候，
        // mWrapRecyclerAdapter也跟着一块刷新)
        mAdapter.registerAdapterDataObserver(mDataObserver);

        //解决LayoutManger为GridLayoutManager时添加头部宽度无法沾满一行的问题
        mWrapRecyclerAdapter.adjustSpanSize(this);

    }

    /**
     * 添加头部
     *
     * 仿照listView源码编写，listView也是内部通过adapter设置的
     * footer view 和header view都按添加顺序显示
     * @param view
     */
    public void addHeaderView(View view){
        if (mWrapRecyclerAdapter == null){
            throw new NullPointerException("adapter cannot be null");
        }

        mWrapRecyclerAdapter.addHeaderView(view);
    }

    /**
     * 添加尾部
     * @param view
     */
    public void addFooterView(View view){
        if (mWrapRecyclerAdapter == null){
            throw new NullPointerException("adapter cannot be null");
        }

        mWrapRecyclerAdapter.addFooterView(view);
    }

    /**
     * 移除头部
     * @param view
     */
    public void removeHeaderView(View view){
        if (mWrapRecyclerAdapter == null){
            throw new NullPointerException("adapter cannot be null");
        }

        mWrapRecyclerAdapter.removeHeaderView(view);
    }

    /**
     * 移除下拉刷新的头布局（默认移除第一个添加的头布局，所以要保证刷新布局就是第一个添加的）
     */
    public void removeRefreshView() {
        if (mWrapRecyclerAdapter == null){
            throw new NullPointerException("adapter cannot be null");
        }

        mWrapRecyclerAdapter.removeRefreshView();
    }

    /**
     * 移除尾部
     * @param view
     */
    public void removeFooterView(View view){
        if (mWrapRecyclerAdapter == null){
            throw new NullPointerException("adapter cannot be null");
        }

        mWrapRecyclerAdapter.removeFooterView(view);
    }

    /**
     * 移除加载下一页的脚布局（默认移除最后一个添加的脚布局，所以要保证加载下一页的脚布局就是最后一个脚布局）
     */
    public void removeLoadMoreView() {
        if (mWrapRecyclerAdapter == null){
            throw new NullPointerException("adapter cannot be null");
        }

        mWrapRecyclerAdapter.removeLoadMoreView();
    }

    /**
     * 三种加载状态都要在布局中引入
     * 加载中的页面需要在开始加载页面之前进行add,否则会被覆盖
     * 添加加载中页面
     * @param loadingView
     */
    public void addLoadingView(View loadingView){
        if (loadingView == null) return;
        mLoadingView = loadingView;
        mLoadingView.setVisibility(VISIBLE);
    }

    /**
     * 三种加载状态都要在布局中引入
     * 空页面的设置可以在任何位置，因为它的显示和隐藏是根据数据来的
     * 添加空页面
     * @param emptyView
     */
    public void addEmptyView(View emptyView){
        if (emptyView == null) return;
        this.mEmptyView = emptyView;
        mEmptyView.setVisibility(GONE);
    }

    /**
     * 三种加载状态都要在布局中引入
     * 加载失败页面在网络异常或者请求服务器异常的时候add,否则会被覆盖
     * 添加加载失败页面
     * @param failureView
     */
    public void addFailureView(View failureView){
        if (failureView == null) return;
        mFailureView = failureView;
        mFailureView.setVisibility(VISIBLE);
    }

    /**
     * 这里目前使用的是数据列表的adapter，究竟这个空页面显示的逻辑是看数据列表没有数据还是
     * 看数据列表和包装类WrapRecyclerAdapter都没有数据，根据个人需求定义
     * 目前添加的头布局或者脚布局都不计算在内
     */
    private void dataChanged() {
        if (mAdapter.getItemCount() == 0) {
            if (mEmptyView != null) {
                showView(mEmptyView,false);
            }
            if (mLoadingView != null){
                dismissView(mLoadingView);
            }
            if (mFailureView != null){
                dismissView(mFailureView);
            }
        }else{
            showView(this,false);
            if (mEmptyView != null) {
                dismissView(mEmptyView);
            }
            if (mLoadingView != null){
                dismissView(mLoadingView);
            }
            if (mFailureView != null){
                dismissView(mFailureView);
            }
        }
    }

    /**
     * 动画过渡
     * @param view
     */
    private void showView(View view,boolean isAnimate) {
        if (view == null)return;
        view.setVisibility(View.VISIBLE);
        if (isAnimate) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(300).setListener(null);
        }
    }

    /**
     * 隐藏view
     * @param view
     */
    private void dismissView(View view) {
        if (view == null) return;
        view.setVisibility(View.GONE);
    }

    /**
     * 观察者  列表Adapter更新 包裹的也需要更新不然列表的notifyItemMoved没效果
     */
    private AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (mAdapter == null)return;
            if (mWrapRecyclerAdapter != mAdapter){
                mWrapRecyclerAdapter.notifyDataSetChanged();
                dataChanged();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mAdapter == null)return;
            if (mWrapRecyclerAdapter != mAdapter){
                mWrapRecyclerAdapter.notifyItemRangeChanged(positionStart,itemCount);
                dataChanged();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mAdapter == null)return;
            if (mWrapRecyclerAdapter != mAdapter){
                mWrapRecyclerAdapter.notifyItemRangeChanged(positionStart,itemCount,payload);
                dataChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mAdapter == null)return;
            if (mWrapRecyclerAdapter != mAdapter){
                mWrapRecyclerAdapter.notifyItemRangeInserted(positionStart,itemCount);
                dataChanged();
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (mAdapter == null)return;
            if (mWrapRecyclerAdapter != mAdapter){
                mWrapRecyclerAdapter.notifyItemRangeRemoved(positionStart,itemCount);
                dataChanged();
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mAdapter == null)return;
            if (mWrapRecyclerAdapter != mAdapter){
                mWrapRecyclerAdapter.notifyItemMoved(fromPosition,toPosition);
                dataChanged();
            }
        }
    };

}
