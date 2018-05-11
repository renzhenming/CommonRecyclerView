package com.rzm.commonrecyclerview.recyclerview.creator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by renzhenming on 2018/3/17.
 * 加载下一页辅助类
 *
 * 加载下一页有两种模式，一种是滑动到底部自动加载，一种是滑动到底部拖动后加载
 * 默认是第一种，这种模式下，只需要重写getLoadView，onLoading，onStopLoad
 * 方法即可，因为没有拖动的情况。如果是第二种模式，那么所有方法都可以重写，视需求而定
 */

public abstract class LoadViewCreator {

    /**
     * 获取加载更多的View,这个View需要设置到RecyclerView中，所以inflate的时候需要parent，在这里回调过来
     * @param context
     * @param parent
     * @return
     */
    public abstract View getLoadView(Context context, ViewGroup parent);

    /**
     * 正在下拉
     * @param currentDragHeight
     * @param dragViewHeight
     * @param currentLoadStatus
     */
    public abstract void onPull(int currentDragHeight,int dragViewHeight,int currentLoadStatus);

    /**
     * 正在加载
     */
    public abstract void onLoading();

    /**
     * 初始化脚布局显示，用于首次显示或者刷新后恢复初始设置
     */
    public abstract void onInit();

    /**
     * 加载完成,分为当前页数据加载完成和全部加载完成
     * isFullComplete：是否全部加载完成
     * 可以根据isFullComplete的状态设置脚布局的显示
     */
    public abstract void onStopLoad(boolean isFullComplete);

}
