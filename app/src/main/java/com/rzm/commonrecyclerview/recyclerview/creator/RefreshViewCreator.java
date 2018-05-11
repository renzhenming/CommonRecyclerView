package com.rzm.commonrecyclerview.recyclerview.creator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by renzhenming on 2018/3/16.
 * 下拉刷新辅助类
 */
public abstract class RefreshViewCreator {

    /**
     * 获取下拉刷新的view，实现可定制
     * @param context
     * @param parent
     * @return
     */
    public abstract View getRefreshView(Context context, ViewGroup parent);


    /**
     * 正在下拉
     * @param currentDragHeight    当前下拉的高度
     * @param refreshViewHeight    下拉刷新view的高度
     * @param currentRefreshState  当前的刷新状态
     */
    public abstract void onPull(int currentDragHeight,int refreshViewHeight,int currentRefreshState);


    /**
     * 正在刷新
     */
    public abstract void onRefreshing();

    /**
     * 刷新完成
     */
    public abstract void onStopRefresh();
}
