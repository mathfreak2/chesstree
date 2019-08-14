package com.example.chesstree;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChessMoveFormatterLayout extends ViewGroup {

    private ArrayList<ChessMoveItem> baseItems = new ArrayList<>();
    private Activity context;
    private int width;
    Point size = new Point();
    DisplayMetrics dm = new DisplayMetrics();
    private float density;

    public ChessMoveFormatterLayout(Context context) {
        super(context);
        this.context = (Activity) context;
        this.context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        Display display = this.context.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        width = size.x;
    }

    public ChessMoveFormatterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = (Activity) context;
        this.context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Display display = this.context.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        width = size.x;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int count = getChildCount();

        for(int i=0; i<count; i++) {

            final ChessMoveItem cmi = (ChessMoveItem) getChildAt(i);
            if(cmi.getVisibility() != GONE) {

                cmi.layout((int)cmi.getX(), (int)cmi.getY(), (int)cmi.getX()+cmi.getMeasuredWidth(), (int)cmi.getY()+cmi.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float height = 0;
        int count = getChildCount();

        if(count == 0) {setMeasuredDimension(0,0); return;}

        for(int i=0; i<count; i++) {

            ChessMoveItem cmi = (ChessMoveItem) getChildAt(i);
            cmi.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            height = Math.max(height, cmi.getY()+cmi.getMeasuredHeight());
        }

        setMeasuredDimension(width, (int)height);
    }

    public void addChessMove(ChessMoveItem cmi) {

        cmi.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cmi.setTextColor(Color.parseColor("#000000"));

        if(cmi.getParentMove() == null) {
            baseItems.add(cmi);
            if(baseItems.size() > 1) cmi.setIsAlternateMove(true);
        }
        else {
            if(cmi.getParentMove().getChildren().size() > 1)
                cmi.setIsAlternateMove(true);
        }

        cmi.setGravity(Gravity.CENTER_VERTICAL);

        //if(SettingsActivity.TWO_COLUMN)
        if(true) twoColumnFormatter(cmi);
        else inLineFormatter(cmi);

        addView(cmi);
    }

    private void twoColumnFormatter(ChessMoveItem cmi) {

        String text = cmi.getText().toString();
        int turns = 0; String turn = text.substring(0, text.indexOf("."));
        for(int i=0; i<turn.length(); i++)
            turns += Math.pow(10, turn.length()-i-1)*Character.getNumericValue(turn.charAt(i));

        float x1, y1;
        y1 = (turns-1)*(cmi.getMeasuredHeight()+5);

        x1 = 25;

        // Case: this chess move is the head of its branch
        if(cmi.isAlternateMove()) {

            y1 = cmi.getParentMove().getY();
            makeSpace(y1, cmi.getMeasuredHeight()+5);
            y1 += cmi.getMeasuredHeight()+20;
            x1 = 10;

            if(!cmi.getParentMove().isAlternateBranch())
                cmi.setTextSize(cmi.getTextSize()/4-3);
        }
        // Case: this chess move is not the head of it's branch
        else if(!cmi.isAlternateMove() && cmi.isAlternateBranch()) {

            y1 = cmi.getParentMove().getY();
            x1 = cmi.getParentMove().getX()+cmi.getParentMove().getMeasuredWidth()+5;

            // Controls wrapping behavior
            if(x1 + cmi.getMeasuredWidth() + 5 > width) {

                makeSpace(y1, cmi.getMeasuredHeight()+5);
                y1 += cmi.getParentMove().getMeasuredHeight()+5;
                x1 = 10;
            }
        }
        // Case: this chess move is not an alternative move and is a part of the main game
        else {
            if(text.contains("...")) x1=(float)width/2;
        }

        cmi.setX(x1);
        cmi.setY(y1);
    }

    private void makeSpace(float y, int space) {

        int count = getChildCount();
        Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();

        for(int i=0; i<count; i++) {

            if(getChildAt(i).getY() > y)
                getChildAt(i).setY(getChildAt(i).getY()+space);
        }
    }

    private void inLineFormatter(TextView tv) {

    }
}
