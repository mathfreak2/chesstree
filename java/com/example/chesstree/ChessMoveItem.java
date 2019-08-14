package com.example.chesstree;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class ChessMoveItem extends android.support.v7.widget.AppCompatTextView {

    private ArrayList<ChessMoveItem> children = new ArrayList<>();
    private String fen;
    private boolean isSelected = true;
    private ChessMoveItem parent;
    private boolean isAlternateMove = false;
    private Paint mPaint;
    private float x;
    private float y;

    public ChessMoveItem(Context context, String fen) {

        this(context, null, fen);
    }

    public ChessMoveItem(Context context, String fen, AttributeSet attrs) {

        this(context, null, fen, attrs);
    }

    public ChessMoveItem(Context context, @Nullable ChessMoveItem parent, String fen) {

        super(context);
        if(parent != null) {
            parent.addChild(this);
            if(parent.getChildren().size() > 1)
                isAlternateMove = true;
        }
        this.parent = parent;
        this.fen = fen;
        setBackgroundColor(Color.parseColor("#fcf003"));
        mPaint = new Paint();

    }

    public ChessMoveItem(Context context, @Nullable ChessMoveItem parent, String fen, AttributeSet attrs) {

        super(context, attrs);
        if(parent != null) {
            parent.addChild(this);
            if(parent.getChildren().size() > 1)
                isAlternateMove = true;
        }
        this.parent = parent;
        this.fen = fen;
        setBackgroundColor(Color.parseColor("#fcf003"));
        mPaint = new Paint();
    }

    public ArrayList<ChessMoveItem> getChildren() {return children;}
    public void addChild(ChessMoveItem child) {children.add(child);}
    public boolean getIsSelected() {return isSelected;}
    public String getFen() {return fen;}
    public ChessMoveItem getParentMove() {return parent;}
    public void setIsAlternateMove(boolean b) {isAlternateMove = b;}
    public boolean isAlternateMove() {return isAlternateMove;}
    public float getX() {return x;}
    public void setX(float x) {this.x = x;}
    public float getY() {return y;}
    public void setY(float y) {this.y = y;}

    public void setIsSelected(boolean s) {
        isSelected = s;
        if(isSelected) setBackgroundColor(Color.parseColor("#fcf003"));
        else setBackgroundColor(Color.parseColor("#ffffff"));
    }

    public boolean isAlternateBranch() {
        if(isAlternateMove) return true;
        else {
            if(parent == null) return false;
            else return parent.isAlternateBranch();
        }
    }

    public boolean equals(ChessMoveItem cmi) {

        if(cmi.getParentMove() == null && parent == null && cmi.getFen().equals(fen)) return true;
        else if(!cmi.getFen().equals(fen)) return false;
        else if(parent == null) return false;
        else if(cmi.getParentMove() != null) return cmi.getParentMove().equals(parent);
        else return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Draw text
        super.onDraw(canvas);
        if (getText().length() > 0) {
            canvas.drawText(getText().toString(), 0, 0, mPaint);
            invalidate();
            requestLayout();
        }
    }
}
