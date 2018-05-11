package com.rzm.commonrecyclerview;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


import com.rzm.commonrecyclerview.recyclerview.adpter.CommonRecyclerAdpater;
import com.rzm.commonrecyclerview.recyclerview.creator.DefaultLoadCreator;
import com.rzm.commonrecyclerview.recyclerview.creator.DefaultRefreshCreator;
import com.rzm.commonrecyclerview.recyclerview.multi.MultiTypeSupport;
import com.rzm.commonrecyclerview.recyclerview.view.CommonRecyclerView;
import com.rzm.commonrecyclerview.recyclerview.view.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TestMyRecyclerViewActivity extends AppCompatActivity {

    private CommonRecyclerView mRecyclerView;

    public static final int SIZE = 20;

    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_my_recyler_view);

        mRecyclerView = (CommonRecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final ArrayList<String> mList = new ArrayList<>();

        final MyAdapter myAdapter = new MyAdapter(getApplicationContext(),mList, new MultiTypeSupport<String>() {
            @Override
            public int getLayoutId(String item, int position) {
                if (position %2 == 1){
                    return R.layout.item_center;
                }
                return R.layout.item_normal;
            }
        });
        myAdapter.setOnItemClickListener(new CommonRecyclerAdpater.OnItemClickListener<String>() {
            @Override
            public void onItemClick(String o, int position) {
                Toast.makeText(getApplicationContext(),o,Toast.LENGTH_SHORT).show();
            }
        });
        myAdapter.setOnLongClickListener(new CommonRecyclerAdpater.OnLongClickListener<String>() {
            @Override
            public void onItemLongClick(String o, int position) {
                Toast.makeText(getApplicationContext(),o,Toast.LENGTH_SHORT).show();
            }
        });

        myAdapter.setOnItemChildClickListener(R.id.text,new CommonRecyclerAdpater.OnItemChildClickListener<String>() {
            @Override
            public void onItemChildClick(String o, int position) {
                Toast.makeText(getApplicationContext(),"text,position="+position,Toast.LENGTH_SHORT).show();
            }
        });
        myAdapter.setOnItemChildClickListener(R.id.image_adapter,new CommonRecyclerAdpater.OnItemChildClickListener<String>() {
            @Override
            public void onItemChildClick(String o, int position) {
                Toast.makeText(getApplicationContext(),"image,position="+position,Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(myAdapter);

        /**
         * RefreshViewCreator必须在所有的头部局之前设置才能有效，没有做位置的设定，默认按添加顺序显示
         */
        mRecyclerView.addRefreshViewCreator(new DefaultRefreshCreator());


        /**
         * footer view 和header view都按添加顺序显示
         */
        View header = LayoutInflater.from(this).inflate(R.layout.item_head, mRecyclerView,false);
        mRecyclerView.addHeaderView(header);

        View footer = LayoutInflater.from(this).inflate(R.layout.item_footer, mRecyclerView,false);
        mRecyclerView.addFooterView(footer);

        /**
         * LoadViewCreator必须在所有的脚布局之后设置才能有效，没有做位置的设定，默认按添加顺序显示
         */
        mRecyclerView.addLoadViewCreator(new DefaultLoadCreator());

        /**
         * 三种加载状态都要在布局中引入
         */
        View view = findViewById(R.id.view_empty);
        View loadingView = findViewById(R.id.view_loading);
        final View loadingFailedView = findViewById(R.id.view_load_failed);

        /**
         * 空页面的设置可以在任何位置，因为它的显示和隐藏是根据数据来的
         */
        mRecyclerView.addEmptyView(view);
        /**
         * 设置加载下一页的方式为拖动加载
         */
        mRecyclerView.setLoadMoreType(CommonRecyclerView.LoadMoreType.Pull);
        Log.i("TAG","头布局个数："+mRecyclerView.getHeaderViewsCount()+",脚布局个数："+mRecyclerView.getFooterViewsCount());
        mRecyclerView.setRefreshEnabled(true);
        mRecyclerView.setLoadMoreEnabled(true);

        Log.i("TAG","头布局个数2："+mRecyclerView.getHeaderViewsCount()+",脚布局个数："+mRecyclerView.getFooterViewsCount());
        /**
         * 加载中的页面需要在开始加载页面之前进行add,否则会被覆盖
         */
        mRecyclerView.addLoadingView(loadingView);

        if (!NetWorkUtils.isNetWorkConn(getApplicationContext())){
            /**
             * 加载失败页面在网络异常或者请求服务器异常的时候add,否则会被覆盖
             */
            mRecyclerView.addFailureView(loadingFailedView);
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < SIZE; i++) {
                    mList.add("测试数据"+i);
                }
                myAdapter.notifyDataSetChanged();
            }
        },1000);

        mRecyclerView.setOnRefreshListener(new RefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentPage = 0;
                        mRecyclerView.onRefreshComplete();
                        mList.clear();
                        for (int i = 0; i < SIZE; i++) {
                            mList.add("测试数据"+i);
                        }
                        myAdapter.notifyDataSetChanged();
                    }
                },1500);
            }
        });

        mRecyclerView.setOnLoadMoreListener(new CommonRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoad() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.onLoadComplete(false);
                        currentPage ++;
                        for (int i = currentPage*SIZE; i < currentPage*SIZE+SIZE; i++) {
                            mList.add("测试数据"+i);
                        }
                        myAdapter.notifyDataSetChanged();
                        if (currentPage == 3)
                            mRecyclerView.onLoadComplete(true);
                    }
                },1000);
            }
        });


        CommonItemTouchHelper touchHelper = new CommonItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 获取触摸响应的方向 包含两个 1.拖动dragFlags 2.侧滑删除swipeFlags
                // 代表只能是向左侧滑删除，当前可以是这样ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT
                int swipeFlags = ItemTouchHelper.LEFT| ItemTouchHelper.RIGHT;
                // 拖动暂不处理默认是0
                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mList.remove(position);
                myAdapter.notifyDataSetChanged();
            }
        });

        //touchHelper.attachToRecyclerView(mRecyclerView);
    }

    class MyAdapter extends CommonRecyclerAdpater<String>{

        public MyAdapter(Context context, List mDataList, int resourseId) {
            super(context, mDataList, resourseId);
        }

        public MyAdapter(Context context, List<String> mDataList, MultiTypeSupport<String> multiTypeSupport) {
            super(context, mDataList, multiTypeSupport);
        }

        @Override
        public void bindHolder(ViewHolder holder, String text, int position) {
            holder.setText(R.id.text,text);

        }
    }

    class CommonItemTouchHelper extends ItemTouchHelper {

        /**
         * Creates an ItemTouchHelper that will work with the given Callback.
         * <p>
         * You can attach ItemTouchHelper to a RecyclerView via
         * {@link #attachToRecyclerView(RecyclerView)}. Upon attaching, it will add an item decoration,
         * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
         *
         * @param callback The Callback which controls the behavior of this touch helper.
         */
        public CommonItemTouchHelper(Callback callback) {
            super(callback);
        }

    }
}
