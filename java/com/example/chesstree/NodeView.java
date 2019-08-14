package com.example.chesstree;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class NodeView extends android.support.v7.widget.AppCompatTextView {

    // Determines where to place it on the the screen
    private int level;

    // The node associated with its drawing
    private Node node;

    // The screen that is being drawn on
    private MainActivity c;

    // The dimension, which changes based on a zoom scale factor, defaults to 34*screen density
    public static float dim;

    // The move string to be drawn on the node
    private String mText = "";

    // The paint this nodeview is drawing with
    private Paint mPaint;

    public NodeView(Context context, AttributeSet attrs, int level, Node node) {

        super(context, attrs);
        c = (MainActivity) context;
        initialize(level, node);
    }

    public NodeView(Context context, int level, Node node) {

        super(context);
        c = (MainActivity) context;
        initialize(level, node);
    }

    private void initialize(int l, Node d) {

        level = l;
        node = d;
        dim = 34*c.density;

        // Sets which color circle to draw based on whose turn it is
        if(level % 2 == 0) setBackgroundResource(R.drawable.circleblack);
        else setBackgroundResource(R.drawable.circle);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Draw text
        super.onDraw(canvas);
        if (mText.length() > 0) {
            canvas.drawText(mText, 0, 0, mPaint);
            invalidate();
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension((int)NodeView.dim, (int)NodeView.dim);
    }

    public int getLevel() {

        return level;
    }

    public boolean getExpanded() {

        return node.getExpanded();
    }

    public void setExpanded(boolean expanded) {

        node.setExpanded(expanded);
    }

    public Node getNode() {

        return node;
    }
}