package com.rzm.commonrecyclerview.recyclerview.adpter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by renzhenming on 2018/3/15.
 * 作为RecyclerView的内部类来处理头部和尾部
 */
public class WrapRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "WrapRecyclerAdapter";

    //传入的被包裹的adapter
    private final RecyclerView.Adapter mAdapter;

    /**
     * 用来存放底部和头部View的集合  比Map要高效一些
     * SparseArrays map integers to Objects. Unlike a normal array of Objects,
     * there can be gaps in the indices. It is intended to be more memory efficient
     * than using a HashMap to map Integers to Objects, both because it avoids
     * auto-boxing keys and its data structure doesn't rely on an extra entry object
     * for each mapping.
     */
    private final SparseArray<View> mHeaderViews;
    private final SparseArray<View> mFooterViews;

    // 基本的头部类型开始位置 用于viewType
    private static int BASE_ITEM_TYPE_HEADER = 10000000;
    // 基本的底部类型开始位置 用于viewType
    private static int BASE_ITEM_TYPE_FOOTER = 20000000;

    public WrapRecyclerAdapter(RecyclerView.Adapter adapter) {
        if (adapter == null){
            throw new NullPointerException("adapter can not be null");
        }
        this.mAdapter = adapter;
        mHeaderViews = new SparseArray<>();
        mFooterViews = new SparseArray<>();
    }

    /**
     * 添加头部view
     *
     * @param view
     */
    public void addHeaderView(View view) {
        // Returns an index for which {@link #valueAt} would return the specified key,
        // or a negative number if no keys map to the specified value.
        int i = mHeaderViews.indexOfValue(view);
        if (i < 0) {
            mHeaderViews.put(BASE_ITEM_TYPE_HEADER++, view);
        }
        notifyDataSetChanged();
    }

    /**
     * 判断当前position是不是header
     *
     * @param position
     * @return
     */
    public boolean isHeaderView(int position) {
        return position < mHeaderViews.size();
    }

    /**
     * 判断当前viewType是不是header
     *
     * @param viewType
     * @return
     */
    public boolean isHeaderViewType(int viewType) {
        int i = mHeaderViews.indexOfKey(viewType);
        return i >= 0;
    }

    /**
     * 获取头布局的数量
     * @return
     */
    public int getHeaderViewsCount(){
        if (mHeaderViews != null){
            return mHeaderViews.size();
        }
        return 0;
    }

    /**
     * 移除header view
     *
     * @param view 被移除的view
     */
    public void removeHeaderView(View view) {
        int i = mHeaderViews.indexOfValue(view);
        if (i < 0) return;
        mHeaderViews.remove(i);
        notifyDataSetChanged();
    }

    /**
     * 移除下拉刷新的头布局，注意，默认会移除第一个头布局，所以
     * 一定要保证下拉刷新头布局是第一个添加的
     */
    public void removeRefreshView() {
        if (getHeaderViewsCount() > 0) {
            mHeaderViews.remove(mHeaderViews.keyAt(0));
            this.notifyDataSetChanged();
        }else{
            throw new NullPointerException("you have not set refresh view");
        }
    }

    /**
     * 添加底部 view
     *
     * @param view
     */
    public void addFooterView(View view) {
        // Returns an index for which {@link #valueAt} would return the specified key,
        // or a negative number if no keys map to the specified value.
        int i = mFooterViews.indexOfValue(view);
        if (i < 0) {
            mFooterViews.put(BASE_ITEM_TYPE_FOOTER++, view);
        }
        notifyDataSetChanged();
    }

    /**
     * 判断当前position是不是footer
     *
     * @param position
     * @return
     */
    public boolean isFooterView(int position) {
        return position >= (mHeaderViews.size() + mAdapter.getItemCount());
    }

    /**
     * 获取脚布局的数量
     * @return
     */
    public int getFooterViewsCount(){
        if (mFooterViews != null){
            return mFooterViews.size();
        }
        return 0;
    }

    /**
     * 获取指定位置的脚布局
     * @return
     */
    public View getFooterView(int position){
        if (mFooterViews != null){
            return mFooterViews.get(mFooterViews.keyAt(position));
        }
        return null;
    }
    /**
     * 判断当前viewType是不是footer
     *
     * @param viewType 添加到集合时候的key值就是viewType
     * @return
     */
    public boolean isFooterViewType(int viewType) {
        int i = mFooterViews.indexOfKey(viewType);
        return i >= 0;
    }


    /**
     * 移除footer view
     *
     * @param view 被移除的view
     */
    public void removeFooterView(View view) {
        int i = mFooterViews.indexOfValue(view);
        if (i < 0) return;
        mFooterViews.remove(i);
        notifyDataSetChanged();
    }

    /**
     * 移除加载下一页的脚布局，注意，默认会移除最后一个脚布局，所以
     * 一定要保证加载下一页的脚布局设置在最后一个
     */
    public void removeLoadMoreView() {
        if (getFooterViewsCount() > 0) {
            mFooterViews.remove(mFooterViews.keyAt(getFooterViewsCount()-1));
            this.notifyDataSetChanged();
        }else{
            throw new NullPointerException("you have not set load more view");
        }
    }

    /**
     * 重写这个方法实现多type类型的view
     * 注意返回的viewType是集合中的key值，也就是viewType
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)){
            return mHeaderViews.keyAt(position);
        }
        if (isFooterView(position)){
            position = position - mHeaderViews.size() - mAdapter.getItemCount();
            return mFooterViews.keyAt(position);
        }
        position = position - mHeaderViews.size();
        return mAdapter.getItemViewType(position);
    }

    /**
     * 根据type的类型创建对应的viewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //当是头部或者尾部的时候，type就是在getItemViewType中返回的头部或尾部集合的key
        if (isHeaderViewType(viewType)){
            View header = mHeaderViews.get(viewType);
            return createHeaderFooterViewHolder(header);
        }
        if (isFooterViewType(viewType)){
            View footer = mFooterViews.get(viewType);
            return createHeaderFooterViewHolder(footer);
        }
        return mAdapter.onCreateViewHolder(parent,viewType);
    }

    /**
     * 创建头部或者尾部的viewHolder
     * @param view
     * @return
     */
    private RecyclerView.ViewHolder createHeaderFooterViewHolder(View view) {
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderView(position) || isFooterView(position))return;
        position = position - mHeaderViews.size();
        mAdapter.onBindViewHolder(holder,position);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount()+mHeaderViews.size()+mFooterViews.size();
    }

    /**
     * 解决GridLayoutManager添加头部和底部不占用一行的问题
     */
    public void adjustSpanSize(RecyclerView recycler) {
        RecyclerView.LayoutManager manager = recycler.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager layoutManager = (GridLayoutManager) manager;
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override public int getSpanSize(int position) {
                    boolean isHeaderOrFooter = isHeaderView(position) || isFooterView(position);
                    return isHeaderOrFooter ? layoutManager.getSpanCount() : 1;
                }
            });
        }
    }

}
