package com.rzm.commonrecyclerview.recyclerview.creator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rzm.commonrecyclerview.R;
import com.rzm.commonrecyclerview.recyclerview.view.CommonRecyclerView;

/**
 * 默认的加载更多布局
 */
public class DefaultLoadCreator extends LoadViewCreator {
    private TextView mLoadMoreView;

    @Override
    public View getLoadView(Context context, ViewGroup parent) {
        View refreshView = LayoutInflater.from(context).inflate(R.layout.layout_default_load_footer, parent, false);
        mLoadMoreView = refreshView.findViewById(R.id.load_tv);
        return refreshView;
    }

    @Override
    public void onPull(int currentDragHeight, int loadViewHeight, int currentLoadStatus) {
        if (currentLoadStatus == CommonRecyclerView.LOAD_STATUS_LOOSEN_LOADING){
            mLoadMoreView.setText("onPull 松开加载");
        }else{
            mLoadMoreView.setText("onPull 上拉加载更多");
        }

    }

    @Override
    public void onLoading() {
        mLoadMoreView.setText("onLoading 正在加载");
    }

    @Override
    public void onInit() {
        mLoadMoreView.setText("onInit 上拉加载更多");
    }

    @Override
    public void onStopLoad(boolean isFullComplete) {
        if (isFullComplete){
            mLoadMoreView.setText("onStopLoad 没有更多数据了");
        }else{
            mLoadMoreView.setText("onStopLoad 上拉加载更多");
        }

    }
}
