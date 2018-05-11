# CommonRecyclerView
封装RecyclerView，实现插拔式的添加下拉刷新上拉加载view，加载下一页的view分为两种模式，一种是到底自动加载，一种是到底后拖动加载，另外可以无限添加头布局和尾布局，更加方便的为item中的view设置点击事件等等

设置多种布局
final MyAdapter myAdapter = new MyAdapter(getApplicationContext(),mList, new MultiTypeSupport<String>() {
            @Override
            public int getLayoutId(String item, int position) {
                if (position %2 == 1){
                    return R.layout.item_center;
                }
                return R.layout.item_normal;
            }
        });
