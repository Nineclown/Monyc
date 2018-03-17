package com.nineclown.monyc.main.list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nineclown.monyc.R;

/**
 * Created by nineClown on 2017-11-24.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private Drawable subDivider;

    private final int verticalSpaceHeight;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DividerItemDecoration(Context context, int verticalSpaceHeight) {
        divider = context.getDrawable(R.drawable.r_divider);
        subDivider = context.getDrawable(R.drawable.r_divider);
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + subDivider.getIntrinsicHeight();
            subDivider.setBounds(left + 20, top, right - 20, bottom);
            subDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        /*if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1 || parent.getChildAdapterPosition(view) != 1) {
            outRect.bottom = verticalSpaceHeight;
        }
        */
        outRect.bottom = verticalSpaceHeight;
    }
/*
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

*/
}
