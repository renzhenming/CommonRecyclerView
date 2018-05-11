package com.rzm.commonrecyclerview.recyclerview.multi;

/**
 * Created by renzhenming on 2018/3/15.
 */

public interface MultiTypeSupport<R> {

    //返回的布局
    int getLayoutId(R data, int position);
}
