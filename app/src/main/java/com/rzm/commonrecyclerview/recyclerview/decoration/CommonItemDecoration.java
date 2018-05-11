package com.rzm.commonrecyclerview.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CommonItemDecoration extends RecyclerView.ItemDecoration {

    private int dividerHeight;
    private Paint dividerPaint;

    /**
     * @param dividerHeight  高度
     * @param colorId        颜色（ContextCompat.getColor(context,colorId）
     */
    public CommonItemDecoration(int dividerHeight, int colorId) {
        dividerPaint = new Paint();
        dividerPaint.setColor(colorId);
        this.dividerHeight = dividerHeight;
    }


    /**
     * getItemOffsets(),实现类似padding的效果 设置上下左右的padding
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //用 getItemOffsets给item下方空出一定高度的空间（例子中是1dp），然后用onDraw绘制这个空间
        outRect.bottom = dividerHeight;
    }

    /**
     * 可以实现类似绘制背景的效果，绘制在内容的下面，（在getItemOffsets腾出的空间上绘制）不会覆盖内容
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount - 2; i++) {
            View view = parent.getChildAt(i);
            float top = view.getBottom();
            float bottom = view.getBottom() + dividerHeight;
            c.drawRect(left, top, right, bottom, dividerPaint);
        }
    }

    /*public CommonItemDecoration(Context context,int leftColorId,int rightColorId) {
        leftPaint = new Paint();
        leftPaint.setColor(leftColorId);
        rightPaint = new Paint();
        rightPaint.setColor(rightColorId);
    }*/

    /**
     * 可以绘制在内容的上面，覆盖内容
     * 如果需要绘制内容覆盖在item上边可以重写此方法，实现
     * 类似一些电商app会给商品加上一个标签，比如“推荐”，“热卖”，“秒杀”等等，可以看到这些标签都是覆盖在内容之上的，
     * 这就可以用onDrawOver()来实现
     */
    /*@Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            boolean isLeft = pos % 2 == 0;
            if (isLeft) {
                float left = child.getLeft();
                float right = left + LEFT_TAG_WIDTH;
                float top = child.getTop();
                float bottom = child.getBottom();
                c.drawRect(left, top, right, bottom, leftPaint);
            } else {
                float right = child.getRight();
                float left = right - RIGHT_TAG_WIDTH;
                float top = child.getTop();
                float bottom = child.getBottom();
                c.drawRect(left, top, right, bottom, rightPaint);
            }
        }
    }*/
}







