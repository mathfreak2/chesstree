package com.example.chesstree;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.RelativeLayout;

public class ScrollingLayout extends RelativeLayout {

    private float mPosX = 0, mPosY = 0;
    private int width, height;
    private MainActivity c;
    private ScaleGestureDetector sgd;

    // Changes the NodeView dimension on screen based on a scale factor when the screen is zoomed
    // Since everything drawn on MainActivity is drawn based on the NodeView dimension, this scales everything
    public class OnPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if(detector != null) {

                float scaleFactor = detector.getScaleFactor();
                NodeView.dim *= scaleFactor;

                NodeLayout nl = ScrollingLayout.this.c.findViewById(R.id.node_layout);
                nl.invalidateAndRequestLayout();

                ScrollingLayout.this.invalidate();
                ScrollingLayout.this.requestLayout();

                return true;
            }
            else return false;
        }
    }

    public ScrollingLayout(Context context) {
        super(context);
        c = (MainActivity) context;
        width = c.screenX;
        height = c.screenY;
        sgd = new ScaleGestureDetector(c, new OnPinchListener());
    }

    public ScrollingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        c = (MainActivity) context;
        width = c.screenX;
        height = c.screenY;
        sgd = new ScaleGestureDetector(c, new OnPinchListener());
    }

    public ScrollingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = (MainActivity) context;
        width = c.screenX;
        height = c.screenY;
        sgd = new ScaleGestureDetector(c, new OnPinchListener());
    }

    // Allows scrolling
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getActionMasked();
        sgd.onTouchEvent(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:

            {
                mPosX = getX() - ev.getRawX();
                mPosY = getY() - ev.getRawY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                // Find the index of the active pointer and fetch its position
                // Calculate the distance moved
                setX(ev.getRawX() + mPosX);
                setY(ev.getRawY() + mPosY);
                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP: {

                // Springs the screen back to the layout if going beyond the bounds of the layout
                if(getX() > 0) setX(0);
                if(getY() > 0) setY(0);
                if(getX() < -width+c.screenX) setX(-width+c.screenX);
                if(getY() < -height+c.screenY) setY(-height+c.screenY);
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                break;
            }
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Sets the measurements of this layout based on its child layout
        NodeLayout nl = c.findViewById(R.id.node_layout);

        if(width < nl.getMeasuredWidth()) width = nl.getMeasuredWidth();
        if(height < nl.getMeasuredHeight()) height = nl.getMeasuredHeight();
        setMeasuredDimension(width, height);
    }
}