# CommonRecyclerView
封装RecyclerView，实现插拔式的添加下拉刷新上拉加载view，加载下一页的view分为两种模式，一种是到底自动加载，一种是到底后拖动加载，另外可以无限添加头布局和尾布局，更加方便的为item中的view设置点击事件等等

设置多种布局
```
final MyAdapter myAdapter = new MyAdapter(getApplicationContext(),mList, new MultiTypeSupport<String>() {
            @Override
            public int getLayoutId(String item, int position) {
                if (position %2 == 1){
                    return R.layout.item_center;
                }
                return R.layout.item_normal;
            }
        });
```
设置item点击事件
```
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
```
设置item中指定ID的view的点击事件
```
myAdapter.setOnItemChildClickListener(R.id.text,new CommonRecyclerAdpater.OnItemChildClickListener<String>() {
            @Override
            public void onItemChildClick(String o, int position) {
                Toast.makeText(getApplicationContext(),"text,position="+position,Toast.LENGTH_SHORT).show();
            }
        });
```
添加刷新头布局,RefreshViewCreator必须在所有的头部局之前设置才能有效，没有做位置的设定，默认按添加顺序显示
```
mRecyclerView.addRefreshViewCreator(new DefaultRefreshCreator());
```
添加加载下一页的脚布局,LoadViewCreator必须在所有的脚布局之后设置才能有效，没有做位置的设定，默认按添加顺序显示
```
mRecyclerView.addLoadViewCreator(new DefaultLoadCreator());
```
添加空页面.空页面的设置可以在任何位置，因为它的显示和隐藏是根据数据来的
```
mRecyclerView.addEmptyView(view);
```
添加加载中页面
```
mRecyclerView.addLoadingView(loadingView);
```
添加加载失败页面
```
mRecyclerView.addFailureView(loadingFailedView);
```
设置加载下一页的模式
```
mRecyclerView.setLoadMoreType(CommonRecyclerView.LoadMoreType.Pull);

mRecyclerView.setLoadMoreType(CommonRecyclerView.LoadMoreType.Auto);
```
设置是否启用下拉刷新和加载下一页
```
mRecyclerView.setRefreshEnabled(true);
mRecyclerView.setLoadMoreEnabled(true);
```
设置刷新监听
```
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
```
设置加载下一页的监听
```
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
```
