package com.rzm.commonrecyclerview.recyclerview.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

/**
 * 通过RecyclerView的ItemDecoration实现字母表形式的多类型列表
 * 需要先进行排序，将需要的文字集合按字母表顺序排序好
 */
public class AlphabetItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "SectionDecoration";

    private DecorationCallback callback;
    private TextPaint textPaint;
    private Paint paint;
    private int topGap;
    private Paint.FontMetrics fontMetrics;


    public AlphabetItemDecoration(Context context, DecorationCallback decorationCallback) {
        this.callback = decorationCallback;

        paint = new Paint();
        paint.setColor(Color.GREEN);

        textPaint = new TextPaint();
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(80);
        textPaint.setColor(Color.BLACK);
        textPaint.getFontMetrics(fontMetrics);
        textPaint.setTextAlign(Paint.Align.LEFT);
        fontMetrics = new Paint.FontMetrics();
        //topGap = res.getDimensionPixelSize(R.dimen.sectioned_top);//32dp
        topGap = 100;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int pos = parent.getChildAdapterPosition(view);
        long groupId = callback.getGroupId(pos);
        if (groupId < 0) return;
        if (pos == 0 || isFirstInGroup(pos)) {//同组的第一个才添加padding
            outRect.top = topGap;
        } else {
            outRect.top = 0;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            long groupId = callback.getGroupId(position);
            if (groupId < 0) return;
            String textLine = callback.getGroupFirstLine(position).toUpperCase();
            if (position == 0 || isFirstInGroup(position)) {
                float top = view.getTop() - topGap;
                float bottom = view.getTop();
                c.drawRect(left, top, right, bottom, paint);//绘制红色矩形
                c.drawText(textLine, left, bottom, textPaint);//绘制文本
            }
        }
    }


    private boolean isFirstInGroup(int pos) {
        if (pos == 0) {
            return true;
        } else {
            long prevGroupId = callback.getGroupId(pos - 1);
            long groupId = callback.getGroupId(pos);
            return prevGroupId != groupId;
        }
    }

    public interface DecorationCallback {

        long getGroupId(int position);

        String getGroupFirstLine(int position);
    }
}